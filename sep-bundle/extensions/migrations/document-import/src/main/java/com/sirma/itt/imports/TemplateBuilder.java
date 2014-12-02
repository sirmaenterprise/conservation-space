package com.sirma.itt.imports;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build templates from the template file that are used for transforming the data in the document
 * file
 *
 * @author kirq4e
 */
public class TemplateBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateBuilder.class);

	/** The Constant LINE_SPLIT. */
	private static final Pattern LINE_SPLIT = Pattern.compile("\\s*(?:;|,)\\s*");

	/**
	 * Parse the templates in the input csv file
	 *
	 * @return List of templates for transforming the document data
	 * @throws Exception
	 *             If an error occurs
	 */
	public List<Template> buildTemplates() throws Exception {
		InputStream inputStream = TemplateBuilder.class.getClassLoader().getResourceAsStream("com/sirma/itt/imports/ECN-DataExtractionTemplate.csv");
		if (inputStream == null) {
			LOGGER.error("Failed to read source file for templates ");
			return null;
		}
		BufferedReader reader = null;
		List<Template> templates = new LinkedList<>();
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

			Template currentTemplate = null;
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] split = LINE_SPLIT.split(line);

				if (split.length < 4) {
					LOGGER.warn("Read line with not sufficient columns {} but extepted 4 from {} ",
							split.length, line);
					continue;
				}

				String options = split[0];
				String subject = split[1];
				String predicate = split[2];
				String value = split[3];

				if (predicate.equals("rdf:type")) {
					currentTemplate = new Template();
					templates.add(currentTemplate);
				}

				currentTemplate.addOption(options, subject, predicate, value);
			}

			return templates;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
}
