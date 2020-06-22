package com.sirma.itt.seip.template.rules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.template.Template;

/**
 * Handles evaluation of template rules against set of filtering criteria.
 *
 * @author Adrian Mitev
 */
public class TemplateRuleUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateRuleUtils.class);

	private static ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

	/**
	 * Disable instantiation.
	 */
	private TemplateRuleUtils() {

	}

	/**
	 * Filters list of templates by matching their rules to filtering criteria. If at least one template has a rule,
	 * matching the filtering criteria, all templates with a rule matching the criteria are returned. If there is no
	 * such template, all the templates without a rule are returned.
	 *
	 * @param templates
	 *            list of templates to filter.
	 * @param criteria
	 *            filtering criteria.
	 * @return filtered list.
	 */
	public static List<Template> filter(List<Template> templates, Map<String, Serializable> criteria) {
		List<Template> result = new ArrayList<>();

		if (!criteria.isEmpty()) {
			Bindings context = new SimpleBindings();
			setContextProperties(context, new HashMap<>(criteria));

			for (Template template : templates) {
				if (matches(context, template)) {
					result.add(template);
				}
			}
		}

		// use the templates without rule if there isn't a template matching the criteria
		if (result.isEmpty()) {
			templates.stream().filter(template -> StringUtils.isEmpty(template.getRule())).forEach(result::add);
		}

		return result;
	}

	private static boolean matches(Bindings context, Template template) {
		boolean matches = false;

		if (StringUtils.isNotEmpty(template.getRule())) {
			try {
				String templateRule = escapeRule(template.getRule());
				matches = (boolean) engine.eval(templateRule, context);
			} catch (ScriptException e) { //NOSONAR
				LOGGER.warn("{} - Can occur when there is a rule with fields that are no longer in the model.",
							e.getMessage());
			}
		}

		return matches;
	}

	/**
	 * Fills context bindings with key values for script evaluation. As currently only single-values are supported, if
	 * the value comes from collection, it is extracted as a single value.
	 *
	 * @param bindings
	 *            context bindings
	 * @param properties
	 *            properties to put in the context
	 */
	private static void setContextProperties(Bindings bindings, Map<String, Serializable> properties) {
		properties.entrySet().stream().peek(entry -> {
			if (entry.getValue() instanceof List<?>) {
				List<?> value = (List<?>) entry.getValue();
				// The UI currently doesn't support creating rules for multi-valued properties
				// Also check if received an empty array
				if (!value.isEmpty()) {
					entry.setValue((Serializable) value.get(0));
				}
			}
		}).forEach(entry -> bindings.put(escapeColon(entry.getKey()), entry.getValue()));
	}

	/**
	 * Makes sure fields in the provided rule are escaped from forbidden characters and can be used in expression evaluation.
	 *
	 * @param rule - the provided template rule
	 * @return escaped rule
	 */
	private static String escapeRule(String rule) {
		String escapedRule = rule;
		if (rule.contains(":")) {
			Set<String> propertiesToEscape = parseRule(rule).keySet()
					.stream()
					.filter(prop -> prop.contains(":"))
					.collect(Collectors.toSet());

			for (String property : propertiesToEscape) {
				escapedRule = escapedRule.replace(property, escapeColon(property));
			}
		}
		return escapedRule;
	}

	private static String escapeColon(String rule) {
		// Colons are forbidden when naming variables
		return rule.replace(':', '_');
	}

	/**
	 * Parses a rule into a map with property names and values matching the rule filtering criteria.
	 *
	 * @param rule
	 *            rule to parse.
	 * @return parsed statements.
	 */
	public static Map<String, Serializable> parseRule(String rule) {
		Map<String, Serializable> result = new LinkedHashMap<>();

		String[] entries = rule.split("&&");

		for (String entry : entries) {
			entry = entry.trim();

			if (entry.contains("||")) {
				handleOrStatements(entry, result);
			} else {
				handleStatement(entry, result);
			}

		}

		return result;
	}

	private static void handleOrStatements(String entry, Map<String, Serializable> parsedStatements) {
		String entryWithoutBrackets = removeWrappingBrackets(entry);

		String[] orStatements = entryWithoutBrackets.split("\\|\\|");

		for (String statement : orStatements) {
			handleStatement(statement, parsedStatements);
		}
	}

	private static void handleStatement(String statement, Map<String, Serializable> parsedStatements) {
		String[] keyValue = statement.split("==");

		String name = keyValue[0].trim();
		String value = keyValue[1].trim();

		if (value.charAt(0) == '"') {
			value = value.substring(1, value.length() - 1);

			List<String> values = (List<String>) parsedStatements.get(name);
			if (values == null) {
				values = new ArrayList<>();
				parsedStatements.put(name, (Serializable) values);
			}

			values.add(value);
		} else {
			parsedStatements.put(name, Boolean.valueOf(value));
		}
	}

	private static String removeWrappingBrackets(String statement) {
		String result = statement;
		if (statement.charAt(0) == '(' && statement.charAt(statement.length() - 1) == ')') {
			result = statement.substring(1, statement.length() - 1);
		}
		return result;
	}

	/**
	 * Gets the hash code of the rule and doesn't honor the order of the passed properties/values. the following two
	 * rules will produce equal hash codes: </br>
	 * <code>
	 * functional == "MDG" && (department == "CEG" || department == "ENG" || department == "HMR") </br>
	 * (department == "ENG" || department == "CEG" || department == "HMR") && functional == "MDG"
	 * </code>
	 *
	 * @param rule
	 * @return
	 */
	public static int getRuleHashCode(String rule) {
		Map<String, Serializable> parsed = TemplateRuleUtils.parseRule(rule);
		sortRuleValues(parsed);
		return parsed.hashCode();
	}

	/**
	 * Checks if the two passed rules are equal. Doesn't honor the order of the passed properties/values. For example,
	 * the following two rules will be equal: </br>
	 * <code>
	 * functional == "MDG" && (department == "CEG" || department == "ENG" || department == "HMR") </br>
	 * (department == "ENG" || department == "CEG" || department == "HMR") && functional == "MDG"
	 * </code>
	 *
	 * @param left
	 *            the first rule
	 * @param right
	 *            the second rule
	 * @return true if both rules are equal
	 */
	public static boolean equals(String left, String right) {
		return getRuleHashCode(left) == getRuleHashCode(right);
	}

	private static void sortRuleValues(Map<String, Serializable> rule) {
		for (Map.Entry<String, Serializable> entry : rule.entrySet()) {
			if (entry.getValue() instanceof List<?>) {
				Collections.sort((List) entry.getValue());
			}
		}
	}
}
