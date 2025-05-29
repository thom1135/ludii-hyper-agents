package agents;

import java.util.HashMap;

import agents.utils.ModelInterface;
import game.Game;
import other.AI;
import other.context.Context;
import other.move.Move;

public class PortfolioAgent extends AI {

	/** Player index */
	protected int player = -1;

	/** Selected SubAgent for current Game */
	protected SubAgent selectedAgent;

	/** Whether the SubAgent has been selected/initialized */
	protected boolean agentInitialised = false;

	/** Selected Heuristics for the current Game */
	protected String selectedHeuristic = null;

	/**
	 * Constructor
	 */
	public PortfolioAgent() {
		this.friendlyName = "Portfolio";
	}

	/**
	 * @return SubAgent's selected Move
	 */
	@Override
	public Move selectAction(final Game game, final Context context, final double maxSeconds, final int maxIterations,
			final int maxDepth) {
		return selectedAgent.agent.selectAction(game, context, maxSeconds, maxIterations, maxDepth);
	}

	/**
	 * Agent and heuristic will be selected based on Game compilation concepts and
	 * ludemes
	 */
	@Override
	public void initAI(final Game game, final int playerID) {
		this.player = playerID;
		if (!agentInitialised) {
			ModelInterface modelInterface = new ModelInterface(game);
			if (this.selectedHeuristic == null)
				this.selectedHeuristic = modelInterface.getHeuristicPrediction();
			HashMap<String, Double> predictedAgent = modelInterface.getAgentPredictions(1d);
			this.selectedAgent = new SubAgent(predictedAgent.keySet().iterator().next(), 1d, selectedHeuristic);
			try {
				selectedAgent.agent.initAI(game, playerID);
			} catch (NullPointerException e) {
				// HeuristicTerm is calling a method on null during initAI -> remove heuristic
				selectedAgent.removeHeuristic(game, playerID);
			}

			agentInitialised = true;
			modelInterface = null;
		}
	}

	/**
	 * Close the AI
	 */
	@Override
	public void closeAI() {
		this.selectedAgent = null;
		this.selectedHeuristic = null;
		this.agentInitialised = false;
	}

	/** @return SubAgent's AIVisualisationData */
	@Override
	public AIVisualisationData aiVisualisationData() {
		return selectedAgent.agent.aiVisualisationData();
	}

	/** @return SubAgent's analysisReport */
	@Override
	public String generateAnalysisReport() {
		return "Portfolio (" + selectedHeuristic + "): " + selectedAgent.agent.generateAnalysisReport();
	}

	/** @return SubAgent's estimate */
	@Override
	public double estimateValue() {
		if (agentInitialised)
			return selectedAgent.agent.estimateValue();
		else {
			return 0f;
		}
	}

	public void setHeuristic(String heuristic) {
		this.selectedHeuristic = heuristic;
	}

	/** @return the name of the selected Heuristics for the current Game */
	public String heuristic() {
		return this.selectedHeuristic;
	}

	/** @return the name of the selected SubAgent */
	public String agent() {
		return this.selectedAgent.agent.friendlyName();
	}
}
