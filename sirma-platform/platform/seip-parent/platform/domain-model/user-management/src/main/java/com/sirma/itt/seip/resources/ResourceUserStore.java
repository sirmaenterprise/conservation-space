package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;

import java.io.Serializable;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.util.SecurityUtil;

/**
 * User store implementation that fetches users based on the {@link UserStore}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ResourceUserStore implements UserStore {
	@Inject
	private ResourceService resourceService;

	@Inject
	private SecurityConfiguration securityConfiguration;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Override
	public User loadBySystemId(Serializable systemId) {
		return (User) resourceService.loadByDbId(systemId);
	}

	@Override
	public User loadByIdentityId(String identity) {
		StringPair userAndTenant = SecurityUtil.getUserAndTenant(identity);
		if (SecurityContext.isSystemTenant(userAndTenant.getSecond())) {
			return buildUser(identity, SecurityContext.SYSTEM_TENANT);
		}
		return resourceService.getResource(identity, ResourceType.USER);
	}

	@Override
	public User loadByIdentityId(String identity, String tenantId) {
		if (SecurityContext.isSystemTenant(tenantId)) {
			return buildUser(identity, tenantId);
		}
		Resource resource = resourceService.getResource(identity, ResourceType.USER);
		if (resource == null) {
			ConfigurationProperty<String> adminUserName = securityConfiguration.getAdminUserName();
			String adminNameConfig = adminUserName.get();
			adminNameConfig = adminNameConfig == null ? SecurityUtil.buildTenantUserId(identity, tenantId)
					: adminNameConfig;
			if (identity.equals(adminNameConfig)) {
				// this case is when initializing new tenant and admin user is
				// requested
				// calling the providers bellow will trigger cyclic loading of
				// users
				return buildUser(identity, tenantId);
			}
		}
		// TODO: remove explicit tenant identification
		if (resource instanceof com.sirma.itt.seip.resources.User) {
			((com.sirma.itt.seip.resources.User) resource).setTenantId(tenantId);
		}
		return (User) resource;
	}

	@Override
	public User setUserTicket(User user, String ticket) {
		if (user instanceof UserWithCredentials) {
			((UserWithCredentials) user).setTicket(ticket);
		}
		return user;
	}

	@Override
	public User wrap(User user) {
		if (user instanceof EmfUser) {
			return user;
		}
		return new EmfUser(user);
	}

	@Override
	public User unwrap(User user) {
		if (user instanceof EmfUser) {
			User target = ((EmfUser) user).getTarget();
			if (target != null) {
				return target;
			}
		}
		return user;
	}

	private User buildUser(String identity, String tenantId) {
		EmfUser systemUser = new EmfUser(identity);
		systemUser.setTenantId(tenantId);
		return wrap(systemUser);
	}

	@Override
	public void setRequestProperties(User user, RequestInfo info) {
		CollectionUtils.addIfAbsent(user.getProperties(), ResourceProperties.LANGUAGE, selectLanguage());

		List<String> list = info.getHeaders().get("Timezone");
		if (isNotEmpty(list)) {
			String timeZoneId = list.get(0);
			TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
			CollectionUtils.addIfAbsent(user.getProperties(), ResourceProperties.TIMEZONE, timeZone);
		}
	}

	private String selectLanguage() {
		return systemConfiguration.getSystemLanguage();
	}
}
