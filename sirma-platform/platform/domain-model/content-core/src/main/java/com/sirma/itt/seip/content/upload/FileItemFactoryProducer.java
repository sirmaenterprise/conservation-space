package com.sirma.itt.seip.content.upload;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import com.sirma.itt.seip.io.TempFileProvider;

/**
 * {@link RepositoryFileItemFactory} bean producer. The factory is initialized with repository folder in application temporary
 * folder.
 *
 * @author BBonev
 */
@ApplicationScoped
public class FileItemFactoryProducer {

	/** The provider. */
	@Inject
	private TempFileProvider provider;

	/**
	 * Produce.
	 *
	 * @return the file item factory
	 */
	@Produces
	@Default
	public RepositoryFileItemFactory produce() {
		// the size could be a parameter at some time
		return new SerializableDiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, null, provider);
	}

}
