package com.sirma.itt.seip.definition.compile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;

/**
 * Parser types for the data type described in XML definitions.
 *
 * @author BBonev
 */
public class TypeParser {

	/** The pattern. */
	static Pattern pattern = Pattern.compile("([a]?)([n]{0,2})(\\.{0,2})(\\d+),?(\\d*)");

	/** The alpha. */
	boolean alpha;

	/** The numeric. */
	boolean numeric;

	/** The min length. */
	int minLength;

	/** The max length. */
	int maxLength;

	/** The logical. */
	boolean logical;

	/** The date. */
	boolean date;

	/** The datetime. */
	boolean datetime;

	/** The precision. */
	int precision;

	/** The any. */
	boolean any;

	/** The type. */
	String type;

	/** The instance. */
	boolean instance;

	/**
	 * Parses the.
	 *
	 * @param type
	 *            the type
	 * @return the type parser
	 */
	public static TypeParser parse(String type) {
		return new TypeParser(type);
	}

	/**
	 * Instantiates a new type perser.
	 *
	 * @param type
	 *            the type
	 */
	TypeParser(String type) {
		if (type == null) {
			return;
		}
		this.type = type.toLowerCase();
		if ("boolean".equals(this.type)) {
			logical = true;
			return;
		}
		if (this.type.startsWith("date")) {
			date = true;
			if ("datetime".equals(this.type)) {
				datetime = true;
			}
			return;
		}
		if ("instance".equals(this.type)) {
			instance = true;
			return;
		}
		Matcher matcher = pattern.matcher(this.type);
		if (!matcher.matches()) {
			any = true;
			return;
		}
		if (StringUtils.isNotBlank(matcher.group(1))) {
			alpha = true;
		}
		if (StringUtils.isNotBlank(matcher.group(2))) {
			numeric = true;
		}
		if (StringUtils.isNotBlank(matcher.group(3))) {
			minLength = 0;
		} else {
			minLength = 1;
		}
		String group = matcher.group(4);
		if (StringUtils.isNotBlank(group)) {
			maxLength = Integer.parseInt(group);
		}
		group = matcher.group(5);
		if (StringUtils.isNotBlank(group)) {
			precision = Integer.parseInt(group);
		}
		if (maxLength == 0) {
			maxLength = minLength + 1;
		}
	}

	/**
	 * Gets the data type definition name.
	 *
	 * @return the data type definition name
	 */
	public String getDataTypeDefinitionName() {
		if (logical) {
			return DataTypeDefinition.BOOLEAN;
		}
		if (alpha) {
			return DataTypeDefinition.TEXT;
		}
		if (instance) {
			return DataTypeDefinition.INSTANCE;
		}
		if (numeric) {
			if (precision == 0) {
				if (maxLength <= 9) {
					return DataTypeDefinition.INT;
				}
				return DataTypeDefinition.LONG;
			}
			if (maxLength <= 7) {
				return DataTypeDefinition.FLOAT;
			}
			return DataTypeDefinition.DOUBLE;
		}
		if (date) {
			if (datetime) {
				return DataTypeDefinition.DATETIME;
			}
			return DataTypeDefinition.DATE;
		}
		return DataTypeDefinition.ANY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TypeParser [alpha=");
		builder.append(alpha);
		builder.append(", minLength=");
		builder.append(minLength);
		builder.append(", maxLength=");
		builder.append(maxLength);
		builder.append(", logical=");
		builder.append(logical);
		builder.append(", date=");
		builder.append(date);
		builder.append(", datetime=");
		builder.append(datetime);
		builder.append(", numeric=");
		builder.append(numeric);
		builder.append(", precision=");
		builder.append(precision);
		builder.append(", any=");
		builder.append(any);
		builder.append(", type=");
		builder.append(type);
		builder.append(", getDataTypeDefinitionName()=");
		builder.append(getDataTypeDefinitionName());
		builder.append("]");
		return builder.toString();
	}
}