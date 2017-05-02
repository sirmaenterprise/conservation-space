package com.sirma.itt.emf.cls.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.easymock.EasyMock;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.commons.utils.date.DateUtils;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.cls.entity.Code;
import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.cls.entity.CodeValueDescription;
import com.sirma.itt.emf.cls.event.CodeListPersistEvent;
import com.sirma.itt.emf.cls.persister.SheetParserImpl;
import com.sirma.itt.emf.cls.retriever.CodeValueSearchCriteria;
import com.sirma.itt.emf.cls.retriever.SearchResult;
import com.sirma.itt.emf.cls.validator.XLSValidator;
import com.sirma.itt.emf.cls.validator.XLSValidatorImpl;
import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.dao.InstanceDao;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.db.transaction.TransactionHelper;
import de.akquinet.jbosscc.needle.db.transaction.VoidRunnable;
import de.akquinet.jbosscc.needle.junit.DatabaseRule;
import de.akquinet.jbosscc.needle.junit.NeedleRule;
import jxl.Sheet;

/**
 * Tests the {@link CodeValue} retrieving of {@link CodeListService} using EasyMock and Needle.
 *
 * @see http://needle.spree.de/
 * @author Mihail Radkov
 */
public class CodeValueServiceImplTest {

	@Rule
	public DatabaseRule databaseRule = new DatabaseRule();

	@Rule
	public NeedleRule needleRule = new NeedleRule(databaseRule);

	/** Entity manager provided by Needle. */
	private final EntityManager em = databaseRule.getEntityManager();

	@Inject
	private InstanceDao codeValueInstanceDao;

	@Inject
	private InstanceDao codeListInstanceDao;

	private EventService eventService;

	@ObjectUnderTest(implementation = CodeListServiceImpl.class)
	private CodeListService service;

	/** Hibernate's session */
	private final Session session = (Session) em.getDelegate();

	/** Hibernate's statistics */
	private final Statistics stat = session.getSessionFactory().getStatistics();

	/**
	 * Executed before every test.
	 *
	 * @throws Exception
	 *             if while filling the data base a problem occurs
	 */
	@Before
	public void before() throws Exception {
		loadDataBase();
		setupMock();
		stat.clear();

		// Mock the event service, used later to check if the events are actually thrown.
		eventService = Mockito.mock(EventService.class);
		ReflectionUtils.setField(service, "eventService", eventService);
	}

	/**
	 * Tests the save new codevalue functionality.
	 *
	 * @throws CodeListException
	 */
	@Test
	public void saveCodeValue() throws CodeListException {
		CodeValue codeValue = new CodeValue();
		codeValue.setExtra1("extra1test");
		codeValue.setValue("codeValueTestzz70Y");

		service.saveCodeValue(codeValue);

		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setIds(Arrays.asList("codeValueTestzz70Y"));

		SearchResult result = service.getCodeValues(criteria);
		assertEquals("extra1test", result.getResults().get(0).getExtra1());
	}

	/**
	 * Tries to save a new code value for an expired code list.
	 *
	 * @throws CodeListException
	 *             if the code list is expired
	 */
	@Test(expected = CodeListException.class)
	public void saveCodeValueForExpiredCodeList() throws CodeListException {
		// We know that code list "2" is expired. Try to create a new code value
		// for it
		CodeValue codeValue = new CodeValue();
		codeValue.setCodeListId("2");
		codeValue.setValue("codeValueTest");
		service.saveCodeValue(codeValue);
	}

	/**
	 * Tries to update a code value for an expired code list.
	 *
	 * @throws CodeListException
	 *             if the code list is expired
	 */
	@Test(expected = CodeListException.class)
	public void updateCodeValueForExpiredCodeList() throws CodeListException {
		// We know that code value PRJ10001 has an expired parent code list. Try
		// to update it.
		CodeValue codeValue = new CodeValue();
		codeValue.setCodeListId("2");
		codeValue.setValue("PRJ10001");
		codeValue.setExtra1("some test");
		service.updateCodeValue(codeValue);
	}

