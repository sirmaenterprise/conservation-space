package com.sirma.itt.seip.main.cli;

import static com.sirma.itt.seip.PropertyConfigsWrapper.CONFIG_INPUT_LAST_SITEID;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.sirma.itt.seip.PropertyConfigsWrapper;
import com.sirma.itt.seip.alfresco4.remote.AbstractRESTClient;
import com.sirma.itt.seip.controlers.InitControler;
import com.sirma.itt.seip.controlers.InitControler.DefinitionType;
import com.sirma.itt.seip.controlers.ProgressMonitor;
import com.sirma.itt.seip.main.Runner;

/**
 * The Class CLIRunner.
 */
public class CLIRunner {

	/** The LOGGER. */
	protected static final Logger LOGGER = Logger.getLogger(CLIRunner.class);

	/**
	 * The main method. Here arguments are mapped to internal properties
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		try {

			File file = new File("log4j.properties");
			if (file.canRead()) {
				PropertyConfigurator.configure(file.getAbsolutePath());
			} else {
				PropertyConfigurator.configure(Runner.class.getResourceAsStream(file.getName()));
			}
			// PropertyConfigsWrapper.getInstance()
			CLIRunner cliRunner = new CLIRunner();

			CommandLine parsed = CLIWrapper.parse(args);
			PropertyConfigsWrapper runtimeProperties = PropertyConfigsWrapper.getInstance();
			runtimeProperties.put("host", parsed.getOptionValue(CLIWrapper.PROP_DMS_HOST));
			runtimeProperties.put("port", parsed.getOptionValue(CLIWrapper.PROP_DMS_PORT));
			runtimeProperties.put("user", parsed.getOptionValue(CLIWrapper.PROP_USERNAME));
			runtimeProperties.put(CONFIG_INPUT_LAST_SITEID, parsed.getOptionValue(CLIWrapper.PROP_DMS_SITE));
			AbstractRESTClient restClient = cliRunner.createRestClient();
			String optionValue = parsed.getOptionValue("operation");
			if (optionValue != null) {

				if ("init".equals(optionValue)) {
					InitControler initControler = new InitControler();
					initControler.setHttpClient(restClient);
					String[] optionValues = parsed.getOptionValues(CLIWrapper.PROP_OPERATION);
					String[] split = optionValues[1].split(",");
					List<DefinitionType> options = new ArrayList<>(split.length);
					for (String string : split) {
						options.add(DefinitionType.type(string));
					}
					String site = runtimeProperties.getProperty(CONFIG_INPUT_LAST_SITEID);
					LOGGER.debug("Going to init the following types: " + options + " in '" + site + "'");
					initControler.init(site, options, new ProgressMonitor() {

						@Override
						public void setProgressInfo(int total) {

						}

						@Override
						public void finish() {

						}
					});
				} else if ("clear".equals(optionValue)) {
					InitControler initControler = new InitControler();
					initControler.setHttpClient(restClient);
					String[] optionValues = parsed.getOptionValues(CLIWrapper.PROP_OPERATION);
					String[] split = optionValues[1].split(",");
					String site = runtimeProperties.getProperty(CONFIG_INPUT_LAST_SITEID);
					LOGGER.debug("Going to clean up the following containers: " + Arrays.toString(split) + " in '"
							+ site + "'");
					initControler.clearSite(site, Arrays.asList(split));
				} else if ("post".equals(optionValue)) {
					InitControler initControler = new InitControler();
					initControler.setHttpClient(restClient);
					String uri = parsed.getOptionValue(CLIWrapper.PROP_DMS_URI);
					String[] post = parsed.getOptionValues(CLIWrapper.PROP_OPERATION);
					initControler.postRequest(uri, post[1]);
				}
			}

		} catch (Exception e) {
			LOGGER.error(e);
		}

	}

	/**
	 * Creates the rest client. Arguments are obtained from the {@link PropertyConfigsWrapper} that is previously update
	 * with the actual values.
	 *
	 * @return the initialized client
	 */
	private AbstractRESTClient createRestClient() {
		AbstractRESTClient httpClient = new AbstractRESTClient();
		httpClient.setUseAuthentication(true);

		PropertyConfigsWrapper configsWrapper = PropertyConfigsWrapper.getInstance();
		String user = configsWrapper.getProperty("user");
		httpClient.setDefaultCredentials(configsWrapper.getProperty("host"),
				Integer.valueOf(configsWrapper.getProperty("port")), user, configsWrapper.getProperty("pass"));
		httpClient.setProtocol(configsWrapper.getProperty("protocol.dms", AbstractRESTClient.PROTOCOL_HTTP));
		LOGGER.debug("User:   " + user + "@" + httpClient.getHost() + ":" + httpClient.getPort());
		return httpClient;
	}
}
