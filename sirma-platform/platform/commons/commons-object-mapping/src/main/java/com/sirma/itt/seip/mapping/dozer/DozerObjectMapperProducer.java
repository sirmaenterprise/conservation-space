package com.sirma.itt.seip.mapping.dozer;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Factory for {@link ObjectMapper} instance backed up by dozer.<br>
 * <b>NOTE: </b>The dozer initialization depends on the extension point defined by {@link DozerMapperMappingProvider}.
 *
 * @author BBonev
 */
@Singleton
class DozerObjectMapperProducer implements Resettable {

	/** The list of additional providers. */
	@Inject
	@ExtensionPoint(DozerMapperMappingProvider.TARGET_NAME)
	private Iterable<DozerMapperMappingProvider> providers;

	/**
	 * Lazy initialized mapper proxy
	 */
	private DozerObjectMapper mapper = new DozerObjectMapper(new CachingSupplier<>(this::initializeMapper));

	/**
	 * Initialize the mapper instance.
	 *
	 * @return the mapper
	 */
	synchronized Mapper initializeMapper() {
		List<String> files = new LinkedList<>();
		for (DozerMapperMappingProvider provider : providers) {
			files.addAll(provider.getMappingUries());
		}
		return new DozerBeanMapper(files);
	}

	/**
	 * Obtain an instance of {@link ObjectMapper}.
	 *
	 * @return Instance of {@link ObjectMapper}.
	 */
	@Produces
	@ApplicationScoped
	public ObjectMapper getMapper() {
		return mapper;
	}

	@PreDestroy
	void onShutdown() {
		Resettable.reset(mapper);
	}

	@Override
	public void reset() {
		Resettable.reset(mapper);
	}
}
