package agents;

import game.Game;
import other.AI;
import utils.HyperAgentFactory;

public class SubAgent {
	protected AI agent;

	protected double voteWeight;

	/**
	 * Constructor
	 * 
	 * @param aiName
	 * @param voteWeight
	 * @param heuristics
	 */
	public SubAgent(String aiName, double voteWeight, String heuristic) {
		this.agent = HyperAgentFactory.createHeuristicAI(aiName, heuristic);
		this.voteWeight = voteWeight;
	}

	/**
	 * Recreate the agent without the heuristic.
	 * 
	 * @param game
	 * @param playerIndex
	 */
	public void removeHeuristic(Game game, int playerIndex) {
		String aiName = agent.friendlyName();
		this.agent = HyperAgentFactory.createHeuristicAI(aiName, "");
		this.agent.initAI(game, playerIndex);
	}
}
