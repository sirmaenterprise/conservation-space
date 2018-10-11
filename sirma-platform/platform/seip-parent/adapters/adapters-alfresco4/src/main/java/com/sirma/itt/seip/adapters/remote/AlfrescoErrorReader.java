package com.sirma.itt.seip.adapters.remote;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The AlfrescoStackTraceReader parses alfresco responses that are with code !=200.
 */
public class AlfrescoErrorReader {

	private static final Logger LOGGER = Logger.getLogger(AlfrescoErrorReader.class);
	private static final String END_MESSAGE_PATTERN = "</u></p><HR";
	private static final String START_MESSAGE_PATTERN = "<b>description</b> <u>";

	/** The start message pattern length. */
	private static final int START_MESSAGE_PATTERN_LENGTH = START_MESSAGE_PATTERN.length();

	/**
	 * Instantiates a new alfresco error reader.
	 */
	private AlfrescoErrorReader() {
		// utility class
	}

	/**
	 * Parses the response from server in readable format.
	 *
	 * @param response
	 *            the response
	 * @return the parsed string or the same message.
	 */
	public static String parse(String response) {
		if (response.startsWith("{")) {
			try {
				JSONObject jsonObject = new JSONObject(response);
				String callstack = jsonObject.getString("callstack");
				return callstack.replaceAll("\",\"", "\",\n\"");
			} catch (JSONException e) {
				LOGGER.warn("JSON parse failed!", e);
				return response;
			}
			// REVIEW: the pattern could be less specific - for example the
			// version
		} else if (response.startsWith("<html><head><title>Apache Tomcat/6.0.29 - Error report</title>")) {
			int startIndex = response.indexOf(START_MESSAGE_PATTERN);
			if (startIndex > -1) {
				int endIndex = response.indexOf(END_MESSAGE_PATTERN, startIndex);
				return response.substring(startIndex + START_MESSAGE_PATTERN_LENGTH, endIndex);
			}
		}
		return response;
	}

	/**
	 * Parses the stack.
	 *
	 * @param e
	 *            the e
	 * @return the string
	 */
	public static String parse(Throwable e) {
		// REVIEW: this could be updated to return and part of the stack trace
		// for example to filter
		// the lines that contains a cmf package for better logging/debugging
		if (e.getMessage() != null) {
			return e.getMessage();
		}
		if (e.getCause() != null && e.getCause().getMessage() != null) {
			return e.getCause().getMessage();
		}
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter); // NOSONAR
		return result.toString();
	}

}
