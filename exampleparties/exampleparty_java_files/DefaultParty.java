package geniusweb.party;

import geniusweb.actions.Action;
import geniusweb.connection.ConnectionEnd;
import geniusweb.inform.Inform;
import tudelft.utilities.listener.Listener;
import tudelft.utilities.logging.ReportToLogger;
import tudelft.utilities.logging.Reporter;

/**
 * Party with default implementation to handle the connection.
 *
 */
public abstract class DefaultParty implements Party, Listener<Inform> {
	private ConnectionEnd<Inform, Action> connection = null;
	protected Reporter reporter;

	public DefaultParty() {
		this(new ReportToLogger("party"));
	}

	public DefaultParty(Reporter rep) {
		this.reporter = rep;
	}

	@Override
	public void connect(ConnectionEnd<Inform, Action> connection) {
		this.connection = connection;
		connection.addListener(this);
	}

	@Override
	public synchronized void disconnect() {
		if (connection != null) {
			connection.removeListener(this);
			this.connection = null;
		}
	}

	@Override
	public void terminate() {
		disconnect();
	}

	/**
	 * @return currently available connection, or null if not currently
	 *         connected.
	 */
	public ConnectionEnd<Inform, Action> getConnection() {
		return connection;
	}

	public Reporter getReporter() {
		return reporter;
	}

}
