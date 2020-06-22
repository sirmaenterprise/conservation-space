package com.sirma.itt.emf.authentication.sso.saml.authenticator;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.authentication.sso.saml.SAMLMessageProcessor;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.mocks.MockProvider;
import com.sirma.itt.seip.security.util.SecurityUtil;

/**
 * The Class BaseSamlAuthenticatorTest.
 */
public class BaseSamlAuthenticatorTest {
	private static final Logger LOGGER = Logger.getLogger(BaseSamlAuthenticatorTest.class);

	/**
	 * Prepare token.
	 */
	@Test
	public void prepareToken() {
		BaseSamlAuthenticator auth = createBaseSamlAuthenticator();
		SystemUserAuthenticator authSystem = MockProvider.provideSystemAuthenticator();

		SecretKey secretKey = SecurityUtil.createSecretKey("AlfrescoCMFLogin@Test");

		byte[] unEncrypted = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHNhbWwycDpSZXNwb25zZSBEZXN0aW5hdGlvbj0iaHR0cHM6Ly8xMC4xMzEuMi4xOTE6ODQ0NC9lbWYvU2VydmljZUxvZ2luIiBJRD0iZmdhY2JkaGNubWljb29nZ2xuamdtbG5pYnBqZGRvaGxsbGZhYWtpZyIgSXNzdWVJbnN0YW50PSIyMDE1LTEwLTI4VDEwOjIzOjA1LjE4NFoiIFZlcnNpb249IjIuMCIgeG1sbnM6c2FtbDJwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiPjxzYW1sMjpJc3N1ZXIgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6bmFtZWlkLWZvcm1hdDplbnRpdHkiIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj5sb2NhbGhvc3Q8L3NhbWwyOklzc3Vlcj48c2FtbDJwOlN0YXR1cz48c2FtbDJwOlN0YXR1c0NvZGUgVmFsdWU9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpzdGF0dXM6U3VjY2VzcyIvPjwvc2FtbDJwOlN0YXR1cz48c2FtbDI6QXNzZXJ0aW9uIElEPSJmbG5iY3BhYWlpZGhhYWlmbGRqZ2ZkbWRra3BhamNtamtnaGVocGFvIiBJc3N1ZUluc3RhbnQ9IjIwMTUtMTAtMjhUMTA6MjM6MDUuMTg0WiIgVmVyc2lvbj0iMi4wIiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+PHNhbWwyOklzc3VlciBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpuYW1laWQtZm9ybWF0OmVudGl0eSI+bG9jYWxob3N0PC9zYW1sMjpJc3N1ZXI+PHNhbWwyOlN1YmplY3Q+PHNhbWwyOk5hbWVJRCBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpuYW1laWQtZm9ybWF0OmVudGl0eSI+YWRtaW5AYmFuY2hldi5iZzwvc2FtbDI6TmFtZUlEPjxzYW1sMjpTdWJqZWN0Q29uZmlybWF0aW9uIE1ldGhvZD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmNtOmJlYXJlciI+PHNhbWwyOlN1YmplY3RDb25maXJtYXRpb25EYXRhIE5vdE9uT3JBZnRlcj0iMjAxNS0xMC0yOFQxMDoyODowNS4xODRaIiBSZWNpcGllbnQ9Imh0dHBzOi8vMTAuMTMxLjIuMTkxOjg0NDQvZW1mL1NlcnZpY2VMb2dpbiIvPjwvc2FtbDI6U3ViamVjdENvbmZpcm1hdGlvbj48L3NhbWwyOlN1YmplY3Q+PHNhbWwyOkNvbmRpdGlvbnMgTm90QmVmb3JlPSIyMDE1LTEwLTI4VDEwOjIzOjA1LjE4NFoiIE5vdE9uT3JBZnRlcj0iMjAxNS0xMC0yOFQxMDoyODowNS4xODRaIj48c2FtbDI6QXVkaWVuY2VSZXN0cmljdGlvbj48c2FtbDI6QXVkaWVuY2U+MTAuMTMxLjIuMTkxXzg0NDQ8L3NhbWwyOkF1ZGllbmNlPjwvc2FtbDI6QXVkaWVuY2VSZXN0cmljdGlvbj48L3NhbWwyOkNvbmRpdGlvbnM+PHNhbWwyOkF1dGhuU3RhdGVtZW50IEF1dGhuSW5zdGFudD0iMjAxNS0xMC0yOFQxMDoyMzowNS4xODVaIj48c2FtbDI6QXV0aG5Db250ZXh0PjxzYW1sMjpBdXRobkNvbnRleHRDbGFzc1JlZj51cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YWM6Y2xhc3NlczpQYXNzd29yZDwvc2FtbDI6QXV0aG5Db250ZXh0Q2xhc3NSZWY+PC9zYW1sMjpBdXRobkNvbnRleHQ+PC9zYW1sMjpBdXRoblN0YXRlbWVudD48L3NhbWwyOkFzc2VydGlvbj48L3NhbWwycDpSZXNwb25zZT4="
				.getBytes(StandardCharsets.UTF_8);

		SAMLTokenInfo token = auth.prepareToken(unEncrypted, secretKey);
		Assert.assertEquals(token.getDecrypted(), unEncrypted);
		Assert.assertNotEquals(token.getEncrypted(), unEncrypted);

		byte[] createdToken = authSystem.createToken("admin;", secretKey);
		LOGGER.info("Crypted text:" + new String(unEncrypted));
		token = auth.prepareToken(createdToken, secretKey);
		Assert.assertEquals(token.getEncrypted(), createdToken);
		Assert.assertNotEquals(token.getDecrypted(), createdToken);

		byte[] decodedSAMLMessage = SAMLMessageProcessor.decodeSAMLMessage(unEncrypted);
		LOGGER.info("Plain text:" + new String(decodedSAMLMessage));
		token = auth.prepareToken(decodedSAMLMessage, secretKey);
		Assert.assertEquals(token.getDecrypted(), SAMLMessageProcessor.encodeSAMLMessage(decodedSAMLMessage));
		Assert.assertEquals(token.getEncrypted(),
				SAMLMessageProcessor.encodeSAMLMessage(SecurityUtil.encrypt(decodedSAMLMessage, secretKey)));
	}

	private BaseSamlAuthenticator createBaseSamlAuthenticator() {
		BaseSamlAuthenticator auth = new BaseSamlAuthenticator() {

			@Override
			public Object authenticate(User toAuthenticate) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public User authenticate(AuthenticationContext authenticationContext) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		return auth;
	}

}
