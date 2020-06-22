package com.sirma.itt.seip.instance.save.expression.evaluation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.BaseEvaluator;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;

/**
 * Expression evaluator for object properties.
 *
 * The property is a field inside the definition and has identifier(s) as value.
 *
 * @author tsdimitrov
 */
@Singleton
public class ObjectPropertyExpressionEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 1L;

	private static final String OBJECT_PROPERTY = "objectProperty";
	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START + "\\{objectProperty\\((.*)\\)\\}");

	private static final String EMPTY_RESULT = "";
	private static final String SPLITTER = ",";
	private static final String WHITESPACE_SPLITTER = "\\s*,\\s*";
	private static final String NULL = "null";

	private static final Whitelist WHITE_LIST;

	@Inject
	private HeadersService headerService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public String getExpressionId() {
		return OBJECT_PROPERTY;
	}

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	static {
		WHITE_LIST = Whitelist.simpleText();
		// supported tags
		WHITE_LIST.addTags("a", "span", "img");
		// supported attributes by tag
		WHITE_LIST.addAttributes("a", "class", "href");
		WHITE_LIST.addAttributes("img", "src");
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String instanceIds = matcher.group(1);
		if (!StringUtils.isNotBlank(instanceIds) || NULL.equals(instanceIds)) {
			return EMPTY_RESULT;
		}
		List<String> ids = Arrays.asList(instanceIds.split(WHITESPACE_SPLITTER));
		return instanceTypeResolver.resolveInstances(ids).stream().map(instance -> getBreadcrumb(instance))
				.collect(Collectors.joining(SPLITTER));
	}

	private String getBreadcrumb(Instance instance) {
		String header = headerService.generateInstanceHeader(instance, DefaultProperties.HEADER_BREADCRUMB);
		if (StringUtils.isNotBlank(header)) {
			return Jsoup.clean(header, WHITE_LIST);
		}
		return EMPTY_RESULT;
	}
}
