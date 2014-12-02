package com.sirma.itt.emf.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;

/**
 * A wrapper class for the logic for calling a process and adding it to a thread
 * pool. Provides a method for terminating the process
 * 
 * @author Ivo Rusev
 */
public class ProcessCaller implements Callable<Integer> {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessCaller.class);
	/** The process builder. */
	private ProcessBuilder processBuilder;
	/** The process. */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer call() throws Exception {
		process = processBuilder.start();
		// One has to handle the error stream or else process
		// builder waits and respectively hangs here.
		handleStream(process.getErrorStream());
		handleStream(process.getInputStream());
		// Wait until process is executed.
		return process.waitFor();
	}

	/**
	 * Terminates the process according to the OS. This is done so if the export
	 * breaks for whatever reason there are no hanging processes.
	 */
	public void terminate() {
		this.process.destroy();
	}

	/**
	 * Handles a stream. If not done correctly the process hangs.
	 * 
	 * @param input
	 *            the input
	 */
	private void handleStream(InputStream input) {
		BufferedReader bufferedReader = null;
		StringBuilder output = new StringBuilder();
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				output.append(line);
			}
			if (LOGGER.isDebugEnabled() && StringUtils.isNotNullOrEmpty(output.toString())) {
				LOGGER.debug("Execution result: {}", output.toString());
			}
		} catch (Exception ex) {
			LOGGER.error("Unexpected error happened while reading error stream "
					+ "from the process builder.", ex);
		}
	}

}
