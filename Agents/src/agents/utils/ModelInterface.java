package agents.utils;

import java.util.HashMap;
import java.util.Map;

import org.jpmml.evaluator.EvaluatorUtil;

import game.Game;

public class ModelInterface {

	Map<String, Double> inputMap = null;

	public ModelInterface(Game game) {
		this.inputMap = ModelUtils.getInputMap(game);
	}

	@SuppressWarnings("unchecked")
	public String getHeuristicPrediction() {
		Map<String, Double> predictions = (Map<String, Double>) EvaluatorUtil
				.decodeAll(ModelUtils.heuristicEvaluator.evaluate(this.inputMap));
		Double maxScore = 0d;
		String predictedHeuristic = "";
		for (String heuristic : predictions.keySet()) {
			if (predictions.get(heuristic) > maxScore) {
				predictedHeuristic = heuristic;
				maxScore = predictions.get(heuristic);
			}
		}
		return predictedHeuristic;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, Double> getAgentPredictions(Double threshold) {
		Map<String, Double> predictions = (Map<String, Double>) EvaluatorUtil
				.decodeAll(ModelUtils.agentEvaluator.evaluate(this.inputMap));
		HashMap<String, Double> selectedAgents = new HashMap<String, Double>();
		if (threshold == 1d) {
			Double topScore = 0d;
			String selectedAgent = null;
			for (String agent : predictions.keySet()) {
				if (predictions.get(agent) > topScore) {
					selectedAgent = agent;
					topScore = predictions.get(agent);
				}
			}
			selectedAgents.put(selectedAgent, 1d);
		} else if (threshold > 0d) {
			for (String agent : predictions.keySet()) {
				if (predictions.get(agent) > threshold * 100) {
					selectedAgents.put(agent, predictions.get(agent));
				}
			}
		} else {
			selectedAgents = (HashMap<String, Double>) predictions;
		}
		return selectedAgents;
	}

}
