package com.sirma.sep.keycloak.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.keycloak.admin.client.resource.AuthenticationManagementResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationExecutionRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

import com.sirma.sep.keycloak.util.KeycloakApiUtil;

/**
 * Utility class for configuring authentication flows.
 *
 * @author smustafov
 */
public class KeycloakAuthFlowConfigurator {

	public static final String SEP_AUTH_FLOW = "sep-auth-flow";
	public static final String SEP_AUTH_SUB_FLOW = "sep-forms";
	public static final String BASIC_FLOW = "basic-flow";

	public static final String COOKIE_AUTHENTICATOR = "auth-cookie";
	public static final String SEP_AUTHENTICATOR = "sep-authenticator";

	/**
	 * Configures custom authentication flow for tenant(realm). The flow includes the following authenticators:
	 * <ol>
	 * <li>Cookie - this authenticator checks if the user have existing session cookie, which means if the
	 * user have active session, he will be logged automatically</li>
	 * <li>SEP - this is a custom authenticator, which extends the default username and password form authenticator
	 * by including a text field for tenant login page switching</li>
	 * </ol>
	 *
	 * @param realmResource the realm's resource
	 */
	public static void configure(RealmResource realmResource) {
		AuthenticationManagementResource authManagement = realmResource.flows();

		String flowId = createFlow(authManagement);

		createCookieExecution(flowId, authManagement);

		createFormsSubFlow(authManagement);

		RealmRepresentation realmRepresentation = realmResource.toRepresentation();
		realmRepresentation.setBrowserFlow(SEP_AUTH_FLOW);
		realmResource.update(realmRepresentation);
	}

	private static String createFlow(AuthenticationManagementResource authManagement) {
		AuthenticationFlowRepresentation flowRepresentation = buildFlow();
		Response response = authManagement.createFlow(flowRepresentation);
		return KeycloakApiUtil.getCreatedId(response);
	}

	private static AuthenticationFlowRepresentation buildFlow() {
		AuthenticationFlowRepresentation flowRepresentation = new AuthenticationFlowRepresentation();
		flowRepresentation.setAlias(SEP_AUTH_FLOW);
		flowRepresentation.setProviderId(BASIC_FLOW);
		flowRepresentation.setBuiltIn(false);
		flowRepresentation.setTopLevel(true);
		return flowRepresentation;
	}

	private static void createCookieExecution(String flowId, AuthenticationManagementResource authManagement) {
		AuthenticationExecutionRepresentation cookieExecution = buildCookieExecution(flowId);
		authManagement.addExecution(cookieExecution);
	}

	private static AuthenticationExecutionRepresentation buildCookieExecution(String flowId) {
		AuthenticationExecutionRepresentation cookie = new AuthenticationExecutionRepresentation();
		cookie.setParentFlow(flowId);
		cookie.setAuthenticator(COOKIE_AUTHENTICATOR);
		cookie.setPriority(0);
		cookie.setRequirement(ActionRequirement.ALTERNATIVE.name());
		return cookie;
	}

	private static void createFormsSubFlow(AuthenticationManagementResource authManagement) {
		Map<String, String> subFlow = buildSubFlow();
		authManagement.addExecutionFlow(SEP_AUTH_FLOW, subFlow);

		Map<String, String> sepAuthenticator = buildSepAuthenticator();
		authManagement.addExecution(SEP_AUTH_SUB_FLOW, sepAuthenticator);

		List<AuthenticationExecutionInfoRepresentation> executions = authManagement.getExecutions(SEP_AUTH_FLOW);
		for (AuthenticationExecutionInfoRepresentation execution : executions) {
			if (SEP_AUTHENTICATOR.equals(execution.getProviderId())) {
				execution.setRequirement(ActionRequirement.REQUIRED.name());
			} else if (execution.getProviderId() == null) {
				execution.setRequirement(ActionRequirement.ALTERNATIVE.name());
			}
			authManagement.updateExecutions(SEP_AUTH_FLOW, execution);
		}
	}

	private static Map<String, String> buildSubFlow() {
		Map<String, String> properties = new HashMap<>();
		properties.put("alias", SEP_AUTH_SUB_FLOW);
		properties.put("type", BASIC_FLOW);
		return properties;
	}

	private static Map<String, String> buildSepAuthenticator() {
		Map<String, String> properties = new HashMap<>();
		properties.put("provider", SEP_AUTHENTICATOR);
		return properties;
	}

	private KeycloakAuthFlowConfigurator() {
		// utility class
	}

}
