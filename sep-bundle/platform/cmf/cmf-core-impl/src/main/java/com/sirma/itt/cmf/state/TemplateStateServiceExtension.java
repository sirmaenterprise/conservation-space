package com.sirma.itt.cmf.state;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.StateServiceExtension;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.template.TemplateInstance;

/**
 * Empty implementation of state service extension for template instance.
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesCmf.TEMPLATE)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 9)
public class TemplateStateServiceExtension implements StateServiceExtension<TemplateInstance> {

	@Override
	public boolean canHandle(Object target) {
		return target instanceof TemplateInstance;
	}

	@Override
	public boolean changeState(TemplateInstance instance, Operation operation) {
		return false;
	}

	@Override
	public String getPrimaryState(TemplateInstance instance) {
		return null;
	}

	@Override
	public String getState(PrimaryStateType stateType) {
		return null;
	}

	@Override
	public boolean isState(PrimaryStateType stateType, String state) {
		return false;
	}

	@Override
	public boolean isInState(PrimaryStateType stateType, TemplateInstance instance) {
		return false;
	}

	@Override
	public int getPrimaryStateCodelist() {
		return 0;
	}

	@Override
	public Integer getSecondaryStateCodelist() {
		return null;
	}

}
