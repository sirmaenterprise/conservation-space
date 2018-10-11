package com.sirma.itt.imports;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Template for transforming the document data
 *
 * @author kirq4e
 */
public class Template {

	private String uriVariableName;
	private String type;
	private String definitionId;
	private Map<String, List<TemplateRow>> options;

	/**
	 * Checks if this template can handle the variable name
	 *
	 * @param variableName
	 *            name of the variable
	 * @return true if the template can handle the variable, else false
	 */
	public boolean canHandle(String variableName) {
		if (options != null) {
			return options.containsKey(variableName);
		}
		return false;
	}

	/**
	 * Adds option to the template
	 *
	 * @param parameter
	 *            Parameter
	 * @param subject
	 *            Subject
	 * @param predicate
	 *            Predicate
	 * @param value
	 *            Value
	 */
	public void addOption(String parameter, String subject, String predicate, String value) {
		if (options == null) {
			options = new LinkedHashMap<>();
		}
		if (StringUtils.isNotBlank(subject) && StringUtils.isNotBlank(predicate)
				&& StringUtils.isNotBlank(value)) {
			if (predicate.equals("rdf:type")) {
				uriVariableName = subject;
				type = value;
			} else if (predicate.equals("emf:type")) {
				definitionId = value;
			} else {
				CollectionUtils.addValueToMap(options, value, new TemplateRow(parameter, subject,
						predicate, value));
			}
		}
	}

	/**
	 * Creates instance according to the template
	 *
	 * @param uri
	 *            URI of the instance
	 * @return Instance properties in CSV Format
	 */
	public String createInstance(String uri) {
		StringBuilder result = new StringBuilder();
		result.append("URI").append(";").append(" ").append(";").append(uri).append(";")
				.append("rdf:type").append(";").append(type);
		if (StringUtils.isNotBlank(definitionId)) {
			result.append("\n ").append(";").append(" ").append(";").append(uri).append(";")
					.append("emf:type").append(";").append(definitionId);
		}
		return result.toString();
	}

	/**
	 * Get options for given variable
	 *
	 * @param variableName
	 *            The variable name
	 * @return List of template options for the variable
	 */
	public List<TemplateRow> getOption(String variableName) {
		if ((options != null) && StringUtils.isNotBlank(variableName)) {
			return options.get(variableName);
		}
		return null;
	}

	/**
	 * Get relations that can be created with the current template for an instance
	 *
	 * @return List of relations
	 */
	public List<TemplateRow> getRelations() {
		List<TemplateRow> relations = new ArrayList<>();
		for (Entry<String, List<TemplateRow>> entry : options.entrySet()) {
			List<TemplateRow> templateRows = entry.getValue();
			for (TemplateRow templateRow : templateRows) {
				String parameter = templateRow.getParameter();
				String value = templateRow.getValue();
				if (StringUtils.isNotBlank(parameter) && parameter.startsWith("RELATION")
						&& StringUtils.isNotBlank(value) && value.endsWith("URI")) {
					relations.add(templateRow);
				}
			}
		}
		return relations;
	}

	/**
	 * Getter method for options.
	 *
	 * @return the options
	 */
	public Map<String, List<TemplateRow>> getOptions() {
		return options;
	}

	/**
	 * Setter method for options.
	 *
	 * @param options
	 *            the options to set
	 */
	public void setOptions(Map<String, List<TemplateRow>> options) {
		this.options = options;
	}

	/**
	 * Getter method for uriVariableName.
	 *
	 * @return the uriVariableName
	 */
	public String getUriVariableName() {
		return uriVariableName;
	}

	/**
	 * Setter method for uriVariableName.
	 *
	 * @param uriVariableName
	 *            the uriVariableName to set
	 */
	public void setUriVariableName(String uriVariableName) {
		this.uriVariableName = uriVariableName;
	}

}
