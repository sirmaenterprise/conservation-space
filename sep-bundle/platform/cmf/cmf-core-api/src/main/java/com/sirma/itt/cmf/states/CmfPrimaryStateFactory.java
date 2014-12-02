package com.sirma.itt.cmf.states;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;

import com.sirma.itt.emf.state.PrimaryStateFactory;
import com.sirma.itt.emf.state.PrimaryStateType;

/**
 * A factory for creating PrimaryState objects.
 * 
 * @author BBonev
 */
@Specializes
@ApplicationScoped
public class CmfPrimaryStateFactory extends PrimaryStateFactory {

	@Override
	public PrimaryStateType create(String key) {
		if (key == null) {
			return PrimaryStates.INITIAL;
		}
		String lowerCase = key.toLowerCase();
		PrimaryStateType type = getFromCache(lowerCase);
		if (type == null) {
			// use enum for the known types
			PrimaryStateType byType = PrimaryStates.getStateTypeById(lowerCase);
			if (byType != null) {
				addToCache(lowerCase, byType);
				return byType;
			}
			return super.create(key);
		}
		return type;
	}

}
