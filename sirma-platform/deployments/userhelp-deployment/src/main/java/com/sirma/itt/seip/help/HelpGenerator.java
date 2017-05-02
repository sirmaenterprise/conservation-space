/**
 * Copyright (c) 2012 23.01.2012 , Sirma ITT. /* /**
 */
package com.sirma.itt.seip.help;

import java.io.File;

import com.sirma.itt.seip.help.util.GenerateHelpExecutor;

/**
 * The HelpGenerator invoker. args[0] should be the zip file containing the help. The result is stored in webapp of
 * current module
 */
public class HelpGenerator {

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		GenerateHelpExecutor.generate(new File(args[0]), new File("src/main/webapp"));
	}
}
