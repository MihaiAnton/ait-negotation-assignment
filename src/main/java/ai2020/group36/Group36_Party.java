package ai2020.group36;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import geniusweb.actions.Offer;
import geniusweb.actions.PartyId;
import geniusweb.actions.Vote;
import geniusweb.actions.Votes;
import geniusweb.bidspace.AllPartialBidsList;
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

/**
 * MOPaC Negotiation Agent
 *
 * @author Mihai Anton, Richard de Jong, Luc Lenferink, Stefan Pretrescu
 */
public class Group36_Party extends DefaultParty {
	private PartyId partyid;
	protected ProfileInterface profileinterface;
	private Progress progress;
	private Votes lastVotes;
	private int issueCount;
	private final Random random = new Random();
	private double reservationValue = 0.0;
	private double FORGET_RATE = 0.2;
	private boolean arePowersSet = false;
	private Map<PartyId, Integer> powers = new HashMap<>();
	private int minPower = 0;
	private int maxPower = 0;

	public Group36_Party() {
		super();
	}

	public Group36_Party(Reporter reporter) {
		super(reporter);
	}

	@Override
	public String getDescription() {
		return "Party for group 36 - adaptive reservation value based on weighted collective actions";
	}

	@Override
	public Capabilities getCapabilities() {
        return new Capabilities(new HashSet<>(Arrays.asList("MOPAC")), Collections.singleton(Profile.class));
	}