	/**
	 * Tests the update codelist functionality.
	 *
	 * @throws CodeListException
	 */
	@Test
	public void updateCodeValue() throws CodeListException {

		CodeValue newVersion = new CodeValue();
		newVersion.setCodeListId("227");
		newVersion.setValue("TSTYPE09");
		newVersion.setExtra1("extra1test");
		service.updateCodeValue(newVersion);

		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setCodeListId("227");
		criteria.setIds(Arrays.asList("TSTYPE09"));
		SearchResult result = service.getCodeValues(criteria);
		assertEquals("extra1test", result.getResults().get(1).getExtra1());
	}

	/**
	 * Tests the logic for adding/deleting code value descriptions.
	 *
	 * @throws CodeListException
	 */
	@Test
	public void updateCodeValueDescriptions() throws CodeListException {
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setCodeListId("227");
		criteria.setIds(Arrays.asList("TSTYPE09"));
		CodeValue retrievedCodeValue = (CodeValue) service.getCodeValues(criteria).getResults().get(0);
		// add a new description
		CodeValueDescription desc = new CodeValueDescription();
		desc.setLanguage("RO");
		desc.setDescription("Test description");
		retrievedCodeValue.getDescriptions().add(desc);
		service.updateCodeValue(retrievedCodeValue);
		retrievedCodeValue = (CodeValue) service.getCodeValues(criteria).getResults().get(0);
		if (retrievedCodeValue.getDescriptions().size() != 3) {
			Assert.fail();
		}
		// delete the newly added description
		retrievedCodeValue.getDescriptions().remove(2);
		service.updateCodeValue(retrievedCodeValue);
		retrievedCodeValue = (CodeValue) service.getCodeValues(criteria).getResults().get(0);
		assertEquals(2, retrievedCodeValue.getDescriptions().size());
	}

	/**
	 * Tries to update an expired code value.
	 *
	 * @throws CodeListException
	 *             if the code value is already expired and can't be edited
	 */
	@Test(expected = CodeListException.class)
	public void updateExpiredCodeValue() throws CodeListException {
		// we know that the code value "DELETED" is expired, so we use it for
		// the test
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setIds(Arrays.asList("DELETED"));
		SearchResult result = service.getCodeValues(criteria);
		CodeValue expiredCodeValue = (CodeValue) result.getResults().get(0);
		expiredCodeValue.setExtra1("extra1test");
		service.updateCodeValue(expiredCodeValue);
	}

	/**
	 * Try to update a codelist that already has an updated future version.
	 *
	 * @throws CodeListException
	 */
	@Test
	public void updateCodeValueWithExistingFutureVersion() throws CodeListException {

		CodeValue newVersion = new CodeValue();
		List<CodeValueDescription> descriptions = new ArrayList<>();
		CodeValueDescription bgDescr = new CodeValueDescription();
		bgDescr.setLanguage("BG");
		descriptions.add(bgDescr);
		newVersion.setCodeListId("227");
		newVersion.setValue("TSTYPE09");
		newVersion.setExtra1("extra1test");
		newVersion.setDescriptions(descriptions);
		service.updateCodeValue(newVersion);

		// TODO create a new codevalue object instead of persisting this one
		// again to test it correctly !
		newVersion.setExtra1("extranotherupdate");
		service.updateCodeValue(newVersion);

		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setCodeListId("227");
		criteria.setIds(Arrays.asList("TSTYPE09"));
		SearchResult result = service.getCodeValues(criteria);
		result = service.getCodeValues(criteria);
		assertEquals(2, result.getResults().size());
		assertEquals("extranotherupdate", result.getResults().get(1).getExtra1());
	}

	/**
	 * Tests the retrieving of all code values with empty search criteria.
	 */
	@Test
	public void retrieveAllCodeValues() {
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		SearchResult result = service.getCodeValues(criteria);
		assertNotNull(result);
		assertEquals(180, result.getResults().size());

		assertFalse(hasJoins(stat));
	}

