package com.sirma.sep.instance.properties.expression.evaluation;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.properties.expression.evaluation.PropertyValueEvaluator;

/**
 * Extracts the value from a specific field and uses this value to get the respective code value description, which is
 * what we want to show in the evaluated expression.
 *
 * @author A. Kunchev
 */
@Extension(target = PropertyValueEvaluator.PLUGIN_NAME, order = 50)
public class CodelistPropertyValueEvaluator implements PropertyValueEvaluator {

	@Inject
	private DefinitionService definitionService;

	@Inject
	private CodelistService codelistService;

	@Override
	public boolean canEvaluate(PropertyDefinition source, PropertyDefinition destination) {
		return PropertyDefinition.hasCodelist().test(source);
	}

	@Override
	public Serializable evaluate(Instance instance, String propertyName) {
		PropertyDefinition propertyDefinition = definitionService.getProperty(propertyName, instance);
		if (propertyDefinition == null) {
			return null;
		}

		Serializable value = instance.get(propertyName);
		Integer codelist = propertyDefinition.getCodelist();
		if (value instanceof Collection<?>) {
			return ((Collection<?>) value).stream().filter(Objects::nonNull)
					.map(element -> codelistService.getDescription(codelist, (String) element))
					.collect(Collectors.joining(", "));
		} else if (value instanceof String) {
			return codelistService.getDescription(codelist, (String) value);
		}

		return null;
	}
}
