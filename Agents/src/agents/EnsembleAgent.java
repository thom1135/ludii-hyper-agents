package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import agents.utils.ModelInterface;
import game.Game;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.move.Move;
import search.minimax.AlphaBetaSearch;
import utils.HyperAgentFactory;

public class EnsembleAgent extends AI {

	/** Player index */
	protected int player = -1;

	/** List of SubAgents */
	protected ArrayList<SubAgent> subAgents = new ArrayList<SubAgent>();

	/** Report for the Ludii Analysis tab */
	protected String analysisReport;

	/** The SubAgent currently searching */
	protected SubAgent currentAgent;

	/** Map of selected moves and weight assigned */
	protected HashMap<Move, Double> selectedMoves = new HashMap<Move, Double>();

	/** Estimate of the current position */
	protected double estimate = 0d;

	/** Indicates whether the list of SubAgents has been generated */
	protected boolean subAgentsInitialised = false;

	/** Sum of all the SubAgents' vote weights */
	protected Double totalVoteWeight = 0d;

	/** String representing current heuristic */
	protected String selectedHeuristic = null;

	/** Whether the SubAgent votes are weighted */
	protected boolean weighted = false;

	/**
	 * Represents the threshold for SubAgents to qualify for ensemble, as a fraction
	 * of the highest predicted score (between 0 and 1)
	 */
	protected Double threshold = 0d;

	/**
	 * Name of the highest weighted SubAgent in the ensemble
	 */
	protected String bestAgent = "-";

	/**
	 * Whether to split thinking time, or simulate parallel by allocating the total
	 * thinking time to each SubAgent
	 */
	protected boolean parallel = false;

	/**
	 * Constructor, Standard Ensemble with all SubAgents, no weighting.
	 */
	public EnsembleAgent() {
		this.friendlyName = "Ensemble Agent";
		weighted = false;
	}

	/**
	 * Constructor, Weighted Ensemble
	 * 
	 * @param breakPoint Double between 0 and 1. Threshold for agents to qualify for
	 *                   the ensemble, as a percentage of the highest predicted
	 *                   score.
	 */
	public EnsembleAgent(double breakPoint) {
		/** Value of -2 -> un-weighted parallel */
		if (breakPoint == -2d) {
			this.parallel = true;
			this.weighted = false;
			this.friendlyName = "Parallel Ensemble";
			return;
		}
		/** Value < 0 -> weighted parallel */
		if (breakPoint < 0) {
			this.parallel = true;
			breakPoint = breakPoint == -1d ? 0 : breakPoint * -1;
		}
		this.friendlyName = (this.parallel ? " Parallel " : "") + "Weighted Ensemble";
		this.weighted = true;
		this.threshold = breakPoint;
	}

	@Override
	public Move selectAction(final Game game, final Context context, double maxSeconds, final int maxIterations,
			final int maxDepth) {
		double highestCount = 0d;
		Move candidateMove = null;
		analysisReport = "";
		for (SubAgent subAgent : subAgents) {
			if (wantsInterrupt)
				return interrupt();
			currentAgent = subAgent;
			Move nextMove = null;
			Double thinkingTime = maxSeconds * (this.parallel ? 1d : (subAgent.voteWeight / totalVoteWeight));
			try {
				nextMove = subAgent.agent.selectAction(game, context, thinkingTime, maxIterations, maxDepth);
			} catch (IndexOutOfBoundsException e) {
				// issue with current agent's application of selected heuristic
				subAgent.removeHeuristic(game, this.player);
				nextMove = subAgent.agent.selectAction(game, context, thinkingTime, maxIterations, maxDepth);
			}
			estimate = subAgent.agent.estimateValue();
			if (provedWin(subAgent)) {
				reportDecision(this.friendlyName() + " (" + selectedHeuristic + "): " + subAgent.agent.friendlyName()
						+ " found proven Win, " + nextMove.actionDescriptionStringLong(context, true));
				return nextMove;
			}
			Double currentCount = selectedMoves.get(nextMove);
			currentCount = currentCount == null ? (subAgent.voteWeight / totalVoteWeight)
					: currentCount + (subAgent.voteWeight / totalVoteWeight);
			if (currentCount > highestCount) {
				highestCount = currentCount;
				candidateMove = nextMove;
			}
			selectedMoves.put(nextMove, currentCount);
		}

		reportDecision(this.friendlyName() + " (" + subAgents.size() + " Agents, " + selectedHeuristic
				+ ") : Consensus " + String.format("%.2f", (float) highestCount * 100f) + "%");

		return candidateMove;
	}

	@Override
	public void initAI(final Game game, final int playerID) {

		this.player = playerID;
		if (!subAgentsInitialised) {

			ModelInterface modelInterface = new ModelInterface(game);
			if (this.selectedHeuristic == null)
				this.selectedHeuristic = modelInterface.getHeuristicPrediction();
			if (weighted) {
				buildWeightedEnsemble(modelInterface, game);
			} else {
				buildStandardEnsemble(game);
			}

			subAgentsInitialised = true;
			modelInterface = null;
		}
	}

