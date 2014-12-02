package com.sirma.itt.emf.help.util;

import java.io.File;

import org.apache.commons.io.IOUtils;

/**
 * Fast and pretty converter between html file and xhtml using wkhtmltopdf
 */
public class HtmlToPdfConverter {
	private String htmlToPdfConvertorLocation;

	/**
	 * Convert the html to a pdf using wkhtmltopdf. If any error occurs a {@link RuntimeException}
	 * is thrown
	 *
	 * @param location
	 *            the location if file - could be any accessible url
	 * @param output
	 *            the output is the store location file as pdf
	 */
	public void convert(String location, File output) {
		// Map<String, String> properties = new HashMap<String, String>();
		// properties.put("char-encoding", "UTF-8");
		ProcessBuilder processBuilder = new ProcessBuilder(getHtmlToPdfConvertorLocation(),
				"--encoding", "UTF-8", location, output.getAbsolutePath());
		Process process;
		try {
			process = processBuilder.start();
			process.waitFor();
			if (process.exitValue() != 0) {
				throw new RuntimeException(
						new String(IOUtils.toByteArray(process.getErrorStream())));
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the html to pdf convertor location.
	 *
	 * @return the html to pdf convertor location
	 */
	private String getHtmlToPdfConvertorLocation() {
		if (htmlToPdfConvertorLocation == null) {
			htmlToPdfConvertorLocation = System
					.getProperty("converter.settings.location.wkhtmltopdf");
		}
		return htmlToPdfConvertorLocation;
	}
}
