package com.sirma.itt.seip.expressions.conditions;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Responsible to parse and evalute conditions
 * 
 * @author Hristo Lungov
 */
public class ConditionsEvaluator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String AND = "AND";
	private static final String NOTIN = "NOTIN";
	private static final String IN = "IN";

	private static final Pattern EMPTY_PATTERN = Pattern.compile("^(-\\[\\w+])$");
	private static final Pattern BRACKETS_PATTERN = Pattern.compile("(\\+|\\-)?\\[|\\]");
	private static final Pattern SINGLE_QUOTES_PATTERN = Pattern.compile("'");
	private static final List<String> OPERATIONS = Arrays.asList(AND, "OR");

	/**
	 * Hide default constructor.
	 */
	private ConditionsEvaluator() {
	}

	/**
	 * Evaluate the condition expression.
	 *
	 * @param conditionExpression
	 *            the condition expression
	 * @return true, if successful
	 */
	public static Predicate<Instance> evaluate(String conditionExpression) {
		String normalized = conditionExpression.replaceAll("[\\s\\r\\n]+", "");
		ConditionsExpressionTokenizer tokenizer = new ConditionsExpressionTokenizer(normalized);
		List<String> tokenizedExpressions = new ArrayList<>(1);
		while (tokenizer.hasNext()) {
			String token = tokenizer.nextToken();
			if (StringUtils.isBlank(token)) {
				LOGGER.warn("Invalid empty or null token in {} expression was found!", conditionExpression);
				continue;
			}
			tokenizedExpressions.add(token);
			if (IN.equalsIgnoreCase(token) || NOTIN.equalsIgnoreCase(token)) {
				token = tokenizer.nextCollection();
				tokenizedExpressions.add(token);
			}
		}
		return evalExpressions(tokenizedExpressions);
	}

	/**
	 * Eval parsed tokenized expressions.
	 *
	 * @param tokenizedExpressions
	 *            the tokenized expressions
	 * @return predicate
	 */
	private static Predicate<Instance> evalExpressions(List<String> tokenizedExpressions) {
		List<String> toNextOperation = getToNextOperation(tokenizedExpressions);
		if (toNextOperation.isEmpty()) {
			return evalExpression(tokenizedExpressions);
		}
		Predicate<Instance> result = evalExpression(toNextOperation);
		int nextIndex = toNextOperation.size();
		String operation = tokenizedExpressions.get(nextIndex);
		List<String> newTokenizedExpressons = tokenizedExpressions.subList(nextIndex + 1, tokenizedExpressions.size());
		if (newTokenizedExpressons.isEmpty()) {
			return result;
		}
		if (AND.equalsIgnoreCase(operation)) {
			return result.and(evalExpressions(newTokenizedExpressons));
		}
		return result.or(evalExpressions(newTokenizedExpressons));
	}

	/**
	 * Eval current expression which could be like:
	 * 
	 * <pre>
	 * [status] IN ('COMPLETED', 'STOPPED')
	 * [status] NOTIN ('COMPLETED', 'STOPPED')
	 * [title]
	 * +[title]
	 * -[title]
	 * </pre>
	 *
	 * @param tokenizedExpressions
	 *            the tokenized expressions
	 * @return the predicate
	 */
	private static Predicate<Instance> evalExpression(List<String> tokenizedExpressions) {
		if (tokenizedExpressions.size() == 1) {
			return evalExistExpressions(tokenizedExpressions);
		} else if (tokenizedExpressions.size() == 3) {
			return evalContainsExpressions(tokenizedExpressions);
		}
		return instance -> false;
	}

	private static Predicate<Instance> evalExistExpressions(List<String> tokenizedExpressions) {
		String tokenizedExpression = tokenizedExpressions.get(0);
		if (EMPTY_PATTERN.matcher(tokenizedExpression).matches()) {
			return instance -> {
				String fieldValue = getFieldValue(instance, tokenizedExpression);
				return StringUtils.isBlank(fieldValue);
			};
		}
		return instance -> {
			String fieldValue = getFieldValue(instance, tokenizedExpression);
			return StringUtils.isNotBlank(fieldValue);
		};
	}

	private static Predicate<Instance> evalContainsExpressions(List<String> tokenizedExpressions) {
		boolean inclusive = IN.equalsIgnoreCase(tokenizedExpressions.get(1));
		String collectionToken = tokenizedExpressions.get(2);
		List<String> collection = Arrays.asList(collectionToken.split(","));
		return instance -> {
			String fieldValue = getFieldValue(instance, tokenizedExpressions.get(0));
			Optional<String> found = collection
					.stream()
						.filter(condition -> stripSingleQuotes(condition).equalsIgnoreCase(fieldValue))
						.findFirst();
			if (inclusive) {
				return found.isPresent();
			}
			return !found.isPresent();
		};
	}

	/**
	 * Gets sub list of expressions to the next operation or empty list if no operation found.
	 *
	 * @param tokenizedExpressions
	 *            the tokenized expressions
	 * @return subList of expressions or empty list if no operation found
	 */
	private static List<String> getToNextOperation(List<String> tokenizedExpressions) {
		for (int i = 0; i < tokenizedExpressions.size(); i++) {
			String token = tokenizedExpressions.get(i);
			if (OPERATIONS.contains(token)) {
				return tokenizedExpressions.subList(0, i);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Strip brackets from token.
	 *
	 * @param token
	 *            the token from expression
	 * @return token without brackets
	 */
	private static String stripBrackets(String token) {
		return BRACKETS_PATTERN.matcher(token).replaceAll("");
	}

	/**
	 * Strip single quotes from token.
	 *
	 * @param token
	 *            the token from expression
	 * @return token without single quotes
	 */
	private static String stripSingleQuotes(String token) {
		return SINGLE_QUOTES_PATTERN.matcher(token).replaceAll("");
	}

	/**
	 * Get value from instance based on token. Where token looks like:
	 * 
	 * <pre>
	 * "[department]"
	 * "[status]"
	 * "+[status]"
	 * "-[status]"
	 * </pre>
	 * 
	 * and will left only property:
	 * 
	 * <pre>
	 * "department"
	 * "status"
	 * </pre>
	 * 
	 * @param instance
	 *            the instance
	 * @param token
	 *            the token
	 * @return property value from instance
	 */
	private static String getFieldValue(Instance instance, String token) {
		String stripped = stripBrackets(token);
		return instance.getAsString(stripped);
	}
}