	/**
	 * Tests the retrieval of specific code values by creating a complex criteria with code list value "227", extra 2
	 * that begins with "qertyu1*" and code values that begins with "tstype" and ends with "1".
	 */
	@Test
	public void retrieveSpecificCodeValues() {
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setCodeListId("227");
		criteria.setExtra2(Arrays.asList("qertyu1*"));
		criteria.setIds(Arrays.asList("tstype*1"));

		SearchResult result = service.getCodeValues(criteria);
		assertNotNull(result);
		assertEquals(2, result.getResults().size());
		assertEquals("TSTYPE01", result.getResults().get(0).getValue());
		assertEquals("TSTYPE11", result.getResults().get(1).getValue());

		assertFalse(hasJoins(stat));
	}

	/**
	 * Test a search criteria for code values that will not return any results.
	 */
	@Test
	public void retrieveNonExistentCodeValues() {
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setExtra1(Arrays.asList("testing testing testing dark side"));

		SearchResult result = service.getCodeValues(criteria);
		assertNotNull(result);
		assertEquals(0, result.getResults().size());

		assertFalse(hasJoins(stat));
	}

	/**
	 * Tests the retrieving of code values by description criteria with different characters case.
	 */
	@Test
	public void retrieveCodeValuesByDescription() {
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setDescriptions(Arrays.asList("apProvE"));

		SearchResult result = service.getCodeValues(criteria);
		assertNotNull(result);
		assertEquals(1, result.getResults().size());
		assertEquals("TR0002", result.getResults().get(0).getValue());
		// JEE7
		// assertTrue(hasJoins(stat));
	}

	/**
	 * Tests the correct retrieving of code values without date range criteria criteria for code list "1".
	 */
	@Test
	public void retrieveWithoutDateRange() {
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setCodeListId("1");

		SearchResult result = service.getCodeValues(criteria);

		assertFalse(result.getResults().isEmpty());
		assertEquals(8, result.getResults().size());
		assertFalse(hasJoins(stat));

		assertTrue(exists(result.getResults(), "IN_PROGRESS"));
	}

	/**
	 * Test the correct retrieving of code values with validity start date criteria for code list "1".
	 */
	@Test
	public void retrieveWithStartDate() {
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setCodeListId("1");
		criteria.setFromDate(DateUtils.createDate(2014, 4, 4));

		SearchResult result = service.getCodeValues(criteria);
		assertFalse(result.getResults().isEmpty());
		assertEquals(7, result.getResults().size());
		assertFalse(hasJoins(stat));

		assertFalse(exists(result.getResults(), "DELETED"));
	}

	/**
	 * Test the correct retrieving of code values with validity end date criteria for code list "1".
	 */
	@Test
	public void retrieveWithEndDate() {
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setCodeListId("1");
		criteria.setToDate(DateUtils.createDate(2014, 1, 1));

		SearchResult result = service.getCodeValues(criteria);

		assertFalse(result.getResults().isEmpty());
		assertEquals(4, result.getResults().size());

		assertFalse(exists(result.getResults(), "SUBMITTED"));
		assertFalse(exists(result.getResults(), "APPROVED"));
		assertFalse(exists(result.getResults(), "IN_PROGRESS"));
		assertFalse(exists(result.getResults(), "DELETED"));

		assertFalse(hasJoins(stat));
	}

	/**
	 * Test the correct retrieving of code values with validity start date and end date criteria for code list "1".
	 */
	@Test
	public void retrieveWithStartAndEndDate() {
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setCodeListId("1");
		criteria.setFromDate(DateUtils.createDate(2014, 1, 2));
		criteria.setToDate(DateUtils.createDate(2014, 3, 3));

		SearchResult result = service.getCodeValues(criteria);

		assertFalse(result.getResults().isEmpty());
		assertEquals(8, result.getResults().size());
		assertFalse(hasJoins(stat));
	}

