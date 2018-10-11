package com.sirma.itt.seip.permissions.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.permissions.role.RoleActionEvaluatorContext;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.ResourceService;

/**
 * Default filters provider. Other plugins could override if it is needed
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = RoleActionFilterProvider.TARGET_NAME, order = 0)
public class RoleActionDefaultFiltersProvider implements RoleActionFilterProvider {

	@Inject
	private ResourceService resourceService;

	@Override
	public Map<String, Predicate<RoleActionEvaluatorContext>> provideFilters() {
		Map<String, Predicate<RoleActionEvaluatorContext>> result = new HashMap<>(2);
		result.put("CREATEDBY",
				e -> isSameAuthority(e.getAuthority(), e.getInstanceProperty(DefaultProperties.CREATED_BY)));
		result.put("LOCKEDBY",
				e -> isSameAuthority(e.getAuthority(), e.getInstanceProperty(DefaultProperties.LOCKED_BY)));
		return result;
	}

	/**
	 * Checks if is same authority.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 * @return true, if is same authority
	 */
	protected boolean isSameAuthority(Serializable first, Serializable second) {
		return resourceService.areEqual(first, second);
	}

}
