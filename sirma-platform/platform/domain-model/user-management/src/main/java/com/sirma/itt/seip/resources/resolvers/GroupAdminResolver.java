package com.sirma.itt.seip.resources.resolvers;

import java.util.List;

import javax.inject.Inject;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.AdminResolver;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Resolves authenticated user if contained in the admin group, defined by {@link SecurityConfiguration#getAdminGroup()}
 *
 * @author smustafov
 */
@Extension(target = AdminResolver.NAME, order = 10)
public class GroupAdminResolver implements AdminResolver {

	@Inject
	private SecurityConfiguration securityConfiguration;

	@Inject
	private ResourceService resourceService;

	@Override
	public boolean isAdmin(SecurityContext securityContext) {
		if (securityContext.isAuthenticated() && securityConfiguration.getAdminGroup().isSet()) {
			String adminGroupName = securityConfiguration.getAdminGroup().get();
			Resource adminGroup = resourceService.getResource(adminGroupName, ResourceType.GROUP);
			if (adminGroup != null) {
				List<String> containedResourceIdentifiers = resourceService.getContainedResourceIdentifiers(adminGroup,
						ResourceType.USER);
				return containedResourceIdentifiers.contains(securityContext.getAuthenticated().getIdentityId());
			}
		}
		return false;
	}

}
