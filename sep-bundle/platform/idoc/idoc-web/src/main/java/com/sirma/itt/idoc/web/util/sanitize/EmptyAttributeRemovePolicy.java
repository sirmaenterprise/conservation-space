package com.sirma.itt.idoc.web.util.sanitize;

import org.owasp.html.AttributePolicy;

import com.sirma.itt.commons.utils.string.StringUtils;

/**
 * Removes entirely the attribute if its value is null or an empty string.
 * 
 * @author Adrian Mitev
 */
public class EmptyAttributeRemovePolicy implements AttributePolicy {

	@Override
	public String apply(String elementName, String attributeName, String value) {
		if (StringUtils.isNullOrEmpty(value)) {
			return null;
		}
		return value;
	}

}
