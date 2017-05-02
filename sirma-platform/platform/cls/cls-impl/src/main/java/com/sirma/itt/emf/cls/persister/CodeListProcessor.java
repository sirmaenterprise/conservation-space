package com.sirma.itt.emf.cls.persister;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.cls.db.ClsQueries;
import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeListSheet;
import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.cls.event.CodeListTruncateEvent;
import com.sirma.itt.emf.cls.event.CodeListUploadEvent;
import com.sirma.itt.emf.cls.validator.CodeListValidator;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Class for persisting code lists and values from excel sheet into DB.
 *
 * @author Nikolay Velkov
 */
@ApplicationScoped
public class CodeListProcessor implements XLSProcessor {

	/** The entity manager used for persisting code lists. */
	@PersistenceContext(unitName = DbDao.PERSISTENCE_UNIT_NAME, type = PersistenceContextType.TRANSACTION)
	private EntityManager em;

	/** Logs information about this class's actions. */
	private static final Logger LOGGER = Logger.getLogger(CodeListProcessor.class);

	/** Data access object for working with code lists. */
	@Inject
	@InstanceType(type = "CodeListInstance")
	private InstanceDao codeListInstanceDao;

	/** Data access object for working with code values. */
	@Inject
	@InstanceType(type = "CodeValueInstance")
	private InstanceDao codeValueInstanceDao;

	/** Service used for firing events. */
	@Inject
	private EventService eventService;

	@Inject
	private TransactionSupport support;

	/**
	 * Parse, validate and persist a {@link CodeListSheet} with code lists and values.
	 *
	 * @param sheet
	 *            the {@link CodeListSheet} to be parsed
	 * @throws PersisterException
	 *             the persister exception, thrown when something goes wrong with the codelist validation
	 */
	@Override
	@Transactional(TxType.REQUIRED)
	public void persistSheet(CodeListSheet sheet) throws PersisterException {
		CodeListValidator validator = new CodeListValidator();
		validator.validateCodeLists(sheet.getCodeLists());
		// Тruncate the cls tables.
		deleteCodeLists();
		// Iterate the values from the excel sheet and persist each one.
		sheet.getCodeLists().stream().forEach(codeList ->
			{
				codeListInstanceDao.saveEntity(codeList);
				codeList.getCodeValues().forEach(codeValueInstanceDao::saveEntity);
			});
		// After the transaction has completed fire the event with the persisted data.
		support.invokeOnSuccessfulTransaction(() ->
			{
				LOGGER.debug("Code lists were persisted.");
				eventService.fire(new CodeListUploadEvent(sheet.getCodeLists()));
			});
	}

	@Override
	public CodeListSheet mergeSheets(Collection<CodeListSheet> sheets) {
		CodeListSheet result = new CodeListSheet();
		for (CodeListSheet sheet : sheets) {
			for (CodeList codeList : sheet.getCodeLists()) {
				if (!result.getCodeLists().contains(codeList)) {
					result.addCodeList(codeList);
				} else {
					CodeList resultCodeList = result
							.getCodeLists()
								.stream()
								.filter(code -> code.getValue().equals(codeList.getValue()))
								.findFirst()
								.get();
					LOGGER.debug("Merging codelist values from cl" + codeList.getValue());
					mergeCodeValues(resultCodeList.getCodeValues(), codeList.getCodeValues());
				}
			}
		}
		return result;
	}

	private static void mergeCodeValues(Collection<CodeValue> target, Collection<CodeValue> source) {
		for (CodeValue value : source) {
			if (target.contains(value)) {
				target.remove(value);
			}
			target.add(value);
		}
	}

	@Override
	public void deleteCodeLists() {
		em.createNativeQuery(ClsQueries.CLS_TRUNCATE).executeUpdate();
		eventService.fire(new CodeListTruncateEvent());
		LOGGER.debug("DB was truncated.");
	}
}
