package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.expressions.BaseEvaluator;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * The convert class to dms properties.
 *
 * @author BBonev
 */
@Singleton
public class ToDmsConverterEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 1321106801746943100L;

	private static final String VALUE = "value";

	private static final Pattern VALUE_EXTRACTION = Pattern.compile(EXPRESSION_START
			+ "\\{to\\.dmsConvert\\(CL(\\d{1,4})\\s*?,\\s*?([\\w]+?)\\s*?,\\s*?(\\w+?)\\s*?\\)\\s*?\\}");

	@Inject
	private CodelistService codelistService;

	@Override
	protected Pattern getPattern() {
		return VALUE_EXTRACTION;
	}

	@Override
	public String getExpressionId() {
		return "to";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		if (values == null || values.length == 0) {
			return null;
		}
		Serializable value = values[0];
		Integer cl = Integer.valueOf(matcher.group(1));
		// the source field
		String from = matcher.group(2);
		// codelist field
		String to = matcher.group(3);
		Map<String, CodeValue> codeValues = codelistService.getCodeValues(cl);
		if (VALUE.equals(from)) {
			CodeValue codeValue = codeValues.get(value.toString());
			if (codeValue != null) {
				return codeValue.getProperties().get(to);
			}
		} else {
			for (Entry<String, CodeValue> entry : codeValues.entrySet()) {
				if (EqualsHelper.nullSafeEquals((String) entry.getValue().getProperties().get(from), value.toString(),
						true)) {
					if (VALUE.equals(to)) {
						return entry.getKey();
					}
					return entry.getValue().getProperties().get(to);
				}
			}
		}
		return value;
	}

}
