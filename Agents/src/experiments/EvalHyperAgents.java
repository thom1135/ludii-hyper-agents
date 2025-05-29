package experiments;

import java.util.Iterator;
import java.util.List;

import agents.utils.ModelInterface;
import experiments.utils.EvalDataUtility;
import game.Game;
import other.AI;
import other.GameLoader;
import utils.HyperAgentFactory;

public class EvalHyperAgents {

	static int PLAYOUT_COUNT = 50;
	static int TURN_LIMIT = 1000;

	protected int[] agentSubList = { 0, EvalDataUtility.evalAgents.size() - 1 };
	protected List<String> gameStrings;
	protected List<String> agentStrings;
	protected int container = 0;
	protected int gameStartIndex = 0;
	protected int agentStartIndex = 0;
	protected int iterationStartIndex = 0;
	protected boolean isFirstUp = true;

	public EvalHyperAgents() {

//		int[] startingPosition = EvalDataUtility.getStartingPosition(agentSubList)
		this.useEnvSubList();
		this.gameStrings = EvalDataUtility.evalGames.subList(gameStartIndex, EvalDataUtility.evalGames.size());
		this.agentStrings = EvalDataUtility.evalAgents.subList(agentSubList[0], agentSubList[1] + 1);

		this.isFirstUp = true;
	}

	public void startExperiment() {
		// Iterate through the games and play each agent against UCT opponents
		int currentGameIndex = gameStartIndex;
		Iterator<String> gameIterator = gameStrings.iterator();

		while (gameIterator.hasNext()) {
			String currentGame = gameIterator.next();
			Iterator<String> agentIterator = agentStrings.listIterator(agentStartIndex - agentSubList[0]);
			int currentAgentIndex = agentStartIndex;
			agentStartIndex = agentSubList[0];

			Game nextGame = null;
			try {
				nextGame = GameLoader.loadGameFromName(currentGame == "Toki.lud" ? "T'oki.lud"
						: currentGame == "Okwe Achalla.lud" ? "Okwe (Achalla).lud"
								: currentGame == "Nine Mens Morris.lud" ? "Nine Men's Morris.lud" : currentGame);
			} catch (NullPointerException e) {
				// Unable to load game -> skip
				EvalDataUtility.recordSkip(container, currentGame, currentGameIndex, true);
				if (this.isFirstUp) {
					this.isFirstUp = false;
				}
				currentGameIndex++;
				continue;
			}

			if (nextGame == null) {
				/**
				 * Don't have the game and no exception? Shouldn't be possible but I really
				 * don't want this to break the experiment -> skip
				 */
				EvalDataUtility.recordSkip(container, currentGame, currentGameIndex, false);
				currentGameIndex++;
				if (this.isFirstUp) {
					this.isFirstUp = false;
				}
				continue;
			}

			ModelInterface modelInterface = new ModelInterface(nextGame);
			String gameHeuristic = modelInterface.getHeuristicPrediction();
			modelInterface = null;

			currentGame = currentGame == "Let's Catch the Lion.lud" ? "Lets Catch the Lion.lud" : currentGame;
			while (agentIterator.hasNext()) {
				String currentAgentString = agentIterator.next();
				AI currentAgent = HyperAgentFactory.createHeuristicAI(currentAgentString, gameHeuristic);
				if (currentAgent.supportsGame(nextGame)) {

					final EvalAgentGameSet games = this.isFirstUp
							? new EvalAgentGameSet(nextGame, currentGame, currentAgent, PLAYOUT_COUNT, gameHeuristic,
									this.iterationStartIndex)
							: new EvalAgentGameSet(nextGame, currentGame, currentAgent, PLAYOUT_COUNT, gameHeuristic);
					games.setGameLengthCap(TURN_LIMIT);
					games.runExperiment();

					if (games.evalResults() == null) {

						EvalDataUtility.recordUnsupported(currentGame, currentAgentString, currentGameIndex,
								currentAgentIndex);
					} else {
						EvalDataUtility.recordResults(currentGame, currentAgentString, currentGameIndex,
								currentAgentIndex, PLAYOUT_COUNT, games.evalResults());
					}

				} else {
					EvalDataUtility.recordUnsupported(currentGame, currentAgentString, currentGameIndex,
							currentAgentIndex);

				}
				currentAgentIndex++;
				if (this.isFirstUp) {
					this.isFirstUp = false;
				}
			}
			currentGameIndex++;
		}
	}

	private void useEnvSubList() {
		int listStart = agentSubList[0];
		int listEnd = agentSubList[1];
		int container_index = container;

		try {
			listStart = Integer.parseInt(System.getenv("AGENT_START"));
			listEnd = Integer.parseInt(System.getenv("AGENT_END"));
			container_index = Integer.parseInt(System.getenv("CONTAINER"));
			gameStartIndex = Integer.parseInt(System.getenv("GAME_INDEX"));
			iterationStartIndex = Integer.parseInt(System.getenv("ITERATION"));
			agentStartIndex = Integer.parseInt(System.getenv("AGENT_INDEX"));
		} catch (NullPointerException | NumberFormatException e) {
			return;
		}
		container = container_index;
		if (listStart > listEnd || listStart < 0 || listEnd < 0 || listStart > agentSubList[1]
				|| listEnd > agentSubList[1])
			return;

		agentSubList = new int[] { listStart, listEnd };

		return;
	}

	public static void main(String[] args) {
		System.out.println("Starting experiment...");
		EvalHyperAgents evaluation = new EvalHyperAgents();
		evaluation.startExperiment();
		System.out.println("Experiment Complete!");
	}
}
