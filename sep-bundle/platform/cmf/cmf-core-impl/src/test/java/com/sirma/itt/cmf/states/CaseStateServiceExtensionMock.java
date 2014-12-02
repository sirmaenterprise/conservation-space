package com.sirma.itt.cmf.states;

import java.io.Serializable;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.state.BaseStateServiceExtension;
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.StateServiceExtension;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * The Class CaseStateServiceExtensionMock.
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesCmf.CASE)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 10)
public class CaseStateServiceExtensionMock extends BaseStateServiceExtension<CaseInstance> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canHandle(Object target) {

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean changeState(CaseInstance instance, Operation operation) {

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPrimaryState(CaseInstance instance) {

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getState(PrimaryStateType stateType) {

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isState(PrimaryStateType stateType, String state) {
		boolean isState = true;
		return isState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInState(PrimaryStateType stateType, CaseInstance instance) {
		boolean isState = false;
		if (instance != null) {
			Serializable status = instance.getProperties().get(CaseProperties.STATUS);
			isState = status != null?status.equals(stateType):false;
		}

//		!CmfPrimaryStates.DELETED.equals(stateType);
		return isState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPrimaryStateCodelist() {

		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getSecondaryStateCodelist() {

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getStateTypeMapping() {

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getPrimaryStateProperty() {

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<CaseInstance> getInstanceClass() {

		return CaseInstance.class;
	}

}
