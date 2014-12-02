package com.sirma.itt.cmf.services.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.ByteArrayFileDescriptor;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileAndPropertiesDescriptor;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.emf.instance.model.DMSInstance;

/**
 * The DMSInstanceAdapterService mock. Uses the tika extractor
 */
@ApplicationScoped
public class CMFInstanceAdapterService implements DMSInstanceAdapterService {

	/** The tika extractor. */
	private File tikaExtractor;

	/** The logger. */
	private Logger logger = Logger.getLogger(CMFInstanceAdapterService.class);

	/**
	 * Instantiates a new cMF instance adapter service.
	 */
	public CMFInstanceAdapterService() {

		try {
			InputStream resourceAsStream = CMFInstanceAdapterService.class
					.getResourceAsStream("tika-extractor.jar");

			tempDir = new File(System.getProperty("java.io.tmpdir"));
			tikaExtractor = new File(tempDir, "tika_extractor.jar");
			FileOutputStream fileOutputStream = new FileOutputStream(tikaExtractor);
			IOUtils.copy(resourceAsStream, fileOutputStream);
			IOUtils.closeQuietly(fileOutputStream);
		} catch (Exception e) {
			logger.warn("Missing tika extractor!");
		}
	}

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7392411462384827705L;

	/** The temp dir. */
	private File tempDir;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> updateNode(DMSInstance instance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public DMSInstance attachFolderToInstance(DMSInstance parent, DMSInstance child)
			throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public FileAndPropertiesDescriptor attachDocumentToInstance(DMSInstance instance,
			FileDescriptor descriptor, String customAspect) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		StringWriter writer = new StringWriter();
		final DocumentInstance documentInstance = (DocumentInstance) instance;
		if (descriptor instanceof ByteArrayFileDescriptor) {
			try {
				IOUtils.copy(descriptor.getInputStream(), writer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			documentInstance.getProperties().put("mimetype", "text/plain");
		} else if (descriptor instanceof FileDescriptor) {
			if (tikaExtractor != null) {

				InputStream stream = null;
				FileInputStream stored = null;
				File store = null;
				try {
					store = new File(tempDir, new File(descriptor.getId()).getName() + ".txt");
					ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar",
							tikaExtractor.getName(), descriptor.getId(), store.getAbsolutePath());
					processBuilder.directory(tempDir);

					Process exec = processBuilder.start();
					// "java -jar " + createTempFile.getName() + " \""
					// + descriptor.getId() + "\"");
					int waitFor = exec.waitFor();
					if (waitFor == 0) {
						stream = exec.getInputStream();
					} else {
						stream = exec.getErrorStream();

					}
					if (logger.isDebugEnabled()) {
						IOUtils.copy(stream, writer);
						// logger.debug(writer.toString());
						writer.close();
						writer = new StringWriter();
					}
					stored = new FileInputStream(store);
					IOUtils.copy(stored, writer);
				} catch (Exception e) {
					e.printStackTrace(new PrintWriter(writer));
				} finally {
					IOUtils.closeQuietly(writer);
					IOUtils.closeQuietly(stored);
					if (!store.delete()) {
						store.deleteOnExit();
					}
				}
			}
		}
		documentInstance.setDmsId(UUID.randomUUID().toString());
		documentInstance.getProperties().put("content", writer.getBuffer().toString());
		writer = null;
		return new FileAndPropertiesDescriptor() {

			/**
			 * Comment for serialVersionUID.
			 */
			private static final long serialVersionUID = 3300151082540654630L;

			@Override
			public String getId() {
				return documentInstance.getDmsId();
			}

			@Override
			public String getContainerId() {
				return null;
			}

			@Override
			public InputStream getInputStream() {
				return null;
			}

			@Override
			public Map<String, Serializable> getProperties() {
				return documentInstance.getProperties();
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String dettachDocumentFromInstance(DMSInstance documentInstance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return documentInstance.getDmsId();
	}

	@Override
	public boolean linkAsChild(DMSInstance parent, DMSInstance child, DMSInstance parentToUnlink,
			String assocName) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return true;
	}

	@Override
	public FileAndPropertiesDescriptor attachDocumenToLibrary(DMSInstance documentInstance,
			FileDescriptor descriptor, String customAspect) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public boolean removeLinkAsChild(DMSInstance parent, DMSInstance child, String assocName)
			throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return true;
	}

	@Override
	public boolean deleteNode(DMSInstance dmsInstance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return true;
	}

}
