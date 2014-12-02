package com.sirma.itt.cmf.services.impl.dao;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.entity.CaseEntity;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.BasePropertyModelCallback;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.properties.dao.PropertyModelCallback;
import com.sirma.itt.emf.properties.model.PropertyModelKey;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Property model call back for case instances.
 *
 * @author BBonev
 */
@InstanceType(type = ObjectTypesCmf.CASE)
@ApplicationScoped
public class CaseInstancePropertyModelCallback extends BasePropertyModelCallback<CaseInstance>
		implements PropertyModelCallback<CaseInstance> {

	private static final Set<Class<?>> SUPPORTED_OBJECTS;
	static {
		SUPPORTED_OBJECTS = CollectionUtils.createHashSet(5);
		SUPPORTED_OBJECTS.add(CaseInstance.class);
		SUPPORTED_OBJECTS.add(CaseEntity.class);
	}

	/** The properties service. */
	@Inject
	private javax.enterprise.inject.Instance<PropertiesService> propertiesService;

	@Inject
	private Logger logger;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<PropertyModelKey, Object> getModel(CaseInstance model) {
		Map<PropertyModelKey, Object> map = new LinkedHashMap<PropertyModelKey, Object>();

		createSubModel(model, map);
		createModel(model, map);
		PropertiesService service = propertiesService.get();
		for (SectionInstance section : model.getSections()) {
			createModel(section, map);
			for (Instance instance : section.getContent()) {
				// if the identifier is not a long then we cannot save it
				if (service.isModelSupported(instance)) {
					createSubModel(instance, map);
					createModel(instance, map);
				}
			}
		}
		return map;
	}

	@Override
	public boolean canHandle(Object model) {
		return (model instanceof CaseInstance) || (model instanceof CaseEntity);
	}

	@Override
	public Set<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}
}