	/**
	 * Close the AI
	 */
	@Override
	public void closeAI() {
		this.currentAgent = null;
		this.subAgents = new ArrayList<SubAgent>();
		this.selectedHeuristic = null;
		this.subAgentsInitialised = false;
		this.totalVoteWeight = 0d;
		this.selectedMoves = new HashMap<Move, Double>();
		this.estimate = 0d;
		this.bestAgent = "-";
		this.analysisReport = null;
	}

	@Override
	public AIVisualisationData aiVisualisationData() {
		if (selectedMoves.size() > 0) {
			int size = selectedMoves.size();
			/** Color will represent number of moves selected [-1(red) -> 1(blue)] */
			FVector valueEstimates = new FVector(size);

			/** Weight will represent the total voteWeight a move has received */
			FVector searchEffort = new FVector(valueEstimates);
			FastArrayList<Move> moves = new FastArrayList<Move>();
			float totalVotes = 0f;
			for (double votes : selectedMoves.values()) {
				totalVotes += votes;
			}
			for (Move m : selectedMoves.keySet()) {
				moves.add(m);
				searchEffort.set(moves.size() - 1, ((float) (double) selectedMoves.get(m) / totalVotes));
				valueEstimates.set(moves.size() - 1, (2 * totalVotes) - 1f);

			}
			return new AIVisualisationData(searchEffort, valueEstimates, moves);
		}
		return null;

	}

	@Override
	public String generateAnalysisReport() {
		return this.analysisReport;
	}

	@Override
	public double estimateValue() {
		if (currentAgent != null) {
			return currentAgent.agent.estimateValue();
		}
		return estimate;
	}

	/**
	 * Create an ensemble with all compatible SubAgents, using the specified
	 * heuristic. SubAgents all have equal vote-weight.
	 * 
	 * @param heuristic
	 * @param game
	 */
	private void buildStandardEnsemble(Game game) {
		System.out.println("Building Standard Ensemble");
		List<String> subAgentStrings = HyperAgentFactory.listSubAgents();
		for (String subAgentString : subAgentStrings) {
			SubAgent nextSubAgent = new SubAgent(subAgentString, 1d, this.selectedHeuristic);
			if (nextSubAgent.agent != null && nextSubAgent.agent.supportsGame(game)) {
				subAgents.add(nextSubAgent);
				nextSubAgent.agent.initAI(game, this.player);
				this.totalVoteWeight += 1d;
			}
		}
	}

	/**
	 * Create an ensemble with vote-weights based on predicted agent win rates for
	 * the set of game (compilation) concepts and ludemes. SubAgents must have a
	 * predicted score >= the highest predicted agent score, multiplied by the
	 * ensemble threshold.
	 * 
	 * @param modelInterface
	 * @param heuristic
	 * @param game
	 */
	private void buildWeightedEnsemble(ModelInterface modelInterface, Game game) {
		HashMap<String, Double> predictedAgents = modelInterface.getAgentPredictions(threshold);
		double heighestWeight = 0d;
		String topAgent = "-";
		for (String predictedAgent : predictedAgents.keySet()) {
			Double voteWeight = predictedAgents.get(predictedAgent);
			SubAgent nextSubAgent = new SubAgent(predictedAgent, voteWeight, this.selectedHeuristic);
			if (nextSubAgent.agent != null && nextSubAgent.agent.supportsGame(game) && voteWeight > 0d) {
				subAgents.add(nextSubAgent);
				try {
					nextSubAgent.agent.initAI(game, this.player);
				} catch (NullPointerException e) {
					e.printStackTrace();
					nextSubAgent.removeHeuristic(game, this.player);
				}

				this.totalVoteWeight += voteWeight;
				if (voteWeight > heighestWeight) {
					heighestWeight = voteWeight;
					topAgent = predictedAgent;
				}
			}
		}
		this.bestAgent = topAgent;
	}

	/**
	 * Move to return if Ludii requests an interrupt. Reset agent votes and un-set
	 * currentAgent.
	 * 
	 * @return
	 */
	private Move interrupt() {
		selectedMoves = new HashMap<Move, Double>();
		currentAgent = null;
		return null;
	}

	/**
	 * Update the analysis report, reset SubAgent votes and un-set currentAgent.
	 * 
	 * @param report
	 */
	private void reportDecision(String report) {
		analysisReport += report;
		currentAgent = null;
		selectedMoves = new HashMap<Move, Double>();
	}

	/**
	 * Check whether Alpha-Beta or BRS+ has proven a win.
	 * 
	 * @param subAgent
	 * @return
	 */
	private boolean provedWin(SubAgent subAgent) {
		if (subAgent.agent instanceof AlphaBetaSearch) {
			return ((AlphaBetaSearch) subAgent.agent).provedWin();
		}
		return false;
	}

	public void setHeuristic(String heuristic) {
		this.selectedHeuristic = heuristic;
	}

	/**
	 * Returns the name of the heuristic that the ensemble agent is using.
	 * 
	 * @return
	 */
	public String heuristic() {
		return this.selectedHeuristic;
	}

	/**
	 * Returns name of the highest weighted subAgent in the ensemble.
	 * 
	 * @return
	 */
	public String bestAgent() {
		return this.bestAgent;
	}

	/**
	 * Return the number of SubAgents in the ensemble.
	 * 
	 * @return
	 */
	public int size() {
		return this.subAgents.size();
	}

}
