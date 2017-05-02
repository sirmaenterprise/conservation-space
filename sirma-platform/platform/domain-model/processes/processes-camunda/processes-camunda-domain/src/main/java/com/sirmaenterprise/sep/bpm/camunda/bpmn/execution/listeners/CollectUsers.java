package com.sirmaenterprise.sep.bpm.camunda.bpmn.execution.listeners;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirmaenterprise.sep.bpm.camunda.service.SecureProcessEngine;

/**
 * {@link CollectUsers} from several sources and returns the unique ids.
 * 
 * @author hlungov
 */
@Singleton
@Named("collectUsers")
public class CollectUsers {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private Instance<IdentityService> identityServiceInstance;

	/**
	 * Used in Camunda BPM Workflows in expressions to fetch users from defined properties.
	 * 
	 * @param execution
	 *            the BPM activity execution
	 * @param sourceProperties
	 *            the properties defined in execution containing users and groups separated by comma
	 * @return the set of unique users ids
	 */
	@SecureProcessEngine
	public Set<String> resolveUsers(DelegateExecution execution, String sourceProperties) {
		Objects.requireNonNull(sourceProperties,
				() -> "Property/ies to read multiple instances assigment authorities is a required parameter: "
						+ sourceProperties);
		return resolveUsersInternal(resolveAuthortiesIds(execution, sourceProperties));
	}

	private Set<String> resolveUsersInternal(Collection<String> authoritiesIds) {
		LOGGER.info("Resolving users from the following authorities: {}", authoritiesIds);
		IdentityService identityService = identityServiceInstance.get();
		try {
			String[] authoritesIdsArray = authoritiesIds.toArray(new String[authoritiesIds.size()]);
			List<User> users = new LinkedList<>(identityService.createUserQuery().userIdIn(authoritesIdsArray).list());
			List<Group> groups = identityService.createGroupQuery().groupIdIn(authoritesIdsArray).list();

			for (Group group : groups) {
				users.addAll(identityService.createUserQuery().memberOfGroup(group.getId()).list());
			}
			return users.stream().map(User::getId).collect(Collectors.toSet());
		} finally {
			identityServiceInstance.destroy(identityService);
		}
	}

	@SuppressWarnings("unchecked")
	private static Set<String> resolveAuthortiesIds(DelegateExecution execution, String source) {
		String[] sourceProperties = source.split(",");
		Set<String> authoritiesIds = new LinkedHashSet<>();
		for (String sourceProperty : sourceProperties) {

			Object variable = execution.getVariable(sourceProperty.trim());
			if (variable instanceof String) {
				authoritiesIds.add((String) variable);
			} else if (variable instanceof Collection) {
				authoritiesIds.addAll((Collection<String>) variable);
			}
		}
		return authoritiesIds;

	}

}
