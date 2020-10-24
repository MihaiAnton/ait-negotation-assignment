package geniusweb.exampleparties.anac2019.winkyagent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import geniusweb.actions.Accept;
import geniusweb.actions.Action;
import geniusweb.actions.EndNegotiation;
import geniusweb.actions.Offer;
import geniusweb.actions.PartyId;
import geniusweb.connection.ConnectionEnd;
import geniusweb.inform.ActionDone;
import geniusweb.inform.Agreements;
import geniusweb.inform.Finished;
import geniusweb.inform.Inform;
import geniusweb.inform.Settings;
import geniusweb.inform.YourTurn;
import geniusweb.issuevalue.Bid;
import geniusweb.party.Capabilities;
import geniusweb.profile.DefaultPartialOrdering;
import geniusweb.profile.PartialOrdering;
import geniusweb.profile.Profile;
import geniusweb.progress.ProgressRounds;
import geniusweb.references.Parameters;
import geniusweb.references.ProfileRef;
import geniusweb.references.ProtocolRef;
import geniusweb.references.Reference;
import tudelft.utilities.listener.DefaultListenable;
import tudelft.utilities.logging.Reporter;

public class WinkyAgentTest {

	private static final String SAOP = "SAOP";
	private static final PartyId otherparty = new PartyId("other");
	private static final String PROFILE = "src/test/resources/jobs1partial20.json";
	private final static ObjectMapper jackson = new ObjectMapper();

	private WinkyAgent party;
	private final TestConnection connection = new TestConnection();
	private final ProtocolRef protocol = mock(ProtocolRef.class);
	private ProgressRounds progress = mock(ProgressRounds.class);
	private Settings settings;
	private PartialOrdering profile;
	private final Parameters parameters = new Parameters();

	@Before
	public void before() throws JsonParseException, JsonMappingException,
			IOException, URISyntaxException {
		when(progress.advance()).thenReturn(progress);
		when(progress.getCurrentRound()).thenReturn(4);
		when(progress.getTotalRounds()).thenReturn(10);

		party = new WinkyAgent();
		settings = new Settings(new PartyId("party1"),
				new ProfileRef(new URI("file:" + PROFILE)), protocol, progress,
				parameters);

		String serialized = new String(Files.readAllBytes(Paths.get(PROFILE)),
				StandardCharsets.UTF_8);
		profile = (PartialOrdering) jackson.readValue(serialized,
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
		// agent should not start acting just after an inform
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
	public void testAgentHasFirstTurn() {
		party.connect(connection);
		party.notifyChange(settings);

		party.notifyChange(new YourTurn());
		assertEquals(1, connection.getActions().size());
		assertTrue(connection.getActions().get(0) instanceof Offer);
	}

	@Ignore // the party does not seem to accept properly. because bug?
	@Test
	public void testAgentAccepts() {
		party.connect(connection);
		party.notifyChange(settings);

		Bid bid = findGoodBid();

		// agent generally does not accept in round 1. But it should accept
		// soon. Also we have progress=40% so it's not a blank atart.
		int round = 1;
		boolean accept = false;
		while (round < 10 && !accept) {
			party.notifyChange(new ActionDone(new Offer(otherparty, bid)));
			party.notifyChange(new YourTurn());
			assertEquals(round, connection.getActions().size());
			accept = connection.getActions().get(round - 1) instanceof Accept;
			round++;
		}
		assertTrue("Party did not accept good offer at all", accept);

	}

	@Test
	public void testAgentLogsFinal() {
		// this log output is optional, this is to show how to check log
		Reporter reporter = mock(Reporter.class);
		party = new WinkyAgent(reporter);
		party.connect(connection);
		party.notifyChange(settings);

		Agreements agreements = mock(Agreements.class);
		when(agreements.toString()).thenReturn("agree");
		party.notifyChange(new Finished(agreements));

		verify(reporter).log(eq(Level.INFO),
				eq("Final ourcome:Finished[agree]"));
	}

	@Test
	public void testAgentsUpdatesProgress() {
		party.connect(connection);
		party.notifyChange(settings);

		party.notifyChange(new YourTurn());
		verify(progress).advance();
	}

	@Test
	public void testGetCapabilities() {
		assertTrue(party.getCapabilities().getBehaviours().contains(SAOP));
	}

	private Bid findGoodBid() {
		// assumes that profile is a DefaultPartial. The top 30% bids should be
		// good.
		if (!(profile instanceof DefaultPartialOrdering))
			throw new IllegalStateException(
					"Test can not be done: there is no good bid with utility>0.7");
		List<Bid> bids = ((DefaultPartialOrdering) profile).getBids();
		return bids.get(bids.size() - 2);
	}

}

/**
 * A "real" connection object, because the party is going to subscribe etc, and
 * without a real connection we would have to do a lot of mocks that would make
 * the test very hard to read.
 *
 */
class TestConnection extends DefaultListenable<Inform>
		implements ConnectionEnd<Inform, Action> {
	private List<Action> actions = new LinkedList<>();

	@Override
	public void send(Action action) throws IOException {
		actions.add(action);
	}

	@Override
	public Reference getReference() {
		return null;
	}

	@Override
	public URI getRemoteURI() {
		return null;
	}

	@Override
	public void close() {

	}

	@Override
	public Error getError() {
		return null;
	}

	public List<Action> getActions() {
		return actions;
	}

}
