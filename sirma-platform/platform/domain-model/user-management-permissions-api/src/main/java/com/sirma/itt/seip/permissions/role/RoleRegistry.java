package com.sirma.itt.seip.permissions.role;

import com.sirma.itt.seip.provider.ProviderRegistry;

/**
 * Register class for the security {@link Role}s.The implementation should provide access to a single instance of a role
 * identified by {@link RoleIdentifier}.
 *
 * @author BBonev
 */
public interface RoleRegistry extends ProviderRegistry<RoleIdentifier, Role> {

}
