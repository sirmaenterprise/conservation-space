package com.sirma.itt.emf.dozer;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

import com.sirma.itt.emf.dozer.provider.DozerMapperMappingProvider;
import com.sirma.itt.emf.plugin.ExtensionPoint;

/**
 * Factory for dozer {@link Mapper}.<br>
 * <b>NOTE: </b>The dozer initialization depends on the extension point defined by
 * {@link DozerMapperMappingProvider}.
 * <p>
 * REVIEW: extract interface
 * 
 * @author BBonev
 */
@ApplicationScoped
public class DozerMapper {

	/** The mapper. */
	private Mapper mapper;

	/** The list of additional providers. */
	@Inject
	@ExtensionPoint(DozerMapperMappingProvider.TARGET_NAME)
	private Iterable<DozerMapperMappingProvider> providers;

	/**
	 * Initialize the mapper instance.
	 */
	@PostConstruct
	public void initializeMapper() {
		List<String> files = new LinkedList<String>();
		for (DozerMapperMappingProvider provider : providers) {
			files.addAll(provider.getMappingUries());
		}
		mapper = new DozerBeanMapper(files);
	}

	/**
	 * Obtain an instance of {@link Mapper}.
	 *
	 * @return Instance of {@link Mapper}.
	 */
	public synchronized Mapper getMapper() {
		return mapper;
	}
}
