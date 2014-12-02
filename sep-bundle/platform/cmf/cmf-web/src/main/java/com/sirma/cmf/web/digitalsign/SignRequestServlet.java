package com.sirma.cmf.web.digitalsign;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sirma.cmf.web.upload.EmfFileItemFactory;
import com.sirma.cmf.web.upload.FileItemDescriptor;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.io.TempFileProvider;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * The SignRequestServlet is proxy to download/upload sign resources to dms.
 * 
 * @author Ivo Rusev
 * @author BBonev
 */
public class SignRequestServlet extends HttpServlet {

	private static final Operation EDIT_DETAILS = new Operation(ActionTypeConstants.EDIT_DETAILS);
	private static final Operation SIGN = new Operation(ActionTypeConstants.SIGN);
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(SignRequestServlet.class);
	/** The Constant debug. */
	private static final boolean debug = LOGGER.isDebugEnabled();

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The temp file provider. */
	@Inject
	private TempFileProvider tempFileProvider;

	/** The content service. */
	@Inject
	private DocumentService documentService;

	/** The case service. */
	@Inject
	private CaseService caseService;

	/** The label provider. */
	@Inject
	private LabelProvider labelProvider;

	/** The authentication service. */
	@Inject
	private AuthenticationService authenticationService;

	/** The event service. */
	@Inject
	private EventService eventService;

	/** The file item factory. */
	@Inject
	private javax.enterprise.inject.Instance<EmfFileItemFactory> fileItemFactory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		processGet(req, resp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		processPost(req, resp);
	}

	/**
	 * Process post request, that is supposed to be upload operation. In header should be provided
	 * the 'documentid' that represents the dmsid for document. The multipart request is parsed and
	 * the field name is used for the new filename.
	 * 
	 * @param req
	 *            the req to process
	 * @param resp
	 *            the response to generate.
	 */
	@Secure
	public void processPost(HttpServletRequest req, HttpServletResponse resp) {
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);
		if (isMultipart) {
			LOGGER.debug("Got a sign process request but was not a multipart request!");
			return;
		}
		// get current user in order to execute the action with it.
		User currentUser = authenticationService.getCurrentUser();
		if (currentUser == null) {
			throw new EmfRuntimeException("User not logged in!");
		}
		String parameter = req.getHeader("documentid");
		if ((parameter == null) || parameter.trim().isEmpty()) {
			throw new EmfRuntimeException("Invalid document or no document was provided!");
		}
		if (debug) {
			LOGGER.debug("Trying to load file for signing: " + parameter);
		}
		CaseInstance caseInstance = findContainingCase(parameter);
		DocumentInstance documentInstance = findDocumentInstance(caseInstance, parameter);

