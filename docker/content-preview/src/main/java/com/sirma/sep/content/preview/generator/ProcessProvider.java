package com.sirma.sep.content.preview.generator;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Provider used to encapsulate the logic of instantiating processes in services.
 *
 * @author Mihail Radkov
 */
@Component
public class ProcessProvider {

	/**
	 * Creates and starts a {@link Process} from the provided {@link List} containing the command and optional
	 * arguments.
	 *
	 * @param commandAndArguments
	 * 		- the command to start (can be relative or an absolute path) and any optional arguments. The command is
	 * 		mandatory and must be the first element in the list!
	 * @return a started {@link Process}
	 * @throws IOException
	 * 		- in the case the process cannot be started with the provided command and arguments
	 */
	public Process getProcess(List<String> commandAndArguments) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(commandAndArguments);
		return processBuilder.start();
	}
}
