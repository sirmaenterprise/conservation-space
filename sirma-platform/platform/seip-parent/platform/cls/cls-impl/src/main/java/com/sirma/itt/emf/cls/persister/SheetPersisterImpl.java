package com.sirma.itt.emf.cls.persister;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.cls.event.CodeListTruncateEvent;
import com.sirma.itt.emf.cls.event.CodeListUploadEvent;
import com.sirma.itt.emf.cls.service.CodeListManagementService;
import com.sirma.itt.emf.cls.util.ClsUtils;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.cls.parser.CodeListSheet;

/**
 * Class for persisting code lists and values from excel sheet into DB.
 *
 * @author Nikolay Velkov
 */
@Singleton
public class SheetPersisterImpl implements SheetPersister {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private CodeListManagementService codeListManagementService;

	@Inject
	private EventService eventService;

	@Inject
	private TransactionSupport support;

	@Override
	public void persist(CodeListSheet sheet) {
		ClsUtils.formatCodeAttributesAndDescriptions(sheet.getCodeLists());

		delete();
		sheet.getCodeLists().forEach(codeListManagementService::saveCodeList);
		// After the transaction has completed fire the event with the persisted data.
		support.invokeOnSuccessfulTransaction(() -> {
			LOGGER.debug("Code lists were persisted.");
			eventService.fire(new CodeListUploadEvent(sheet.getCodeLists()));
		});
	}

	@Override
	public void delete() {
		codeListManagementService.deleteAvailableCodes();
		eventService.fire(new CodeListTruncateEvent());
	}
}
