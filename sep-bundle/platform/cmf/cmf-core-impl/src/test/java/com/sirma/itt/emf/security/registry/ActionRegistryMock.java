package com.sirma.itt.emf.security.registry;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfAction;

/**
 * Registry for user roles. To register new role/s a provider interface must be implemented
 * {@link com.sirma.itt.emf.security.model.RoleProvider}.
 * <p>
 *
 */
@ApplicationScoped
public class ActionRegistryMock extends ActionRegistryImpl {
	Map<Pair<Class<?>, String>, Action> actions = new HashMap<Pair<Class<?>, String>, Action>();

	@Override
	public Action find(Pair<Class<?>, String> roleId) {
		if (!actions.containsKey(roleId)) {
			EmfAction emfAction = new EmfAction(roleId.getSecond());
			actions.put(roleId, emfAction);
		}

		return actions.get(roleId);
	}

	@Override
	protected String getCacheName() {
		// TODO Auto-generated method stub
		return null;
	}


}
