package ai2020.group36;

import geniusweb.bidspace.pareto.ParetoLinearAdditive;
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
import geniusweb.party.DefaultParty;
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
    double FORGET_RATE = 0.9;
    int issueCount;

    Map<PartyId, Integer> powers = new HashMap<>();
    boolean powersSet = false;
    int minPower = 0;
    int maxPower = 0;
    double MIN_POWER_INCREASE = 1.5;
    double MAX_POWER_INCREASE = 1;

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
     * @param bid the bid to check
     * @return true iff bid is good for us.
     */
    private boolean isGood(Bid bid) {
        if (bid == null) {
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

    private int getMinPower() {

        List<Integer> list = this.powers.values().stream().collect(Collectors.toList());
        int power = 0;
        int toConsider = Integer.max(1, (int) (list.size() / 2));

        for (int i = 0; i < toConsider; i++) {
            power += list.get(i);
        }
        return power;
    }


    /**
     * MOPaC Phase 2 - voting
     *
     * @param voting the Voting object containing the options
     * @return our next Votes.
     */
    private Votes vote(Voting voting) throws IOException {
        if (!powersSet) {
            this.powers = voting.getPowers();
            this.minPower = getMinPower();
            this.maxPower = this.powers.values().stream().reduce(0, Integer::sum);

            powersSet = true;
        }

        int maxPower = (int) (this.reservationValue * this.maxPower * MAX_POWER_INCREASE);

        Set<Vote> votes = voting.getBids().stream().distinct()
                .filter(offer -> isGood(offer.getBid()))
                .map(offer -> new Vote(me, offer.getBid(), (int) (this.minPower * MIN_POWER_INCREASE), 1 + Math.max((int) (this.minPower * MIN_POWER_INCREASE), maxPower)))
                .collect(Collectors.toSet());
        return new Votes(me, votes);
    }

    /**
     * MOPaC Phase 3 - opting in
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

        for (Vote vote : this.lastVotes.getVotes()) {
            bids.add(vote.getBid());
        }
        int maxPower = (int) (this.reservationValue * this.maxPower * MAX_POWER_INCREASE);

        for (Votes _votes : voting.getVotes()) {
            for (Vote vote : _votes.getVotes()) {
                if (getBidUtility(this.profileint.getProfile(), vote.getBid()) >= this.reservationValue && !bids.contains(vote.getBid())) {
                    bids.add(vote.getBid());
                }
            }
        }

        Set<Vote> votes = new HashSet<>();
        for (Bid bid : bids) {
            votes.add(new Vote(me, bid, (int) (MIN_POWER_INCREASE * this.minPower), 1 + Math.max((int) (MIN_POWER_INCREASE * this.minPower), maxPower)));
        }

        return new Votes(me, votes);
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