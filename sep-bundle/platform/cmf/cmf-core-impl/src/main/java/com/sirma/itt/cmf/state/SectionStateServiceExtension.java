package com.sirma.itt.cmf.state;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.SectionProperties;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.state.StateServiceExtension;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Extension for state service for {@link SectionInstance} that shares the states of a case.
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesCmf.SECTION)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 12)
public class SectionStateServiceExtension extends
		BaseCaseTreeStateServiceExtension<SectionInstance> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean changeState(SectionInstance instance, Operation operation) {
		// we does not change the state of the case from here
		return false;
	}

	@Override
	public String getPrimaryState(SectionInstance instance) {
		if (instance == null) {
			return null;
		}
		// for standalone folders
		if (instance.isStandalone()
				&& SectionProperties.PURPOSE_FOLDER.equals(instance.getPurpose())) {
			return super.getPrimaryState(instance);
		}
		// check the state in the case if any
		if ((instance.getOwningInstance() != null)
				&& (instance.getOwningInstance().getProperties() != null)) {
			return (String) instance.getOwningInstance().getProperties()
					.get(getPrimaryStateProperty());
		}
		return super.getPrimaryState(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<SectionInstance> getInstanceClass() {
		return SectionInstance.class;
	}

}
