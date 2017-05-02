package com.sirma.cmf.mock.service;

import javax.enterprise.inject.Alternative;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.instance.state.StateService;

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
	public <I extends Instance> String getState(PrimaryStates stateType, Class<I> target) {

		return null;
	}

	@Override
	public <I extends Instance> boolean isState(PrimaryStates stateType, Class<I> target, String state) {

		return false;
	}

	@Override
	public <I extends Instance> boolean isInState(PrimaryStates stateType, I instance) {

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
	public <I extends Instance> boolean isStateAs(Class<I> target, String state, PrimaryStates... stateType) {

		return false;
	}

	@Override
	public <I extends Instance> boolean isInStates(I instance, PrimaryStates... stateTypes) {

		return false;
	}

	@Override
	public <I extends Instance> boolean isInActiveState(I instance) {
		return false;
	}

}
