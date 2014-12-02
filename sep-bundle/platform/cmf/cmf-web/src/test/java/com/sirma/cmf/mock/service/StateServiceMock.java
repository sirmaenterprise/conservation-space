package com.sirma.cmf.mock.service;

import javax.enterprise.inject.Alternative;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * The Class StateServiceMock.
 * 
 * @author svelikov
 */
@Alternative
public class StateServiceMock implements StateService {

	@Override
	public <I extends Instance> void changeState(I instance, Operation operation) {


	}

	@Override
	public <I extends Instance> String getPrimaryState(I instance) {

		return null;
	}

	@Override
	public <I extends Instance> String getState(PrimaryStateType stateType, Class<I> target) {

		return null;
	}

	@Override
	public <I extends Instance> boolean isState(PrimaryStateType stateType, Class<I> target,
			String state) {

		return false;
	}

	@Override
	public <I extends Instance> boolean isInState(PrimaryStateType stateType, I instance) {

		return false;
	}

	@Override
	public <I extends Instance> int getPrimaryStateCodelist(Class<I> target) {

		return 0;
	}

	@Override
	public <I extends Instance> Integer getSecondaryStateCodelist(Class<I> target) {

		return null;
	}

	@Override
	public <I extends Instance> boolean isStateAs(Class<I> target, String state,
			PrimaryStateType... stateType) {

		return false;
	}

	@Override
	public <I extends Instance> boolean isInStates(I instance, PrimaryStateType... stateTypes) {

		return false;
	}

}
