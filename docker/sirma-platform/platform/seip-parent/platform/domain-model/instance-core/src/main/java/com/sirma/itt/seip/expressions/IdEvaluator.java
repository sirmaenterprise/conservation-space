package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.Purposable;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.CMInstance;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Expression for extracting various IDs.
 *
 * @author BBonev
 */
@Singleton
public class IdEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 4668280896926449638L;

	private static final Pattern PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{id\\.?(cm|dm|db|uid|type|purpose|identifier|name)?(\\.format)?" + FROM_PATTERN + MATCHES + "\\}");

	private static final Pattern DIGITS_ONLY = Pattern.compile("\\D+");

	@Inject
	private DefinitionService definitionService;

	@Override
	protected Pattern getPattern() {
		return PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "id";
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		Serializable serializable = evaluateFrom(matcher.group(FROM_GROUP), context, values);

		if (serializable == null) {
			return validateAndReturn("NO ID", "", matcher);
		}

		String subId = matcher.group(1);
		String format = matcher.group(2);
		if (StringUtils.isBlank(subId) || "cm".equals(subId) && serializable instanceof CMInstance) {
			String managementId = ((CMInstance) serializable).getContentManagementId();
			if (StringUtils.isNotBlank(format) && StringUtils.isNotBlank(managementId)) {
				managementId = DIGITS_ONLY.matcher(managementId).replaceAll("");
			}
			return validateAndReturn(cleanId(managementId), "", matcher);
		} else if ("dm".equals(subId) && serializable instanceof DMSInstance) {
			return validateAndReturn(cleanId(((DMSInstance) serializable).getDmsId()), "", matcher);
		} else if ("db".equals(subId) && serializable instanceof Entity) {
			Serializable id = ((Entity) serializable).getId();
			ShortUri uri = converter.convert(ShortUri.class, id);
			if (uri != null) {
				id = uri.toString();
			}
			return validateAndReturn(cleanId(id), "", matcher);
		} else if ("uid".equals(subId) && serializable instanceof PropertyModel) {
			Serializable result = ((PropertyModel) serializable)
					.getProperties()
						.get(DefaultProperties.UNIQUE_IDENTIFIER);
			return validateAndReturn(cleanId(result), "", matcher);
		} else if ("type".equals(subId) && serializable instanceof Instance) {
			String identifier = getDefinitionId((Instance) serializable);
			return validateAndReturn(cleanId(identifier), "", matcher);
		} else if ("purpose".equals(subId)) {
			if (serializable instanceof Purposable) {
				return validateAndReturn(((Purposable) serializable).getPurpose(), "null", matcher);
			}
			return "null";
		} else if ("identifier".equals(subId) && serializable instanceof Identity) {
			return validateAndReturn(((Identity) serializable).getIdentifier(), "", matcher);
		} else if ("name".equals(subId) && serializable instanceof Named) {
			return validateAndReturn(((Named) serializable).getName(), "", matcher);
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
		DefinitionModel model = definitionService.getInstanceDefinition(instance);
		String identifier = instance.getIdentifier();
		if (model != null) {
			PropertyDefinition property = PathHelper.findProperty(model, (PathElement) model, DefaultProperties.TYPE);
			if (property != null && StringUtils.isNotBlank(property.getDefaultValue())) {
				identifier = property.getDefaultValue();
			}
		}
		return identifier;
	}

	/**
	 * Cleans the id from any internal/system parts. Default implementation does not modify the id. The method is called
	 * just before the id validation.
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
