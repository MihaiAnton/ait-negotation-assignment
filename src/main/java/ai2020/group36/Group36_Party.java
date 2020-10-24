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
                lastOptedForVotes = optIn((Voting) info);
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
                new HashSet<>(Collections.singletonList("MOPAC")));
    }

    @Override
    public String getDescription() {
        return "Party for group 36 - description of our awesome party"; //TODO
    }

    /**
     * Update session progress
     *
     * @param info the received info. Used to determine if this is the last info of the round
     */
    private void updateRound(Inform info) {
        // TODO
    }

    /**
     * MOPaC Phase 1 - bidding
     * Called whenever it's the agent's turn
     */
    private Action makeOffer() throws IOException {
        Bid bid = null;
        // TODO
        return new Offer(me, bid);
    }

    /**
     * @param bid the bid to check
     * @return true iff bid is good for us.
     */
    private boolean isGood(Bid bid) {
        // TODO
        return false;
    }

    /**
     * MOPaC Phase 2 - voting
     *
     * @param voting the Voting object containing the options
     * @return our next Votes.
     */
    private Votes vote(Voting voting) throws IOException {
        // TODO

        Set<Vote> votes = new HashSet<>();
        return new Votes(me, votes);
    }

    /**
     * MOPaC Phase 3 - opting in
     *
     * @param voting the Voting object containing the options
     * @return our next Votes.
     */
    private Votes optIn(Voting voting) throws IOException {
        // TODO

        Set<Vote> votes = new HashSet<>();
        return new Votes(me, votes);
    }
}