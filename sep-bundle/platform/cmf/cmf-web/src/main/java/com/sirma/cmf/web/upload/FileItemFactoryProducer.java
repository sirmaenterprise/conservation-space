package com.sirma.cmf.web.upload;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import com.sirma.itt.emf.io.TempFileProvider;

/**
 * {@link EmfFileItemFactory} bean producer. The factory is initialized with repository folder in
 * application temporary folder.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class FileItemFactoryProducer {

	/** The Constant index. */
	private final static AtomicLong index = new AtomicLong(0);

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
	public EmfFileItemFactory produce() {
		// the size could be a parameter at some time
		return new SerializableDiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD,
				createTempFolder());
	}

	/**
	 * Creates temporary folder for the next 24 hours that will be used for the current upload
	 * session.
	 * 
	 * @return the created folder
	 */
	private File createTempFolder() {
		return provider.createTempDir("FileFactory-" + System.currentTimeMillis() + '-'
				+ index.getAndIncrement());
	}

}
