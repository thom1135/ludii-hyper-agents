package utils;

import java.util.Arrays;
import java.util.List;

import agents.EnsembleAgent;
import agents.PortfolioAgent;
import manager.ai.AIRegistry;
import metadata.ai.heuristics.Heuristics;
import other.AI;
import search.mcts.MCTS;
import search.mcts.MCTS.QInit;
import search.mcts.backpropagation.MonteCarloBackprop;
import search.mcts.finalmoveselection.RobustChild;
import search.mcts.playout.MAST;
import search.mcts.playout.RandomPlayout;
import search.mcts.selection.McGRAVE;
import search.mcts.selection.ProgressiveHistory;
import search.mcts.selection.UCB1;
import search.minimax.AlphaBetaSearch;

public class HyperAgentFactory {

	public static void registerHyperAgents() {
		if (!AIRegistry.registerAI("Portfolio", () -> {
			return new PortfolioAgent();
		}, (game) -> {
			return true;
		}))
			System.err.println("WARNING! Failed to register Portfolio because one with that name already existed!");
		if (!AIRegistry.registerAI("Ensemble", () -> {
			return new EnsembleAgent();
		}, (game) -> {
			return true;
		}))
			System.err.println("WARNING! Failed to register Ensemble because one with that name already existed!");

		if (!AIRegistry.registerAI("Wieghted Ensemble", () -> {
			return new EnsembleAgent(0d);
		}, (game) -> {
			return true;
		}))
			System.err.println("WARNING! Failed to register Ensemble because one with that name already existed!");

		if (!AIRegistry.registerAI("Ensemble 50", () -> {
			return new EnsembleAgent(0.5);
		}, (game) -> {
			return true;
		}))
			System.err.println("WARNING! Failed to register Ensemble 50 because one with that name already existed!");

		if (!AIRegistry.registerAI("(P) Weighted Ensemble", () -> {
			return new EnsembleAgent(-1d);
		}, (game) -> {
			return true;
		}))
			System.err.println("WARNING! Failed to register Ensemble -1 because one with that name already existed!");
		if (!AIRegistry.registerAI("(P) Ensemble", () -> {
			return new EnsembleAgent(-2d);
		}, (game) -> {
			return true;
		}))
			System.err.println("WARNING! Failed to register Ensemble -2 because one with that name already existed!");
	}

	public static AI createAI(final String aiName) {
		switch (aiName) {
		case "Portfolio":
			return new PortfolioAgent();
		case "Ensemble":
			return new EnsembleAgent();
		case "Ensemble 0":
			return new EnsembleAgent(0);
		case "Ensemble 50":
			return new EnsembleAgent(0.5);
		case "Ensemble -1":
			return new EnsembleAgent(-1d);
		case "Ensemble -2":
			return new EnsembleAgent(-2d);
		case "Ensemble -50":
			return new EnsembleAgent(-0.5);
		case "Weighted Ensemble":
			return new EnsembleAgent(0);
		case "Parallel Weighted Ensemble":
			return new EnsembleAgent(-1d);
		case "Parallel Ensemble":
			return new EnsembleAgent(-2d);
		default:
			return AIFactory.createAI(aiName);
		}
	}

	public static AI createHeuristicAI(final String aiName, final String heursiticName) {
		AI agent = null;
		Heuristics heuristic = AIUtils.convertStringtoHeuristic(heursiticName);
		switch (aiName) {
		case "Alpha-Beta":
			agent = new AlphaBetaSearch(heuristic);
			break;
		case "Biased MCTS":
			agent = MCTS.createBiasedMCTS(0.0);
			break;
		case "Ensemble":
			agent = new EnsembleAgent();
			break;
		case "Ensemble 0":
			agent = new EnsembleAgent(0);
			break;
		case "Ensemble 50":
			agent = new EnsembleAgent(0.50);
			break;
		case "Ensemble -1":
			agent = new EnsembleAgent(-1d);
			break;
		case "Ensemble -2":
			agent = new EnsembleAgent(-2d);
			break;
		case "Ensemble -50":
			agent = new EnsembleAgent(-0.5);
			break;
		case "Weighted Ensemble":
			agent = new EnsembleAgent(0);
			break;
		case "Parallel Weighted Ensemble":
			agent = new EnsembleAgent(-1d);
			break;
		case "Parallel Ensemble":
			agent = new EnsembleAgent(-2d);
			break;
		case "MAST":
			agent = new MCTS(new UCB1(), new MAST(200, 0.1), new MonteCarloBackprop(), new RobustChild());
			((MCTS) agent).setQInit(QInit.PARENT);
			break;
		case "MC-GRAVE":
			agent = new MCTS(new McGRAVE(), new RandomPlayout(200), new MonteCarloBackprop(), new RobustChild());
			((MCTS) agent).setQInit(QInit.INF);
			break;
		case "Portfolio":
			agent = new PortfolioAgent();
			((PortfolioAgent) agent).setHeuristic(heursiticName);
			break;
		case "Progressive History":
			agent = new MCTS(new ProgressiveHistory(), new RandomPlayout(200), new MonteCarloBackprop(),
					new RobustChild());
			((MCTS) agent).setQInit(QInit.PARENT);
			break;
		case "Random":
			agent = new RandomAI();
			break;
		case "UCT":
			agent = MCTS.createUCT();
			break;
		}

		if (agent instanceof MCTS) {
			/**
			 * Previous search tree may be out of sync with actual game when MCTS agent is
			 * part of an ensemble -> disable
			 */
			if (agent instanceof MCTS) {
				((MCTS) agent).setTreeReuse(false);
			}
			agent.setHeuristics(heuristic);
		}

		if (agent instanceof EnsembleAgent) {
			((EnsembleAgent) agent).setHeuristic(heursiticName);
		}

		agent.setFriendlyName(aiName);

		return agent;
	}

	public static List<String> listSubAgents() {
		return Arrays.asList("Alpha-Beta", "MAST", "MC-GRAVE", "Progressive History", "UCT");
	}
}
