package com.sirma.itt.seip.rule.preconditions;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.inject.Named;

import com.sirma.itt.emf.rule.BaseDynamicInstanceRule;
import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.emf.rule.RulePrecondition;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rule.model.PropertyMapping;
import com.sirma.itt.seip.rule.model.PropertyValueChange;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Precondition that checks for changes in property values.
 *
 * @author BBonev
 */
@Named(PropertyChangePrecondition.PROPERTY_CHANGE_NAME)
public class PropertyChangePrecondition extends BaseDynamicInstanceRule implements RulePrecondition {

	public static final String PROPERTY_CHANGE_NAME = "propertyChange";
	private Collection<PropertyMapping> mappingConfiguration;

	@Override
	@SuppressWarnings("unchecked")
	public boolean configure(Context<String, Object> configuration) {
		if (!super.configure(configuration)) {
			return false;
		}
		Collection<Map<String, Object>> mappingConfig = configuration.getIfSameType(PropertyMapping.NAME,
				Collection.class);
		mappingConfiguration = PropertyMapping.parse(mappingConfig);
		return CollectionUtils.isNotEmpty(mappingConfiguration);
	}

	@Override
	public String getPrimaryOperation() {
		return PROPERTY_CHANGE_NAME;
	}

	@Override
	public String getName() {
		return PROPERTY_CHANGE_NAME;
	}

	@Override
	public boolean checkPreconditions(RuleContext processingContext) {
		Instance instance = processingContext.getTriggerInstance();
		Instance previousVersion = processingContext.getPreviousInstanceVersion();

		for (PropertyMapping propertyMapping : mappingConfiguration) {
			if (isPropertyChanged(instance, previousVersion, propertyMapping.getFrom(),
					propertyMapping.getValueChange())) {
				return true;
			}
		}
		return false;
	}

	private static boolean isPropertyChanged(Instance instance, Instance previousVersion, String property,
			PropertyValueChange valueChange) {

		Serializable newValue = instance.getProperties().get(property);
		Serializable oldValue = previousVersion == null ? null : previousVersion.getProperties().get(property);

		switch (valueChange) {
			case ADDED:
				return newValue != null && oldValue == null;
			case REMOVED:
				return newValue == null && oldValue != null;
			case CHANGED:
				return !EqualsHelper.nullSafeEquals(newValue, oldValue);
			default:
				break;
		}
		// implement me!
		return false;
	}

}
