package geniusweb.exampleparties.timedependentparty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import geniusweb.actions.Accept;
import geniusweb.actions.EndNegotiation;
import geniusweb.actions.Offer;
import geniusweb.actions.PartyId;
import geniusweb.bidspace.AllBidsList;
import geniusweb.inform.ActionDone;
import geniusweb.inform.Agreements;
import geniusweb.inform.Finished;
import geniusweb.inform.Settings;
import geniusweb.inform.YourTurn;
import geniusweb.issuevalue.Bid;
import geniusweb.party.Capabilities;
import geniusweb.profile.Profile;
import geniusweb.profile.utilityspace.LinearAdditiveUtilitySpace;
import geniusweb.progress.ProgressRounds;
import geniusweb.references.Parameters;
import geniusweb.references.ProfileRef;
import geniusweb.references.ProtocolRef;
import tudelft.utilities.logging.Reporter;

public class TimeDependentPartyTest {

	private static final String SAOP = "SAOP";
	private static final PartyId otherparty = new PartyId("other");
	private static final String PROFILE = "src/test/resources/testprofile.json";
	private final static ObjectMapper jackson = new ObjectMapper();

	private TimeDependentParty party;
	private TestConnection connection = new TestConnection();
	private ProtocolRef protocol = new ProtocolRef("SAOP");
	private ProgressRounds progress = mock(ProgressRounds.class);
	private Parameters parameters = new Parameters();
	private Settings settings;
	private LinearAdditiveUtilitySpace profile;

	@Before
	public void before() throws JsonParseException, JsonMappingException,
			IOException, URISyntaxException {
		party = new TimeDependentParty() {

			@Override
			public String getDescription() {
				return "test";
			}

			@Override
			public double getE() {
				return 2; // conceder-like
			}
		};
		settings = new Settings(new PartyId("party1"),
				new ProfileRef(new URI("file:" + PROFILE)), protocol, progress,
				parameters);

		String serialized = new String(Files.readAllBytes(Paths.get(PROFILE)),
				StandardCharsets.UTF_8);
		profile = (LinearAdditiveUtilitySpace) jackson.readValue(serialized,
				Profile.class);

	}

	@Test
	public void smokeTest() {
	}

	@Test
	public void getDescriptionTest() {
		assertNotNull(party.getDescription());
	}

	@Test
	public void getCapabilitiesTest() {
		Capabilities capabilities = party.getCapabilities();
		assertFalse("party does not define protocols",
				capabilities.getBehaviours().isEmpty());
	}

	@Test
	public void testInformConnection() {
		party.connect(connection);
		// Party should not start acting just after an inform
		assertEquals(0, connection.getActions().size());
	}

	@Test
	public void testInformSettings() {
		party.connect(connection);
		connection.notifyListeners(settings);
		assertEquals(0, connection.getActions().size());
	}

	@Test
	public void testInformAndConnection() {
		party.connect(connection);
		party.notifyChange(settings);
		assertEquals(0, connection.getActions().size());
	}

	@Test
	public void testOtherWalksAway() {
		party.connect(connection);
		party.notifyChange(settings);

		party.notifyChange(new ActionDone(new EndNegotiation(otherparty)));

		// party should not act at this point
		assertEquals(0, connection.getActions().size());
	}

	@Test
	public void testPartyHasFirstTurn() {
		party.connect(connection);
		party.notifyChange(settings);

		party.notifyChange(new YourTurn());
		assertEquals(1, connection.getActions().size());
		assertTrue(connection.getActions().get(0) instanceof Offer);
	}

	@Test
	public void testPartyAccepts() {
		party.connect(connection);
		party.notifyChange(settings);

		Bid bid = findBestBid();
		party.notifyChange(new ActionDone(new Offer(otherparty, bid)));
		party.notifyChange(new YourTurn());
		assertEquals(1, connection.getActions().size());
		assertTrue(connection.getActions().get(0) instanceof Accept);

	}

	@Test
	public void testPartyLogsFinal() {
		// this log output is optional, this is to show how to check log
		Reporter reporter = mock(Reporter.class);
		party = new TimeDependentParty(reporter) {

			@Override
			public String getDescription() {
				return "test";
			}

			@Override
			public double getE() {
				return 2; /// conceder like
			}
		};
		party.connect(connection);
		party.notifyChange(settings);
		Agreements agreements = mock(Agreements.class);
		when(agreements.toString()).thenReturn("agree");

		party.notifyChange(new Finished(agreements));

		verify(reporter).log(eq(Level.INFO),
				eq("Final ourcome:Finished[agree]"));
	}

	@Test
	public void testPartysUpdatesProgress() {
		party.connect(connection);
		party.notifyChange(settings);

		party.notifyChange(new YourTurn());
		verify(progress).advance();
	}

	@Test
	public void testGetCapabilities() {
		assertTrue(party.getCapabilities().getBehaviours().contains(SAOP));
	}

	@Test
	public void testUtilityTarget() {
		TimeDependentParty tdp = new TimeDependentParty();
		BigDecimal N02 = new BigDecimal("0.2");
		BigDecimal N043 = new BigDecimal("0.42521212");
		BigDecimal goal = tdp.getUtilityGoal(0.1, 1.2, N02, N043);
		assertTrue(goal.compareTo(N02) > 0);
		assertTrue(goal.compareTo(N043) < 0);
	}

	private Bid findBestBid() {
		BigDecimal bestvalue = BigDecimal.ZERO;
		Bid best = null;
		for (Bid bid : new AllBidsList(profile.getDomain())) {
			BigDecimal util = profile.getUtility(bid);
			if (util.compareTo(bestvalue) > 0) {
				best = bid;
				bestvalue = util;
			}
		}
		return best;
	}

}
