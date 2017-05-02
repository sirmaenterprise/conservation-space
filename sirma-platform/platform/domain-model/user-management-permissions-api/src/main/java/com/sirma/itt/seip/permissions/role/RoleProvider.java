package com.sirma.itt.seip.permissions.role;

import com.sirma.itt.seip.provider.MapProvider;

/**
 * Extension point for adding more {@link Role}s to the application
 *
 * @author BBonev
 */
public interface RoleProvider extends MapProvider<RoleIdentifier, Role> {

}
