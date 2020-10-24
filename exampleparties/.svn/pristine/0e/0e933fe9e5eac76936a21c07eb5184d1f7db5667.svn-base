package geniusweb.exampleparties.simpleboa;

import geniusweb.boa.BoaState;
import geniusweb.boa.DefaultBoa;
import geniusweb.boa.InstantiationFailedException;
import geniusweb.boa.acceptancestrategy.AcceptanceStrategy;
import geniusweb.boa.acceptancestrategy.TimeDependentAcceptanceStrategy;
import geniusweb.boa.biddingstrategy.BiddingStrategy;
import geniusweb.boa.biddingstrategy.TimeDependentBiddingStrategy;
import geniusweb.inform.Settings;
import geniusweb.opponentmodel.FrequencyOpponentModel;
import geniusweb.opponentmodel.OpponentModel;
import geniusweb.party.Party;
import tudelft.utilities.logging.Reporter;

/**
 * This example illustrates how to make a custom party by extending Boa and
 * plugging in some components. This is an easy way to make a {@link Party} that
 * is not requiring configuration/parameters. This also demonstrates how to hard
 * set parameters of strategies so that the party runs as expected without
 * requiring any parameters to be set.
 */
public class SimpleBoa extends DefaultBoa {
	public SimpleBoa() {
		super();
	}

	public SimpleBoa(Reporter reporter) {
		super(reporter); // for debugging
	}

	@Override
	protected Class<? extends OpponentModel> getOpponentModel(Settings settings)
			throws InstantiationFailedException {
		return FrequencyOpponentModel.class;
	}

	@Override
	protected BiddingStrategy getBiddingStrategy(Settings settings)
			throws InstantiationFailedException {
		return new TimeDependentBiddingStrategy() {
			@Override
			protected Double getE(BoaState state) {
				return 0.7;
			}

			@Override
			protected Double getK(BoaState state) {
				return 0.2;
			}
		};
	}

	@Override
	protected AcceptanceStrategy getAccceptanceStrategy(Settings settings)
			throws InstantiationFailedException {
		return new TimeDependentAcceptanceStrategy() {
			@Override
			protected Double getE(BoaState state) {
				return 0.7;
			}

			@Override
			protected Double getK(BoaState state) {
				return 0.2;
			}
		};
	}

	@Override
	public String getDescription() {
		return "Demo of hard coding a party with Boa Components";
	}
}
