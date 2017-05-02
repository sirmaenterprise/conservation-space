package com.sirma.itt.cmf.state;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.instance.state.StateServiceExtension;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.template.TemplateInstance;

/**
 * Empty implementation of state service extension for template instance.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.TEMPLATE)
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
	public String getState(PrimaryStates stateType) {
		return null;
	}

	@Override
	public boolean isState(PrimaryStates stateType, String state) {
		return false;
	}

	@Override
	public boolean isInState(PrimaryStates stateType, TemplateInstance instance) {
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

	@Override
	public boolean isInActiveState(TemplateInstance instance) {
		return true;
	}

}
