package com.sirma.itt.imports;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.constants.DocumentProperties;
import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * Transforms a document according to the given templates
 * 
 * @author kirq4e
 */
public class DocumentTransformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentTransformer.class);

	/** The Constant LINE_SPLIT. */
	private static final Pattern LINE_SPLIT = Pattern.compile("\\s*(?:;|,)\\s*");

	private static final String QUERY_PART_BY_ID = "select ?instance ?instanceType where {"
			+ "?instance a pdm:Part ." + "?instance dcterms:identifier ?identifier ."
			+ "?instance sesame:directType ?instanceType ."
			+ "FILTER( NOT EXISTS {?instance emf:isDeleted \"true\"^^xsd:boolean}) ." + "}";

	private final SearchService searchService;

	/**
	 * Initializes the Document transformer
	 * 
	 * @param searchService
	 *            Search service for performing queries
	 */
	public DocumentTransformer(SearchService searchService) {
		this.searchService = searchService;
	}

	/**
	 * Transforms the input file by the given templates
	 * 
	 * @param properties
	 *            of the input file
	 * @param templates
	 *            List of templates
	 * @return Content of the transformed file. Returns null if it failes to read the source file
	 * @throws Exception
	 *             If an error occurs
	 */
	public String transformDataFile(Map<String, Serializable> properties, List<Template> templates)
			throws Exception {
		InputStream inputStream = getFileStream(properties);
		if (inputStream == null) {
			LOGGER.error("Failed to read source file for parsing from {}", properties);
			return null;
		}
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

			Template currentTemplate = templates.get(0);
			// map between variable and URI
			Map<String, String> uris = new HashMap<>();
			// map between ID(ecnId + partNumber + bomPartNumber) and URI so we can identify new
			// objects
			Map<String, String> idToURI = new HashMap<>();

			// result data
			StringBuilder result = new StringBuilder();

			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] split = LINE_SPLIT.split(line, 7);

				if (split.length < 7) {
					LOGGER.warn("Read line without sufficient columns {} but extepted 7 from {} ",
							split.length, line);
					continue;
				}

				String varName = split[0].trim();
				// TODO remove this check when the implementation is sure about
				if (varName.contains("[")) {
					varName = varName.substring(0, varName.indexOf("["));
				}

				String ecnId = split[1];
				String partNumber = split[2];
				String bomPartNumber = split[3];
				// 4 is the title of the field
				String selector = split[5];
				String value = split[6].trim();

				if (!value.isEmpty()
						&& LINE_SPLIT.matcher(String.valueOf(value.charAt(value.length() - 1)))
								.matches()) {
					value = value.substring(0, value.length() - 1);
				}

				// find the correspoding template for the field
				if (!currentTemplate.canHandle(varName)) {
					for (Template template : templates) {
						if (template.canHandle(varName)) {
							currentTemplate = template;
							break;
						}
					}
				}

				List<TemplateRow> templateRows = currentTemplate.getOption(varName);
				if (templateRows == null) {
					LOGGER.debug("Skip transformation of line: {}", line);
					continue;
				}

				String id = ecnId + partNumber + bomPartNumber;

				if (!idToURI.containsKey(id)
						|| !uris.containsKey(currentTemplate.getUriVariableName())) {
					String variableName = currentTemplate.getUriVariableName();
					String uri = null;

					if (StringUtils.isNotBlank(partNumber)) {
						// search for part number only if the bom is not specified
						if (StringUtils.isBlank(bomPartNumber)) {
							uri = searchInstance(partNumber, "QUERY_PART_BY_ID", QUERY_PART_BY_ID);
							idToURI.put(partNumber, uri);
						}
					}

					if (uri == null) {
						uri = generateURI();
						idToURI.put(value, uri);
					}

					uris.put(variableName, uri);
					idToURI.put(id, uri);

					appendWithSeparator(result, currentTemplate.createInstance(uri));

					List<TemplateRow> relations = currentTemplate.getRelations();

					// append simple relations for which we know all properties
					for (TemplateRow templateRow : relations) {
						appendRelation(templateRow, uris, variableName, null, result);
					}
				}

				for (TemplateRow templateRow : templateRows) {
					String parameter = templateRow.getParameter();
					String predicate = templateRow.getPredicate();

					if (StringUtils.isNotBlank(parameter) && parameter.startsWith("RELATION")) {
						if ("bomPartNum".equals(varName)) {
							String partURI = null;
							if (idToURI.containsKey(value)) {
								partURI = idToURI.get(value);
								uris.put(varName, partURI);
							} else {
								partURI = searchInstance(value, "QUERY_PART_BY_ID",
										QUERY_PART_BY_ID);
								if (partURI == null) {
									partURI = generateURI();
									String partData = generatePartData(partURI, value);
									result.append(partData);
								}
								uris.put(varName, partURI);
								idToURI.put(value, partURI);
							}
						}
						appendRelation(templateRow, uris, varName, value, result);
					} else {
						appendWithSeparator(result, parameter, selector,
								uris.get(templateRow.getSubject()), predicate, value);
					}
				}
			}

			LOGGER.debug("RESULT: \n{}", result);
			LOGGER.debug("END:");

			return result.toString();

		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * Executes the query for finding the instance by identifier
	 * 
	 * @param identifier
	 *            The instance identifier
	 * @param queryName
	 *            The name of the query
	 * @param query
	 *            The query
	 * @return URI of the instance if it is found, else returns null
	 */
	private String searchInstance(String identifier, String queryName, String query) {
		LOGGER.debug("Search for instance with ID {}", identifier);
		SearchArguments<Instance> arguments = new SearchArguments<Instance>();
		arguments.setSparqlQuery(true);
		arguments.setQueryName(queryName);
		arguments.setStringQuery(query);

		arguments.getArguments().put("identifier", identifier);
		// execute search
		searchService.search(Instance.class, arguments);

		List<Instance> partURI = arguments.getResult();
		if (!partURI.isEmpty()) {
			LOGGER.debug("Found {} results for value {}", partURI.size(), identifier);
			Instance partInstance = partURI.get(0);
			String uri = partInstance.getId().toString();
			LOGGER.debug("Received URI:{} for ID: {}", uri, identifier);
			return uri;
		}
		LOGGER.debug("No result found for value {} with query : {}", identifier, query);
		return null;
	}

	/**
	 * Appends relation to the transformed file. Appends complex relation or simple relation
	 * according to the predicate
	 * 
	 * @param templateRow
	 *            The template
	 * @param uris
	 *            Map with URIS
	 * @param varName
	 *            Name of the variable
	 * @param value
	 *            Value for the variable
	 * @param result
	 *            Transformed file as string
	 */
	private void appendRelation(TemplateRow templateRow, Map<String, String> uris, String varName,
			String value, StringBuilder result) {

		String parameter = templateRow.getParameter();
		String predicate = templateRow.getPredicate();
		String subject = templateRow.getSubject();

		String uri = uris.get(varName);
		if ((uri == null)) {
			if (org.apache.commons.lang.StringUtils.isBlank(value)) {
				// we have invalid value here so no valid relation to generate
				return;
			}
			// TODO search for the value with the corresponding query
			uri = "emf:" + value;
		}

		if (predicate.startsWith("createComplexRelation")) {
			// generate complex relation
			predicate = predicate.substring(predicate.indexOf("-") + 1);
			String relationURI = generateURI();

			appendWithSeparator(result, templateRow.getParameter(), " ", uris.get(subject),
					relationURI, uri);
			appendWithSeparator(result, "URI", " ", relationURI, "rdf:type", "emf:Relation");
			appendWithSeparator(result, " ", " ", relationURI, "emf:createdBy", "emf:system");
			appendWithSeparator(result, " ", " ", relationURI, "emf:relationType", predicate);
			appendWithSeparator(result, " ", " ", relationURI, "emf:isActive", "true");
		} else {
			// generate simple relation
			appendWithSeparator(result, parameter, " ", uris.get(subject), predicate, uri);
		}

	}

	/**
	 * Generates URI
	 * 
	 * @return URI for new object
	 */
	private String generateURI() {
		return SequenceEntityGenerator.generateId().toString();
	}

	/**
	 * Append the values as CSV divided by semicolon
	 * 
	 * @param source
	 *            Source to append to
	 * @param values
	 *            Values to append
	 * @return The source with appended values
	 */
	private StringBuilder appendWithSeparator(StringBuilder source, String... values) {
		for (String value : values) {
			source.append(value).append(";");
		}

		source.append("\n");
		return source;
	}

	/**
	 * Gets the file stream.
	 * 
	 * @param properties
	 *            the properties
	 * @return the file stream
	 */
	private InputStream getFileStream(Map<String, Serializable> properties) {
		Serializable serializable = properties.get(DocumentProperties.FILE_LOCATOR);
		if (serializable instanceof FileDescriptor) {
			return ((FileDescriptor) serializable).getInputStream();
		}
		return null;
	}

	/**
	 * Generates data for a missing Part
	 * 
	 * @param uri
	 *            Part URI
	 * @param partNumber
	 *            Part number
	 * @return String representation of the Part
	 */
	private String generatePartData(String uri, String partNumber) {
		StringBuilder builder = new StringBuilder();
		appendWithSeparator(builder, "URI", " ", uri, "rdf:type", "pdm:Part");
		appendWithSeparator(builder, " ", " ", uri, "emf:type", "QVI2PART");
		appendWithSeparator(builder, " ", " ", uri, "dcterms:identifier", partNumber);
		appendWithSeparator(builder, " ", " ", uri, "dcterms:title", partNumber);

		return builder.toString();
	}

}
