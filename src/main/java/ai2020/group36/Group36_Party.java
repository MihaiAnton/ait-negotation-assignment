package ai2020.group36;

import geniusweb.party.DefaultParty;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import geniusweb.actions.Accept;
import geniusweb.actions.Action;
import geniusweb.actions.Offer;
import geniusweb.actions.PartyId;
import geniusweb.actions.Vote;
import geniusweb.actions.Votes;
import geniusweb.bidspace.AllPartialBidsList;
import geniusweb.inform.ActionDone;
import geniusweb.inform.Finished;
import geniusweb.inform.Inform;
import geniusweb.inform.OptIn;
import geniusweb.inform.Settings;
import geniusweb.inform.Voting;
import geniusweb.inform.YourTurn;
import geniusweb.issuevalue.Bid;
import geniusweb.party.Capabilities;
import geniusweb.profile.PartialOrdering;
import geniusweb.profile.Profile;
import geniusweb.profile.utilityspace.UtilitySpace;
import geniusweb.profileconnection.ProfileConnectionFactory;
import geniusweb.profileconnection.ProfileInterface;
import geniusweb.progress.Progress;
import geniusweb.progress.ProgressRounds;
import tudelft.utilities.logging.Reporter;

public class Group36_Party extends DefaultParty {

    private Bid lastReceivedBid = null;
    private PartyId me;
    private final Random random = new Random();
    protected ProfileInterface profileint;
    private Progress progress;
    private Settings settings;
    private Votes lastVotes;
    private Votes lastOptedForVotes;
    private String protocol;

    private Bid reservationBid;
    private double reservationValue;
    double FORGET_RATE = 0.2;
    int issueCount;

    Map<PartyId, Integer> powers = new HashMap<>();
    boolean powersSet = false;
    int minPower = 0;
    int maxPower = 0;

    public Group36_Party() {
    }

    public Group36_Party(Reporter reporter) {
        super(reporter); // for debugging
    }