	/**
	 * Test the correct retrieving of code values with an offset. Test for correct total number of items and correct
	 * results size.
	 */
	@Test
	public void retrieveWithOffset() {
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setCodeListId("227");
		criteria.setOffset(7);

		SearchResult result = service.getCodeValues(criteria);
		assertFalse(result.getResults().isEmpty());
		assertEquals(24, result.getTotal());
		assertEquals(17, result.getResults().size());

		assertEquals("TSTYPE08", result.getResults().get(0).getValue());
	}

	/**
	 * Test the correct retrieving of code values with an offset which will load the last page. Test for correct total
	 * number of items and correct results size.
	 */
	@Test
	public void retrieveWithOffsetLastPage() {
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setCodeListId("227");
		criteria.setOffset(20);
		criteria.setLimit(5);

		SearchResult result = service.getCodeValues(criteria);
		assertFalse(result.getResults().isEmpty());
		assertEquals(24, result.getTotal());
		assertEquals(4, result.getResults().size());

		assertEquals("TSTYPE21", result.getResults().get(0).getValue());
	}

	/**
	 * Test the correct retrieving of code values with an offset and limit. Test for correct total number of items and
	 * correct results size.
	 */
	@Test
	public void retrieveWithOffsetAndLimit() {
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setCodeListId("227");
		criteria.setOffset(8);
		criteria.setLimit(5);

		SearchResult result = service.getCodeValues(criteria);
		assertFalse(result.getResults().isEmpty());
		assertEquals(24, result.getTotal());
		assertEquals(5, result.getResults().size());

		assertEquals("TSTYPE09", result.getResults().get(0).getValue());
	}

	/**
	 * Tests the save new codevalue functionality.
	 *
	 * @throws CodeListException
	 */
	@Test
	public void testIfCodeValueEventIsFiredOnSave() throws CodeListException {
		CodeValue codeValue = new CodeValue();
		codeValue.setValue("testIfCodeValueEventIsFired1");
		service.saveCodeValue(codeValue);

		codeValue = new CodeValue();
		codeValue.setValue("testIfCodeValueEventIsFired2");
		service.saveCodeValue(codeValue);

		Mockito.verify(eventService, Mockito.times(2)).fire(Mockito.any(CodeListPersistEvent.class));
	}

	@Test
	public void testIfCodeValueEventIsFiredOnUpdate() throws CodeListException {
		CodeValue newVersion = new CodeValue();
		newVersion.setCodeListId("227");
		newVersion.setValue("TSTYPE09");
		newVersion.setExtra1("extra1test");
		service.updateCodeValue(newVersion);
		Mockito.verify(eventService).fire(Mockito.any(CodeListPersistEvent.class));
	}


	/**
	 * Gets the queries out of the sesion's statistics and checks for the presence of any joins in the first query.
	 *
	 * @param st
	 *            the statistics
	 * @return true if there is a join or false if not
	 */
	private boolean hasJoins(Statistics st) {
		String[] queries = st.getQueries();
		String lastQuery = queries[0];
		return lastQuery.contains("join");
	}

