package ai2020.group36;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import geniusweb.actions.*;
import geniusweb.bidspace.AllBidsList;
import geniusweb.connection.ConnectionEnd;
import geniusweb.inform.*;
import geniusweb.issuevalue.Bid;
import geniusweb.party.Capabilities;
import geniusweb.profile.Profile;
import geniusweb.profile.utilityspace.LinearAdditive;
import geniusweb.progress.ProgressRounds;
import geniusweb.references.Parameters;
import geniusweb.references.ProfileRef;
import geniusweb.references.ProtocolRef;
import geniusweb.references.Reference;
import org.junit.Before;
import org.junit.Test;
import tudelft.utilities.listener.DefaultListenable;
import tudelft.utilities.logging.Reporter;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class Group36_PartyTest {

    private static final PartyId PARTY1 = new PartyId("party1");
    private static final String SAOP = "SAOP";
    private static final PartyId otherparty = new PartyId("other");
    private static final String PROFILE = "src/test/resources/testprofile.json";
    private final static ObjectMapper jackson = new ObjectMapper();

    private Group36_Party party;
    private final TestConnection connection = new TestConnection();
    private final ProtocolRef protocol = new ProtocolRef(SAOP);
    private final ProtocolRef mopacProtocol = new ProtocolRef("MOPAC");
    private final ProgressRounds progress = mock(ProgressRounds.class);
    private Settings settings, mopacSettings;
    private LinearAdditive profile;
    private final Parameters parameters = new Parameters();

    @Before
    public void before() throws JsonParseException, JsonMappingException,
            IOException, URISyntaxException {
        party = new Group36_Party();
        settings = new Settings(PARTY1,
                new ProfileRef(new URI("file:" + PROFILE)), protocol, progress,
                parameters);
        mopacSettings = new Settings(PARTY1,
                new ProfileRef(new URI("file:" + PROFILE)), mopacProtocol,
                progress, parameters);

        String serialized = new String(Files.readAllBytes(Paths.get(PROFILE)),
                StandardCharsets.UTF_8);
        profile = (LinearAdditive) jackson.readValue(serialized, Profile.class);

    }

    @Test
    public void smokeTest() {
    }

    @Test
    public void getDescriptionTest() {
        fail();
    }

    @Test
    public void getCapabilitiesTest() {
        fail();
    }

    @Test
    public void testInformConnection() {
        fail();
    }

    @Test
    public void testInformSettings() {
        fail();
    }

    @Test
    public void testInformAndConnection() {
        fail();
    }

    @Test
    public void testOtherWalksAway() {
        fail();
    }

    @Test
    public void testAgentHasFirstTurn() {
        fail();
    }

    @Test
    public void testAgentAccepts() {
        fail();
    }

    @Test
    public void testAgentLogsFinal() {
        fail();
    }

    @Test
    public void testAgentsUpdatesSAOPProgress() {
        fail();
    }

    @Test
    public void testAgentsUpdatesMOPACProgress() {
        fail();
    }

    @Test
    public void testGetCapabilities() {
        fail();
    }

    private Bid findGoodBid() {
        for (Bid bid : new AllBidsList(profile.getDomain())) {
            if (profile.getUtility(bid)
                    .compareTo(BigDecimal.valueOf(0.7)) > 0) {
                return bid;
            }
        }
        throw new IllegalStateException(
                "Test can not be done: there is no good bid with utility>0.7");
    }

    @Test
    public void testVoting() throws URISyntaxException {
        fail();
    }
}


/**
 * A "real" connection object, because the party is going to subscribe etc, and
 * without a real connection we would have to do a lot of mocks that would make
 * the test very hard to read.
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