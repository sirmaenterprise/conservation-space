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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.commons.utils.date.DateUtils;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.cls.entity.Code;
import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.event.CodeListPersistEvent;
import com.sirma.itt.emf.cls.persister.SheetParserImpl;
import com.sirma.itt.emf.cls.retriever.CodeListSearchCriteria;
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
 * Tests the {@link CodeList} retrieving of {@link CodeListService} using EasyMock and Needle.
 *
 * @see http://needle.spree.de/
 * @author Mihail Radkov
 * @author Nikolay Velkov
 * @author Vilizar Tsonev
 */
public class CodeListServiceImplTest {

	@Rule
	public DatabaseRule databaseRule = new DatabaseRule();

	@Rule
	public NeedleRule needleRule = new NeedleRule(databaseRule);

	/** Entity manager provided by Needle. */
	private final EntityManager em = databaseRule.getEntityManager();

	@Inject
	private InstanceDao codeListInstanceDao;

	@ObjectUnderTest(implementation = CodeListServiceImpl.class)
	private CodeListService service;

	private EventService eventService;

	/** Hibernate's session */
	private final Session session = (Session) em.getDelegate();

	/** Hibernate's statistics */
	private final Statistics stat = session.getSessionFactory().getStatistics();

	/**
	 * Executed before every test. Prepares test database, mock ups and clears hibernate's statistics.
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
	 * Tests the save new codelist functionality.
	 *
	 * @throws CodeListException
	 */
	@Test
	public void saveCodeList() throws CodeListException {
		CodeList codeList = new CodeList();
		codeList.setDisplayType((short) 1);
		codeList.setExtra1("extra1test");
		codeList.setValue("codeListTestzz70Y");

		service.saveOrUpdateCodeList(codeList, false);

		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setIds(Arrays.asList("codeListTestzz70Y"));

		SearchResult result = service.getCodeLists(criteria);
		assertEquals(Short.valueOf("1"), ((CodeList) result.getResults().get(0)).getDisplayType());
		assertEquals("extra1test", result.getResults().get(0).getExtra1());
	}

	/**
	 * Tests the save new codelist functionality.
	 *
	 * @throws CodeListException
	 */
	@Test(expected = CodeListException.class)
	public void saveExistingCodeList() throws CodeListException {
		CodeList codeList = new CodeList();
		codeList.setDisplayType((short) 1);
		codeList.setExtra1("extra1test");
		codeList.setValue("1");

		service.saveOrUpdateCodeList(codeList, false);
	}

	/**
	 * Tests the update codelist functionality.
	 *
	 * @throws CodeListException
	 */
	@Test
	public void updateCodeList() throws CodeListException {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setIds(Arrays.asList("1"));

		SearchResult result = service.getCodeLists(criteria);
		CodeList codeList = (CodeList) result.getResults().get(0);
		codeList.setExtra1("extra1test");
		codeList.getDescriptions().remove(1);
		service.saveOrUpdateCodeList(codeList, true);

		result = service.getCodeLists(criteria);
		assertEquals("extra1test", result.getResults().get(0).getExtra1());
		assertEquals(1, codeList.getDescriptions().size());
	}

	/**
	 * Tries to update a codelist that does not exist in the database, meaning there is no codelist with that value in
	 * the database.
	 *
	 * @throws CodeListException
	 */
	@Test(expected = CodeListException.class)
	public void updateNonExistingCodeList() throws CodeListException {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setIds(Arrays.asList("1"));

		SearchResult result = service.getCodeLists(criteria);
		CodeList codeList = (CodeList) result.getResults().get(0);
		codeList.setValue("ulala");
		codeList.setExtra1("extra1test");
		service.saveOrUpdateCodeList(codeList, true);
	}