		// Create a new file upload handler
		EmfFileItemFactory itemFactory = fileItemFactory.get();
		File repository = itemFactory.getRepository();
		ServletFileUpload upload = new ServletFileUpload(itemFactory);
		try {
			// iterate over data
			List<?> items = upload.parseRequest(req);
			for (Object el : items) {
				if (!(el instanceof FileItem)) {
					LOGGER.warn("Not a fileitem " + el);
				}
				FileItem item = (FileItem) el;
				if (!item.isFormField()) {
					FileDescriptor descriptor = new FileItemDescriptor(item.getFieldName(), item,
							itemFactory);
					setFileDataToInstance(documentInstance, descriptor);

					saveChanges(documentInstance, caseInstance);
				}
			}
		} catch (FileUploadException e) {
			LOGGER.error("Failed parse the sign upload request", e);
			throw new EmfRuntimeException(e);
		} finally {
			tempFileProvider.deleteFile(repository);
		}
	}

	/**
	 * Sets the file data to instance.
	 * 
	 * @param documentInstance
	 *            the target document instance
	 * @param signedFile
	 *            a file descriptor with the signed file
	 */
	private void setFileDataToInstance(DocumentInstance documentInstance, FileDescriptor signedFile) {

		// set the new version data
		documentInstance.getProperties().put(DocumentProperties.IS_MAJOR_VERSION, Boolean.FALSE);
		documentInstance.getProperties().put(DocumentProperties.VERSION_DESCRIPTION,
				labelProvider.getValue("document.sign.auto.description"));
		documentInstance.getProperties().put(DocumentProperties.FILE_LOCATOR, signedFile);
		// mark as signed using this prop
		documentInstance.getProperties().put(DocumentProperties.DOCUMENT_SIGNED_DATE, new Date());
		// indicate that we a signing
		documentInstance.getProperties().put(DocumentProperties.DOCUMENT_SIGNED, Boolean.TRUE);

		documentInstance.getProperties().put(DocumentProperties.DOCUMENT_SIGNED_BY,
				authenticationService.getCurrentUser());
	}

	/**
	 * Find containing case.
	 * 
	 * @param parameter
	 *            the parameter
	 * @return the case instance
	 */
	private CaseInstance findContainingCase(String parameter) {
		CaseInstance caseInstance = caseService.getPrimaryCaseForDocument(parameter);
		if (caseInstance == null) {
			throw new EmfRuntimeException("Document is not recognized in the system!");
		}
		return caseInstance;
	}

	/**
	 * Find document instance.
	 * 
	 * @param caseInstance
	 *            the case instance
	 * @param parameter
	 *            the parameter
	 * @return the document instance
	 */
	private DocumentInstance findDocumentInstance(CaseInstance caseInstance, String parameter) {
		DocumentInstance loadInstance = null;
		for (SectionInstance sectionInstance : caseInstance.getSections()) {
			for (Instance documentInstance : sectionInstance.getContent()) {
				if ((documentInstance instanceof DocumentInstance)
						&& EqualsHelper.nullSafeEquals(((DocumentInstance) documentInstance).getDmsId(),
								parameter)) {
					loadInstance = (DocumentInstance) documentInstance;
					break;
				}
			}
			if (loadInstance != null) {
				break;
			}
		}
		// this should not happen on this step
		if (loadInstance == null) {
			throw new EmfRuntimeException("Document is not recognized in the system!");
		}
		return loadInstance;
	}

	/**
	 * Save changes to the document and case
	 * 
	 * @param document
	 *            the document instance to save
	 * @param caseInstance
	 *            the case instance
	 */
	private void saveChanges(DocumentInstance document, CaseInstance caseInstance) {
		// save the document
		final DocumentInstance instance = document;
		// is this required to call it in this context when it should be the same as logged in user
		SecurityContextManager.callAs(authenticationService.getCurrentUser(),
				new Callable<DocumentInstance>() {
			@Override
			public DocumentInstance call() throws Exception {
				return documentService.save(instance, SIGN);
			}
		});
		try {
			RuntimeConfiguration.setConfiguration(
					RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN, Boolean.TRUE);
			caseService.save(caseInstance, EDIT_DETAILS);
			// fire event that the document was signed successfully
			AfterDigitalSignEvent afterDigitalSignEvent = new AfterDigitalSignEvent();
			afterDigitalSignEvent.setCaseInstance(caseInstance);
			afterDigitalSignEvent.setDocumentInstance(instance);
			eventService.fire(afterDigitalSignEvent);
		} finally {
			RuntimeConfiguration
					.clearConfiguration(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
		}
	}

	/**
	 * Process the request.
	 * 
	 * @param req
	 *            HttpServletRequest.
	 * @param resp
	 *            HttpServletResponse.
	 */
	@Secure
	public void processGet(HttpServletRequest req, HttpServletResponse resp) {
		DocumentInstance doc = new DocumentInstance();
		String parameter = req.getHeader("documentid");
		if ((parameter == null) || parameter.trim().isEmpty()) {
			throw new EmfRuntimeException("Invalid document is provided!");
		}
		doc.setDmsId(parameter);
		doc.setProperties(new HashMap<String, Serializable>(1));
		File content = documentService.getContent(doc);
		try (InputStream input = new FileInputStream(content);
				OutputStream writer = resp.getOutputStream()) {
			resp.setCharacterEncoding("UTF-8");
			resp.reset();
			IOUtils.copyLarge(input, writer);
			writer.flush();
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

}
