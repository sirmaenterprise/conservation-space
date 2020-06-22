package com.sirma.sep.keycloak;

import static com.sirma.sep.keycloak.ExtendedLDAPConstants.DEFAULT_BASE_DN;
import static com.sirma.sep.keycloak.ExtendedLDAPConstants.DEFAULT_GROUPS_DN;
import static com.sirma.sep.keycloak.ExtendedLDAPConstants.DEFAULT_ORGANIZATION_ATTRIBUTE;
import static com.sirma.sep.keycloak.ExtendedLDAPConstants.DEFAULT_ORGANIZATION_CLASS;
import static com.sirma.sep.keycloak.ExtendedLDAPConstants.DEFAULT_USERS_DN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.idm.store.ldap.LDAPIdentityStore;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link ExtendedLDAPStorageProviderFactory}.
 *
 * @author smustafov
 */
public class ExtendedLDAPStorageProviderFactoryTest {

	private static final String REALM_NAME = "sep.test";

	private ExtendedLDAPStorageProviderFactory factory;

	@Mock
	private KeycloakSession session;
	@Mock
	private RealmModel realm;
	@Mock
	private ComponentModel model;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		factory = new ExtendedLDAPStorageProviderFactory();

		when(realm.getName()).thenReturn(REALM_NAME);
	}

	@Test(expected = ReadOnlyException.class)
	public void onCreate_Should_ThrowException_When_LdapStorageIsNotWritableAndNoOrganization() {
		LDAPIdentityStore identityStore = mockIdentityStore(UserStorageProvider.EditMode.READ_ONLY,
				new MultivaluedHashMap<>());

		factory.onCreate(session, realm, model);

		verify(identityStore, never()).add(any(LDAPObject.class));
	}

	@Test
	public void onCreate_Should_DoNothing_When_LdapOrganizationAlreadyExists() {
		LDAPIdentityStore identityStore = mockIdentityStore(UserStorageProvider.EditMode.WRITABLE,
				new MultivaluedHashMap<>());
		when(identityStore.countQueryResults(any(LDAPQuery.class))).thenReturn(1);

		factory.onCreate(session, realm, model);

		verify(identityStore, never()).add(any(LDAPObject.class));
	}

	@Test
	public void onCreate_Should_CreateOrganizationInLDAP() {
		LDAPIdentityStore identityStore = mockIdentityStore(UserStorageProvider.EditMode.WRITABLE,
				new MultivaluedHashMap<>());

		factory.onCreate(session, realm, model);

		verifyOrganization(identityStore);
	}

	@Test
	public void onCreate_Should_ReadLdapConfigurations() {
		MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
		config.putSingle(ExtendedLDAPConstants.BASE_DN, "dc=example,dc=com");
		config.putSingle(ExtendedLDAPConstants.ORGANIZATION_CLASS_KEY, "organization");
		config.putSingle(ExtendedLDAPConstants.ORGANIZATION_ATTRIBUTE_KEY, "o");
		config.putSingle(ExtendedLDAPConstants.USERS_DN_KEY, "people");
		config.putSingle(ExtendedLDAPConstants.GROUPS_DN_KEY, "roles");

		LDAPIdentityStore identityStore = mockIdentityStore(UserStorageProvider.EditMode.WRITABLE, config);

		factory.onCreate(session, realm, model);

		String expectedRealmDn = "o=" + REALM_NAME + ",dc=example,dc=com";
		String expectedUsersDn = "o=people,o=" + REALM_NAME + ",dc=example,dc=com";
		String expectedGroupsDn = "o=roles,o=" + REALM_NAME + ",dc=example,dc=com";

		verifyOrganization(identityStore, "o", "organization", expectedRealmDn, expectedUsersDn, expectedGroupsDn);
	}

	@Test
	public void preRemove_Should_DoNothing_When_LdapStoreIsNotWritable() {
		LDAPIdentityStore identityStore = mockIdentityStore(UserStorageProvider.EditMode.READ_ONLY,
				new MultivaluedHashMap<>());

		factory.preRemove(session, realm, model);

		verify(identityStore, never()).add(any(LDAPObject.class));
	}

	@Test
	public void preRemove_Should_DoNothing_When_LdapOrganizationAlreadyRemoved() {
		LDAPIdentityStore identityStore = mockIdentityStore(UserStorageProvider.EditMode.WRITABLE,
				new MultivaluedHashMap<>());

		factory.preRemove(session, realm, model);

		verify(identityStore, never()).add(any(LDAPObject.class));
	}

	@Test
	public void preRemove_Should_RemoveOrganization() {
		LDAPIdentityStore identityStore = mockIdentityStore(UserStorageProvider.EditMode.WRITABLE,
				new MultivaluedHashMap<>());
		when(identityStore.countQueryResults(any(LDAPQuery.class))).thenReturn(1);

		factory.preRemove(session, realm, model);

		ArgumentCaptor<LDAPObject> argumentCaptor = ArgumentCaptor.forClass(LDAPObject.class);
		verify(identityStore).remove(argumentCaptor.capture());
		verifySubOrganization(argumentCaptor.getValue(),
				DEFAULT_ORGANIZATION_ATTRIBUTE + "=" + REALM_NAME + "," + DEFAULT_BASE_DN);
	}

	private LDAPIdentityStore mockIdentityStore(UserStorageProvider.EditMode editMode,
			MultivaluedHashMap<String, String> config) {
		when(model.getConfig()).thenReturn(config);

		LDAPStorageProvider ldapStorageProvider = mock(LDAPStorageProvider.class);
		when(ldapStorageProvider.getEditMode()).thenReturn(editMode);

		LDAPIdentityStore identityStore = mock(LDAPIdentityStore.class);
		when(ldapStorageProvider.getLdapIdentityStore()).thenReturn(identityStore);

		when(session.getProvider(UserStorageProvider.class, model)).thenReturn(ldapStorageProvider);

		return identityStore;
	}

	private void verifyOrganization(LDAPIdentityStore identityStore) {
		String expectedRealmDn = DEFAULT_ORGANIZATION_ATTRIBUTE + "=" + REALM_NAME + "," + DEFAULT_BASE_DN;
		String expectedUsersDn =
				DEFAULT_ORGANIZATION_ATTRIBUTE + "=" + DEFAULT_USERS_DN + "," + DEFAULT_ORGANIZATION_ATTRIBUTE + "="
						+ REALM_NAME + "," + DEFAULT_BASE_DN;
		String expectedGroupsDn =
				DEFAULT_ORGANIZATION_ATTRIBUTE + "=" + DEFAULT_GROUPS_DN + "," + DEFAULT_ORGANIZATION_ATTRIBUTE + "="
						+ REALM_NAME + "," + DEFAULT_BASE_DN;

		verifyOrganization(identityStore, DEFAULT_ORGANIZATION_ATTRIBUTE, DEFAULT_ORGANIZATION_CLASS, expectedRealmDn,
				expectedUsersDn, expectedGroupsDn);
	}

	private void verifyOrganization(LDAPIdentityStore identityStore, String expectedOrgAttribute,
			String expectedOrgClass, String expectedRealmDn, String expectedUsersDn, String expectedGroupsDn) {
		ArgumentCaptor<LDAPObject> argumentCaptor = ArgumentCaptor.forClass(LDAPObject.class);
		verify(identityStore, times(3)).add(argumentCaptor.capture());

		List<LDAPObject> ldapObjects = argumentCaptor.getAllValues();

		verifySubOrganization(ldapObjects.get(0), expectedOrgAttribute, expectedOrgClass, expectedRealmDn);
		verifySubOrganization(ldapObjects.get(1), expectedOrgAttribute, expectedOrgClass, expectedUsersDn);
		verifySubOrganization(ldapObjects.get(2), expectedOrgAttribute, expectedOrgClass, expectedGroupsDn);
	}

	private void verifySubOrganization(LDAPObject subOrganization, String expectedDn) {
		verifySubOrganization(subOrganization, DEFAULT_ORGANIZATION_ATTRIBUTE, DEFAULT_ORGANIZATION_CLASS, expectedDn);
	}

	private void verifySubOrganization(LDAPObject subOrganization, String expectedOrgAttribute, String expectedOrgClass,
			String expectedDn) {
		assertEquals(expectedOrgAttribute, subOrganization.getRdnAttributeName());
		assertEquals(expectedDn, subOrganization.getDn().toString());
		assertEquals(Collections.singletonList(expectedOrgClass), subOrganization.getObjectClasses());
	}

}
