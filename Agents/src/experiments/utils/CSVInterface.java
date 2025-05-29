package experiments.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.opencsv.CSVWriter;

public class CSVInterface {

	protected CSVWriter csvWriter;
	protected String evaluation = "evaluation";

	public CSVInterface(String filePath, String type) {
		evaluation = type;
		boolean fileExists = Files.exists(Paths.get(filePath));
		boolean directoryExists = Files.exists(Paths.get("results"));
		try {
			if (!directoryExists) {
				Files.createDirectories(Paths.get("results"));
			}
			FileWriter writer = new FileWriter(filePath, true);
			csvWriter = new CSVWriter(writer);
			if (!fileExists) {
				csvWriter.writeNext(getHeader());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean insertRecords(String[] results) {
		boolean success = false;
		try {
			csvWriter.writeNext(results);
			csvWriter.close();
			success = true;
		} catch (IOException e) {
			try {
				csvWriter.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return success;
	}

	private String[] getHeader() {
		String[] header = evaluation == "evaluation"
				? new String[] { "game_name", "agent_name", "game_index", "agent_index", "games_played", "score",
						"mean", "standard_deviation", "variance", "duration", "natural_end", "move_limit", "turn_limit",
						"supported", "selected_heuristic", "selected_agent", "ensemble_size", "completed_at" }
				: evaluation == "gameResult"
						? new String[] { "game_name", "agent_name", "iteration", "score", "duration", "natural_end",
								"move_limit", "turn_limit", "supported", "selected_heuristic", "selected_agent",
								"ensemble_size", "completed_at" }
						: new String[] { "container_index", "game_name", "game_index", "exception", "skipped_at" };
		return header;
	}

}
