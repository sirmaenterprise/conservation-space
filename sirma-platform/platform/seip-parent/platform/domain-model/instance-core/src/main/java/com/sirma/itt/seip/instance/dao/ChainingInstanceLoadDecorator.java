package com.sirma.itt.seip.instance.dao;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.collections.FixedBatchSpliterator;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.properties.RelationalNonPersistentPropertiesExtension;
import com.sirma.itt.seip.instance.properties.SemanticNonPersistentPropertiesExtension;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.instance.version.VersionProperties;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Used to collect all InstanceLoadDecorator implementations and call them. This class is skipped, because it is not
 * registered as extension. <br>
 * In addition, sets temporary instance property after decoration, which is used to prevent multiple decorations, if the
 * instance is already decorated.
 *
 * @author A. Kunchev
 * @author BBonev
 */
@ApplicationScoped
public class ChainingInstanceLoadDecorator implements InstanceLoadDecorator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int CHARACTERISTICS = Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE
			| Spliterator.NONNULL;

	/** Flag that is set to the instances as property after decoration. Used to prevent multiple decorations. */
	private static final String DECORATED = "$decorated$";
	private static final Consumer<Instance> SET_DECORATED = instance -> instance.add(DECORATED, Boolean.TRUE);
	private static final Predicate<Instance> NOT_DECORATED = instance -> !instance.isPropertyPresent(DECORATED);

	private static final Predicate<InstanceLoadDecorator> PARALLEL_PROCESSING_FILTER = InstanceLoadDecorator::allowParallelProcessing;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.postInstanceLoad.parallel", defaultValue = "true", sensitive = true, type = Boolean.class, system = true, label = "Defines if multiple threads should be used when performing post instance decoration like fetching thumbnails or favories")
	private ConfigurationProperty<Boolean> parallelPostInstanceLoading;

	@Inject
	@ExtensionPoint(InstanceLoadDecorator.INSTANCE_DECORATOR)
	private Plugins<InstanceLoadDecorator> decorators;

	@Inject
	@ExtensionPoint(InstanceLoadDecorator.VERSION_INSTANCE_DECORATOR)
	private Plugins<InstanceLoadDecorator> versionDecorators;

	@Inject
	private SecurityContextManager securityContextManager;

	/**
	 * Collects all of the implementation of this interface registered as extensions and call there concrete
	 * implementations of the same method. This method is used, when the instances are loaded and when they are
	 * refreshed.
	 *
	 * @param instance to be decorated
	 */
	@Override
	public <I extends Instance> void decorateInstance(I instance) {
		if (instance == null || isDecorated(instance)
				|| Options.DISABLE_POST_INSTANCE_LOAD_DECORATION.isEnabled()) {
			return;
		}

		Plugins<InstanceLoadDecorator> plugins = isVersion(instance) ? versionDecorators : decorators;
		runDecorators(plugins, securityContextManager.wrap().consumer(decorator -> decorator.decorateInstance(instance)));
		SET_DECORATED.accept(instance);
	}

	private static boolean isVersion(Instance instance) {
		return instance.isValueNotNull(VersionProperties.IS_VERSION)
				|| InstanceVersionService.isVersion(instance.getId());
	}

	/*
	 * First runs async decorators and then run sync decorators so that work done by the async
	 * decorators could be integrated using the sync decorators
	 */
	private void runDecorators(Plugins<InstanceLoadDecorator> plugins,
			Consumer<? super InstanceLoadDecorator> decoratorConsumer) {
		getAsyncDecorators(plugins).forEach(decoratorConsumer);
		getSyncDecorators(plugins).forEach(decoratorConsumer);
	}

	/**
	 * Collects all of the implementation of this interface registered as extensions and call there concrete
	 * implementations of the this method. This method is used, when the instances are passed on batch.
	 *
	 * @param collection the collection of instances that will be decorated somehow
	 */
	@Override
	public <I extends Instance> void decorateResult(Collection<I> collection) {
		if (CollectionUtils.isEmpty(collection) || Options.DISABLE_POST_INSTANCE_LOAD_DECORATION.isEnabled()) {
			return;
		}

		Collection<I> versions = new ArrayList<>(collection.size());
		Collection<I> instances = new ArrayList<>(collection.size());

		filterAndDistribute(collection, versions, instances);

		decorate(versions, versionDecorators);
		decorate(instances, decorators);
	}

	@Override
	public void markAsDecorated(Instance instance) {
		SET_DECORATED.accept(instance);
	}

	@Override
	public void clearDecoratedStatus(Instance instance) {
		instance.remove(DECORATED);
	}

	@Override
	public boolean isDecorated(Instance instance) {
		return instance.isPropertyPresent(DECORATED);
	}

	private static <I extends Instance> void filterAndDistribute(Collection<I> all, Collection<I> versions,
			Collection<I> instances) {
		all.stream().filter(NOT_DECORATED).forEach(instance -> {
			if (isVersion(instance)) {
				versions.add(instance);
			} else {
				instances.add(instance);
			}
		});
	}

	private <I extends Instance> void decorate(Collection<I> collection, Plugins<InstanceLoadDecorator> plugins) {
		if (collection.isEmpty()) {
			return;
		}

		LOGGER.trace("Decorating [{}] instances.", collection.size());
		runDecorators(plugins, securityContextManager.wrap().consumer(decorator -> decorator.decorateResult(collection)));
		collection.forEach(SET_DECORATED);
	}

	private Stream<InstanceLoadDecorator> getAsyncDecorators(Plugins<InstanceLoadDecorator> plugins) {
		FixedBatchSpliterator<InstanceLoadDecorator> spliterator = FixedBatchSpliterator
				.batchedSpliterator(Spliterators.spliteratorUnknownSize(plugins.iterator(), CHARACTERISTICS), 1);
		return StreamSupport.stream(spliterator, parallelPostInstanceLoading.get().booleanValue()).filter(
				PARALLEL_PROCESSING_FILTER);
	}

	private static Stream<InstanceLoadDecorator> getSyncDecorators(Plugins<InstanceLoadDecorator> plugins) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(plugins.iterator(), CHARACTERISTICS), false)
					.filter(PARALLEL_PROCESSING_FILTER.negate());
	}

	/**
	 * Exclusion that prevents writing of the {@link ChainingInstanceLoadDecorator#DECORATED} property in the cache.
	 *
	 * @author BBonev
	 */
	@Extension(target = SemanticNonPersistentPropertiesExtension.TARGET_NAME, order = 232)
	static class InstanceDecoratorSemanticPropertiesExclude implements SemanticNonPersistentPropertiesExtension {

		@Override
		public Set<String> getNonPersistentProperties() {
			return Collections.singleton(DECORATED);
		}
	}

	/**
	 * Exclusion that prevents writing of the {@link ChainingInstanceLoadDecorator#DECORATED} property in the RDB.
	 *
	 * @author BBonev
	 */
	@Extension(target = RelationalNonPersistentPropertiesExtension.TARGET_NAME, order = 232)
	static class InstanceDecoratorRelationalPropertiesExclude implements RelationalNonPersistentPropertiesExtension {

		@Override
		public Set<String> getNonPersistentProperties() {
			return Collections.singleton(DECORATED);
		}
	}
}