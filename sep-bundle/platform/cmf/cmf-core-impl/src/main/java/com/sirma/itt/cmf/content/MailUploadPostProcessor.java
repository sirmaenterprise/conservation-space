package com.sirma.itt.cmf.content;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.LocalProxyFileDescriptor;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.content.extract.MailExtractor;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.io.TempFileProvider;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.util.FileUtil;

/**
 * Extraction of mails as post process of uploading eml/msg files.
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = UploadPostProcessor.TARGET_NAME, order = 1)
public class MailUploadPostProcessor implements UploadPostProcessor {

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MailUploadPostProcessor.class);

	/** The default type to search for. */
	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_DOCUMENT_DEFAULT_ATTACHMENT_TYPE, defaultValue = "OT210027")
	private String defaultAttachmentType;

	/** The properties. */
	@Inject
	@ExtensionPoint(value = MailExtractor.TARGET_NAME)
	private Iterable<MailExtractor<?>> mailExtractors;

	/** The delete mail attachment on extract. */
	private boolean deleteMailAttachmentOnExtract = false;

	/** DocumentService instance. */
	@Inject
	protected DocumentService documentService;

	/** The link service. */
	@Inject
	private LinkService linkService;

	@Inject
	private TempFileProvider fileProvider;

	/**
	 * On mail uploaded extract the content and return it as list of created documents<br>
	 * {@inheritDoc}
	 */
	@Override
	public List<DocumentInstance> proccess(final DocumentInstance instance) throws Exception {
		Instance owningInstance = instance.getOwningInstance();

		if ((owningInstance instanceof SectionInstance)
				&& !documentService.canUploadDocumentInSection(
						(SectionInstance) instance.getOwningInstance(), defaultAttachmentType)) {
			LOGGER.warn("Don't have permissions to upload file of type: {} in section ",
					defaultAttachmentType, owningInstance.getIdentifier());
			return Collections.singletonList(instance);
		}

		// get the mail store location
		Serializable descriptor = instance.getProperties().get(DocumentProperties.FILE_LOCATOR);
		if (!(descriptor instanceof FileDescriptor)) {
			LOGGER.error("Mail attachment processor could not find actual file (" + descriptor
					+ ") or not implemented yet!");
			return Collections.singletonList(instance);
		}

		return splitMailAttachments(getTempFolder(), (FileDescriptor) descriptor,
				instance);
	}

	/**
	 * Gets the temp folder.
	 * 
	 * @return the temp folder
	 */
	private File getTempFolder() {
		Serializable folder = RuntimeConfiguration
				.getConfiguration(RuntimeConfigurationProperties.UPLOAD_SESSION_FOLDER);
		if (folder instanceof File) {
			return (File) folder;
		}
		return fileProvider.createTempDir(UUID.randomUUID().toString());
	}

	/**
	 * Split mail attachments and add them to the current document section. Naming is handled by the
	 * 
	 * @param tempDir
	 *            the dir where mail is stored and where to store the attachments.
	 * @param descriptor
	 *            is the currently uploaded mail descriptor
	 * @param selectedDocumentInstance
	 *            the selected document instance that is uploaded
	 * @return the list of created instances during split of mail
	 * @throws IOException
	 *             the exception on any error
	 */
	private List<DocumentInstance> splitMailAttachments(File tempDir, FileDescriptor descriptor,
			DocumentInstance selectedDocumentInstance) throws IOException {

		if (!(selectedDocumentInstance.getOwningInstance() instanceof SectionInstance)) {
			LOGGER.error("Not implemented mail attachments to {}, yet", (selectedDocumentInstance
					.getOwningInstance() != null ? selectedDocumentInstance.getOwningInstance()
					.getClass().getSimpleName() : " non section parent"));
			return Collections.singletonList(selectedDocumentInstance);
		}
		SectionInstance owningInstance = (SectionInstance) selectedDocumentInstance
				.getOwningInstance();
		for (MailExtractor<?> extractor : mailExtractors) {
			File mailFile = new File(getFilePath(descriptor));
			if (extractor.isApplicable(mailFile) != null) {
				List<DocumentInstance> createdDocuments = extractAttachments(extractor, descriptor,
						selectedDocumentInstance, owningInstance, mailFile.getName(), tempDir);
				return createdDocuments;
			}
		}
		return Collections.singletonList(selectedDocumentInstance);
	}

	/**
	 * Extract attachments.
	 * 
	 * @param extractor
	 *            the extractor
	 * @param descriptor
	 *            the descriptor
	 * @param selectedDocumentInstance
	 *            the selected document instance
	 * @param owningInstance
	 *            the owning instance
	 * @param filename
	 *            the filename
	 * @param tempDir
	 *            the temp dir
	 * @return the list
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	private List<DocumentInstance> extractAttachments(MailExtractor<?> extractor,
			FileDescriptor descriptor, DocumentInstance selectedDocumentInstance,
			SectionInstance owningInstance, String filename, File tempDir) throws IOException {
		List<File> attachments = (List<File>) extractor.extractMail(descriptor,
				isDeleteMailAttachmentOnExtract(), tempDir, filename,
				MailExtractor.PART_ATTACHMENTS).get(MailExtractor.PART_ATTACHMENTS);

		List<DocumentInstance> createdDocuments = new ArrayList<>(attachments.size() + 1);
		// add all attachments with the mail itself
		createdDocuments.add(selectedDocumentInstance);
		createDocumentInstances(selectedDocumentInstance, owningInstance, attachments,
				createdDocuments);
		return createdDocuments;
	}

	/**
	 * Creates the document instances.
	 * 
	 * @param selectedDocumentInstance
	 *            the selected document instance
	 * @param owningInstance
	 *            the owning instance
	 * @param attachments
	 *            the attachments
	 * @param createdDocuments
	 *            the created documents
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void createDocumentInstances(DocumentInstance selectedDocumentInstance,
			SectionInstance owningInstance, List<File> attachments,
			List<DocumentInstance> createdDocuments) throws IOException {
		for (File fileAttachment : attachments) {
			if (fileAttachment.length() == 0) {
				// probably does not exists or directory or just empty
				LOGGER.debug("Skipping extraction of empty file {}", fileAttachment);
				continue;
			}
			// create new instance
			DocumentInstance createdDocumentInstance = documentService
					.createDocumentInstance(owningInstance, defaultAttachmentType);
			if (createdDocumentInstance == null) {
				LOGGER.warn("Cannot upload more files of type {} to current {} section ",
						defaultAttachmentType, owningInstance.getIdentifier());
				return;
			}
			// clear the purpose of created document if marked for iDoc
			// NOTE: this will break if the mail attachment is an actual iDoc
			// but we can't check the mimetype here so..
			Pair<String, String> nameAndExtension = FileUtil.splitNameAndExtension(fileAttachment);
			if ((nameAndExtension.getSecond() == null)
					|| !nameAndExtension.getSecond().contains("htm")) {
				createdDocumentInstance.setPurpose(null);
			}
			// prepare metadata for document
			updateDocumentModelOnUpload(fileAttachment, createdDocumentInstance);
			createdDocumentInstance.getProperties().put(
					DocumentProperties.VERSION_DESCRIPTION, "");
			createdDocumentInstance.getProperties().put(
					DocumentProperties.IS_MAJOR_VERSION, Boolean.TRUE);

			// link them
			linkService.link(selectedDocumentInstance, createdDocumentInstance,
					LinkConstants.LINK_MAIL_ATTACHMENT, LinkConstants.LINK_ATTACHMENT_MAIL,
					LinkConstants.DEFAULT_SYSTEM_PROPERTIES);
			createdDocuments.add(createdDocumentInstance);
		}
	}

	/**
	 * Gets the file path.
	 * 
	 * @param descriptor
	 *            the descriptor
	 * @return the file path
	 */
	private String getFilePath(FileDescriptor descriptor) {
		String prefix = descriptor.getId();
		if (descriptor instanceof LocalProxyFileDescriptor) {
			prefix = ((LocalProxyFileDescriptor) descriptor).getProxiedId();
		}
		return prefix;
	}

	/**
	 * Method to update the document properties before upload.
	 *
	 * @param fileAttachment
	 *            is the file that is extracted
	 * @param documentInstance
	 *            is the currently uploaded instance
	 * @throws IOException
	 *             if any error occurs
	 */
	private void updateDocumentModelOnUpload(File fileAttachment,
			DocumentInstance documentInstance) throws IOException {

		Map<String, Serializable> properties = documentInstance.getProperties();

		properties.put(DocumentProperties.FILE_LOCATOR,
				new LocalProxyFileDescriptor(FileUtil.ensureValidName(fileAttachment.getName()),
						fileAttachment));

		// No empty title is allowed - CMF-6612
		// for mail attachments we set the file name as title - the user could change it later
		properties.put(DocumentProperties.TITLE, fileAttachment.getName());

		properties.put(DocumentProperties.FILE_SIZE,
				FileUtil.humanReadableByteCount(fileAttachment.length()));
	}

	/**
	 * Checks if is delete mail attachment on extract.
	 *
	 * @return the deleteMailAttachmentOnExtract
	 */
	public boolean isDeleteMailAttachmentOnExtract() {
		return deleteMailAttachmentOnExtract;
	}

	/**
	 * Sets the delete mail attachment on extract.
	 *
	 * @param deleteMailAttachmentOnExtract
	 *            the deleteMailAttachmentOnExtract to set
	 */
	public void setDeleteMailAttachmentOnExtract(boolean deleteMailAttachmentOnExtract) {
		this.deleteMailAttachmentOnExtract = deleteMailAttachmentOnExtract;
	}
}
