package experiments.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import main.Status.EndType;
import main.math.statistics.Stats;

public class EvalDataUtility {

	static int RETRY_SECONDS = 5;
	static int RETRY_ATTEMPTS = 12;

	public static final List<String> evalAgents = Arrays.asList("Alpha-Beta", "Ensemble 0", "Ensemble", "Random",
			"MC-GRAVE", "Portfolio", "Progressive History", "UCT", "MAST", "Ensemble -1", "Ensemble -2");

	public static final List<String> evalGames = Arrays.asList("Puhulmutu.lud", "Shatranj (14x14).lud", "Hoshi.lud",
			"Fettas.lud", "N-Mesh.lud", "Mahbouseh.lud", "Astralesce and Constellation.lud", "Spava.lud",
			"Rock-Paper-Scissors.lud", "La Yagua.lud", "Sig wa Duqqan (Houmt Taourit).lud", "Redstone.lud",
			"Annuvin.lud", "Cascades.lud", "Minefield.lud", "Tara.lud", "Murus Gallicus.lud", "HexGo.lud",
			"Lights Out.lud", "Battleships.lud", "Tank Tactics.lud", "Tibetan Jiuqi.lud", "Pentago.lud",
			"Game of Dwarfs.lud", "Windflowers.lud", "So Long Sucker.lud", "Pic.lud", "Tama.lud", "Plotto.lud",
			"Labirintus.lud", "Agon.lud", "Azteka.lud", "Let's Catch the Lion.lud", "Gobblet Gobblers.lud",
			"4 Squared.lud", "Chaupar.lud", "Morpharaoh.lud", "Pyrga.lud", "Romavoa.lud", "Polypods.lud", "Fission.lud",
			"Rithmomachia.lud", "Paintscape.lud", "Dodo.lud", "Tandems.lud", "Beirut Chess.lud", "Chong (Sakhalin).lud",
			"Caturvimsatikosthakatmiki Krida.lud", "Millieu.lud", "Trianon.lud");

	public static int[] getStartingPosition(int[] agentIndices) {
		String agentClause = "agent_index >= " + agentIndices[0] + " AND agent_index <= " + agentIndices[1];

		String queryString = "SELECT MAX(game_index) as game, MAX(agent_index) as agent FROM evaluations\n" + "WHERE "
				+ agentClause + " AND game_index=(SELECT MAX(game_index) FROM evaluations WHERE " + agentClause + ");";

		int agent = 0, game = 0, currentAttmept = 0;
		boolean success = false;
		ResultSet queryResult = null;

		while (!success && currentAttmept < EvalDataUtility.RETRY_ATTEMPTS) {
			currentAttmept++;
			DBInterface dbInterface = new DBInterface();
			queryResult = dbInterface.retrieveRecords(queryString);

			if (queryResult != null) {
				try {
					queryResult.next();
					game = queryResult.getInt("game");
					agent = queryResult.getInt("agent");
				} catch (SQLException e) {
					dbInterface.closeConnection();
					EvalDataUtility.delay();
					continue;
				}
				dbInterface.closeConnection();
				success = true;
			} else {
				EvalDataUtility.delay();
			}
		}

		if (agent < agentIndices[0])
			agent = agentIndices[0];
		else if (agent >= agentIndices[1]) {
			agent = agentIndices[0];
			game++;
		} else if (!(agent == agentIndices[0] && game == 0)) {
			agent++;
		}

		return new int[] { game, agent };
	}

	public static boolean recordSkip(int containerIndex, String gameName, int gameIndex, boolean exception) {
		String insertString = "INSERT INTO skip (container_index, game_name, game_index, exception)\n" + "VALUES("
				+ containerIndex + ", '" + gameName.substring(0, gameName.length() - 4) + "', " + gameIndex + ", "
				+ exception + ");";

		int currentAttmept = 0;
		boolean success = false;

		while (!success && currentAttmept < EvalDataUtility.RETRY_ATTEMPTS) {
			currentAttmept++;

			DBInterface dbInterface = new DBInterface();
			success = dbInterface.insertRecords(insertString);
			dbInterface.closeConnection();

			if (!success)
				EvalDataUtility.delay();
		}

		String[] skipCSVStrings = new String[] { "" + containerIndex, gameName.substring(0, gameName.length() - 4),
				"" + gameIndex, "" + exception, "" + new java.sql.Timestamp(System.currentTimeMillis()) };

		CSVInterface csvInterface = new CSVInterface("results/skip.csv", "skip");
		csvInterface.insertRecords(skipCSVStrings);
		return success;
	}

