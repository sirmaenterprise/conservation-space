package com.sirma.sep.keycloak.flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.admin.client.resource.AuthenticationManagementResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationExecutionRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link KeycloakAuthFlowConfigurator}.
 *
 * @author smustafov
 */
public class KeycloakAuthFlowConfiguratorTest {

	@Mock
	private RealmResource realmResource;

	@Mock
	private AuthenticationManagementResource authManagement;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);

		when(realmResource.flows()).thenReturn(authManagement);

		mockFlowCreationResponse();
	}

	@Test
	public void should_ConfigureProperAuthFlow() {
		RealmRepresentation realmRepresentation = stubRealmRepresentation();

		when(authManagement.getExecutions(KeycloakAuthFlowConfigurator.SEP_AUTH_FLOW)).thenReturn(
				Arrays.asList(buildExecutionRepresentation(KeycloakAuthFlowConfigurator.SEP_AUTHENTICATOR),
						buildExecutionRepresentation(null)));

		KeycloakAuthFlowConfigurator.configure(realmResource);

		verifyCreatedFlow();

		verifyCookieExecutionCreated();

		verifySubFlowCreated();

		verifyRealmRepresentation(realmRepresentation);
	}

	private void verifyCreatedFlow() {
		ArgumentCaptor<AuthenticationFlowRepresentation> argumentCaptor = ArgumentCaptor
				.forClass(AuthenticationFlowRepresentation.class);
		verify(authManagement).createFlow(argumentCaptor.capture());

		AuthenticationFlowRepresentation createdFlow = argumentCaptor.getValue();
		assertEquals(KeycloakAuthFlowConfigurator.SEP_AUTH_FLOW, createdFlow.getAlias());
		assertEquals(KeycloakAuthFlowConfigurator.BASIC_FLOW, createdFlow.getProviderId());
		assertTrue(createdFlow.isTopLevel());
		assertFalse(createdFlow.isBuiltIn());
	}

	private void verifyCookieExecutionCreated() {
		ArgumentCaptor<AuthenticationExecutionRepresentation> argumentCaptor = ArgumentCaptor
				.forClass(AuthenticationExecutionRepresentation.class);
		verify(authManagement).addExecution(argumentCaptor.capture());

		AuthenticationExecutionRepresentation cookieAuth = argumentCaptor.getValue();
		assertNotNull(cookieAuth.getParentFlow());
		assertEquals(KeycloakAuthFlowConfigurator.COOKIE_AUTHENTICATOR, cookieAuth.getAuthenticator());
		assertEquals(0, cookieAuth.getPriority());
		assertEquals(ActionRequirement.ALTERNATIVE.name(), cookieAuth.getRequirement());
	}

	private void verifySubFlowCreated() {
		ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);
		verify(authManagement)
				.addExecutionFlow(eq(KeycloakAuthFlowConfigurator.SEP_AUTH_FLOW), argumentCaptor.capture());

		Map<String, String> subFlowProperties = argumentCaptor.getValue();
		Map<String, String> expectedSubFlowProperties = new HashMap<>();
		expectedSubFlowProperties.put("alias", KeycloakAuthFlowConfigurator.SEP_AUTH_SUB_FLOW);
		expectedSubFlowProperties.put("type", KeycloakAuthFlowConfigurator.BASIC_FLOW);
		assertEquals(expectedSubFlowProperties, subFlowProperties);

		verify(authManagement)
				.addExecution(eq(KeycloakAuthFlowConfigurator.SEP_AUTH_SUB_FLOW), argumentCaptor.capture());
		Map<String, String> executionProperties = argumentCaptor.getValue();
		Map<String, String> expectedExecutionProperties = new HashMap<>();
		expectedExecutionProperties.put("provider", KeycloakAuthFlowConfigurator.SEP_AUTHENTICATOR);
		assertEquals(expectedExecutionProperties, executionProperties);

		verifyExecutionRequirements();
	}

	private void verifyExecutionRequirements() {
		ArgumentCaptor<AuthenticationExecutionInfoRepresentation> argumentCaptor = ArgumentCaptor
				.forClass(AuthenticationExecutionInfoRepresentation.class);

		verify(authManagement, times(2))
				.updateExecutions(eq(KeycloakAuthFlowConfigurator.SEP_AUTH_FLOW), argumentCaptor.capture());

		List<AuthenticationExecutionInfoRepresentation> allValues = argumentCaptor.getAllValues();
		assertEquals(ActionRequirement.REQUIRED.name(), allValues.get(0).getRequirement());
		assertEquals(ActionRequirement.ALTERNATIVE.name(), allValues.get(1).getRequirement());
	}

	private void verifyRealmRepresentation(RealmRepresentation realmRepresentation) {
		assertEquals(KeycloakAuthFlowConfigurator.SEP_AUTH_FLOW, realmRepresentation.getBrowserFlow());
	}

	private RealmRepresentation stubRealmRepresentation() {
		RealmRepresentation realmRepresentation = new RealmRepresentation();
		when(realmResource.toRepresentation()).thenReturn(realmRepresentation);
		return realmRepresentation;
	}

	private void mockFlowCreationResponse() {
		when(authManagement.createFlow(any())).thenReturn(
				Response.created(URI.create("http://idp/auth/flows/" + KeycloakAuthFlowConfigurator.SEP_AUTH_FLOW))
						.build());
	}

	private static AuthenticationExecutionInfoRepresentation buildExecutionRepresentation(String providerId) {
		AuthenticationExecutionInfoRepresentation executionInfoRepresentation = new AuthenticationExecutionInfoRepresentation();
		executionInfoRepresentation.setProviderId(providerId);
		return executionInfoRepresentation;
	}

}
