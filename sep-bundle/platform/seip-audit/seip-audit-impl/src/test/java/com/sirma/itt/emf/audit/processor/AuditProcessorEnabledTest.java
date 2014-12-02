package com.sirma.itt.emf.audit.processor;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.emf.audit.TestUtils;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.db.AuditDao;
import com.sirma.itt.emf.audit.db.AuditDaoImpl;

import de.akquinet.jbosscc.needle.annotation.InjectInto;
import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.DatabaseRule;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Test the logic in {@link AuditProcessor} when it is enabled.
 * 
 * @author Mihail Radkov
 */
public class AuditProcessorEnabledTest {

	/** The database rule. */
	@Rule
	public DatabaseRule databaseRule = new DatabaseRule();

	/** The needle rule. */
	@Rule
	public NeedleRule needleRule = new NeedleRule(databaseRule);

	/** Entity manager provided by Needle. */
	private EntityManager em = databaseRule.getEntityManager();

	/** The Audit DAO service. It is injected into {@link AuditProcessor}. */
	@ObjectUnderTest(implementation = AuditDaoImpl.class)
	@InjectInto(targetComponentId = "ap")
	private AuditDao service;

	@ObjectUnderTest(id = "ap", implementation = AuditProcessorImpl.class)
	private AuditProcessor processor;

	/** Enabling the {@link AuditProcessor}. */
	@InjectInto(targetComponentId = "ap")
	private Boolean enabled = true;

	/**
	 * Tests the processing of {@link AuditActivity} when {@link AuditProcessor} is enabled.
	 * 
	 * @throws Exception
	 *             if while operating with the database a problem occurs
	 */
	@Test
	public void testProcessing() throws Exception {
		Date date = new Date();
		AuditActivity activity = new AuditActivity();
		activity.setEventDate(date);
		TestUtils.executeInTransaction(databaseRule, processor, activity);

		List<AuditActivity> res = TestUtils.getAllActivities(em);
		assertEquals(1, res.size());
		assertEquals(date, res.get(0).getEventDate());
	}

}
