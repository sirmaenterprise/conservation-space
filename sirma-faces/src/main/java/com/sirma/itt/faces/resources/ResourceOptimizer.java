package com.sirma.itt.faces.resources;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Performs optimization on resources using YUICompressor.
 * 
 * @author Adrian Mitev
 */
public class ResourceOptimizer {

	/**
	 * Compresses CSS using YUI compressor.
	 * 
	 * @param css
	 *            CSS to compress.
	 * @return compressed CSS.
	 */
	public String compressCSS(String css) {
		try {
			CssCompressor compressor = new CssCompressor(new StringReader(css));
			StringWriter writer = new StringWriter();
			compressor.compress(writer, -1);
			return writer.getBuffer().toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Compresses JavaScript using YUI compressor.
	 * 
	 * @param javascript
	 *            JavaScript to compress.
	 * @return compressed JavaScript.
	 */
	public String compressJavascript(String javascript) {
		try {
			JavaScriptCompressor compressor = new JavaScriptCompressor(
					new StringReader(javascript), new SystemOutErrorReporter());
			StringWriter writer = new StringWriter();
			compressor.compress(writer, 1 << 20, false, false, false, false);
			return writer.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Custom error reporter.
	 * 
	 * @author Adrian Mitev
	 */
	private class SystemOutErrorReporter implements ErrorReporter {

		@Override
		public void warning(String arg0, String arg1, int arg2, String arg3,
				int arg4) {
			System.out.println("WARNING: "
					+ format(arg0, arg1, arg2, arg3, arg4));
		}

		@Override
		public void error(String arg0, String arg1, int arg2, String arg3,
				int arg4) {
			System.out
					.println("ERROR: " + format(arg0, arg1, arg2, arg3, arg4));
		}

		@Override
		public EvaluatorException runtimeError(String arg0, String arg1,
				int arg2, String arg3, int arg4) {
			System.out.println("RUNTIME ERROR: "
					+ format(arg0, arg1, arg2, arg3, arg4));
			return new EvaluatorException(arg0);
		}

		/**
		 * Formats error.
		 * 
		 * @param arg0
		 * @param arg1
		 * @param arg2
		 * @param arg3
		 * @param arg4
		 * @return formatted error.
		 */
		private String format(String arg0, String arg1, int arg2, String arg3,
				int arg4) {
			return String.format("%s%s at line %d, column %d:\n%s", arg0,
					arg1 == null ? "" : ":" + arg1, arg2, arg4, arg3);
		}
	}
}
