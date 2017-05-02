package com.sirma.itt.seip.content.ocr;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.content.event.ContentUpdatedEvent;
import com.sirma.itt.seip.content.ocr.status.OCRStatus;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.tasks.Schedule;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * The Class OCRService responsible for OCR-ing the uploaded documents in system.
 *
 * @author Hristo Lungov
 */
@Singleton
public class OCRService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	static final String NAME = "OCRService";
	static final String SOURCE_ID = "sourceId";
	static final String MIMETYPE = "mimetype";
	static final String OCR_STATUS_PROP = "emf:ocrStatus";
	static final String OCR_CONTENT_PROP = "emf:ocrContent";
	static final String OCR_QUERY = ResourceLoadUtil.loadResource(OCRService.class, "ocrQuery.sparql");
	static final String OCR_QUERY_STATUS_PARAM = "notStarted";

	@Inject
	@ExtensionPoint(OCREngine.TARGET_NAME)
	private Plugins<OCREngine> ocrEngines;

	@Inject
	private SearchService searchService;

	@Inject
	private InstanceService instanceService;

	@Inject
	private LockService lockService;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private TransactionSupport transactionSupport;

	/**
	 * Ocr Schedule used to check if there are available documents in "Not Started" status and to Start OCR Process. The
	 * period of time is specified by cron configuration expresion.
	 */
	@RunAsAllTenantAdmins
	@Schedule(identifier = NAME, transactionMode = TransactionMode.NOT_SUPPORTED)
	@ConfigurationPropertyDefinition(name = "ocr.schedule.cron", defaultValue = "0 0/1 * ? * *", system = true, sensitive = true, label = "Cron expression that defines when to execute the ocr schedule")
	public void ocrProcess() {
		SearchArguments<Instance> ocrSearchFilter = searchService.getFilter(OCR_QUERY, Instance.class, null);
		ocrSearchFilter.getArguments().put(OCR_QUERY_STATUS_PARAM, OCRStatus.NOT_STARTED.toString());
		searchService.search(Instance.class, ocrSearchFilter);
		doOcr(ocrSearchFilter.getResult());
	}

	/**
	 * Method to start/force OCR of instances manually. Normal OCR process is triggered by {@link ContentUpdatedEvent}
	 *
	 * @see com.sirma.itt.seip.content.ocr.OCRService#doOcr(com.sirma.itt.seip.domain.instance.Instance)
	 *
	 * @param instances
	 *            the instances
	 */
	public void doOcr(Collection<Instance> instances) {
		instances.forEach(instance -> doOcr(instance));
	}

	/**
	 * On content update observer used to observe for update of documents and reset the OCR Status to "Not Started"
	 *
	 * @param event
	 *            the ContentUpdatedEvent
	 */
	public void onContentUpdate(@Observes ContentUpdatedEvent event) {
		Serializable instance = event.getOwner();
		if (instance instanceof Instance) {
			Serializable instanceId = ((Instance) instance).getId();
			Instance loadedInstance = instanceService.loadByDbId(instanceId);
			if (loadedInstance != null && loadedInstance.isUploaded()) {
				LockInfo lockStatus = lockService.lockStatus(loadedInstance.toReference());
				if (lockStatus.isLocked()) {
					return;
				}
				loadedInstance.add(OCR_STATUS_PROP, OCRStatus.NOT_STARTED.toString());
				try {
					Options.DISABLE_AUDIT_LOG.enable();
					instanceService.save(loadedInstance, null);
				} finally {
					Options.DISABLE_AUDIT_LOG.disable();
				}
			}
		}
	}

	/**
	 * Method to start/force OCR of instances manually. Normal OCR process is triggered by {@link ContentUpdatedEvent}.
	 * OCR and get the text content from the given Instance {@link FileDescriptor}. The actual OCR will be done by
	 * {@link OCREngine} that supports the given mimetype of the source content.
	 *
	 * The text extracted from applicable OCR Engine will be saved in "ocrContent" property.
	 *
	 * @param instance
	 *            the instance
	 */
	public void doOcr(Instance instance) {
		Instance loadedInstance = instanceService.loadByDbId(instance.getId());
		TimeTracker tracker = TimeTracker.createAndStart();
		InstanceReference reference = loadedInstance.toReference();
		LockInfo lockSt = lockService.lockStatus(reference);
		if (lockSt.isLocked()) {
			return;
		}
		try {
			ContentInfo contentInfo = instanceContentService.getContent(instance.getId(), Content.PRIMARY_CONTENT);
			if (contentInfo == null || !contentInfo.exists()) {
				LOGGER.warn("Missing Primary Content this instance should not be here.");
				loadedInstance.add(OCR_STATUS_PROP, OCRStatus.EXCLUDED.toString());
			} else {
				Optional<String> ocrText = ocrEngines
						.stream()
							.filter(ocrEngine -> ocrEngine.isApplicable(contentInfo.getMimeType()))
							.map(ocrEngine -> callOcrEngine(ocrEngine, contentInfo.getMimeType(), contentInfo))
							.filter(Objects::nonNull)
							.findFirst();
				if (ocrText.isPresent()) {
					LOGGER.trace("Extracted Text after OCR: {}", ocrText.get());
					loadedInstance.add(OCR_STATUS_PROP, OCRStatus.COMPLETED.toString());
					loadedInstance.add(OCR_CONTENT_PROP, ocrText.get());
				} else {
					loadedInstance.add(OCR_STATUS_PROP, OCRStatus.EXCLUDED.toString());
				}
			}
		} catch (Exception e) {
			LOGGER.error("OCR failed because: {}, will set ocr status: {}.", e.getMessage(), OCRStatus.FAILED.toString(), e);
			loadedInstance.add(OCR_STATUS_PROP, OCRStatus.FAILED.toString());
		} finally {
			transactionSupport.invokeInNewTx(() -> {
				LockInfo lockStatus = lockService.lockStatus(reference);
				if (!lockStatus.isLocked()) {
					try {
						Options.DISABLE_AUDIT_LOG.enable();
						instanceService.save(loadedInstance, null);
					} finally {
						Options.DISABLE_AUDIT_LOG.disable();
					}
				}
			});
			LOGGER.debug("Content ocr took {} ms", tracker.stop());
		}

	}

	private static String callOcrEngine(OCREngine ocrEngine, String mimeType, FileDescriptor descriptor) {
		try {
			return ocrEngine.doOcr(mimeType, descriptor);
		} catch (Exception e) {
			LOGGER.debug("Failed to ocr content for: {} ,using: {}", descriptor.getId(), ocrEngine, e);
			throw new EmfRuntimeException("Failed to ocr content for: " + descriptor.getId() + ",using: " + ocrEngine + " !");
		}
	}

}
