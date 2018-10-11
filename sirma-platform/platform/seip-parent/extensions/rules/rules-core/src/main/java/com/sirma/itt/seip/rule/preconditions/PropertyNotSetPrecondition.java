package com.sirma.itt.seip.rule.preconditions;

import java.util.Collection;

import javax.inject.Named;

import com.sirma.itt.emf.rule.BaseDynamicInstanceRule;
import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.emf.rule.RulePrecondition;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rule.model.PropertyMapping;

/**
 * Property precondition checks
 *
 * @author BBonev
 */
@Named(PropertyNotSetPrecondition.PROPERTY_NOT_SET_NAME)
public class PropertyNotSetPrecondition extends BaseDynamicInstanceRule implements RulePrecondition {

	public static final String PROPERTY_NOT_SET_NAME = "propertyNotSet";
	private Collection<PropertyMapping> propertyMappings;

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
		return PROPERTY_NOT_SET_NAME;
	}

	@Override
	public String getPrimaryOperation() {
		return PROPERTY_NOT_SET_NAME;
	}

	@Override
	public boolean isAsyncSupported() {
		return false;
	}

	@Override
	public boolean checkPreconditions(RuleContext processingContext) {
		Instance instance = processingContext.getTriggerInstance();
		for (PropertyMapping mapping : propertyMappings) {
			if (checkProperty(mapping, instance)) {
				return false;
			}
		}
		return true;
	}

	private boolean checkProperty(PropertyMapping mapping, Instance instance) {
		return instance.getProperties().containsKey(mapping.getFrom());
	}

}
