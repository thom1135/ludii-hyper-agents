package experiments.utils;

import main.Status.EndType;
import main.math.statistics.Stats;

public class EvalResults {

	protected Stats agentPoints;

	protected Stats gameDurations;

	protected Stats[] gameEndTypes;

	protected String selectedHeuristic;

	protected String selectedAgent;

	protected int ensembleSize = 1;

	public EvalResults(String agentName) {
		agentPoints = new Stats(agentName + " points");
		gameDurations = new Stats("Game durations");
		gameEndTypes = new Stats[3];
		gameEndTypes[0] = new Stats("Natural Ends");
		gameEndTypes[1] = new Stats("Move Limits");
		gameEndTypes[2] = new Stats("Turn Limits");
	}

	public void setHyperAgentData(String heuristic) {
		this.selectedHeuristic = heuristic;
		this.selectedAgent = "-";
	}

	public void setHyperAgentData(String heuristic, String agent, int ensembleSize) {
		this.selectedHeuristic = heuristic;
		this.selectedAgent = agent;
		this.ensembleSize = ensembleSize;
	}

	public void recordResult(String gameName, String agentName, int interation, double agentScore, double gameDuration,
			EndType gameEndType) {
		agentPoints.addSample((agentScore + 1d) / 2d);
		gameDurations.addSample(gameDuration);
		gameEndTypes[0].addSample(gameEndType == EndType.NaturalEnd ? 1d : 0d);
		gameEndTypes[1].addSample(gameEndType == EndType.MoveLimit ? 1d : 0d);
		gameEndTypes[2].addSample(gameEndType == EndType.TurnLimit ? 1d : 0d);
		EvalDataUtility.recordGameResult(gameName, agentName, (agentScore + 1d) / 2d, gameDuration, gameEndType,
				interation, selectedHeuristic, selectedAgent, ensembleSize);
	}

	public Stats agentPoints() {
		agentPoints.measure();
		return agentPoints;
	}

	public Stats gameDurations() {
		gameDurations.measure();
		return gameDurations;
	}

	public Stats[] gameEndTypes() {
		for (Stats gameEndType : gameEndTypes) {
			gameEndType.measure();
		}
		return gameEndTypes;
	}

	public String selectedHeuristic() {
		return this.selectedHeuristic;
	}

	public String selctedAgent() {
		return this.selectedAgent;
	}

	public int ensembleSize() {
		return this.ensembleSize;
	}
}
