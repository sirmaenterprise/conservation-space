package com.sirma.itt.seip.idp.wso2.authority;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import com.sirma.itt.seip.idp.exception.IDPClientException;
import com.sirma.itt.seip.resources.security.PasswordChangeFailException;
import com.sirma.itt.seip.resources.security.PasswordChangeFailException.PasswordFailType;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;

/**
 * Tests for {@link IDPUserCredentialServiceImpl}.
 *
 * @author smustafov
 */
public class IDPUserCredentialServiceImplTest {

	@InjectMocks
	private IDPUserCredentialServiceImpl credentialService;

	@Mock
	private Instance<UserStoreManager> userStoreManagerInstance;

	@Mock
	private UserStoreManager userStoreManager;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(userStoreManagerInstance.get()).thenReturn(userStoreManager);
	}

	@Test
	public void testChangeUserPassword() throws IDPClientException, UserStoreException {
		String oldPass = "123456";
		String newPass = "654321";
		boolean result = credentialService.changeUserPassword("john@domain.com", oldPass, newPass);

		assertTrue(result);
		verify(userStoreManager).updateCredential("john", newPass, oldPass);
	}

	@Test
	public void testChangeUserPassword_wrongOldPassword() throws IDPClientException, UserStoreException {
		String oldPass = "123456";
		String newPass = "654321";
		boolean result = false;

		doThrow(new UserStoreException()).when(userStoreManager).updateCredential(anyObject(), anyObject(),
				anyObject());
		try {
			result = credentialService.changeUserPassword("john@domain.com", oldPass, newPass);
		} catch (PasswordChangeFailException e) {
			assertEquals(PasswordFailType.WRONG_OLD_PASSWORD, e.getType());
			assertFalse(result);
		}
	}

	@Test
	public void testValidatePassword_emptyNewPassword() {
		try {
			credentialService.validatePassword("123", "");
			fail("PasswordChangeFailException expected");
		} catch (PasswordChangeFailException e) {
			assertEquals(PasswordChangeFailException.PasswordFailType.NEW_PASSWORD_EMPTY, e.getType());
		}
	}

	@Test
	public void testValidatePassword_emptyOldPassword() {
		try {
			credentialService.validatePassword("", "123");
			fail("PasswordChangeFailException expected");
		} catch (PasswordChangeFailException e) {
			assertEquals(PasswordChangeFailException.PasswordFailType.OLD_PASSWORD_EMPTY, e.getType());
		}
	}

	@Test
	public void testValidatePassword_samePassword() {
		try {
			credentialService.validatePassword("123456", "123456");
			fail("PasswordChangeFailException expected");
		} catch (PasswordChangeFailException e) {
			assertEquals(PasswordChangeFailException.PasswordFailType.SAME_PASSWORD, e.getType());
		}
	}

	@Test
	public void testValidatePassword_shortPassword() {
		try {
			credentialService.validatePassword("123456", "123");
			fail("PasswordChangeFailException expected");
		} catch (PasswordChangeFailException e) {
			assertEquals(PasswordChangeFailException.PasswordFailType.SHORT_PASSWORD, e.getType());
		}
	}

	@Test
	public void testValidatePassword_longPassword() {
		try {
			credentialService.validatePassword("123456", "1234567890myLongPassword^QWERTYUIOPASDFGHJKLZXCVBNM");
			fail("PasswordChangeFailException expected");
		} catch (PasswordChangeFailException e) {
			assertEquals(PasswordChangeFailException.PasswordFailType.LONG_PASSWORD, e.getType());
		}
	}

	@Test
	public void should_HaveName() {
		assertEquals(SecurityConfiguration.WSO_IDP, credentialService.getName());
	}

}
