package com.sirma.itt.cmf.services.observers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.event.cases.AfterCasePersistEvent;
import com.sirma.itt.cmf.event.document.AfterDocumentPersistEvent;
import com.sirma.itt.cmf.event.folder.AfterFolderPersistEvent;
import com.sirma.itt.cmf.services.adapter.CMFCaseInstanceAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.security.Secure;

/**
 * Observer for failed transaction events for CMF objects. The observer tries to delete from DMS the
 * successfully created instances if the overall transaction for creation failed.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Secure
public class CmfInstanceCreateRollbackObserver {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CmfInstanceCreateRollbackObserver.class);

	/** The case instance adapter service. */
	@Inject
	private CMFCaseInstanceAdapterService caseInstanceAdapterService;
	/** The adapter services. */
	@Inject
	private CMFDocumentAdapterService documentAdapterService;

	/** The generic adapter service. */
	@Inject
	private DMSInstanceAdapterService genericAdapterService;

	/**
	 * Deletes case on transaction fail.
	 * 
	 * @param event
	 *            the event
	 */
	public void onCaseRollback(
			@Observes(during = TransactionPhase.AFTER_FAILURE) AfterCasePersistEvent event) {
		LOGGER.debug("Trying to delete case from DMS on failed transaction for creation");
		CaseInstance instance = event.getInstance();
		if (StringUtils.isNotNullOrEmpty(instance.getDmsId())) {
			try {
				caseInstanceAdapterService.deleteCaseInstance(instance, true);
			} catch (DMSException e) {
				LOGGER.warn("Failed to delete case from DMS on failed transaction", e);
			}
		}
	}

	/**
	 * Deletes document on transaction fail
	 * 
	 * @param event
	 *            the event
	 */
	public void onDocumentRollback(
			@Observes(during = TransactionPhase.AFTER_FAILURE) AfterDocumentPersistEvent event) {
		LOGGER.debug("Trying to delete document from DMS on failed transaction for creation");
		DocumentInstance instance = event.getInstance();
		if (StringUtils.isNotNullOrEmpty(instance.getDmsId())
				|| Boolean.TRUE.equals(instance.getProperties().get(DefaultProperties.IS_IMPORTED))) {
			// does not delete imported instance - this is very bad
			return;
		}
		try {
			CaseInstance parent = InstanceUtil.getParent(CaseInstance.class, instance);
			if (parent != null) {
				instance.getProperties().put(DocumentProperties.CASE_DMS_ID,
						((DMSInstance) parent).getDmsId());
				documentAdapterService.deleteAttachment(instance);
			} else {
				genericAdapterService.deleteNode(instance);
			}
		} catch (DMSException e) {
			LOGGER.warn("Failed to delete document from DMS on failed transaction", e);
		}
	}

	/**
	 * Deletes folder on transaction fail
	 * 
	 * @param event
	 *            the event
	 */
	public void onFolderRollback(
			@Observes(during = TransactionPhase.AFTER_FAILURE) AfterFolderPersistEvent event) {
		LOGGER.debug("Trying to delete folder from DMS on failed transaction for creation");
		FolderInstance instance = event.getInstance();
		if (StringUtils.isNotNullOrEmpty(instance.getDmsId())
				|| Boolean.TRUE.equals(instance.getProperties().get(DefaultProperties.IS_IMPORTED))) {
			// does not delete imported instance - this is very bad
			return;
		}
		try {
			genericAdapterService.deleteNode(instance);
		} catch (DMSException e) {
			LOGGER.warn("Failed to delete folder from DMS on failed transaction", e);
		}
	}
}
