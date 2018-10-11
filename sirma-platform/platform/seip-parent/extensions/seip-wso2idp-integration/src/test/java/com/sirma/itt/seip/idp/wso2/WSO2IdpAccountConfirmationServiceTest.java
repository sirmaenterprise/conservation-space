package com.sirma.itt.seip.idp.wso2;

import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryService;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;

import com.sirma.itt.seip.idp.config.IDPConfiguration;
import com.sirma.itt.seip.resources.security.AccountConfirmationFailedException;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * Test for {@link WSO2IdpAccountConfirmationService}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 29/08/2017
 */
public class WSO2IdpAccountConfirmationServiceTest {

	@InjectMocks
	private WSO2IdpAccountConfirmationService service;

	@Mock
	private UserInformationRecoveryService recoveryService;
	@Spy
	private InstanceProxyMock<UserInformationRecoveryService> informationRecoveryService = new InstanceProxyMock<>();
	@Mock
	private IDPConfiguration idpConfig;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		informationRecoveryService.set(recoveryService);
		when(idpConfig.getIdpServerURL()).thenReturn(new ConfigurationPropertyMock<>("https://localhost:9443/samlsso"));
	}

	@Test
	public void retrieveCaptchaLink() throws Exception {
		when(recoveryService.getCaptcha()).thenReturn(getCaptchaInfoBean());

		String captchaLink = service.retrieveCaptchaLink("aCode");
		assertEquals("https://localhost:9443/images/captcha1.jpg", captchaLink);
	}

	@Test
	public void retrieveCaptchaLink_ShouldPrependForwardSlash_When_ImagePathHasNone() throws Exception {
		when(recoveryService.getCaptcha()).thenReturn(getCaptchaInfoBean("images/captcha1.jpg"));

		String captchaLink = service.retrieveCaptchaLink("aCode");
		assertEquals("https://localhost:9443/images/captcha1.jpg", captchaLink);
	}

	private CaptchaInfoBean getCaptchaInfoBean(String imagePath) {
		CaptchaInfoBean captchaInfoBean = getCaptchaInfoBean();
		captchaInfoBean.setImagePath(imagePath);
		return captchaInfoBean;
	}

	private CaptchaInfoBean getCaptchaInfoBean() {
		CaptchaInfoBean captchaBean = new CaptchaInfoBean();
		captchaBean.setImagePath("/images/captcha1.jpg");
		captchaBean.setSecretKey("secret");
		return captchaBean;
	}

	@Test(expected = AccountConfirmationFailedException.class)
	public void retrieveCaptchaLink_shouldFailIfCannotFetchCaptcha() throws Exception {
		when(recoveryService.getCaptcha()).thenThrow(RemoteException.class);
		try {
			service.retrieveCaptchaLink("aCode");
		} catch (AccountConfirmationFailedException e) {
			assertEquals(ConfirmationFailType.UNKNOWN, e.getFailType());
			throw e;
		}

	}

	@Test
	public void confirmAccount() throws Exception {
		when(recoveryService.getCaptcha()).thenReturn(getCaptchaInfoBean());

		// register the confirmation code
		service.retrieveCaptchaLink("aCode");

		VerificationBean bean = new VerificationBean();
		bean.setVerified(true);
		bean.setKey("verification");
		when(recoveryService.verifyConfirmationCode(eq("user@tenant.com"), eq("aCode"), any())).thenReturn(bean);

		service.confirmAccount("user", "pass", "aCode", "123456", "tenant.com");

		verify(recoveryService).updatePassword("user@tenant.com", "verification", "pass");
	}

	@Test(expected = AccountConfirmationFailedException.class)
	public void confirmAccount_shouldFailOnVerification() throws Exception {
		when(recoveryService.getCaptcha()).thenReturn(getCaptchaInfoBean());

		// register the confirmation code
		service.retrieveCaptchaLink("aCode");

		VerificationBean bean = new VerificationBean();
		bean.setVerified(false);
		bean.setError("17001");
		when(recoveryService.verifyConfirmationCode(eq("user@tenant.com"), eq("aCode"), any())).thenReturn(bean);

		try {
			service.confirmAccount("user", "pass", "aCode", "123456", "tenant.com");
		} catch (AccountConfirmationFailedException e) {
			assertEquals(ConfirmationFailType.USER_DOES_NOT_EXIST, e.getFailType());
			throw e;
		}
	}

	@Test(expected = AccountConfirmationFailedException.class)
	public void confirmAccount_shouldFailOnVerification_withError() throws Exception {
		when(recoveryService.getCaptcha()).thenReturn(getCaptchaInfoBean());

		// register the confirmation code
		service.retrieveCaptchaLink("aCode");

		when(recoveryService.verifyConfirmationCode(eq("user@tenant.com"), eq("aCode"), any())).thenThrow(
				RemoteException.class);

		try {
			service.confirmAccount("user", "pass", "aCode", "123456", "tenant.com");
		} catch (AccountConfirmationFailedException e) {
			assertEquals(ConfirmationFailType.UNKNOWN, e.getFailType());
			throw e;
		}
	}

	@Test(expected = AccountConfirmationFailedException.class)
	public void confirmAccount_shouldFailOnUnknownConfirmationCode() throws Exception {
		try {
			service.confirmAccount("user", "pass", "aCode", "123456", "tenant.com");
		} catch (AccountConfirmationFailedException e) {
			assertEquals(ConfirmationFailType.UNEXPECTED_CODE, e.getFailType());
			throw e;
		}
	}

	@Test(expected = AccountConfirmationFailedException.class)
	public void confirmAccount_shouldFailOnMissingUserName() throws Exception {

		try {
			service.confirmAccount(null, "pass", "aCode", "123456", "tenant.com");
		} catch (AccountConfirmationFailedException e) {
			assertEquals(ConfirmationFailType.INVALID_USERNAME, e.getFailType());
			throw e;
		}
	}

	@Test(expected = AccountConfirmationFailedException.class)
	public void confirmAccount_shouldFailOnMissingPassword() throws Exception {
		try {
			service.confirmAccount("user", "", "aCode", "123456", "tenant.com");
		} catch (AccountConfirmationFailedException e) {
			assertEquals(ConfirmationFailType.INVALID_PASSWORD, e.getFailType());
			throw e;
		}
	}

	@Test(expected = AccountConfirmationFailedException.class)
	public void confirmAccount_shouldFailOnMissingCaptchaAnswer() throws Exception {
		try {
			service.confirmAccount("user", "pass", "aCode", "  ", "tenant.com");
		} catch (AccountConfirmationFailedException e) {
			assertEquals(ConfirmationFailType.INVALID_CAPTCHA_ANSWER, e.getFailType());
			throw e;
		}
	}

	@Test(expected = AccountConfirmationFailedException.class)
	public void confirmAccount_shouldFailOnMissingConfirmationCode() throws Exception {
		try {
			service.confirmAccount("user", "pass", null, "123456", "tenant.com");
		} catch (AccountConfirmationFailedException e) {
			assertEquals(ConfirmationFailType.INVALID_OR_EXPIRED_CONFIRMATION_CODE, e.getFailType());
			throw e;
		}
	}

	@Test(expected = AccountConfirmationFailedException.class)
	public void confirmAccount_shouldFail_When_CaptchAnswerIsInvalid() throws Exception {
		try {
			VerificationBean verificationBean = new VerificationBean();
			verificationBean.setVerified(false);
			verificationBean.setError("18004 Captcha answer is invalid");

			when(recoveryService.getCaptcha()).thenReturn(getCaptchaInfoBean());
			when(recoveryService.verifyConfirmationCode(anyString(), anyString(), any(CaptchaInfoBean.class)))
					.thenReturn(verificationBean);

			service.retrieveCaptchaLink("code");
			service.confirmAccount("user", "pass", "code", "123456", "tenant.com");
		} catch (AccountConfirmationFailedException e) {
			assertEquals(ConfirmationFailType.INVALID_CAPTCHA_ANSWER, e.getFailType());
			throw e;
		}
	}

}