    @Override
    public void notifyChange(Inform info) {
        try {
            if (info instanceof Settings) {
                Settings settings = (Settings) info;
                this.profileint = ProfileConnectionFactory
                        .create(settings.getProfile().getURI(), getReporter());
                this.me = settings.getID();
                this.progress = settings.getProgress();
                this.settings = settings;
                this.protocol = settings.getProtocol().getURI().getPath();

                this.issueCount = this.profileint.getProfile().getDomain().getIssues().size();
                this.reservationBid = this.profileint.getProfile().getReservationBid();
                this.reservationValue = 0;

                try {
                    this.reservationValue = getBidUtility(this.profileint.getProfile(), this.reservationBid);
                } catch (Exception e) {
                }

            } else if (info instanceof ActionDone) {        // Action completed by other bidder
                Action otheract = ((ActionDone) info).getAction();
                if (otheract instanceof Offer) {
                    lastReceivedBid = ((Offer) otheract).getBid();
                }
            } else if (info instanceof YourTurn) {          // 1. Bidding phase
                Action action = makeOffer();
                getConnection().send(action);
            } else if (info instanceof Voting) {            // 2. Voting phase
                lastVotes = vote((Voting) info);
                getConnection().send(lastVotes);
            } else if (info instanceof OptIn) {             // 3. OptIn phase
                lastOptedForVotes = optIn((OptIn) info);
                getConnection().send(lastOptedForVotes);
            } else if (info instanceof Finished) {          // 4. Negotiation finished
                getReporter().log(Level.INFO, "Final outcome:" + info);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to handle info", e);
        }
        updateRound(info);
    }

    @Override
    public Capabilities getCapabilities() {
        return new Capabilities(
                new HashSet<>(Arrays.asList("SAOP", "AMOP", "MOPAC")));
    }

    @Override
    public String getDescription() {
        return "Party for group 36 - adaptive reservation value based on weighted collective actions";
    }

    /**
     * Update session progress
     *
     * @param info the received info. Used to determine if this is the last info of the round
     */
    private void updateRound(Inform info) {
        if (protocol == null)
            return;
        switch (protocol) {
            case "SAOP":
            case "SHAOP":
                if (!(info instanceof YourTurn))
                    return;
                break;
            case "MOPAC":
                if (!(info instanceof OptIn))
                    return;
                break;
            default:
                return;
        }
        // if we get here, round must be increased.
        if (progress instanceof ProgressRounds) {
            progress = ((ProgressRounds) progress).advance();
        }
    }

    /**
     * The agent will split the difference between it's reservation value in 4 and pick the bid
     * that is the closest to the 3rd quarter of the split, being 3/4 'selfish'.
     * In case of error in finding such a value, the agent returns a random bid.
     * MOPaC Phase 1 - bidding
     * Called whenever it's the agent's turn
     */
    private Action makeOffer() throws IOException {
        Action action;
        if ((protocol.equals("SAOP") || protocol.equals("SHAOP"))
                && isGood(lastReceivedBid)) {
            action = new Accept(me, lastReceivedBid);
        } else {
            Bid bid = null;
            double selectedUtility = 0;

            AllPartialBidsList bidspace = new AllPartialBidsList(
                    profileint.getProfile().getDomain());

            double q3Value = ((3 + this.reservationValue) / 4);

            for (Bid possibleBid : bidspace) {
                double utility = getBidUtility(this.profileint.getProfile(), possibleBid);
                if (utility > q3Value) {
                    if (bid == null || selectedUtility > utility) {
                        bid = possibleBid;
                        selectedUtility = utility;
                    }
                }
            }

            if (bid == null) {
                long i = random.nextInt(bidspace.size().intValue());
                bid = bidspace.get(BigInteger.valueOf(i));
                action = new Offer(me, bid);
            }

            action = new Offer(me, bid);

        }
        return (action);

    }

    /**
     * Check whether or not a bid is good, which mean the following:
     * - the bid is valid
     * - the bid contains all the issues
     * - the utility of the bid is larger than the reservation value
     *
     * @param bid the bid to check
     * @return true iff bid is good for us.
     */
    private boolean isGood(Bid bid) {
        if (bid == null || bid.getIssues().size() < this.issueCount) {
            return false;
        }
        Profile profile;
        try {
            profile = profileint.getProfile();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        if (profile instanceof UtilitySpace)
            return ((UtilitySpace) profile).getUtility(bid).doubleValue() >= this.reservationValue;
        if (profile instanceof PartialOrdering) {
            return ((PartialOrdering) profile).isPreferredOrEqual(bid,
                    profile.getReservationBid());
        }
        return false;
    }

    /**
     * Computes the minimum acceptance power of the agent, which is the sum of the powers of
     * the most powerful 50% parties.
     *
     * @return integer representing minimum power
     */
    private int getMinPower() {

        List<Integer> list = new ArrayList<>(this.powers.values());
        int power = 0;
        int toConsider = Integer.max(1, (int) (list.size() / 2));

        for (int i = 0; i < toConsider; i++) {
            power += list.get(i);
        }
        return power;
    }


    /**
     * MOPaC Phase 2 - voting
     * Returns a list of votes based on the minimum and maximum power values and the reservation value.
     *
     * @param voting the Voting object containing the options
     * @return our next Votes.
     */
    private Votes vote(Voting voting) throws IOException {
        if (!powersSet) {                   // set the information regarding the powers involved
            this.powers = voting.getPowers();
            this.minPower = getMinPower();
            this.maxPower = this.powers.values().stream().reduce(0, Integer::sum);
            powersSet = true;
        }

        int maxPower = (int) (this.reservationValue * this.maxPower);   // adaptive maximum power

        Set<Vote> votes = voting.getBids().stream().distinct()
                .filter(offer -> isGood(offer.getBid()))
                .map(offer -> new Vote(me, offer.getBid(), this.minPower, 1 + Math.max(this.minPower, maxPower)))
                .collect(Collectors.toSet());
        return new Votes(me, votes);
    }

    /**
     * MOPaC Phase 3 - opting in
     * Accepts all the previous bids + the bids for which there is a chance of forming a partial consensus.
     *
     * @param voting the Voting object containing the options
     * @return our next Votes.
     */
    private Votes optIn(OptIn voting) throws IOException {

        double weightedUtilities = 0;
        double sumOfPowers = 0;
        for (Integer value : this.powers.values()) {
            sumOfPowers += value;
        }

        // adjust the reservation value based on the offers of others
        for (Votes votes : voting.getVotes()) {
            double utilitySum = 0;
            int voteCount = 0;
            for (Vote vote : votes.getVotes()) {
                double utility = getBidUtility(this.profileint.getProfile(), vote.getBid());
                utilitySum += utility;
                voteCount += 1;
            }
            if (voteCount > 0) {
                utilitySum /= voteCount;
                weightedUtilities += utilitySum * (this.powers.get(votes.getActor()) / sumOfPowers);
            }
        }

        this.reservationValue = this.reservationValue * (1 - FORGET_RATE) + FORGET_RATE * weightedUtilities;

        Set<Bid> bids = new HashSet<>();

        for (Vote vote : this.lastVotes.getVotes()) {           // add the votes already votred for
            bids.add(vote.getBid());
        }
        int maxPower = (int) (this.reservationValue * this.maxPower);

        for (Votes _votes : voting.getVotes()) {                // for each new vote, add it if it's not in the set already
            for (Vote vote : _votes.getVotes()) {               // and if either has a high probability of forming a consensus or if it is acceptable
                if (!bids.contains(vote.getBid()) &&
                        (willFormConsensus(voting, vote.getBid(), this.minPower) ||
                                getBidUtility(this.profileint.getProfile(), vote.getBid()) >= this.reservationValue))

                    bids.add(vote.getBid());
            }
        }

        // convert bids to votes
        Set<Vote> votes = new HashSet<>();
        for (Bid bid : bids) {
            votes.add(new Vote(me, bid, this.minPower, 1 + Math.max(this.minPower, maxPower)));
        }

        return new Votes(me, votes);
    }

    /**
     * Checks whether a (partial) consensus will be formed on the bid.
     * Checks if the sum of powers of the parties that voted for it is greater than a certain minimum threshold.
     *
     * @param optin    the votes in the opt in phase
     * @param bid      the bid to check for
     * @param minPower the minimum power for accepting the bid
     * @return true if it will for consensus
     */
    private boolean willFormConsensus(OptIn optin, Bid bid, int minPower) {
        int sumOfPowers = 0;

        for (Votes votes : optin.getVotes()) {
            for (Vote vote : votes.getVotes()) {
                if (vote.getBid() == bid) {
                    PartyId partyId = vote.getActor();
                    sumOfPowers += this.powers.get(partyId);
                    break;
                }
            }
        }

        return sumOfPowers >= minPower;
    }

    /**
     * Returns the utility of a bid given a profile
     *
     * @param profile of the bidder
     * @param bid
     * @return double representing total utility
     */
    private double getBidUtility(Profile profile, Bid bid) {
        return ((UtilitySpace) profile).getUtility(bid).doubleValue();
    }
}