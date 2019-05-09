package com.sirma.itt.seip.rest.provider;

import javax.inject.Inject;
import javax.validation.BootstrapConfiguration;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.validation.GeneralValidatorImpl;
import org.jboss.resteasy.spi.validation.GeneralValidator;

import com.sirma.itt.seip.rest.utils.JaxRsAnnotationParameterNameProvider;

/**
 * JAX-RS provider for configuring the {@link GeneralValidator}.
 *
 * @author yasko
 */
@Provider
public class GeneralValidatorContextResolver implements ContextResolver<GeneralValidator> {

	@Inject
	private JaxRsAnnotationParameterNameProvider parameterNameProvider;

	@Override
	public GeneralValidator getContext(Class<?> type) {
		Configuration<?> config = Validation.byDefaultProvider().configure();
		BootstrapConfiguration bootstrapConfiguration = config.getBootstrapConfiguration();

		config.parameterNameProvider(parameterNameProvider);
		return new GeneralValidatorImpl(config.buildValidatorFactory(),
				bootstrapConfiguration.isExecutableValidationEnabled(),
				bootstrapConfiguration.getDefaultValidatedExecutableTypes());
	}
}
