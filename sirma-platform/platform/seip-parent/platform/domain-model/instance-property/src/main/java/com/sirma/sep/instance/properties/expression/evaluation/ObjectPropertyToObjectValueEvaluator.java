package com.sirma.sep.instance.properties.expression.evaluation;

import java.io.Serializable;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.properties.expression.evaluation.PropertyValueEvaluator;

/**
 * Evaluate a object property when destination field is another object property field. Resolve instance and extract all
 * needed properties (id / compact_header).
 *
 * @author Stella D
 */
@Extension(target = PropertyValueEvaluator.PLUGIN_NAME, order = 90)
public class ObjectPropertyToObjectValueEvaluator implements PropertyValueEvaluator {

	@Inject
	private HeadersService headersService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public boolean canEvaluate(PropertyDefinition source, PropertyDefinition destination) {
		return PropertyDefinition.isObjectProperty().test(source)
				&& PropertyDefinition.isObjectProperty().test(destination);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializable evaluate(Instance instance, String propertyName) {
		JsonArrayBuilder instancePropertiesArray = Json.createArrayBuilder();

		getInstances(instance, propertyName, instanceTypeResolver).forEach(resolvedInstance -> {
			JsonObjectBuilder instanceProperties = Json.createObjectBuilder();
			JsonObjectBuilder headers = Json.createObjectBuilder();
			headers.add(DefaultProperties.HEADER_COMPACT,
					headersService.generateInstanceHeader(resolvedInstance, DefaultProperties.HEADER_COMPACT));

			instanceProperties.add("id", (String) resolvedInstance.getId());
			instanceProperties.add("headers", headers);
			instancePropertiesArray.add(instanceProperties);
		});

		return instancePropertiesArray.build().toString();
	}

}
