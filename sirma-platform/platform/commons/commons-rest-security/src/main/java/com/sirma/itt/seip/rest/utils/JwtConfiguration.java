package com.sirma.itt.seip.rest.utils;

import java.lang.invoke.MethodHandles;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jose4j.mac.MacUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.security.annotation.RunAsSystem;

/**
 * Register of JWT configuration keys and factories.
 *
 * @author yasko
 */
@Singleton
public class JwtConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/**
	 * Configuration property definition for {@code security.jwt.key} used for signing JWTs.
	 */
	@ConfigurationPropertyDefinition(type = Key.class, system = true, sensitive = true, label = "JWT signing key")
	public static final String SECURITY_JWT_KEY = "security.jwt.key";

	/**
	 * Configuration property definition for {@code security.jwt.issuer} used for the {@code iss} claim in JWTs.
	 */
	@ConfigurationPropertyDefinition(system = true, sensitive = true, label = "JWT issuer claim")
	public static final String SECURITY_JWT_ISSUER = "security.jwt.issuer";

	@Inject
	@Configuration(SECURITY_JWT_KEY)
	private ConfigurationProperty<Key> key;

	@Inject
	@Configuration(SECURITY_JWT_ISSUER)
	private ConfigurationProperty<String> issuer;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "security.authenticator.jwt.paramName", defaultValue = "APIKey", system = true, label = "The name of the the parameter name for the JWT authenticator.")
	private ConfigurationProperty<String> parameterName;

	@Inject
	private ConfigurationManagement configurationManagement;

	/**
	 * Initialize JTW configurations with default values.
	 */
	@Startup
	@RunAsSystem(protectCurrentTenant = false)
	protected void initialize() {
		Collection<com.sirma.itt.seip.configuration.db.Configuration> configurations = new ArrayList<>(2);
		if (key.isNotSet()) {
			configurations.add(
					new com.sirma.itt.seip.configuration.db.Configuration(key.getName(), generateNewJwtSigningKey()));
		}
		if (issuer.isNotSet()) {
			configurations.add(
					new com.sirma.itt.seip.configuration.db.Configuration(issuer.getName(), generateNewJwtIssuer()));
		}

		if (!configurations.isEmpty()) {
			configurationManagement.addConfigurations(configurations);
			// force value reloading
			key.valueUpdated();
			issuer.valueUpdated();
		}
	}

	/**
	 * Getter for the JWT signing key.
	 *
	 * @return Key used to sign JWT claims.
	 */
	public Key getKey() {
		key.requireConfigured();
		return key.get();
	}

	/**
	 * Getter for the JWT issuer ({@code iss}) claim.
	 *
	 * @return issuer value.
	 */
	public String getIssuer() {
		issuer.requireConfigured();
		return issuer.get();
	}

	/**
	 * Gets the jwt parameter name used for sending the authentication key
	 *
	 * @return the jwt parameter name
	 */
	public String getJwtParameterName() {
		return parameterName.get();
	}

	/**
	 * Converter for {@code security.jwt.key} configuration property.
	 *
	 * @param context
	 *            Context containing the raw key value (base64) encoded.
	 * @return {@link SecretKey} instance created for the raw base64 encoded value.
	 */
	@ConfigurationConverter(JwtConfiguration.SECURITY_JWT_KEY)
	static Key getJwtSigningKey(ConverterContext context) {
		String raw = context.getRawValue();
		if (raw == null) {
			return null;
		}
		return new SecretKeySpec(Base64.getDecoder().decode(raw), MacUtil.HMAC_SHA256);
	}

	private static String generateNewJwtSigningKey() {
		try {
			KeyGenerator generator = KeyGenerator.getInstance(MacUtil.HMAC_SHA256);
			SecretKey key = generator.generateKey();
			LOGGER.info("Generated new JWT key");
			return Base64.getEncoder().encodeToString(key.getEncoded());
		} catch (NoSuchAlgorithmException e) {
			throw new ConfigurationException(
					"Could not generate JWT signing key due to invalid algorithm " + MacUtil.HMAC_SHA256, e);
		}
	}

	private static String generateNewJwtIssuer() {
		String string = UUID.randomUUID().toString();
		LOGGER.info("Generated new JWT Issuer Id {}", string);
		return string;
	}
}
