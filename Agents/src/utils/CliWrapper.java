package utils;

import app.PlayerCLI;
import experiments.EvalHyperAgents;

/**
 * Simple class to check the for the "--eval-hyper-agents" argument at args[0].
 * If not found, passes the args to the Ludii PlayerCLI.
 */
public class CliWrapper {
	/**
	 * @param args The first argument is expected to be a command to run, with all
	 *             subsequent arguments being passed onto the called command.
	 */
	public static void runCommand(final String[] args) {
		final String commandArg = args[0];

		if (commandArg.equalsIgnoreCase("--eval-hyper-agents")) {
			if (args.length > 1)
				System.out.println("Warning: --eval-hyper-agents does not currently accept additional arguments!");
			EvalHyperAgents.main(args);
		} else {
			PlayerCLI.runCommand(args);
		}
	}
}
