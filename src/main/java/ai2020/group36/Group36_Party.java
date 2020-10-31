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

    private PartyId partyid;
    protected ProfileInterface profileinterface;
    //	private Progress progress;
//	private String protocol;
    private Integer maxpower;
    private Integer minpower;

    private Votes lastvotes;

    public Group36_Party()
    {
        super();
    }

    public Group36_Party(Reporter reporter)
    {
        super(reporter);
    }

    @Override
    public String getDescription()
    {
        return "TODO: draft agent";
    }

    @Override
    public Capabilities getCapabilities()
    {
        return new Capabilities(new HashSet<>(Arrays.asList("MOPAC", "SAOP")));
    }

    @Override
    public void notifyChange(Inform info)
    {
        try
        {
            if (info instanceof Settings)
            {
                Settings settings = (Settings) info;

                this.partyid = settings.getID();
                this.profileinterface = ProfileConnectionFactory.create(settings.getProfile().getURI(), getReporter());
//				this.progress = settings.getProgress();
//				this.protocol = settings.getProtocol().getURI().getPath();
                Object val = settings.getParameters().get("minPower");
                this.minpower = (val instanceof Integer) ? (Integer) val : 2;
                Object val2 = settings.getParameters().get("maxPower");
                this.maxpower = (val2 instanceof Integer) ? (Integer) val2 : Integer.MAX_VALUE;
            }
            else if (info instanceof YourTurn)
            {
                this.getConnection().send(this.makeOffer());
            }
            else if (info instanceof Voting)
            {
                this.getConnection().send(this.vote((Voting) info));
            }
            else if (info instanceof OptIn)
            {
                this.getConnection().send(this.optIn((OptIn) info));
            }
            else if (info instanceof Finished)
            {
                this.getReporter().log(Level.INFO, "Final ourcome:" + info);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to handle info", e);
        }
    }

    // TODO
    private Offer makeOffer() throws IOException
    {
        // for demo. Obviously full bids have higher util in general
        AllPartialBidsList bidspace = new AllPartialBidsList(this.profileinterface.getProfile().getDomain());
        Bid bid = null;
        for (int attempt = 0; attempt < 20 && !isGood(bid); attempt ++)
        {
            long i = new Random().nextInt(bidspace.size().intValue());
            bid = bidspace.get(BigInteger.valueOf(i));
        }
        return new Offer(this.partyid, bid);
    }

    private boolean isGood(Bid bid)
    {
        if (bid == null)
        {
            return false;
        }
        else
        {
            Profile profile;
            try
            {
                profile = this.profileinterface.getProfile();
            }
            catch (IOException e)
            {
                throw new IllegalStateException(e);
            }

            if (profile instanceof UtilitySpace)
            {
                return ((UtilitySpace) profile).getUtility(bid).doubleValue() > 0.6;
            }
            else if (profile instanceof PartialOrdering) // Do we need this??
            {
                return ((PartialOrdering) profile).isPreferredOrEqual(bid, profile.getReservationBid());
            }
            else
            {
                return false;
            }
        }
    }

    private Votes vote(Voting voting) throws IOException
    {
        Set<Vote> votes = voting.getBids().stream().distinct()
                .filter(offer -> isGood(offer.getBid()))
                .map(offer -> new Vote(this.partyid, offer.getBid(), this.minpower, this.maxpower))
                .collect(Collectors.toSet());

        this.lastvotes = new Votes(this.partyid, votes);

        return new Votes(this.partyid, votes);
    }

    // TODO
    private Votes optIn(OptIn optin) throws IOException
    {
        // Temporarily
        return this.lastvotes;
    }
}