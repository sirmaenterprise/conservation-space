package com.sirma.sep.email.service;

import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.exception.EmailIntegrationException;

/**
 * Implementation of the {@link EmailAddressGeneratorService}.
 *
 * @author svelikov
 */
@Singleton
public class EmailAddressGeneratorServiceImpl implements EmailAddressGeneratorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailAddressGeneratorServiceImpl.class);

	private static final String PART_DOMAIN = "domain";
	private static final String PART_LOCAL_TENAT_ID = "localTenatId";
	private static final String PART_TEST_MODE_SUFFIX = "testModeSuffix";
	private static final String PART_TEMPLATE = "template";
	private static final String PART_SEQUENCE_ID = "sequenceId";

	private static final String AT = "@";
	private static final String SEPARATOR = "-";
	private static final int EMAIL_LOCAL_PART_MAX_LENGTH = 64;
	private static final Pattern INVALID_CHARS = Pattern.compile("[^A-Za-z0-9._-]");
	private static final Pattern CONSECUTIVE_SEPARATORS = Pattern.compile("[" + SEPARATOR + "]{2,}");

	@Inject
	private SecurityContext securityContext;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private ExpressionsManager expressionsManager;

	@Inject
	private EmailAddressResolver emailAddressResolver;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	@Override
	public String generateEmailAddress(Instance instance) throws EmailIntegrationException {
		String emailAddress;
		Map<String, String> parts = new HashMap<>();
		try {
			// - evaluate template part
			addTemplatePart(parts, instance);
			// - add test mode suffix if need
			addTestModeSuffix(parts);
			// - add domain
			addEmailDomain(parts);
			// - check for collisions and add sequence id
			resolveCollision(instance, parts);
			// - validate length and trim template part
			validateLength(parts);
			// - build the address
			emailAddress = buildEmailAddress(parts);
		} catch (Exception e) {
			LOGGER.error("Unable to generate email address for instance: [{}]", instance.getId());
			throw new EmailIntegrationException("Unable to generate email address for instance: " + instance.getId(),
					e);
		}
		return emailAddress;
	}

	private static void validateLength(Map<String, String> parts) {
		String emailAddress = buildEmailAddress(parts);
		String localPart = emailAddress.split(AT)[0];
		if (localPart.length() > EMAIL_LOCAL_PART_MAX_LENGTH) {
			// cut to given length
			int sequenceIdLength = parts.get(PART_SEQUENCE_ID).length() == 0 ? 0
					: parts.get(PART_SEQUENCE_ID).length() + 1;
			int testModeSuffixLength = parts.get(PART_TEST_MODE_SUFFIX).length() == 0 ? 0
					: parts.get(PART_TEST_MODE_SUFFIX).length() + 1;
			int localTenantIdLength = parts.get(PART_LOCAL_TENAT_ID).length() == 0 ? 0
					: parts.get(PART_LOCAL_TENAT_ID).length() + 1;
			int cut = sequenceIdLength + testModeSuffixLength + localTenantIdLength;
			String templatePart = parts.get(PART_TEMPLATE);
			int templateLength = EMAIL_LOCAL_PART_MAX_LENGTH - cut;
			templatePart = templatePart.substring(0, templateLength);
			parts.put(PART_TEMPLATE, templatePart);
		}
	}

	private void resolveCollision(Instance instance, Map<String, String> parts) {
		parts.put(PART_SEQUENCE_ID, "");
		String emailAddress = buildEmailAddress(parts);
		boolean addressExists = emailAddressResolver.getEmailAddress(emailAddress) != null;
		if (addressExists) {
			String type = (String) instance.type().getId();
			IRI classUri = namespaceRegistryService.buildUri(type);
			String className = classUri.getLocalName().toLowerCase();
			String sequenceId = expressionsManager.evaluateRule("${seq({+" + className + "})}", String.class);
			parts.put(PART_SEQUENCE_ID, sequenceId);
		}
	}

	/**
	 * Evaluate the rule for the email address. Resulting string after evaluation is sanitized and cut to given length
	 * before to be returned.
	 *
	 * @param parts
	 *            The email address parts.
	 * @param instance
	 *            Current instance.
	 * @throws EmailIntegrationException
	 */
	private void addTemplatePart(Map<String, String> parts, Instance instance) throws EmailIntegrationException {
		DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(instance);
		if (instanceDefinition.getField(EMAIL_ADDRESS).get() == null) {
			LOGGER.error("Can't generate email address because of missing email address field in the definition.");
			throw new EmailIntegrationException(
					"Can't generate email address because of missing email address field in the definition.");
		}
		PropertyDefinition propertyDefinition = instanceDefinition.getField(EMAIL_ADDRESS).get();
		ExpressionContext context = expressionsManager.createDefaultContext(instance, propertyDefinition, null);
		Serializable evaluated = expressionsManager.evaluateRule(propertyDefinition, context, instance);

		String generatedPart = Objects.toString(evaluated, "");
		generatedPart = generateFallbackAddressIfNeed(generatedPart, instance);
		generatedPart = sanitize(generatedPart);
		parts.put(PART_TEMPLATE, generatedPart);
	}

	/**
	 * Sanitizes and validates the local part of the email address.
	 */
	private static String sanitize(String generatedPart) {
		String sanitized = generatedPart;
		// trim
		sanitized = sanitized.trim();
		// replace all invalid characters
		sanitized = INVALID_CHARS.matcher(sanitized).replaceAll(SEPARATOR);
		// strip consecutive separators
		sanitized = CONSECUTIVE_SEPARATORS.matcher(sanitized).replaceAll(SEPARATOR);
		// remove last dash if any
		sanitized = sanitized.endsWith("-") ? sanitized.substring(0, sanitized.length() - 2) : sanitized;
		return sanitized;
	}

	/**
	 * Builds a fallback email name if generated part is missing.
	 */
	private String generateFallbackAddressIfNeed(String generatedPart, Instance instance) {
		if (generatedPart.isEmpty()) {
			StringBuilder emailAddress = new StringBuilder();
			String type = (String) instance.type().getId();
			IRI classUri = namespaceRegistryService.buildUri(type);
			String className = classUri.getLocalName().toLowerCase();
			emailAddress.append(className);
			emailAddress.append("-");
			emailAddress.append(expressionsManager.evaluateRule("${seq({+" + className + "})}", String.class));
			return emailAddress.toString();
		}
		return generatedPart;
	}

	/**
	 * If system is in development mode append test email prefix to prevent duplication.
	 */
	private void addTestModeSuffix(Map<String, String> parts) {
		parts.put(PART_TEST_MODE_SUFFIX, "");
		if (StringUtils.isNotBlank(emailIntegrationConfiguration.getTestEmailPrefix().get())) {
			parts.put(PART_TEST_MODE_SUFFIX, emailIntegrationConfiguration.getTestEmailPrefix().get());
		}
	}

	/**
	 * If the tenant id in which the instance is created is the same as the configured domain address for the current
	 * tenant, then the tenant id is used as email domain. Otherwise the configured domain address is used as email
	 * domain and the tenant id is appended to the email name. Both variants are as follows:
	 * <p>
	 * objectTitle-1@tenant.org
	 * </p>
	 * <p>
	 * objectTitle-1-tenant-org@domain.org
	 * </p>
	 *
	 * @param parts
	 *            The email address parts.
	 */
	private void addEmailDomain(Map<String, String> parts) {
		if (securityContext.getCurrentTenantId().equals(emailIntegrationConfiguration.getTenantDomainAddress().get())) {
			parts.put(PART_LOCAL_TENAT_ID, "");
			// tenant id is added as domain for the email
			parts.put(PART_DOMAIN, securityContext.getCurrentTenantId());
		} else {
			// tenant id is added in the address local part
			String currentTenantId = securityContext.getCurrentTenantId();
			currentTenantId = currentTenantId.replace(AT, SEPARATOR);
			parts.put(PART_LOCAL_TENAT_ID, currentTenantId);
			parts.put(PART_DOMAIN, emailIntegrationConfiguration.getTenantDomainAddress().get());
		}
	}

	/**
	 * Assembles the email address using provided parts.
	 *
	 * @param parts
	 *            The email address parts.
	 * @return Email address.
	 */
	private static String buildEmailAddress(Map<String, String> parts) {
		StringBuilder emailAddress = new StringBuilder();
		emailAddress.append(parts.get(PART_TEMPLATE));
		if (StringUtils.isNotBlank(parts.get(PART_SEQUENCE_ID))) {
			emailAddress.append(SEPARATOR).append(parts.get(PART_SEQUENCE_ID));
		}
		if (StringUtils.isNotBlank(parts.get(PART_TEST_MODE_SUFFIX))) {
			emailAddress.append(SEPARATOR).append(parts.get(PART_TEST_MODE_SUFFIX));
		}
		if (StringUtils.isNotBlank(parts.get(PART_LOCAL_TENAT_ID))) {
			emailAddress.append(SEPARATOR).append(parts.get(PART_LOCAL_TENAT_ID));
		}
		String sanitized = sanitize(emailAddress.toString());
		emailAddress.setLength(0);
		emailAddress.append(sanitized);
		emailAddress.append(AT).append(parts.get(PART_DOMAIN));
		return emailAddress.toString();
	}
}