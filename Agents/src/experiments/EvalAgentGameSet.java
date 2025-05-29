package experiments;

import java.util.ArrayList;
import java.util.List;

import agents.EnsembleAgent;
import agents.PortfolioAgent;
import experiments.utils.EvalResults;
import game.Game;
import main.Status.EndType;
import other.AI;
import other.RankUtils;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import utils.HyperAgentFactory;

public class EvalAgentGameSet {
	/** The game to play */
	protected Game game = null;

	/** SQL compatible game name */
	protected String gameName;

	/** The agent to be evaluated (Player 1) */
	protected AI agent = null;

	/** Number of games to run (by default 100) */
	protected int numGames = 100;

	protected String selectedHeuristic = "";

	/**
	 * If a game lasts for more turns than this, we'll terminate it as a draw (by
	 * default -1, i.e. no limit)
	 */
	protected int gameLengthCap = -1;

	/** Max seconds per move for AI (by default 1.0 second) */
	protected double maxSeconds = 1.0;

	/** The results of the last experiment run. */
	protected EvalResults evalResults = null;

	protected int startIterationIndex = 0;

	public EvalAgentGameSet(Game selectedGame, String gameName, AI selectedAgent, int gameCount,
			String selectedHeuristic) {
		this.game = selectedGame;
		this.gameName = gameName;
		this.agent = selectedAgent;
		this.numGames = gameCount;
		this.selectedHeuristic = selectedHeuristic;
		this.evalResults = new EvalResults(agent.friendlyName());
		this.startIterationIndex = 0;
	}

	public EvalAgentGameSet(Game selectedGame, String gameName, AI selectedAgent, int gameCount,
			String selectedHeuristic, int startIteration) {
		this.game = selectedGame;
		this.gameName = gameName;
		this.agent = selectedAgent;
		this.numGames = gameCount;
		this.selectedHeuristic = selectedHeuristic;
		this.evalResults = new EvalResults(agent.friendlyName());
		this.startIterationIndex = startIteration;
	}

	public void runExperiment() {
		if (gameLengthCap >= 0)
			game.setMaxTurns(Math.min(gameLengthCap, game.getMaxTurnLimit()));

		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);

		final int numPlayers = game.players().count();

		final List<AI> agents = new ArrayList<>();
		agents.add(null);
		agents.add(agent);
		while (agents.size() < numPlayers + 1) {
			agents.add(HyperAgentFactory.createHeuristicAI("UCT", selectedHeuristic));
		}

		for (int gameCounter = this.startIterationIndex; gameCounter < numGames; ++gameCounter) {
			game.start(context);

			for (int p = 1; p < agents.size(); p++) {
				try {
					agents.get(p).initAI(game, p);
				} catch (NullPointerException e) {
					// Error with heuristic -> game unsupported by agent -> record skip
					evalResults = null;
					return;
				}

			}

			final Model model = context.model();

			while (!context.trial().over()) {

				model.startNewStep(context, agents, maxSeconds);
			}

			if (context.trial().over()) {
				final double score = RankUtils.agentUtilities(context)[1];
				final int numMovesPlayed = context.trial().numMoves() - context.trial().numInitialPlacementMoves();
				final EndType endType = context.trial().status().endType();

				if (evalResults.selctedAgent() == null || evalResults.selectedHeuristic() == null) {
					if (agent instanceof EnsembleAgent) {
						evalResults.setHyperAgentData(((EnsembleAgent) agent).heuristic(),
								((EnsembleAgent) agent).bestAgent(), ((EnsembleAgent) agent).size());
					} else if (agent instanceof PortfolioAgent) {
						evalResults.setHyperAgentData(((PortfolioAgent) agent).heuristic(),
								((PortfolioAgent) agent).agent(), 1);
					} else {
						evalResults.setHyperAgentData(selectedHeuristic);
					}
				}

				evalResults.recordResult(gameName, agent.friendlyName(), gameCounter, score, numMovesPlayed, endType);
			}

			for (AI ai : agents) {
				if (ai != null)
					ai.closeAI();
			}

		}
	}

	public void setGameLengthCap(int maxTurns) {
		this.gameLengthCap = maxTurns;
	}

	public EvalResults evalResults() {
		return evalResults;
	}

}
