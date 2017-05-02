package com.sirma.itt.seip.help;

import java.lang.invoke.MethodHandles;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

/**
 * The SSLService provides methods to manipulate the ssl behavior of the userhelp application.
 */
public class SSLService {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Instantiates a new SSL service.
	 */
	public SSLService() {
		// no state to init
	}

	/**
	 * Disable ssl validation by implementing the trusted certificate check.
	 *
	 * @return true, if successful
	 */
	public boolean disableSSLValidation() {
		try {

			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new java.security.cert.X509Certificate[0];
				}

				@Override
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
					// skip check
				}

				@Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
					// skip check
				}
			} };

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			return true;
		} catch (Exception e) {
			LOGGER.error("SSL setup failed!", e);
		}
		return false;
	}
}
