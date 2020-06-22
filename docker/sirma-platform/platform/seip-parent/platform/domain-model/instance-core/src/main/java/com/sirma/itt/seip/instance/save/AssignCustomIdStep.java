package com.sirma.itt.seip.instance.save;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Sets custom IRI for the instance if it exists in its properties
 * 
 * @author kirq4e
 */
@Extension(target = InstanceSaveStep.NAME, enabled = true, order = 0.5)
public class AssignCustomIdStep implements InstanceSaveStep {

	@Inject
	private DatabaseIdManager idManager;

	@Override
	public void beforeSave(InstanceSaveContext saveContext) {
		Instance instance = saveContext.getInstance();
		Serializable customIri = instance.remove("customIRI");
		if (!idManager.isPersisted(instance) && customIri != null) {
			idManager.unregister(instance);
			instance.setId(customIri);
			idManager.register(instance);
		}
	}

	@Override
	public String getName() {
		return "setCustomIdStep";
	}

}
