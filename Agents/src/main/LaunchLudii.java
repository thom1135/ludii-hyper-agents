package main;

import java.util.LinkedHashMap;

import agents.utils.ModelUtils;
import app.StartDesktopApp;
import utils.CliWrapper;
import utils.HyperAgentFactory;

public class LaunchLudii {

	public static void main(String args[]) {

		HyperAgentFactory.registerHyperAgents();

		// load model evaluators
		ModelUtils.agentEvaluator.evaluate(new LinkedHashMap<String, Double>());
		ModelUtils.heuristicEvaluator.evaluate(new LinkedHashMap<String, Double>());

		if (args.length > 0) {
			CliWrapper.runCommand(args);
		} else {
			StartDesktopApp.main(args);
		}
	}
}
