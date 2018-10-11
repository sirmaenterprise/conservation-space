package com.sirma.itt.seip.template.utils;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.rules.TemplateRuleUtils;

/**
 * Provides commonly used functionality for managing templates.
 * 
 * @author Vilizar Tsonev
 */
public class TemplateUtils {

	/** The forbidden characters for template identifier */
	public static final Pattern FORBIDDEN_NAME_CHARACTERS = Pattern.compile("\\s|\\W", Pattern.UNICODE_CHARACTER_CLASS);

	private static final String EMAIL_TEMPLATE_TYPE = "emailTemplate";

	private TemplateUtils() {
		// utility class
	}

	/**
	 * Generates template identifier from the given title. Forbidden characters are cut off (see
	 * {@link TemplateUtils#FORBIDDEN_NAME_CHARACTERS}) and it is converted to lower case.
	 * 
	 * @param string
	 *            is the title string to build identifier from
	 * @return the identifier
	 */
	public static String buildIdFromTitle(String string) {
		return FORBIDDEN_NAME_CHARACTERS.matcher(string).replaceAll("").toLowerCase();
	}

	private static String calculateTemplateGroupHash(Template template) {
		StringBuilder hashBuilder = new StringBuilder(20);
		hashBuilder.append(template.getForType());
		hashBuilder.append(template.getPurpose());

		if (StringUtils.isNotBlank(template.getRule())) {
			hashBuilder.append(TemplateRuleUtils.getRuleHashCode(template.getRule()));
		}

		return hashBuilder.toString();
	}

	/**
	 * Filters the passed templates, leaving only the non-mail ones and aggregates them by group. The aggregation is
	 * performed as a hash is generated for each template, taking as parameters its forType + purpose + rule (if any)
	 * and it is used as a key in the map. All templates that conform to the given group (hash) are put in a list, as a
	 * value of that key. The hash-code of the template rule is calculated via
	 * {@link TemplateRuleUtils#getRuleHashCode(String)} so it does not depend of the rule's key-values order.
	 * 
	 * @param templates is the input list of templates
	 * @return the mapping of group hash -> list of templates
	 */
	public static Map<String, List<Template>> groupNonMailTemplatesByHash(List<Template> templates) {
		return templates
				.stream()
				.filter(template -> StringUtils.isNotBlank(template.getForType()))
				.filter(template -> !EMAIL_TEMPLATE_TYPE.equals(template.getForType()))
				.collect(Collectors.groupingBy(TemplateUtils::calculateTemplateGroupHash));
	}
}
