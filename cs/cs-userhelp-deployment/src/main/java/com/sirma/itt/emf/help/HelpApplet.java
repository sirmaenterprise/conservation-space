package com.sirma.itt.emf.help;

import java.net.URL;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.JApplet;
import javax.swing.UIManager;

/**
 * The Class HelpApplet.
 */
public class HelpApplet extends JApplet {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -5233325480208645136L;
	private HelpSet hs;
	private HelpBroker hb;
	private static final String USERHELP_FILE = "userhelp.hs";
	private static final String helpIndexLocation = "/userhelp/" + USERHELP_FILE;

	/**
	 * Instantiates a new help applet.
	 */
	public HelpApplet() {
		//
	}

	/*
	 * (non-Javadoc)
	 * @see java.applet.Applet#init()
	 */
	@Override
	public void init() {
		if (hs == null) {
			createHelpSet();
			hb = new FancyHelpBroker(hs);
		}
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		hb.setDisplayed(true);
	}

	/*
	 * (non-Javadoc)
	 * @see java.applet.Applet#start()
	 */
	@Override
	public void start() {
		super.start();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.applet.Applet#stop()
	 */
	@Override
	public void stop() {

		hs = null;
		hb = null;
	}

	/**
	 * Creates the help set.
	 */
	private void createHelpSet() {
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
					localURL = new URL(getCodeBase().getProtocol(), getCodeBase().getHost(),
							getCodeBase().getPort(), helpIndexLocation);
				} else {
					localURL = new URL(getCodeBase(), helpIndexLocation);
				}
			} else {
				localURL = new URL(getCodeBase().getProtocol(), getCodeBase().getHost(),
						getCodeBase().getPort(), USERHELP_FILE);
			}
			hs = new HelpSet(localClassLoader, localURL);
		} catch (Exception localException) {
			localException.printStackTrace();
			return;
		}
	}
}