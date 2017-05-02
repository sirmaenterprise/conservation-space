package com.sirmaenterprise.sep.bpm.camunda.security;

import static org.camunda.bpm.engine.authorization.Permissions.ACCESS;
import static org.camunda.bpm.engine.authorization.Resources.APPLICATION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.webapp.impl.security.auth.Authentication;
import org.camunda.bpm.webapp.impl.security.auth.UserAuthentication;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirmaenterprise.sep.bpm.camunda.configuration.CamundaConfiguration;
import com.sirmaenterprise.sep.bpm.camunda.util.ProcessEngineUtil;

/**
 * Authenticator for Camunda web integration based on the current credentials in SEP
 * 
 * @author bbanchev
 */
@Singleton
public class SSOAuthenticator {
	/** Simple engine name describing resources not related to any engine */
	public static final String ANY_ENGINE = "processEngine$any$" + UUID.randomUUID();
	private static final String[] APPS = new String[] { "cockpit", "tasklist", "admin" };
	@Inject
	private SecurityContext securityContext;
	@Inject
	private CamundaConfiguration camundaConfiguration;

	/**
	 * Creates a {@link Authentication} based on the current context.
	 * 
	 * @param requestedEngineName
	 *            is the requested engine to generate {@link Authentication} for. might be null
	 * @return the generated {@link Authentication} or null on invalid context or wrong tenant
	 */
	public Authentication doLogin(String requestedEngineName) {
		if (!securityContext.isActive()) {
			return null;
		}
		String engineName;
		List<Group> groupList;
		ProcessEngine processEngine = null;

		boolean unspecifiedProcessEngine = StringUtils.isBlank(requestedEngineName)
				|| ANY_ENGINE.equals(requestedEngineName);

		String usernameLocal = securityContext.getAuthenticated().getSystemId().toString();
		if (!unspecifiedProcessEngine) {
			engineName = camundaConfiguration.getEngineName().get();
			if (!requestedEngineName.equals(engineName)) {
				// request diff tenant - don't login
				return null;
			}
			processEngine = ProcessEngineUtil.lookupProcessEngine(engineName);
			if (processEngine == null) {
				throw new InvalidRequestException(Status.BAD_REQUEST,
						"Process engine with name " + engineName + " does not exist");
			}
			// make sure authentication is executed without authentication :)
			processEngine.getIdentityService().clearAuthentication();
			// get user's groups
			groupList = processEngine.getIdentityService().createGroupQuery().groupMember(usernameLocal).list();
		} else {
			groupList = Collections.emptyList();
			engineName = ANY_ENGINE;
		}

		UserAuthentication newAuthentication = buildAuthentication(engineName, groupList, processEngine, usernameLocal);
		return newAuthentication;

	}

	private static UserAuthentication buildAuthentication(String engineName, List<Group> groupList,
			ProcessEngine processEngine, String usernameLocal) {
		// transform into array of strings:
		List<String> groupIds = new ArrayList<>(groupList.size());

		for (Group group : groupList) {
			groupIds.add(group.getId());
		}

		HashSet<String> authorizedApps = new HashSet<>(APPS.length);

		if (processEngine != null && processEngine.getProcessEngineConfiguration().isAuthorizationEnabled()) {
			for (String application : APPS) {
				boolean userAuthorized = processEngine.getAuthorizationService().isUserAuthorized(usernameLocal,
						groupIds, ACCESS, APPLICATION, application);
				if (userAuthorized) {
					authorizedApps.add(application);
				}
			}

		} else {
			Collections.addAll(authorizedApps, APPS);
		}

		// create new authentication
		UserAuthentication newAuthentication = new UserAuthentication(usernameLocal, engineName);
		newAuthentication.setAuthorizedApps(authorizedApps);
		newAuthentication.setGroupIds(groupIds);
		return newAuthentication;
	}
}
