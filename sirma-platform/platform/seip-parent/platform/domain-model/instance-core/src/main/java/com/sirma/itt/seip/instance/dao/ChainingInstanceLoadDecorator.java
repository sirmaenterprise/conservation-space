/**
 *
 */
package com.sirma.itt.seip.instance.dao;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
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
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Used to collect all InstanceLoadDecorator implementations and call them. This class is skipped, because it is not
 * registered as extension.
 *
 * @author A. Kunchev
 * @author BBonev
 */
@ApplicationScoped
public class ChainingInstanceLoadDecorator implements InstanceLoadDecorator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int CHARACTERISTICS = Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE
			| Spliterator.NONNULL;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.postInstanceLoad.parallel", defaultValue = "true", sensitive = true, type = Boolean.class, system = true, label = "Defines if multiple threads should be used when performing post instance decoration like fetching thumbnails or favories")
	private ConfigurationProperty<Boolean> parallelPostInstanceLoading;

	@Inject
	@ExtensionPoint(InstanceLoadDecorator.TARGET_NAME)
	private Iterable<InstanceLoadDecorator> decorators;

	@Inject
	private SecurityContextManager securityContextManager;

	/**
	 * Collects all of the implementation of this interface registered as extensions and call there concrete
	 * implementations of the same method. This method is used, when the instances are loaded and when they are
	 * refreshed.
	 *
	 * @param instance
	 *            the instance, which will be decorated
	 */
	@Override
	public <I extends Instance> void decorateInstance(I instance) {
		if (instance == null || Options.DISABLE_POST_INSTANCE_LOAD_DECORATION.isEnabled()) {
			return;
		}
		runDecorators(securityContextManager.wrap().consumer(decorator -> decorator.decorateInstance(instance)));
	}

	/*
	 * First runs async decorators and then run sync decorators so that work done by the async
	 * decorators could be integrated using the sync decorators
	 */
	void runDecorators(Consumer<? super InstanceLoadDecorator> decoratorConsumer) {
		getAsyncDecorators().forEach(decoratorConsumer);
		getSyncDecorators().forEach(decoratorConsumer);
	}

	/**
	 * Collects all of the implementation of this interface registered as extensions and call there concrete
	 * implementations of the this method. This method is used, when the instances are passed on batch.
	 *
	 * @param collection
	 *            the collection of instances that will be decorated somehow
	 */
	@Override
	public <I extends Instance> void decorateResult(Collection<I> collection) {
		if (CollectionUtils.isEmpty(collection) || Options.DISABLE_POST_INSTANCE_LOAD_DECORATION.isEnabled()) {
			return;
		}

		LOGGER.trace("Decorating [{}] instances.", collection.size());
		runDecorators(securityContextManager.wrap().consumer(decorator -> decorator.decorateResult(collection)));
	}

	Stream<InstanceLoadDecorator> getAsyncDecorators() {
		return StreamSupport.stream(
				FixedBatchSpliterator.batchedSpliterator(
						Spliterators.spliteratorUnknownSize(decorators.iterator(), CHARACTERISTICS), 1),
						parallelPostInstanceLoading.get().booleanValue())
					.filter(decorator -> decorator.allowParallelProcessing());
	}

	Stream<InstanceLoadDecorator> getSyncDecorators() {
		return StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(decorators.iterator(), CHARACTERISTICS), false)
					.filter(decorator -> !decorator.allowParallelProcessing());
	}

}
