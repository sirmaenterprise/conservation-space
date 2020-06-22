package com.sirma.itt.emf.audit.db;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;
import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.util.ReflectionUtils;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.db.transaction.TransactionHelper;
import de.akquinet.jbosscc.needle.db.transaction.VoidRunnable;
import de.akquinet.jbosscc.needle.junit.DatabaseRule;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Tests {@link AuditDao} service for persisting and retrieving of {@link AuditActivity}.
 *
 * @author Mihail Radkov
 * @author Nikolay Velkov
 */
public class AuditDaoTest {

	/** The database rule. */
	@Rule
	public DatabaseRule databaseRule = new DatabaseRule();

	/** The needle rule. */
	@Rule
	public NeedleRule needleRule = new NeedleRule(databaseRule);

	@Mock
	private DatabaseIdManager idManager;

	/** Entity manager provided by Needle. */
	private final EntityManager em = databaseRule.getEntityManager();

	@ObjectUnderTest(implementation = AuditDbDao.class)
	private DbDao dbDao = new AuditDbDao();

	/** The service. */
	private AuditDao service;

	/**
	 * Truncate the db, reset the index and republish the database records before every test.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void before() throws Exception {
		MockitoAnnotations.initMocks(this);
		service = new AuditDaoImpl();
		ReflectionUtils.setFieldValue(service, "dbDao", dbDao);

		Mockito.when(idManager.isPersisted(Matchers.any(Entity.class))).thenReturn(false);
		ReflectionUtils.setFieldValue(dbDao, "idManager", idManager);
		TransactionHelper helper = databaseRule.getTransactionHelper();
		helper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(EntityManager entityManager) throws Exception {
				em.createNativeQuery("TRUNCATE SCHEMA PUBLIC RESTART IDENTITY AND COMMIT NO CHECK").executeUpdate();
				for (int i = 0; i < 5; i++) {
					service.publish(getTestActivity(i));
				}
			}
		});
	}

	/**
	 * Try to publish an activity and see if it is persisted to the db.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testPublish() throws Exception {
		TransactionHelper helper = databaseRule.getTransactionHelper();
		helper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(EntityManager entityManager) throws Exception {
				service.publish(getTestActivity(5));
			}
		});

		List<AuditActivity> activities = em
				.createQuery("select aa from AuditActivity aa", AuditActivity.class)
					.getResultList();
		assertEquals(6, activities.size());
		AuditActivity activity = activities.get(5);
		assertEquals("Object ID5", activity.getObjectID());

	}

	/**
	 * Try to get some activities by their ids.
	 */
	@Test
	public void getActivitiesByIds() {
		ServiceResult activities = service.getActivitiesByIDs(Arrays.asList(1L, 2L, 3L));
		assertEquals(3, activities.getRecords().size());
		AuditActivity activity = activities.getRecords().get(1);
		assertEquals("Object ID1", activity.getObjectID());
	}

	/**
	 * Tests if the passed ID order is preserved after retrieving from the data base.
	 */
	@Test
	public void testOrder() {
		ServiceResult activities = service.getActivitiesByIDs(Arrays.asList(3L, 1L, 2L));
		assertEquals(3, activities.getRecords().size());
		assertEquals(new Long(3L), activities.getRecords().get(0).getId());
		assertEquals(new Long(1L), activities.getRecords().get(1).getId());
		assertEquals(new Long(2L), activities.getRecords().get(2).getId());
	}

	/**
	 * Tests retrieving of missing activities.
	 */
	@Test
	public void testMissingActivities() {
		ServiceResult activities = service.getActivitiesByIDs(Arrays.asList(999L));
		assertEquals(0, activities.getRecords().size());
	}

	/**
	 * Try to get some activities from the db without specifying ids. Should return an empty list.
	 */
	@Test
	public void getActivitiesByIdsEmpty() {
		ServiceResult result = service.getActivitiesByIDs(new ArrayList<Long>());
		assertEquals(result.getTotal(), 0);
	}

	/**
	 * Try to persist a null activity.
	 */
	@Test
	public void testPersistNull() {
		service.publish(null);
		List<AuditActivity> activities = em
				.createQuery("select aa from AuditActivity aa", AuditActivity.class)
					.getResultList();
		assertEquals(activities.size(), 5);
	}

	/**
	 * Initializes a test audit activity.
	 *
	 * @param i
	 *            the i
	 * @return the test activity
	 */
	private AuditActivity getTestActivity(int i) {
		AuditActivity activity = new AuditActivity();

		activity.setObjectID("Object ID" + i);

		return activity;
	}
}
