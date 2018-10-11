package com.sirma.itt.seip.expressions.conditions;

import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used for helping spliting condition expressions to tokens.
 * 
 * @author Hristo Lungov
 */
public class ConditionsExpressionTokenizer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Pattern CONDITIONS_PATTERN = Pattern
			.compile("^[+-]?\\[.*]|(AND|OR|IN|NOTIN)|(\\(|.*\\)|\\s)$");
	private static final Pattern COLLECTION_TOKEN_PATTERN = Pattern.compile("(?<=\\()(.+?)(?=\\))");
	private String expression;
	private int expressionLength;
	private int pointer = 0;
	private String sub = "";

	/**
	 * Instantiates a new conditions expression tokenizer.
	 *
	 * @param expression
	 *            the whole condition expression
	 */
	public ConditionsExpressionTokenizer(String expression) {
		this.expression = expression;
		this.expressionLength = expression.length();
	}

	/**
	 * Returns next token from condition expression.
	 *
	 * @return the next token found
	 */
	public String nextToken() {
		for (; pointer < expressionLength; pointer++) {
			this.sub += this.expression.substring(this.pointer, this.pointer + 1);
			boolean match = CONDITIONS_PATTERN.matcher(this.sub).matches();
			if (match) {
				String token = this.sub;
				this.sub = "";
				this.pointer = this.pointer + 1;
				return token;
			}
		}
		return null;
	}

	/**
	 * Verify that reading isn't over. By checking the length of read expression.
	 *
	 * @return true, if successful
	 */
	public boolean hasNext() {
		return this.pointer < this.expressionLength;
	}

	/**
	 * Used for fetching expressions like ('STOPPED','COMPLETED'). Those expressions are usually after IN or NOTIN
	 * token.
	 *
	 * @return token like 'STOPPED','COMPLETED'
	 */
	public String nextCollection() {
		String subExpr = this.expression.substring(this.pointer);
		Matcher matcher = COLLECTION_TOKEN_PATTERN.matcher(subExpr);
		if (matcher.find()) {
			String result = matcher.group();
			if (StringUtils.isNotBlank(result)) {
				this.pointer += result.length() + 2;
				return result;
			}
		}
		LOGGER.warn("Can't match regex {} on string {}. Please check the definition!",
				COLLECTION_TOKEN_PATTERN.pattern(), subExpr);
		return StringUtils.EMPTY;
	}

}