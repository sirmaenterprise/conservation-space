package com.sirma.sep.instance.properties.expression.evaluation;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jsoup.Jsoup;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.properties.expression.evaluation.PropertyValueEvaluator;

/**
 * Evaluate a object property when destination field is text field. Resolve instance and generate breadcrumb header. If
 * property is multivalued all headers of objects will be populated into result separated by comma.
 *
 * @author Boyan Tonchev.
 */
@Extension(target = PropertyValueEvaluator.PLUGIN_NAME, order = 30)
public class ObjectPropertyToTextValueEvaluator implements PropertyValueEvaluator {

	@Inject
	private HeadersService headersService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public boolean canEvaluate(PropertyDefinition source, PropertyDefinition destination) {
		return PropertyDefinition.isObjectProperty().test(source)
				&& PropertyDefinition.hasType(DataTypeDefinition.TEXT).test(destination);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializable evaluate(Instance instance, String propertyName) {

		return getInstances(instance, propertyName, instanceTypeResolver)
				.map(resolvedInstance -> headersService.generateInstanceHeader(resolvedInstance,
						DefaultProperties.HEADER_BREADCRUMB))
				.filter(Objects::nonNull).map(header -> Jsoup.parse(header).text()).collect(Collectors.joining(", "));
	}
}
