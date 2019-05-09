package com.sirma.itt.emf.cls.persister;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.cls.event.CodeListTruncateEvent;
import com.sirma.itt.emf.cls.event.CodeListUploadEvent;
import com.sirma.itt.emf.cls.service.CodeListManagementService;
import com.sirma.itt.emf.cls.util.ClsUtils;
import com.sirma.itt.emf.cls.validator.CodeValidator;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;
import com.sirma.sep.cls.parser.CodeListSheet;

/**
 * Class for persisting code lists and values from excel sheet into DB.
 *
 * @author Nikolay Velkov
 */
@Singleton
public class CodeListPersisterImpl implements CodeListPersister {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private CodeValidator codeValidator;

	@Inject
	private CodeListManagementService codeListManagementService;

	@Inject
	private EventService eventService;

	@Inject
	private TransactionSupport support;

	@Override
	public void override(CodeListSheet sheet) {
		List<CodeList> codeLists = sheet.getCodeLists();
		formatAndValidate(codeLists);
		delete();
		codeLists.forEach(codeListManagementService::saveCodeList);
		// After the transaction has completed fire the event with the persisted data.
		support.invokeOnSuccessfulTransaction(() -> {
			LOGGER.debug("Code lists were overridden.");
			eventService.fire(new CodeListUploadEvent(codeLists));
		});
	}

	@Override
	public void persist(CodeList codeList) {
		persist(Collections.singletonList(codeList));
	}

	@Override
	public void persist(List<CodeList> codeLists) {
		formatAndValidate(codeLists);
		codeLists.forEach(codeListManagementService::saveCodeList);
	}

	@Override
	public void persist(CodeValue codeValue) {
		formatAndValidate(codeValue);
		codeListManagementService.saveCodeValue(codeValue);
	}

	@Override
	public void delete() {
		codeListManagementService.deleteAvailableCodes();
		eventService.fire(new CodeListTruncateEvent());
	}

	private void formatAndValidate(List<CodeList> codeLists) {
		ClsUtils.formatCodeAttributesAndDescriptions(codeLists);
		codeValidator.validateCodeLists(codeLists);
	}

	private void formatAndValidate(CodeValue codeValue) {
		ClsUtils.formatCodeAttributesAndDescriptions(codeValue);
		codeValidator.validateCodeValue(codeValue);
	}
}
