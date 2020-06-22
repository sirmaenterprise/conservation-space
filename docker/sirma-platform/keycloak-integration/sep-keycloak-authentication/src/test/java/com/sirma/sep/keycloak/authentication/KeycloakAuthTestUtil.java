package com.sirma.sep.keycloak.authentication;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.PublicKey;

import org.keycloak.adapters.AdapterUtils;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.rotation.HardcodedPublicKeyLocator;
import org.keycloak.common.util.PemUtils;
import org.keycloak.common.util.Time;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.representations.JsonWebToken;

import com.sirma.sep.keycloak.ClientProperties;

/**
 * Conains utility methods for testing authentication.
 *
 * @author smustafov
 */
public class KeycloakAuthTestUtil {

	public static final String TENANT_ID = "sep.test";

	public static final String REALM_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCrVrCuTtArbgaZzL1hvh0xtL5"
			+ "mc7o0NqPVnYXkLvgcwiC3BjLGw1tGEGoJaXDuSaRllobm53JBhjx33UNv+5z/UMG4kytBWxheNVKnL6GgqlNabMaFfPLPCF8kAgKnsi7"
			+ "9NMo+n6KnSY8YeUmec/p2vjO2NjsSAVcWEQMVhJ31LwIDAQAB";

	public static final String REALM_PRIVATE_KEY = "MIICXAIBAAKBgQCrVrCuTtArbgaZzL1hvh0xtL5mc7o0NqPVnYXkLvgcwiC3Bj"
			+ "LGw1tGEGoJaXDuSaRllobm53JBhjx33UNv+5z/UMG4kytBWxheNVKnL6GgqlNabMaFfPLPCF8kAgKnsi79NMo+n6KnSY8YeUmec/p2vj"
			+ "O2NjsSAVcWEQMVhJ31LwIDAQABAoGAfmO8gVhyBxdqlxmIuglbz8bcjQbhXJLR2EoS8ngTXmN1bo2L90M0mUKSdc7qF10LgETBzqL8jY"
			+ "lQIbt+e6TH8fcEpKCjUlyq0Mf/vVbfZSNaVycY13nTzo27iPyWQHK5NLuJzn1xvxxrUeXI6A2WFpGEBLbHjwpx5WQG9A+2scECQQDvdn"
			+ "9NE75HPTVPxBqsEd2z10TKkl9CZxu10Qby3iQQmWLEJ9LNmy3acvKrE3gMiYNWb6xHPKiIqOR1as7L24aTAkEAtyvQOlCvr5kAjVqrEK"
			+ "Xalj0Tzewjweuxc0pskvArTI2Oo070h65GpoIKLc9jf+UA69cRtquwP93aZKtW06U8dQJAF2Y44ks/mK5+eyDqik3koCI08qaC8HYq2w"
			+ "Vl7G2QkJ6sbAaILtcvD92ToOvyGyeE0flvmDZxMYlvaZnaQ0lcSQJBAKZU6umJi3/xeEbkJqMfeLclD27XGEFoPeNrmdx0q10Azp4NfJ"
			+ "AY+Z8KRyQCR2BEG+oNitBOZ+YXF9KCpH3cdmECQHEigJhYg+ykOvr1aiZUMFT72HU0jnmQe2FVekuG+LJUt2Tm7GtMjTFoGpf0JwrVuZ"
			+ "N39fOYAlo+nTixgeW7X8Y=";

	public static String generateToken(String username) {
		return generateToken(username, Time.currentTime() + 1000000);
	}

	public static String generateToken(String realm, String username) {
		return generateToken(realm, username, Time.currentTime() + 1000000);
	}

	public static String generateToken(String username, int expiration) {
		return generateToken(TENANT_ID, username, expiration);
	}

	public static String generateToken(String tenant, String username, int expiration) {
		JsonWebToken reqToken = new JsonWebToken();
		reqToken.id(AdapterUtils.generateId());
		reqToken.issuer("http://localhost:8090/auth/realms/sep.test");
		reqToken.subject(username);
		reqToken.setOtherClaims(ClientProperties.USERNAME_CLAIM_NAME, username);
		reqToken.type("Bearer");
		int now = Time.currentTime();
		reqToken.issuedAt(now);
		reqToken.expiration(expiration);
		reqToken.notBefore(now);

		if (tenant != null) {
			reqToken.setOtherClaims(ClientProperties.TENANT_MAPPER_NAME, tenant);
		}

		return new JWSBuilder().jsonContent(reqToken).rsa256(PemUtils.decodePrivateKey(REALM_PRIVATE_KEY));
	}

	public static KeycloakDeployment getMockedDeployment() {
		return getMockedDeployment(TENANT_ID);
	}

	public static KeycloakDeployment getMockedDeployment(String realm) {
		KeycloakDeployment keycloakDeployment = mock(KeycloakDeployment.class);
		when(keycloakDeployment.getRealm()).thenReturn(realm);
		when(keycloakDeployment.getRealmInfoUrl()).thenReturn("http://localhost:8090/auth/realms/sep.test");

		PublicKey publicKey = PemUtils.decodePublicKey(REALM_PUBLIC_KEY);
		HardcodedPublicKeyLocator publicKeyLocator = new HardcodedPublicKeyLocator(publicKey);
		when(keycloakDeployment.getPublicKeyLocator()).thenReturn(publicKeyLocator);
		return keycloakDeployment;
	}

}
