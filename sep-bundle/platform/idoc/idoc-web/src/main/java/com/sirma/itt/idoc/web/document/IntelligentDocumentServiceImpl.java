/**
 * Copyright (c) 2013 30.07.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.idoc.web.document;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import com.sirma.itt.cmf.beans.ByteArrayFileDescriptor;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.VersionInfo;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.DocumentInfoOperation;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Implements the methods defined in {@link IntelligentDocumentService}.
 *
 * @author Adrian Mitev
 */
@Stateless
public class IntelligentDocumentServiceImpl implements IntelligentDocumentService {

	private static final Operation CREATE_IDOC = new Operation(ActionTypeConstants.CREATE_IDOC);

	@Inject
	private CaseService caseInstanceService;

	@Inject
	private DocumentService documentService;

	@Inject
	private AuthenticationService authenticationService;

	@Inject
	private PropertiesService propertiesService;

	@Inject
	private ServiceRegister serviceRegister;

	/** The instance service. */
	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	@Override
	public DocumentInstance create(Serializable caseId, Serializable sectionId, String definitionId) {
		CaseInstance caseInstance = caseInstanceService.loadByDbId(caseId);
		// find the relevant section
		SectionInstance sectionInstance = null;
		for (SectionInstance current : caseInstance.getSections()) {
			if (current.getId().equals(sectionId)) {
				sectionInstance = current;
				break;
			}
		}

		return documentService.createDocumentInstance(sectionInstance, definitionId);
	}

	@Override
	public DocumentInstance save(DocumentInstance documentInstance, String content) {
		Map<String, Serializable> properties = documentInstance.getProperties();
		setDocumentContent(documentInstance.getProperties(), content);

		if (!SequenceEntityGenerator.isPersisted(documentInstance)) {
			// set to identify the document as "intelligent"
			documentInstance.setPurpose(IntelligentDocumentProperties.DOCUMENT_PURPOSE);

			properties.put(DocumentProperties.MIMETYPE, "text/html");

			// save document and then attach it to the section
			documentService.save(documentInstance, CREATE_IDOC);
			instanceService.attach(documentInstance.getOwningInstance(), CREATE_IDOC,
					documentInstance);

			RuntimeConfiguration.setConfiguration(
					RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN, Boolean.TRUE);

			CaseInstance caseInstance = InstanceUtil
					.getParent(CaseInstance.class, documentInstance);
			try {
				// update the case without saving children to mark it as modified
				caseInstanceService.save(caseInstance, CREATE_IDOC);
			} finally {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			}
		}

		return documentInstance;
	}

	@Override
	public DocumentInstance save(Serializable id, String content, String title) {
		DocumentInstance documentInstance = documentService.loadByDbId(id);
		if (documentInstance.isLocked()) {
			String currentUserId = authenticationService.getCurrentUserId();
			String lockedBy = (String) documentInstance.getProperties().get(
					DocumentProperties.LOCKED_BY);
			if (currentUserId.equals(lockedBy)) {
				documentService.unlock(documentInstance, false);
				documentInstance.getProperties().put(DocumentProperties.TITLE, title);
				setDocumentContent(documentInstance.getProperties(), content);

				documentService.save(documentInstance, new Operation(ActionTypeConstants.UPLOAD));
				return documentInstance;
			}
		}
		return documentInstance;
	}

	@Override
	public Instance save(Class<? extends Instance> type, Serializable id, Serializable documentId,
			String content, String title) {
		InstanceService<Instance, DefinitionModel> service = serviceRegister
				.getInstanceService(type);
		Instance instance = service.loadByDbId(id);
		// REVIEW - why null TODO
		return null;
	}

	@Override
	public void updateProperties(Serializable id, Map<String, Serializable> properties) {
		DocumentInstance documentInstance = documentService.loadByDbId(id);
		documentInstance.getProperties().putAll(properties);

		propertiesService.saveProperties(documentInstance);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public DocumentInstance load(Serializable id, Serializable caseId, Serializable sectionId) {
		CaseInstance caseInstance = caseInstanceService.loadByDbId(caseId);
		// find the relevant section
		SectionInstance sectionInstance = null;
		for (SectionInstance current : caseInstance.getSections()) {
			if (current.getId().equals(sectionId)) {
				sectionInstance = current;
				break;
			}
		}

		DocumentInstance documentInstance = null;
		for (Instance current : sectionInstance.getContent()) {
			if (current.getId().equals(id)) {
				documentInstance = (DocumentInstance) current;
				break;
			}
		}

		return documentInstance;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String loadContent(DocumentInstance documentInstance) {
		InputStream contentStream = documentService.getContentStream(documentInstance);
		if (contentStream != null) {
			try {
				return IOUtils.toString(contentStream, "UTF-8");
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<VersionInfo> getVersions(DocumentInstance documentInstance) {
		Set<DocumentInfoOperation> set = Collections.synchronizedSet(EnumSet
				.of(DocumentInfoOperation.DOCUMENT_VERSIONS));

		Map<DocumentInfoOperation, Serializable> info = documentService.getDocumentInfo(
				documentInstance, set);
		return (List<VersionInfo>) info.get(DocumentInfoOperation.DOCUMENT_VERSIONS);
	}

	@Override
	public DocumentInstance loadVersion(Serializable documentId, String version) {
		DocumentInstance instance = documentService.loadByDbId(documentId);
		if (instance != null) {
			DocumentInstance documentVersion = documentService
					.getDocumentVersion(instance, version);
			return documentVersion;
		}
		return null;
	}

	/**
	 * Sets the document content in document properties using a file locator so the new content will
	 * be saved.
	 *
	 * @param properties
	 *            document properties.
	 * @param content
	 *            content to set.
	 */
	private void setDocumentContent(Map<String, Serializable> properties, String content) {
		try {
			byte[] contentAsArray = content.getBytes("UTF-8");
			properties.put(DocumentProperties.FILE_LOCATOR, new ByteArrayFileDescriptor(
					(String) properties.get(DocumentProperties.NAME), contentAsArray));
			properties.put(DocumentProperties.FILE_SIZE, contentAsArray.length);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
