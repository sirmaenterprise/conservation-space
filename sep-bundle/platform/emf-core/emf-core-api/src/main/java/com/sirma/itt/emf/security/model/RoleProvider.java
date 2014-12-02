package com.sirma.itt.emf.security.model;

import com.sirma.itt.emf.provider.MapProvider;

/**
 * Extension point for adding more {@link Role}s to the application
 *
 * @author BBonev
 */
public interface RoleProvider extends MapProvider<RoleIdentifier, Role> {

}
