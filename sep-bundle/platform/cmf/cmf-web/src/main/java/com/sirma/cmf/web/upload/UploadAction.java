package com.sirma.cmf.web.upload;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIInput;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.function.RichFunction;

import com.sirma.cmf.web.document.NewDocumentAction;
import com.sirma.itt.cmf.beans.LocalProxyFileDescriptor;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.content.UploadPostProcessor;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.io.TempFileProvider;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.FileUtil;

/**
 * Backing bean for file upload operations.
 */
@Named
@ViewAccessScoped
public class UploadAction extends NewDocumentAction implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2135275835545918747L;

	/** The Constant EMPTY_STRING. */
	private static final String EMPTY_STRING = "";

	/** The files. */
	private ArrayList<UploadedFile> files = new ArrayList<UploadedFile>();

	/** The uploads available. */
	@Config(name = CmfConfigurationProperties.LIMIT_MAX_FILES_NUMBER, defaultValue = "1")
	@Inject
	private int uploadsAvailable;
	/** Accepted file type. */
	private String acceptedFileType;

	/** Description provided when a new document version is uploaded. */
	private String newVersionDescription;

	/** If uploaded file is a first version or is update for a old one. */
	private boolean isFirstVersion = true;

	/** Whether the new version is a major one or minor. */
	private boolean isMajorVersion = false;

	/** Document instance for which a new version is uploaded. */
	private DocumentInstance documentInstance;

	/** temp file provider. */
	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	@ExtensionPoint(value = UploadPostProcessor.TARGET_NAME)
	private Iterable<UploadPostProcessor> postProcessors;

	/**
	 * Prepare ui before uploading.
	 * 
	 * @param selectedSection
	 *            SectionInstance where this action is initiated.
	 */
	public void prepareUpload(SectionInstance selectedSection) {
		setDocumentInstance(null);
		setSectionInstance(selectedSection);
		setDescription(null);
		setTitle(null);

		filterFileTypesByCase(selectedSection, null);

		log.debug("CMFWeb: executed UploadAction.prepareUpload: new document is to be uploaded");
	}

	/**
	 * Prepare upload for attaching a new version.
	 * 
	 * @param selectedDocument
	 *            DocumentInstance.
	 */
	public void prepareUpload(DocumentInstance selectedDocument) {

		if (selectedDocument == null) {
			log.debug("CMFWeb: executed UploadAction.prepareUpload: Document instance, not available !");
		} else {
			selectedDocument = documentService.loadByDbId(selectedDocument.getId());
			getDocumentContext().setDocumentInstance(selectedDocument);
			String docName = (String) selectedDocument.getProperties().get(DocumentProperties.NAME);
			isFirstVersion = false;
			acceptedFileType = docName.substring(docName.lastIndexOf(".") + 1);

			setDocumentInstance(selectedDocument);
			// FIXME: document may not be in section but in object and in that case we would have
			// ClassCastException here
			Instance sectionInstance = selectedDocument.getOwningInstance();
			if (sectionInstance != null) {
				setSectionInstance((SectionInstance) sectionInstance);
				Instance caseInstance = getDocumentContext().getInstance(CaseInstance.class);
				if (caseInstance == null) {
					caseInstance = ((SectionInstance) sectionInstance).getOwningInstance();
					getDocumentContext().addInstance(caseInstance);
				}
			}
			setIsMajorVersion(false);
			setNewVersionDescription(null);

			log.debug("CMFWeb: executed UploadAction.prepareUpload: new version for document is to be uploaded");
		}
	}

	/**
	 * Finds the submitted value from a ui component with given id if found in the view root.
	 * 
	 * @param id
	 *            the id
	 * @return the field submitted value
	 */
	protected Object getFieldSubmittedValue(String id) {
		Object submittedValue = null;

		UIInput component = (UIInput) RichFunction.findComponent(id);
		if (component != null) {
			submittedValue = component.getSubmittedValue();
		}

		return submittedValue;
	}

	/**
	 * Listener for upload event. REVIEW "listener" ne e dobro ime za method
	 * 
	 * @param event
	 *            the event
	 * @throws Exception
	 *             on error
	 */
	public void listener(FileUploadEvent event) throws Exception {

		setTitle((String) getFieldSubmittedValue("fileTitleValue"));
		setDescription((String) getFieldSubmittedValue("description"));
		setNewVersionDescription((String) getFieldSubmittedValue("newVersionDescription"));
		String versionType = (String) getFieldSubmittedValue("versionType");
		if (versionType != null) {
			setIsMajorVersion(Boolean.parseBoolean(versionType));
		}

		String docTitle = getTitle();

		log.debug("CMFWeb: executing UploadAction.listener for document with title: " + docTitle);

		org.richfaces.model.UploadedFile item = event.getUploadedFile();
		File tempDir = null;
		CaseInstance caseInstance = getDocumentContext().getInstance(CaseInstance.class);
		boolean errorOnUpload = false;
		try {
			DocumentInstance selectedDocumentInstance = getDocumentInstance();

			SectionInstance sectionForSelectedDocument = getSectionInstance();
			if (selectedDocumentInstance == null) {
				if (documentService
						.canUploadDocumentInSection(sectionForSelectedDocument, docTitle)) {
					selectedDocumentInstance = documentService.createDocumentInstance(
							sectionForSelectedDocument, docTitle);
					setDocumentInstance(selectedDocumentInstance);
				} else {
					log.warn("!!!   Not allowed document upload of type=" + docTitle
							+ " in section=" + sectionForSelectedDocument.getIdentifier()
							+ "   !!!");
					return;
				}
			} else {
				if (!selectedDocumentInstance.hasDocument()) {
					String attachmentType = (String) selectedDocumentInstance.getProperties().get(
							DocumentProperties.TYPE);
					// REVIEW zashto se pravi tazi proverka pri prikachvane na
					// nova versiq za syshtestvuvasht file?
					if (documentService.canUploadDocumentInSection(sectionForSelectedDocument,
							attachmentType)) {
						selectedDocumentInstance = documentService.createDocumentInstance(
								sectionForSelectedDocument, attachmentType);
					}
				}
			}

			if (selectedDocumentInstance == null) {
				log.warn("!!!   NULL document instance on upload document   !!!");
				return;
			}

			Map<String, Serializable> properties = selectedDocumentInstance.getProperties();
			tempDir = updateDocumentModel(item, properties, selectedDocumentInstance);

			// if file is created - upload should continue
			if (tempDir != null) {
				RuntimeConfiguration.setConfiguration(
						RuntimeConfigurationProperties.UPLOAD_SESSION_FOLDER, tempDir);

				List<DocumentInstance> toUpload = new ArrayList<DocumentInstance>();
				for (UploadPostProcessor nextProcessor : postProcessors) {
					toUpload.addAll(nextProcessor.proccess(selectedDocumentInstance));
				}

				selectedDocumentInstance.getProperties().remove(DocumentProperties.THUMBNAIL_IMAGE);
				DocumentInstance[] documentInstances = toUpload
						.toArray(new DocumentInstance[toUpload.size()]);
				documentService.upload(sectionForSelectedDocument, true, documentInstances);

				RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
				instanceService.save(caseInstance, new Operation(ActionTypeConstants.UPLOAD));
			}

			afterUploadAction();

			acceptedFileType = null;
			isFirstVersion = true;
			setDocumentInstance(null);
			setSectionInstance(null);

		} catch (Exception e) {
			errorOnUpload = true;
			log.error("Exception during file upload!", e);
		} finally {
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.UPLOAD_SESSION_FOLDER);
			tempFileProvider.deleteFile(tempDir);
			if (errorOnUpload || (tempDir == null)) {
				// if failed to upload reload the instance
				reloadCaseInstance();
			}
		}
	}

	/**
	 * Fills the properties of DocumentInstance on upload event.
	 * 
	 * @param item
	 *            is the uploaded item
	 * @param properties
	 *            The document instance properties map.
	 * @param documentInstance
	 *            the document instance to upload to
	 * @return the created temp dir on null on some error
	 * @throws IOException
	 *             on error
	 */
	private File updateDocumentModel(org.richfaces.model.UploadedFile item,
			Map<String, Serializable> properties, DocumentInstance documentInstance)
			throws IOException {

		UploadedFile uploadedFile = new UploadedFile();
		uploadedFile.setLength(item.getSize());
		uploadedFile.setName(item.getName());
		uploadedFile.setMimetype(item.getContentType());
		uploadedFile.setData(item.getInputStream());

		Instance sectionInstance = documentInstance.getOwningInstance();
		File parentFolder = tempFileProvider.createTempDir("Upload-"
				+ ((OwnedModel) sectionInstance).getOwningInstance().getId().toString()
						.replace(':', '-') + "-"
				+ sectionInstance.getId().toString().replace(':', '-') + "-"
				+ System.currentTimeMillis());
		File storageDir = updateDocumentModelOnUpload(uploadedFile, properties, documentInstance,
				parentFolder, log);

		if (isFirstVersion) {
			properties.put(DocumentProperties.DESCRIPTION, getDescription());
			properties.put(DocumentProperties.TITLE, getTitle());
			setNewVersionDescription(getDescription());
		}

		properties.put(DocumentProperties.VERSION_DESCRIPTION, getNewVersionDescription());
		properties.put(DocumentProperties.IS_MAJOR_VERSION, Boolean.valueOf(getIsMajorVersion()));
		files.add(uploadedFile);

		setDescription(null);
		setNewVersionDescription(null);
		setIsMajorVersion(false);
		setTitle(null);
		return storageDir;
	}

	/**
	 * Method to update the document properties before upload.
	 * 
	 * @param uploadedFile
	 *            is the bean holding uploaded data
	 * @param properties
	 *            are the current instance properties
	 * @param documentInstance
	 *            is the currently uploaded instance
	 * @param parentFolder
	 *            is the dir where to store instance
	 * @param log
	 *            is custom logger
	 * @return the parentFolder on success
	 * @throws IOException
	 *             if any error occurs
	 */
	static File updateDocumentModelOnUpload(UploadedFile uploadedFile,
			Map<String, Serializable> properties, DocumentInstance documentInstance,
			File parentFolder, Logger log) throws IOException {

		// check if created
		if (parentFolder == null) {
			log.error("\n==================================\n\tDocument will not be uploaded because: No temporary directory"
					+ "\n==================================");
			return null;
		}
		String finalFileName = new File(uploadedFile.getName()).getName();
		int lastSeparator = finalFileName.lastIndexOf(File.separatorChar);
		if (lastSeparator > 0) {
			finalFileName = finalFileName.substring(lastSeparator + 1, finalFileName.length());
		}
		File localFile = null;
		if (finalFileName.getBytes("UTF-8").length > 255) {
			String extension = FileUtil.splitNameAndExtension(finalFileName).getSecond();
			if (StringUtils.isNullOrEmpty(extension)) {
				extension = ".tmp";
			}
			localFile = new File(parentFolder, System.nanoTime() + "." + extension);
		} else {
			localFile = new File(parentFolder, finalFileName);
		}
		// copy the file now
		BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(localFile));
		InputStream inputStream = uploadedFile.getData();
		long size = IOUtils.copyLarge(inputStream, output);

		log.debug("CMFWeb: UploadAction.updateDocumentModelOnUpload( ) size " + size + " "
				+ uploadedFile.getLength());

		IOUtils.closeQuietly(inputStream);
		IOUtils.closeQuietly(output);

		properties.put(DocumentProperties.FILE_LOCATOR, new LocalProxyFileDescriptor(finalFileName,
				localFile.getAbsolutePath()));
		properties.put(DocumentProperties.FILE_SIZE,
				humanReadableByteCount(uploadedFile.getLength()));

		return parentFolder;
	}

	/**
	 * Converts bytes into human readable format.
	 * 
	 * @param bytes
	 *            bytes to convert.
	 * @return human readable string.
	 */
	public static String humanReadableByteCount(long bytes) {
		int unit = 1000;
		if (bytes < unit) {
			return bytes + " B";
		}
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = "kMGTPE".charAt(exp - 1) + "";
		return String.format("%.1f%sB", Double.valueOf(bytes / Math.pow(unit, exp)), pre);
	}

	/**
	 * Gets item type count in file upload menu behind each menu item.
	 * 
	 * @param original
	 *            string
	 * @return extracted count as string
	 */
	public String getItemCount(String original) {
		String result = EMPTY_STRING;
		if (StringUtils.isNullOrEmpty(original)) {
			result = EMPTY_STRING;
		} else {
			int leftIndex = original.lastIndexOf("(");
			int rightIndex = original.lastIndexOf(")");
			if ((leftIndex > 0) && (rightIndex > 0)) {
				result = original.substring(leftIndex, rightIndex + 1);
			}
		}
		return " " + result;
	}

	/**
	 * Gets the files.
	 * 
	 * @return the files
	 */
	public ArrayList<UploadedFile> getFiles() {
		return files;
	}

	/**
	 * Sets the files.
	 * 
	 * @param files
	 *            the new files
	 */
	public void setFiles(ArrayList<UploadedFile> files) {
		this.files = files;
	}

	/**
	 * Gets the uploads available.
	 * 
	 * @return the uploads available
	 */
	public int getUploadsAvailable() {
		return uploadsAvailable;
	}

	/**
	 * Sets the uploads available.
	 * 
	 * @param uploadsAvailable
	 *            the new uploads available
	 */
	public void setUploadsAvailable(int uploadsAvailable) {
		this.uploadsAvailable = uploadsAvailable;
	}

	/**
	 * Getter method for newVersionDescription.
	 * 
	 * @return the newVersionDescription
	 */
	public String getNewVersionDescription() {
		return newVersionDescription;
	}

	/**
	 * Setter method for newVersionDescription.
	 * 
	 * @param newVersionDescription
	 *            the newVersionDescription to set
	 */
	public void setNewVersionDescription(String newVersionDescription) {
		this.newVersionDescription = newVersionDescription;
	}

	/**
	 * Getter method for isMajorVersion.
	 * 
	 * @return the isMajorVersion
	 */
	public boolean getIsMajorVersion() {
		return isMajorVersion;
	}

	/**
	 * Setter method for isMajorVersion.
	 * 
	 * @param isMajorVersion
	 *            the isMajorVersion to set
	 */
	public void setIsMajorVersion(boolean isMajorVersion) {
		this.isMajorVersion = isMajorVersion;
	}

	/**
	 * Getter method for acceptedFileType.
	 * 
	 * @return the acceptedFileType
	 */
	public String getAcceptedFileType() {
		return acceptedFileType;
	}

	/**
	 * Setter method for acceptedFileType.
	 * 
	 * @param acceptedFileType
	 *            the acceptedFileType to set
	 */
	public void setAcceptedFileType(String acceptedFileType) {
		this.acceptedFileType = acceptedFileType;
	}

	/**
	 * Getter method for documentInstance.
	 * 
	 * @return the documentInstance
	 */
	public DocumentInstance getDocumentInstance() {
		return documentInstance;
	}

	/**
	 * Setter method for documentInstance.
	 * 
	 * @param documentInstance
	 *            the documentInstance to set
	 */
	public void setDocumentInstance(DocumentInstance documentInstance) {
		this.documentInstance = documentInstance;
	}

	/**
	 * Getter method for isFirstVersion.
	 * 
	 * @return the isFirstVersion
	 */
	public boolean isFirstVersion() {
		return isFirstVersion;
	}

	/**
	 * Setter method for isFirstVersion.
	 * 
	 * @param isFirstVersion
	 *            the isFirstVersion to set
	 */
	public void setFirstVersion(boolean isFirstVersion) {
		this.isFirstVersion = isFirstVersion;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterUploadAction() {
		// TODO Auto-generated method stub

	}

}
