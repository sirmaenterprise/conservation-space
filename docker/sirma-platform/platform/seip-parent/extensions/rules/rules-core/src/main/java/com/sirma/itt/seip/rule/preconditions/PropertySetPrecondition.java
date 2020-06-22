package com.sirma.itt.seip.rule.preconditions;

import java.io.Serializable;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.rule.BaseDynamicInstanceRule;
import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.emf.rule.RulePrecondition;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rule.model.PropertyMapping;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Property precondition checks
 *
 * @author BBonev
 */
@Named(PropertySetPrecondition.PROPERTY_SET_NAME)
public class PropertySetPrecondition extends BaseDynamicInstanceRule implements RulePrecondition {

	public static final String PROPERTY_SET_NAME = "propertySet";

	private Collection<PropertyMapping> propertyMappings;

	@Inject
	private TypeConverter typeConverter;

	@Override
	public boolean configure(Context<String, Object> configuration) {
		if (!super.configure(configuration)) {
			return false;
		}

		Collection<?> mappings = configuration.getIfSameType(PropertyMapping.NAME, Collection.class);
		propertyMappings = PropertyMapping.parse(mappings);
		return CollectionUtils.isNotEmpty(propertyMappings);
	}

	@Override
	public String getName() {
		return PROPERTY_SET_NAME;
	}

	@Override
	public String getPrimaryOperation() {
		return PROPERTY_SET_NAME;
	}

	@Override
	public boolean isAsyncSupported() {
		return false;
	}

	@Override
	public boolean checkPreconditions(RuleContext processingContext) {
		Instance instance = processingContext.getTriggerInstance();
		for (PropertyMapping mapping : propertyMappings) {
			if (!matchProperty(mapping, instance)) {
				return false;
			}
		}
		return true;
	}

	private boolean matchProperty(PropertyMapping mapping, Instance instance) {
		Serializable serializable = instance.getProperties().get(mapping.getFrom());
		if (serializable == null) {
			return false;
		}
		if (mapping.getValue() != null) {
			Serializable converted = typeConverter.tryConvert(serializable.getClass(), mapping.getValue());
			return converted != null && EqualsHelper.nullSafeEquals(serializable, converted);
		}
		return true;
	}

}
