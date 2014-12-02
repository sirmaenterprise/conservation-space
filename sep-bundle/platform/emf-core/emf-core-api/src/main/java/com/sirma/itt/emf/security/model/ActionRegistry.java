package com.sirma.itt.emf.security.model;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.provider.ProviderRegistry;

/**
 * Register class for the security {@link Action}s.The implementation should provide access to a
 * single instance of a action identified by a pair of target class and action id.
 * 
 * @author BBonev
 */
public interface ActionRegistry extends ProviderRegistry<Pair<Class<?>, String>, Action> {

}
