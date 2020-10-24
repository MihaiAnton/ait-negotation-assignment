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
    private Votes lastvotes;
    private String protocol;

    public Group36_Party() {
    }

    public Group36_Party(Reporter reporter) {
        super(reporter); // for debugging
    }

    @Override
    public void notifyChange(Inform info) {
        // TODO
    }

    @Override
    public Capabilities getCapabilities() {
        return new Capabilities(
                new HashSet<>(Collections.singletonList("MOPAC")));
    }

    @Override
    public String getDescription() {
        // TODO
        return "";
    }

    /**
     * Update {@link #progress}
     *
     * @param info the received info. Used to determine if this is the last info
     *             of the round
     */
    private void updateRound(Inform info) {
        // TODO
    }

    /**
     * send our next offer
     */
    private void makeOffer() throws IOException {
        // TODO
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
     * @param voting the {@link Voting} object containing the options
     * @return our next Votes.
     */
    private Votes vote(Voting voting) throws IOException {
       // TODO

        Set<Vote> votes = new HashSet<>();
        return new Votes(me, votes);
    }
}