	/**
	 * Tries to edit an expired code list, which is a code list with validTo date before the current date.
	 *
	 * @throws CodeListException
	 *             if the code list is already expired and can't be edited
	 */
	@Test(expected = CodeListException.class)
	public void updateExpiredCodeList() throws CodeListException {
		// update some old codelist to be expired
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setIds(Arrays.asList("1"));
		SearchResult result = service.getCodeLists(criteria);
		CodeList expiredCodeList = (CodeList) result.getResults().get(0);
		expiredCodeList.setValidFrom(DateUtils.createDate(2012, 1, 1));
		expiredCodeList.setValidTo(DateUtils.createDate(2012, 2, 1));
		service.saveOrUpdateCodeList(expiredCodeList, true);
		// try to update the expired code list
		CodeList newCodeList = new CodeList();
		newCodeList.setValue("1");
		service.saveOrUpdateCodeList(expiredCodeList, true);
	}

	/**
	 * Tests the retrieving of all code lists with empty search criteria.
	 *
	 * @throws CodeListException
	 */
	@Test
	public void retrieveAll() throws CodeListException {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		SearchResult result = service.getCodeLists(criteria);
		// SearchResult result2 = service.getCodeLists(criteria);
		assertFalse(result.getResults().isEmpty());
		assertEquals(19, result.getTotal());
		assertEquals("1", result.getResults().get(0).getValue());
		assertEquals("238", result.getResults().get(18).getValue());
		assertFalse(hasJoins(stat));
	}

	/**
	 * Tests the retrieval of specific code lists by creating a complex criteria values that begins with "2" and applies
	 * an offset of 2 results.
	 */
	@Test
	public void retrieveSpecific() {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setIds(Arrays.asList("2*"));
		criteria.setLimit(2);

		SearchResult result = service.getCodeLists(criteria);

		assertFalse(result.getResults().isEmpty());
		assertEquals("2", result.getResults().get(0).getValue());
		assertEquals("200", result.getResults().get(1).getValue());
		// JEE7
		// assertTrue(hasJoins(stat));
	}

	/**
	 * Test a search criteria for code lists that will not return any results.
	 */
	@Test
	public void nonExistent() {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setExtra5(Arrays.asList("non existent extra in the code lists."));
		SearchResult result = service.getCodeLists(criteria);
		assertTrue(result.getResults().isEmpty());
		assertEquals(0, result.getTotal());
		assertFalse(hasJoins(stat));
	}

	/**
	 * Tests the retrieving of code lists by description criteria including special character.
	 */
	@Test
	public void retrieveByDescription() {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setDescriptions(Arrays.asList("*state*"));

		SearchResult result = service.getCodeLists(criteria);
		assertFalse(result.getResults().isEmpty());
		assertEquals(4, result.getTotal());
		assertEquals("1", result.getResults().get(0).getValue());
		assertEquals("101", result.getResults().get(1).getValue());
		assertEquals("102", result.getResults().get(2).getValue());
		assertEquals("106", result.getResults().get(3).getValue());

		for (Code cl : result.getResults()) {
			if (cl instanceof CodeList) {
				assertNotNull(((CodeList) cl).getDescriptions());
			}
		}
	}

	/**
	 * Tests the correct retrieving of code lists without date range criteria criteria.
	 */
	@Test
	public void retrieveWithoutDateRange() {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		SearchResult result = service.getCodeLists(criteria);

		assertFalse(result.getResults().isEmpty());
		assertEquals(19, result.getTotal());
		assertFalse(hasJoins(stat));
	}

	/**
	 * Test the correct retrieving of code lists with validity start date criteria.
	 */
	@Test
	public void retrieveWithStartDate() {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setFromDate(DateUtils.createDate(2014, 4, 4));

		SearchResult result = service.getCodeLists(criteria);
		assertFalse(result.getResults().isEmpty());
		assertEquals(18, result.getTotal());
		assertFalse(hasJoins(stat));

		assertFalse(exists(result.getResults(), "2"));
	}

	/**
	 * Test the correct retrieving of code lists with validity end date criteria.
	 */
	@Test
	public void retrieveWithEndDate() {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setToDate(DateUtils.createDate(2004, 1, 1));

		SearchResult result = service.getCodeLists(criteria);
		assertFalse(result.getResults().isEmpty());
		assertEquals(16, result.getTotal());

		assertFalse(exists(result.getResults(), "1"));
		assertFalse(exists(result.getResults(), "3"));
		assertFalse(exists(result.getResults(), "4"));

		assertFalse(hasJoins(stat));
	}

