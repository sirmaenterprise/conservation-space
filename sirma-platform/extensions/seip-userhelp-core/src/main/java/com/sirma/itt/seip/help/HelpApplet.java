package com.sirma.itt.seip.help;

import java.lang.invoke.MethodHandles;
import java.net.URL;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.JApplet;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

/**
 * The Class HelpApplet.
 */
public class HelpApplet extends JApplet {
	/** Comment for serialVersionUID. */
	private static final long serialVersionUID = -5233325480208645136L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	private static final String USERHELP_FILE = "userhelp.hs";
	private static final String USERHELP_FILE_LOCATION = "/userhelp/" + USERHELP_FILE;

	private HelpSet hs;
	private transient HelpBroker hb;

	/**
	 * Instantiates a new help applet.
	 */
	public HelpApplet() {
		//
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		if (hs == null) {
			hs = createHelpSet();
			hb = new FancyHelpBroker(hs);
		}
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			LOGGER.error("Error during applet init!", e);
		}
		hb.setDisplayed(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() {
		super.start();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			LOGGER.error("Error during applet startup!", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() {
		hs = null;
		hb = null;
	}

	/**
	 * Creates the help set.
	 */
	private HelpSet createHelpSet() {
		ClassLoader localClassLoader = getClass().getClassLoader();
		try {
			URL localURL = null;
			String resourceURL = getParameter("resourceURL");
			if (resourceURL == null) {
				String path = getCodeBase().getPath();
				if (path.startsWith("/")) {
					path = path.substring(1);
				}
				String[] split = path.split("/");
				if (split.length > 0) {
					localURL = new URL(getCodeBase().getProtocol(), getCodeBase().getHost(), getCodeBase().getPort(),
							USERHELP_FILE_LOCATION);
				} else {
					localURL = new URL(getCodeBase(), USERHELP_FILE_LOCATION);
				}
			} else {
				localURL = new URL(getCodeBase().getProtocol(), getCodeBase().getHost(), getCodeBase().getPort(),
						USERHELP_FILE);
			}
			return new HelpSet(localClassLoader, localURL);
		} catch (Exception e) {
			LOGGER.error("Error during help set creation!", e);
		}
		return null;
	}
}