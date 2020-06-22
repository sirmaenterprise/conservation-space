package com.sirma.itt.seip.definition;

import java.lang.invoke.MethodHandles;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;

/**
 * Default implementation of the {@link InstancePropertyNameResolver} that uses a request scoped cache to store already
 * resolved definitions for an instance in order to provide better performance. If request context is not available
 * then the resolver will fall back to on demand definition lookup.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 19/07/2018
 */
public class InstancePropertyNameResolverImpl implements InstancePropertyNameResolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DefinitionService definitionService;

	@Inject
	private InstancePropertyNameResolverCache cache;

	@Override
	public String resolve(Instance instance, String fieldUri) {
		return getOrResolveModel(instance)
				.getField(fieldUri)
				.map(PropertyDefinition::getName)
				.orElse(fieldUri);
	}

	private DefinitionModel getOrResolveModel(Instance instance) {
		try {
			return cache.getOrResolveModel(instance.getId(), resolveDefinition(instance));
		} catch (ContextNotActiveException e) {
			// for the cases when this is used outside of a transaction like parallel stream processing
			return resolveDefinition(instance).get();
		}
	}

	@Override
	public Function<String, String> resolverFor(Instance instance) {
		DefinitionModel model = getOrResolveModel(instance);
		return property -> model.getField(property)
				.map(PropertyDefinition::getName)
				.orElse(property);
	}

	private Supplier<DefinitionModel> resolveDefinition(Instance instance) {
		return () -> {
			LOGGER.trace("Resolving model for {}", instance.getId());
			return definitionService.getInstanceDefinition(instance);
		};
	}
}
