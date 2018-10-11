package com.sirma.itt.seip.main.cli;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * The Class CLIWrapper.
 */
public class CLIWrapper {

	public static final String PROP_OPERATION = "operation";
	public static final String PROP_USERNAME = "username";
	public static final String PROP_DMS_HOST = "dmsh";
	public static final String PROP_DMS_PORT = "dmsp";
	public static final String PROP_DMS_SITE = "dmssite";
	public static final String PROP_DMS_URI = "uri";
	/** The cmd. */
	private static CommandLine cmd;

	/**
	 * Instantiates a new CLI wrapper.
	 */
	private CLIWrapper() {

	}

	/**
	 * Parses the.
	 *
	 * @param args
	 *            the args
	 * @return the command line
	 */
	@SuppressWarnings("static-access")
	public static CommandLine parse(String[] args) {
		if (cmd != null) {
			return cmd;
		}
		if (args != null) {
			CommandLineParser parser = new BasicParser();
			Options options = new Options();

			options.addOption(new Option("nogui", "No GUI (uses CLI params)"));
			options.addOption(new Option("help", "Print help"));

			OptionBuilder.hasArgs();
			OptionBuilder.withArgName(PROP_DMS_HOST);
			OptionBuilder.withDescription("DMS host IP (-dmsh 127.0.0.1)");
			OptionBuilder.withValueSeparator('=');
			// register the needed params
			options.addOption(OptionBuilder.create(PROP_DMS_HOST));

			OptionBuilder.hasArgs();
			OptionBuilder.withArgName(PROP_DMS_PORT);
			OptionBuilder.withDescription("DMS host port (-dmsp 8080)");
			OptionBuilder.withValueSeparator('=');
			options.addOption(OptionBuilder.create(PROP_DMS_PORT));

			OptionBuilder.hasArgs();
			OptionBuilder.withArgName(PROP_DMS_SITE);
			OptionBuilder.withDescription("DMS site (-dmssite dom)");
			OptionBuilder.withValueSeparator('=');
			options.addOption(OptionBuilder.create(PROP_DMS_SITE));

			OptionBuilder.hasArgs();
			OptionBuilder.withArgName(PROP_DMS_URI);
			OptionBuilder.withDescription("DMS POST URI (-uri /api/sites)");
			OptionBuilder.withValueSeparator('=');
			options.addOption(OptionBuilder.create(PROP_DMS_URI));

			OptionBuilder.hasArgs();
			OptionBuilder.withArgName(PROP_USERNAME);
			OptionBuilder.withDescription("Authenticated user(-username system)");
			OptionBuilder.withValueSeparator('=');
			options.addOption(OptionBuilder.create(PROP_USERNAME));

			OptionBuilder.hasArgs();
			OptionBuilder.withArgName("arguments=args");
			OptionBuilder.withDescription(
					"The opration clear,init,post (-operation init=\"workflow,bpmn,task,case,document,template,generic,object,project\")");
			OptionBuilder.withValueSeparator('=');
			options.addOption(OptionBuilder.create(PROP_OPERATION));

			try {
				cmd = parser.parse(options, args);
				if (cmd.hasOption("help")) {
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp("SEIP Admin tool", options);
				}
				return cmd;
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}

		}
		return null;
	}

	/**
	 * Getter method for cmd.
	 *
	 * @return the cmd
	 */
	public static CommandLine getCmd() {
		return cmd;
	}
}