	public static boolean recordResults(String gameName, String agentName, int gameIndex, int agentIndex,
			int gamesPlayed, EvalResults results) {

		if (results == null)
			return false;

		Stats agentResults = results.agentPoints();
		Stats durationResults = results.gameDurations();
		Stats[] endTypeResults = results.gameEndTypes();

		String insertString = "INSERT INTO evaluations (game_name, agent_name, game_index, agent_index, games_played, score, "
				+ "mean, standard_deviation, variance, duration, natural_end, move_limit, turn_limit, supported, selected_heuristic, selected_agent, ensemble_size)\n"
				+ "VALUES('" + gameName.substring(0, gameName.length() - 4) + "', '" + agentName + "', " + gameIndex
				+ ", " + agentIndex + ", " + gamesPlayed + ", " + agentResults.sum() + ", " + agentResults.mean() + ", "
				+ agentResults.sd() + ", " + agentResults.varn() + ", " + durationResults.mean() + ", "
				+ endTypeResults[0].mean() + ", " + endTypeResults[1].mean() + ", " + endTypeResults[2].mean() + ", "
				+ true + ", '" + results.selectedHeuristic() + "', '" + results.selctedAgent() + "', "
				+ results.ensembleSize() + ");";

		int currentAttmept = 0;
		boolean success = false;

		while (!success && currentAttmept < EvalDataUtility.RETRY_ATTEMPTS) {
			currentAttmept++;

			DBInterface dbInterface = new DBInterface();
			success = dbInterface.insertRecords(insertString);
			dbInterface.closeConnection();

			if (!success)
				EvalDataUtility.delay();
		}

		String[] resultsCSVStrings = new String[] { gameName.substring(0, gameName.length() - 4), agentName,
				"" + gameIndex, "" + agentIndex, "" + gamesPlayed, "" + agentResults.sum(), "" + agentResults.mean(),
				"" + agentResults.sd(), "" + agentResults.varn(), "" + durationResults.mean(),
				"" + endTypeResults[0].mean(), "" + endTypeResults[1].mean(), "" + endTypeResults[2].mean(), "" + true,
				"" + results.selectedHeuristic(), "" + results.selctedAgent(), "" + results.ensembleSize(),
				"" + new java.sql.Timestamp(System.currentTimeMillis()) };

		CSVInterface csvInterface = new CSVInterface("results/evaluation.csv", "evaluation");
		csvInterface.insertRecords(resultsCSVStrings);
		return success;
	}

	public static boolean recordGameResult(String gameName, String agentName, double agentScore, double gameDuration,
			EndType gameEndType, int iteration, String selectedHeuristic, String selectedAgent, int ensembleSize) {

		Boolean naturalEnd = gameEndType == EndType.NaturalEnd;
		Boolean moveLimit = gameEndType == EndType.MoveLimit;
		Boolean turnLimit = gameEndType == EndType.TurnLimit;

		String insertString = "INSERT INTO game_result (game_name, agent_name, iteration, score, duration, "
				+ "natural_end, move_limit, turn_limit, supported, selected_heuristic, selected_agent, ensemble_size)\n"
				+ "VALUES('" + gameName.substring(0, gameName.length() - 4) + "', '" + agentName + "', " + iteration
				+ ", " + agentScore + ", " + gameDuration + ", " + naturalEnd + ", " + moveLimit + ", " + turnLimit
				+ ", " + true + ", '" + selectedHeuristic + "', '" + selectedAgent + "', " + ensembleSize + ");";

		int currentAttmept = 0;
		boolean success = false;

		while (!success && currentAttmept < EvalDataUtility.RETRY_ATTEMPTS) {
			currentAttmept++;

			DBInterface dbInterface = new DBInterface();
			success = dbInterface.insertRecords(insertString);
			dbInterface.closeConnection();

			if (!success)
				EvalDataUtility.delay();
		}

		String[] resultsCSVStrings = new String[] { gameName.substring(0, gameName.length() - 4), agentName,
				"" + iteration, "" + agentScore, "" + gameDuration, "" + naturalEnd, "" + moveLimit, "" + turnLimit,
				"" + true, "" + selectedHeuristic, "" + selectedAgent, "" + ensembleSize,
				"" + new java.sql.Timestamp(System.currentTimeMillis()) };

		CSVInterface csvInterface = new CSVInterface("results/gameResults.csv", "gameResult");
		csvInterface.insertRecords(resultsCSVStrings);
		return success;
	}

	public static boolean recordUnsupported(String gameName, String agentName, int gameIndex, int agentIndex) {

		String insertString = "INSERT INTO evaluations (game_name, agent_name, game_index, agent_index, games_played, score, "
				+ "mean, standard_deviation, variance, duration, natural_end, move_limit, turn_limit, supported, selected_heuristic, selected_agent, ensemble_size)\n"
				+ "VALUES('" + gameName.substring(0, gameName.length() - 4) + "', '" + agentName + "', " + gameIndex
				+ ", " + agentIndex + ", 0,-1, -1, -1, -1, -1, -1, -1, -1, " + false + ",'-','-', -1);";

		int currentAttmept = 0;
		boolean success = false;

		while (!success && currentAttmept < EvalDataUtility.RETRY_ATTEMPTS) {
			currentAttmept++;

			DBInterface dbInterface = new DBInterface();
			success = dbInterface.insertRecords(insertString);
			dbInterface.closeConnection();

			if (!success)
				EvalDataUtility.delay();
		}

		String[] resultsCSVStrings = new String[] { gameName.substring(0, gameName.length() - 4), agentName,
				"" + gameIndex, "" + agentIndex, "0", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "" + false, "-",
				"-", "-1", "" + new java.sql.Timestamp(System.currentTimeMillis()) };

		CSVInterface csvInterface = new CSVInterface("results/evaluation.csv", "evaluation");
		csvInterface.insertRecords(resultsCSVStrings);

		return success;
	}

	public static void delay() {
		Object lock = new Object();
		synchronized (lock) {
			try {
				lock.wait(EvalDataUtility.RETRY_SECONDS * 1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

}