	@Override
	public void notifyChange(Inform info) {
		try {
			if (info instanceof Settings) { // 0. setup
				this.log(Level.FINEST, "Settings setup");
				Settings settings = (Settings) info;

				this.partyid = settings.getID();
				this.profileinterface = ProfileConnectionFactory.create(settings.getProfile().getURI(), getReporter());
				this.progress = settings.getProgress();
				this.issueCount = this.profileinterface.getProfile().getDomain().getIssues().size();

				try {
					this.reservationValue = this.retrieveBidUtility(this.profileinterface.getProfile(),
							this.profileinterface.getProfile().getReservationBid());
					this.log(Level.FINEST, "Retrieved reservation bit");
				} catch (Exception e) {
					this.log(Level.WARNING, "No reservation bit present");
				}
				this.getReporter().log(Level.INFO, "Settings setup complete");
			} else if (info instanceof YourTurn) { // 1. Bidding phase
				this.log(Level.FINEST, "Bidding phase");
				this.getConnection().send(this.makeOffer());
			} else if (info instanceof Voting) { // 2. Voting phase
				this.log(Level.FINEST, "Voting phase");
				this.getConnection().send(this.vote((Voting) info));
			} else if (info instanceof OptIn) { // 3. OptIn phase
				this.log(Level.FINEST, "Opt-in phase");
				this.getConnection().send(this.optIn((OptIn) info));

				// Advance process once round is finished.
				if (this.progress instanceof ProgressRounds) {
					this.progress = ((ProgressRounds) progress).advance();
					this.getReporter().log(Level.INFO,
							"Round number: " + ((ProgressRounds) this.progress).getCurrentRound());
				}
			} else if (info instanceof Finished) { // 4. Negotiation finished
				this.log(Level.INFO, "Final ourcome: " + info);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to handle info", e);
		}
	}

	/**
	 * MOPaC Phase 1 - bidding | The agent will split the difference between it's
	 * reservation value in 4 and pick the bid that is the closest to the 3rd
	 * quarter of the split, being 3/4 'selfish'. If such a value does not exist,
	 * the agent returns a random offer.
	 *
	 * @return our offer
	 */
	private Offer makeOffer() throws IOException {
		AllPartialBidsList bidspace = new AllPartialBidsList(this.profileinterface.getProfile().getDomain());
		Bid bid = null;
		double selectedUtility = 0;
		double q3Value = ((3 + this.reservationValue) / 4);
		this.log(Level.FINEST, "Reservation value: " + this.reservationValue);
		this.log(Level.FINEST, "Q3 value: " + q3Value);

		// If existing, find the bid in the possible bidspace that has the highest
		// utility and has a utility higher
		// than the q3Value.
		for (Bid possibleBid : bidspace) {
			double utility = this.retrieveBidUtility(this.profileinterface.getProfile(), possibleBid);
			if (utility > q3Value && (bid == null || selectedUtility > utility)) {
				bid = possibleBid;
				selectedUtility = utility;
			}
		}

		if (bid == null) {
			// No suitable bid found, make a random offer.
			this.log(Level.FINEST, "No suitable bid found, random offer");
			long i = this.random.nextInt(bidspace.size().intValue());
			bid = bidspace.get(BigInteger.valueOf(i));
			return new Offer(this.partyid, bid);
		} else {
			// Suitable bid found, make an offer with this bid.
			this.log(Level.FINEST, "Suitable bid found");
			return new Offer(this.partyid, bid);
		}
	}

	/**
	 * Checks whether or not a bid is good, which means the following: the bid is
	 * valid, the bid contains all the issues, and the utility of the bid is larger
	 * than the reservation value.
	 *
	 * @param bid the bid to check
	 * @return true iff bid is good for us.
	 */
	private boolean isGood(Bid bid) {
		if (bid == null || bid.getIssues().size() < this.issueCount) {
			return false;
		} else {
			Profile profile;
			try {
				profile = this.profileinterface.getProfile();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}

			if (profile instanceof UtilitySpace) {
				return ((UtilitySpace) profile).getUtility(bid).doubleValue() > this.reservationValue;
			} else if (profile instanceof PartialOrdering) {
				return ((PartialOrdering) profile).isPreferredOrEqual(bid, profile.getReservationBid());
			} else {
				return false;
			}
		}
	}

	/**
	 * MOPaC Phase 2 - voting | Returns a list of votes based on the minimum and
	 * maximum power values and the reservation value.
	 *
	 * @param voting the Voting object containing the options
	 * @return our votes.
	 */
	private Votes vote(Voting voting) throws IOException {
		// Set the information regarding the powers involved.
		if (!this.arePowersSet) {
			this.getReporter().log(Level.FINEST, "Set powers");
			this.powers = voting.getPowers();
			this.minPower = this.getMinPower();
			this.maxPower = this.powers.values().stream().reduce(0, Integer::sum);
			this.log(Level.FINEST, "Min power: " + this.minPower);
			this.log(Level.FINEST, "Max power: " + this.maxPower);
			this.arePowersSet = true;
		}

		// Create our own votes.
		Set<Vote> votes = voting.getBids().stream().distinct().filter(offer -> isGood(offer.getBid())).map(
				offer -> new Vote(this.partyid, offer.getBid(), this.minPower, 1 + Math.max(this.minPower, maxPower)))
				.collect(Collectors.toSet());

		// Store our votes for the opt-in phase.
		this.lastVotes = new Votes(this.partyid, votes);

		this.log(Level.FINEST, "Number of votes: " + this.lastVotes.getVotes().size());

		return this.lastVotes;
	}

	/**
	 * Computes the minimum acceptance power of the agent, which is the sum of the
	 * powers of the most powerful 50% parties.
	 *
	 * @return integer representing minimum power
	 */
	private int getMinPower() {
		// Sort powers in descending order.
		List<Integer> list = new ArrayList<>(this.powers.values());
		Collections.sort(list, Collections.reverseOrder());

		// Sum the upper halve of the list.
		int power = 0;
		int toConsider = Integer.max(1, (int) (list.size() / 2));
		for (int i = 0; i < toConsider; i++) {
			power += list.get(i);
		}
		return power;
	}

	/**
	 * MOPaC Phase 3 - opting | The agent accepts all the previous bids and on top
	 * of that the bids for which there is a chance of forming a partial consensus.
	 *
	 * @param voting the Voting object containing the options
	 * @return our opt-in votes.
	 */
	private Votes optIn(OptIn voting) throws IOException {
		double weightedUtilities = 0;
		double sumOfPowers = 0;

		// Get the total sum of powers.
		for (Integer value : this.powers.values()) {
			sumOfPowers += value;
		}

		// Adjust the reservation value based on the offers of others.
		for (Votes votes : voting.getVotes()) {
			double utilitySum = 0;
			int voteCount = 0;

			// Sum the utilies and count the number of votes.
			for (Vote vote : votes.getVotes()) {
				double utility = this.retrieveBidUtility(this.profileinterface.getProfile(), vote.getBid());
				utilitySum += utility;
				voteCount += 1;
			}

			// Calculate the weighted utilities by multiplying the average utility with the
			// relative weight of the agent.
			if (voteCount > 0) {
				weightedUtilities += (utilitySum / voteCount) * (this.powers.get(votes.getActor()) / sumOfPowers);
			}
		}
		// Change the reservation value using the weighted utilities.
		this.reservationValue = this.reservationValue * (1 - FORGET_RATE) + FORGET_RATE * weightedUtilities;

		this.log(Level.FINEST, "Weighted utilities: " + weightedUtilities);
		this.log(Level.FINEST, "Reservation value: " + this.reservationValue);

		// Create a new set of bids that we want to vote on.
		Set<Bid> bids = new HashSet<>();

		// Add the bids of the votes already voted for.
		for (Vote vote : this.lastVotes.getVotes()) {
			bids.add(vote.getBid());
		}

		for (Votes _votes : voting.getVotes()) {
			// For each new vote, add it to the set of bids if it's not in the set already
			// and if either has a high
			// probability of forming a consensus or if it is acceptable.
			for (Vote vote : _votes.getVotes()) {
				if (!bids.contains(vote.getBid()) && (willFormConsensus(voting, vote.getBid(), this.minPower)
						|| this.retrieveBidUtility(this.profileinterface.getProfile(),
								vote.getBid()) >= this.reservationValue)) {
					bids.add(vote.getBid());
				}
			}
		}

		this.log(Level.FINEST, "Number if new votes: " + (bids.size() - this.lastVotes.getVotes().size()));

		// Convert bids to votes
		int maxPower = (int) (this.reservationValue * this.maxPower);
		Set<Vote> votes = new HashSet<>();
		for (Bid bid : bids) {
			votes.add(new Vote(this.partyid, bid, this.minPower, 1 + Math.max(this.minPower, maxPower)));
		}
		return new Votes(this.partyid, votes);
	}

	/**
	 * Checks whether a (partial) consensus will be formed on the bid. Checks if the
	 * sum of powers of the parties that voted for it is greater than a certain
	 * minimum threshold.
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
	private double retrieveBidUtility(Profile profile, Bid bid) {
		return ((UtilitySpace) profile).getUtility(bid).doubleValue();
	}

	private void log(Level level, String message) {
		if (this.partyid != null) {
			this.getReporter().log(level, this.partyid.getName() + " " + message);
		} else {
			this.getReporter().log(level, message);
		}
	}
}
