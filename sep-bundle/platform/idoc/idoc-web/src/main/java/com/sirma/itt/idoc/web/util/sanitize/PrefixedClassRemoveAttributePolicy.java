package com.sirma.itt.idoc.web.util.sanitize;

import java.util.regex.Pattern;

import org.owasp.html.AttributePolicy;

import com.sirma.itt.commons.utils.string.StringUtils;

/**
 * Meant to be applied to html element class attribute and remove values that start with the
 * specified prefix.
 * 
 * @author yasko
 */
public class PrefixedClassRemoveAttributePolicy implements AttributePolicy {

	private Pattern pattern;

	/**
	 * @param prefix
	 *            CSS class value prefix to look for.
	 */
	public PrefixedClassRemoveAttributePolicy(String prefix) {
		this.pattern = Pattern.compile(prefix + "[a-zA-Z0-9\\-_]+");
	}

	@Override
	public String apply(String elementName, String attributeName, String value) {
		if (StringUtils.isNotNullOrEmpty(value)) {
			return pattern.matcher(value).replaceAll("").trim();
		}
		return value;
	}
}
