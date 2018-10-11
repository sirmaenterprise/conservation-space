package com.sirma.sep.export;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper class for the logic for calling a process and adding it to a thread pool. Provides a method for terminating
 * the process
 *
 * @author Ivo Rusev
 */
public class ProcessCaller implements Callable<Integer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private ProcessBuilder processBuilder;

	private Process process;

	/**
	 * Instantiates a new process caller.
	 *
	 * @param processBuilder
	 *            the process builder
	 */
	public ProcessCaller(ProcessBuilder processBuilder) {
		this.processBuilder = processBuilder;
	}

	@Override
	public Integer call() throws Exception {
		process = processBuilder.start();
		// One has to handle the error stream or else process
		// builder waits and respectively hangs here.
		handleStream(process.getErrorStream(), true);
		handleStream(process.getInputStream(), false);
		// Wait until process is executed.
		return process.waitFor();
	}

	/**
	 * Terminates the process according to the OS. This is done so if the export breaks for whatever reason there are no
	 * hanging processes.
	 */
	public void terminate() {
		process.destroy();
	}

	/**
	 * Handles a stream. If not done correctly the process hangs.
	 *
	 * @param input
	 *            the input
	 * @param isErrorStream
	 *            boolean parameter to indicate is this an error stream. If true the stream will be logged as error.
	 */
	private static void handleStream(InputStream input, boolean isErrorStream) {
		BufferedReader bufferedReader = null;
		StringBuilder output = new StringBuilder();
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				output.append("\n" + line);
			}
			if (output.length() > 0) {
				if (isErrorStream) {
					LOGGER.error("Error during execution: {}", output.toString());
				} else if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Execution result: {}", output.toString());
				}
			}
		} catch (Exception ex) {
			LOGGER.error("Unexpected error happened while reading error stream " + "from the process builder.", ex);
		}
	}

}
