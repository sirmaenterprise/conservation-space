/**
 *
 */
package com.sirma.itt.seip.main;

import java.awt.EventQueue;
import java.io.File;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.sirma.itt.seip.CMFToolBaseFrame;
import com.sirma.itt.seip.main.cli.CLIRunner;
import com.sirma.itt.seip.main.cli.CLIWrapper;

/**
 * The Tool Runner.
 *
 * @author bbanchev
 */
public class Runner {

	/**
	 * Launch the application.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		// init logging
		File file = new File("log4j.properties");
		if (file.canRead()) {
			PropertyConfigurator.configure(file.getAbsolutePath());
		} else {
			PropertyConfigurator.configure(Runner.class.getResourceAsStream(file.getName()));
		}
		CommandLine parse = CLIWrapper.parse(args);
		if (parse.hasOption("nogui")) {
			if (!parse.hasOption("help")) {
				CLIRunner.main(args);
			} else {
				Logger.getLogger(Runner.class).info("Exiting...");
			}
			return;
		}

		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			// handle exception
		}

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				CMFToolBaseFrame frame = new CMFToolBaseFrame();
				try {
					frame.init();
				} catch (Exception e) {
					frame.log(e);
				}
			}

		});
	}
}