	/**
	 * Checks if provided string value exists in list of {@link CodeValue} as value.
	 *
	 * @param codeLists
	 *            the list of {@link CodeValue}
	 * @param value
	 *            the provided value to be checked
	 * @return true if exists in the list or false if not
	 */
	// TODO: Make it generic and move it in utility class.
	private boolean exists(List<? extends Code> codeLists, String value) {
		for (Code cl : codeLists) {
			if (value.equals(cl.getValue())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Imitates the low level behavior of {@link InstanceDao#loadInstancesByDbKey()}. Extracts {@link CodeList} from a
	 * list where all {@link CodeList} are kept after persisting into the test database.
	 *
	 * @param ids
	 *            the IDs of {@link CodeList} to be extracted
	 * @return list of {@link CodeList}
	 */
	private List<CodeList> getCodeLists(List<Long> ids) {
		if (ids.size() != 0) {
			TypedQuery<CodeList> query = em.createQuery("select cl from CodeList cl where cl.id in :ids",
					CodeList.class);
			query.setParameter("ids", ids);

			return query.getResultList();
		}
		List<CodeList> emptyList = new ArrayList<>();
		return emptyList;
	}

	/**
	 * Imitates the low level behavior of {@link InstanceDao#loadInstancesByDbKey()}. Extracts {@link CodeValue} from a
	 * list where all {@link CodeValue} are kept after persisting into the test database.
	 *
	 * @param ids
	 *            the IDs of {@link CodeValue} to be extracted
	 * @return list of {@link CodeValue}
	 */
	private List<CodeValue> getCodeValues(List<Long> ids) {
		if (ids.size() != 0) {
			TypedQuery<CodeValue> query = em.createQuery("select cv from CodeValue cv where cv.id in :ids",
					CodeValue.class);
			query.setParameter("ids", ids);
			return query.getResultList();
		}
		List<CodeValue> emptyList = new ArrayList<>();
		return emptyList;
	}

	/**
	 * Overrides the {@link InstanceDao#loadInstancesByDbKey()} mock's method to return list of {@link CodeList}
	 * corresponding to the provided list of IDs.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setupMock() {
		EasyMock
				.expect(codeListInstanceDao.loadInstancesByDbKey(EasyMock.anyObject(List.class), EasyMock.anyBoolean()))
					.andAnswer(() -> {
						List<Long> ids = (List<Long>) EasyMock.getCurrentArguments()[0];
						return getCodeLists(ids);
					})
					.anyTimes();
		EasyMock
				.expect(codeValueInstanceDao.loadInstancesByDbKey(EasyMock.anyObject(List.class)))
					.andAnswer(() -> {
						List<Long> ids = (List<Long>) EasyMock.getCurrentArguments()[0];
						return getCodeValues(ids);
					})
					.anyTimes();
		EasyMock
				.expect(codeValueInstanceDao.saveEntity(EasyMock.anyObject(Entity.class)))
					.andAnswer(() -> {
						TransactionHelper helper = databaseRule.getTransactionHelper();
						helper.executeInTransaction(new VoidRunnable() {
							@Override
							public void doRun(EntityManager entityManager) throws Exception {
								CodeValue cv = (CodeValue) EasyMock.getCurrentArguments()[0];
								CodeValue newCv = em.merge(cv);
								em.persist(newCv);
							}
						});
						return EasyMock.getCurrentArguments()[0];
					})
					.anyTimes();
		EasyMock.replay(codeValueInstanceDao);
	}

	/**
	 * Fills the database with valid test code lists and values. They are retrieved as input stream and then validated
	 * and parsed. Finally they are persisted in the database in one transaction.
	 *
	 * @throws Exception
	 *             if while preparing the test date a problem occurs
	 */
	private void loadDataBase() throws Exception {
		XLSValidator validator = new XLSValidatorImpl();
		InputStream is = getTestFileAsStream("valid-codelists.xls");

		Sheet codeListSheet = validator.getValidatedCodeListSheet(is);
		SheetParserImpl sheetParser = new SheetParserImpl();
		final List<CodeList> testCodeLists = sheetParser.parseXLS(codeListSheet).getCodeLists();

		TransactionHelper helper = databaseRule.getTransactionHelper();
		helper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(EntityManager entityManager) throws Exception {
				for (CodeList cl : testCodeLists) {
					em.persist(cl);
					for (CodeValue cv : cl.getCodeValues()) {
						em.persist(cv);
					}
				}
			}
		});
	}

	/**
	 * Returns a file as input stream based on its path after this class's upper package.
	 *
	 * @param filePath
	 *            the file's path
	 * @return the file as stream
	 */
	private InputStream getTestFileAsStream(String filePath) {
		return CodeValueServiceImplTest.class.getResourceAsStream("../" + filePath);
	}
}