	/**
	 * Test the correct retrieving of code lists with validity start date and end date criteria.
	 */
	@Test
	public void retrieveWithStartAndEndDate() {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setFromDate(DateUtils.createDate(2014, 1, 2));
		criteria.setToDate(DateUtils.createDate(2014, 3, 3));

		SearchResult result = service.getCodeLists(criteria);
		assertFalse(result.getResults().isEmpty());
		assertEquals(17, result.getTotal());

		assertFalse(exists(result.getResults(), "2"));
		assertFalse(exists(result.getResults(), "4"));

		assertFalse(hasJoins(stat));
	}

	/**
	 * Test the correct retrieving of code lists with an offset. Test for correct total number of items and correct
	 * results size.
	 */
	@Test
	public void retrieveWithOffset() {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setOffset(7);

		SearchResult result = service.getCodeLists(criteria);
		assertFalse(result.getResults().isEmpty());
		assertEquals(19, result.getTotal());
		assertEquals(12, result.getResults().size());

		assertEquals("101", result.getResults().get(0).getValue());
	}

	/**
	 * Test the correct retrieving of code lists with an offset which will load the last page. Test for correct total
	 * number of items and correct results size.
	 */
	@Test
	public void retrieveWithOffsetLastPage() {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setOffset(15);
		criteria.setLimit(5);

		SearchResult result = service.getCodeLists(criteria);
		assertFalse(result.getResults().isEmpty());
		assertEquals(19, result.getTotal());
		assertEquals(4, result.getResults().size());

		assertEquals("229", result.getResults().get(0).getValue());
	}

	/**
	 * Test the correct retrieving of code lists with an offset and limit. Test for correct total number of items and
	 * correct results size.
	 */
	@Test
	public void retrieveWithOffsetAndLimit() {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setOffset(8);
		criteria.setLimit(5);

		SearchResult result = service.getCodeLists(criteria);
		assertFalse(result.getResults().isEmpty());
		assertEquals(19, result.getTotal());
		assertEquals(5, result.getResults().size());

		assertEquals("102", result.getResults().get(0).getValue());
	}

	@Test
	public void testIfCodeListPersistEventIsFired() throws CodeListException {
		CodeList codeList = new CodeList();
		codeList.setDisplayType((short) 1);
		codeList.setExtra1("extra1");
		codeList.setValue("cl1");

		service.saveOrUpdateCodeList(codeList, false);

		codeList = new CodeList();
		codeList.setDisplayType((short) 1);
		codeList.setExtra1("extra1");
		codeList.setValue("cl2");
		service.saveOrUpdateCodeList(codeList, false);

		Mockito.verify(eventService, Mockito.times(2)).fire(Mockito.any(CodeListPersistEvent.class));
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
	 * Checks if provided string value exists in list of {@link CodeList} as value.
	 *
	 * @param codeLists
	 *            the list of {@link CodeList}
	 * @param value
	 *            the provided value to be checked
	 * @return true if exists in the list or false if not
	 */
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
				.expect(codeListInstanceDao.saveEntity(EasyMock.anyObject(Entity.class)))
					.andAnswer(() -> {
						TransactionHelper helper = databaseRule.getTransactionHelper();
						helper.executeInTransaction(new VoidRunnable() {
							@Override
							public void doRun(EntityManager entityManager) throws Exception {
								CodeList cl = (CodeList) EasyMock.getCurrentArguments()[0];
								CodeList newCl = em.merge(cl);
								entityManager.persist(newCl);
								entityManager.persist(EasyMock.getCurrentArguments()[0]);
							}
						});
						return EasyMock.getCurrentArguments()[0];
					})
					.anyTimes();
		EasyMock.replay(codeListInstanceDao);
	}

	/**
	 * Fills the database with valid test code lists and values. They are retrieved as input stream and then validated
	 * and parsed. Finally they are persisted in the database in one transaction.
	 *
	 * @throws Exception
	 *             if while preparing the test database a problem occurs
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
					entityManager.persist(cl);
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
		return CodeListServiceImplTest.class.getResourceAsStream("../" + filePath);
	}

	// List<CodeList> bla = em.createQuery("select cl from CodeList cl",
	// CodeList.class)
	// .getResultList();
	// System.out.println(bla.size());
}
