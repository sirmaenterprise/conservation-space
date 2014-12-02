package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.Purposable;
import com.sirma.itt.emf.instance.model.CMInstance;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Expression for extracting various IDs.
 *
 * @author BBonev
 */
public class IdEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4668280896926449638L;
	private static final Pattern PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{id\\.?(cm|dm|db|uid|type|purpose)?(\\.format)?" + FROM_PATTERN + MATCHES + "\\}");

	private static final Pattern DIGITS_ONLY = Pattern.compile("\\D+");
	@Inject
	private DictionaryService dictionaryService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pattern getPattern() {
		return PATTERN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {
		Serializable serializable = evaluateFrom(matcher.group(FROM_GROUP), context, values);

		String subId = matcher.group(1);
		String format = matcher.group(2);
		if (StringUtils.isNullOrEmpty(subId)
				|| ("cm".equals(subId) && (serializable instanceof CMInstance))) {
			String managementId = ((CMInstance) serializable).getContentManagementId();
			if (StringUtils.isNotNullOrEmpty(format) && StringUtils.isNotNullOrEmpty(managementId)) {
				managementId = DIGITS_ONLY.matcher(managementId).replaceAll("");
			}
			return validateAndReturn(cleanId(managementId), "", matcher);
		} else if ("dm".equals(subId) && (serializable instanceof DMSInstance)) {
			return validateAndReturn(cleanId(((DMSInstance) serializable).getDmsId()), "", matcher);
		} else if ("db".equals(subId) && (serializable instanceof Entity)) {
			return validateAndReturn(cleanId(((Entity) serializable).getId()), "", matcher);
		} else if ("uid".equals(subId) && (serializable instanceof PropertyModel)) {
			Serializable result = ((PropertyModel) serializable).getProperties().get(
					DefaultProperties.UNIQUE_IDENTIFIER);
			return validateAndReturn(cleanId(result), "", matcher);
		} else if ("type".equals(subId) && (serializable instanceof Instance)) {
			String identifier = getDefinitionId((Instance) serializable);
			return validateAndReturn(cleanId(identifier), "", matcher);
		} else if ("purpose".equals(subId)) {
			if (serializable instanceof Purposable) {
				return validateAndReturn(((Purposable) serializable).getPurpose(), "null", matcher);
			}
			return "null";
		}
		return validateAndReturn("NO ID", "", matcher);
	}

	/**
	 * Gets the definition id.
	 * 
	 * @param instance
	 *            the serializable
	 * @return the definition id
	 */
	private String getDefinitionId(Instance instance) {
		DefinitionModel model = dictionaryService.getInstanceDefinition(instance);
		String identifier = instance.getIdentifier();
		if (model != null) {
			PropertyDefinition property = PathHelper.findProperty(model, (PathElement) model,
					DefaultProperties.TYPE);
			if ((property != null) && StringUtils.isNotNullOrEmpty(property.getDefaultValue())) {
				identifier = property.getDefaultValue();
			}
		}
		return identifier;
	}

	/**
	 * Cleans the id from any internal/system parts. Default implementation does not modify the id.
	 * The method is called just before the id validation.
	 *
	 * @param id
	 *            the id
	 * @return the id
	 */
	protected Serializable cleanId(Serializable id) {
		if (id instanceof String) {
			return id.toString().replace("activiti$", "");
		}
		return id;
	}

}
