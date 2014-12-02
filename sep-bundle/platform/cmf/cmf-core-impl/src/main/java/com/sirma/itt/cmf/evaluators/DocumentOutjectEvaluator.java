package com.sirma.itt.cmf.evaluators;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.evaluation.BaseEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.exceptions.TypeConversionException;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Evaluator class for outjecting values to document instances from a task instance.
 * <p>
 * <code><pre> ${outject(sourseField, sectionId.selectorField[selectorvalue].destinationField)} - copy task field to all found documents
 * ${outject([sourceValue], sectionId.selectorField[selectorvalue].destinationField)} - copy given value to to all found documents
 * ${outject(sourseField, sectionId.[selectorvalue].destinationField)} - copy task field to all found documents (default selectorField=type)</pre></code>
 * 
 * @author BBonev
 */
public class DocumentOutjectEvaluator extends BaseEvaluator {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -140540828543092459L;
	/** The Constant VALUE_EXTRACTION. */
	private static final Pattern VALUE_EXTRACTION = Pattern
			.compile(EXPRESSION_START
					+ "\\{outject\\((?:(\\w+)|\\[(\\w+)\\])\\s*,\\s*(\\w+)(\\.\\w+)?\\[(\\w+)\\]\\.(\\w+)\\)\\}");
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pattern getPattern() {
		return VALUE_EXTRACTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {
		if ((values == null) || (values.length < 2)) {
			return null;
		}
		Serializable taskInstance = getCurrentInstance(context, values);
		CaseInstance caseInstance = getTypedParameter(values, CaseInstance.class);
		// we need that argument so if not present then we a done
		if ((caseInstance == null) || (taskInstance == null)) {
			// we could also throw an exception
			return null;
		}

		String sourceField = matcher.group(1);
		String sourceValue = matcher.group(2);
		String sectionId = matcher.group(3);
		String documentFieldName = matcher.group(4);
		if (StringUtils.isNullOrEmpty(documentFieldName)) {
			documentFieldName = DocumentProperties.TYPE;
		} else {
			documentFieldName = documentFieldName.substring(1);
		}
		String selectorValue = matcher.group(5);

		String destinationField = matcher.group(6);

		Serializable serializable;
		if (StringUtils.isNullOrEmpty(sourceValue) && (taskInstance instanceof PropertyModel)) {
			serializable = ((PropertyModel) taskInstance).getProperties().get(sourceField);
		} else {
			serializable = sourceValue;
		}

		for (SectionInstance sectionInstance : caseInstance.getSections()) {
			if (EqualsHelper.nullSafeEquals(sectionId, sectionInstance.getIdentifier(), true)) {
				for (Instance documentInstance : sectionInstance.getContent()) {
					Map<String, Serializable> properties = documentInstance.getProperties();
					Serializable selector = properties.get(documentFieldName);
					if (selector != null) {
						try {
							// get the original selector value and convert the
							// expression value to the same type
							Serializable convert = converter.convert(selector.getClass(),
									selectorValue);
							// now the can compare the two values
							if (EqualsHelper.nullSafeEquals(selector, convert)) {
								documentInstance.getProperties()
										.put(destinationField, serializable);
							}
						} catch (TypeConversionException e) {
							logger.warn("Failed to convert expression value " + selectorValue
									+ " to field's " + documentFieldName + " value type"
									+ selector.getClass() + " due to " + e.getMessage());
						}
					}
				}
				// no need to iterate for all sections
				break;
			}
		}

		return serializable;
	}

}
