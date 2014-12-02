package com.sirma.itt.emf.cls.persister;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import jxl.Sheet;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.cls.db.ClsQueries;
import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.cls.event.CodeListPersistEvent;
import com.sirma.itt.emf.cls.event.CodeListTruncateEvent;
import com.sirma.itt.emf.cls.event.CodeValuePersistEvent;
import com.sirma.itt.emf.cls.validator.CodeListValidator;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;

/**
 * Class for persisting code lists and values from excel sheet into DB.
 * 
 * @author Nikolay Velkov
 */
@Stateless
public class CodeListProcessor implements XLSProcessor {

	/** The entity manager used for persisting code lists. */
	@Inject
	private EntityManager em;

	/** Logs information about this class's actions. */
	private static final Logger LOGGER = Logger.getLogger(CodeListProcessor.class);

	/** Data access object for working with code lists. */
	@Inject
	@InstanceType(type = "CodeListInstance")
	private InstanceDao<CodeList> codeListInstanceDao;

	/** Data access object for working with code values. */
	@Inject
	@InstanceType(type = "CodeValueInstance")
	private InstanceDao<CodeValue> codeValueInstanceDao;

	/** Service used for firing events. */
	@Inject
	private EventService eventService;

	/**
	 * Parse, validate and persist an excel sheet with code lists and values.
	 * 
	 * @param sheet
	 *            the sheet to be parsed
	 * @throws PersisterException
	 *             the persister exception, thrown when something goes wrong with the codelist
	 *             validation
	 */
	@Override
	public void persistSheet(Sheet sheet) throws PersisterException {
		SheetParser sheetParser = new SheetParser();
		CodeListValidator validator = new CodeListValidator();
		List<CodeList> entities = sheetParser.parseXLS(sheet);
		validator.validateCodeLists(entities);
		deleteCodeLists();
		for (CodeList codeList : entities) {
			codeListInstanceDao.saveEntity(codeList);
			for (CodeValue codeValue : codeList.getCodeValues()) {
				codeValueInstanceDao.saveEntity(codeValue);
				eventService.fire(new CodeValuePersistEvent(codeValue));
			}
			eventService.fire(new CodeListPersistEvent(codeList));
		}
		LOGGER.debug("Code lists were persisted.");
	}

	/**
	 * Delete any existing code lists from DB using native query.
	 */
	private void deleteCodeLists() {
		em.createNativeQuery(ClsQueries.CLS_TRUNCATE).executeUpdate();
		eventService.fire(new CodeListTruncateEvent());
		LOGGER.debug("DB was truncated.");
	}
}
