package com.sirma.itt.cmf.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.testutil.BaseArquillianCITest;
import com.sirma.itt.cmf.testutil.CmfTestResourcePackager;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.event.instance.InstanceCreateEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * The Class CaseServiceCITest is responsible to test all the methods in {@link CaseService}.
 *
 * @author bbanchev
 */
public class CaseServiceCITest extends BaseArquillianCITest {

	/** The case service. */
	@Inject
	private CaseService caseService;

	/** The created instance. */
	private static CaseInstance createdInstance;

	/** The fired events. */
	private static Map<Class<?>, Instance> firedEvents = new HashMap<>();

	/**
	 * Creates the deployment.
	 *
	 * @return the web archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		System.out.println("Starting test: " + CaseServiceCITest.class);
		return defaultBuilder(new CmfTestResourcePackager()).packageWar();
	}

	/**
	 * Test create instance.
	 */
	@Test(enabled = true)
	public void testCreateInstance() {
		CaseDefinition defintions = getDefinition(CaseInstance.class, DEFAULT_DEFINITION_ID_CASE);

		createdInstance = caseService.createInstance(defintions, null);
		Assert.assertNotNull(createdInstance);
		assertEquals(createdInstance.getIdentifier(), DEFAULT_DEFINITION_ID_CASE,
				"Definition should be set");
		Assert.assertEquals(firedEvents.get(InstanceCreateEvent.class), createdInstance);
		Assert.assertNotNull(createdInstance.getProperties() != null);
		Assert.assertNotNull(createdInstance.getSections() != null);
		Assert.assertEquals(createdInstance.getSections().size(), 3);
	}

	/**
	 * Batch load case instance.
	 */
	@Test(enabled = false)
	public void batchLoadCaseInstance() {
		fail("Test not implemented");
	}

	/**
	 * Close case instance.
	 */
	@Test(enabled = false)
	public void closeCaseInstance() {
		fail("Test not implemented");
	}

	/**
	 * Delete.
	 */
	@Test(enabled = false)
	public void delete() {
		fail("Test not implemented");
	}

	/**
	 * Test get the primary case for document.
	 */
	@Test(enabled = false)
	public void getPrimaryCaseForDocument() {
		fail("Test not implemented");
	}

	/**
	 * Save.
	 */
	@Test(enabled = false)
	public void save() {
		fail("Test not implemented");
	}

	/**
	 * Save case instance. Check the dms integration, section and properties persistence.
	 */
	@Test(enabled = true, dependsOnMethods = "testCreateInstance")
	public void saveCaseInstance() {

		assertTrue(createdInstance.getDmsId() == null, "Dms id should be null");
		CaseInstance saved = caseService.save(createdInstance, new Operation(
				ActionTypeConstants.CREATE_CASE));
		assertTrue(saved.getId() != null, "Id should not be null");
		assertTrue(saved.getIdentifier() != null, "Idintefier should not be null");
		String caseDmsId = saved.getDmsId();
		assertTrue(caseDmsId != null, "Dms id should not be null");
		Assert.assertEquals(firedEvents.get(BeforeInstancePersistEvent.class), createdInstance);
		Assert.assertEquals(firedEvents.get(AfterInstancePersistEvent.class), saved);
		Assert.assertEquals(firedEvents.get(InstanceChangeEvent.class), saved);
		// remove cached
		firedEvents.remove(InstanceChangeEvent.class);
		List<SectionInstance> sections = saved.getSections();
		assertTrue(sections.size() > 0, "Should contain sections");
		for (SectionInstance sectionInstance : sections) {
			assertEquals(sectionInstance.getDmsId(), caseDmsId,
					"Dms id should not be null for section");
		}
		String newTitle = "Case is edited";
		saved.getProperties().put(CaseProperties.TITLE, newTitle);
		caseService.save(saved, new Operation(ActionTypeConstants.EDIT_DETAILS));
		assertEquals(saved.getDmsId(), caseDmsId, "Dms id should be the same after update");
		assertEquals(saved.getProperties().get(CaseProperties.TITLE), newTitle,
				"Title should be updated");
		Assert.assertEquals(firedEvents.get(InstanceChangeEvent.class), saved);
	}

	/**
	 * On case before persist.
	 *
	 * @param event
	 *            the event
	 */
	public void onCaseBeforePersist(@Observes BeforeInstancePersistEvent<Instance, ?> event) {
		if (event.getInstance() instanceof CaseInstance) {
			firedEvents.put(BeforeInstancePersistEvent.class, event.getInstance());
		}
	}

	/**
	 * On case after persist.
	 *
	 * @param event
	 *            the event
	 */
	public void onCaseAfterPersist(@Observes AfterInstancePersistEvent<Instance, ?> event) {
		if (event.getInstance() instanceof CaseInstance) {
			firedEvents.put(AfterInstancePersistEvent.class, event.getInstance());
		}
	}

	/**
	 * On case created.
	 *
	 * @param event
	 *            the event
	 */
	public void onCaseCreated(@Observes InstanceCreateEvent<Instance> event) {
		if (event.getInstance() instanceof CaseInstance) {
			firedEvents.put(InstanceCreateEvent.class, event.getInstance());
		}
	}

	/**
	 * On case change.
	 *
	 * @param event
	 *            the event
	 */
	public void onCaseChange(@Observes InstanceChangeEvent<Instance> event) {
		if (event.getInstance() instanceof CaseInstance) {
			firedEvents.put(InstanceChangeEvent.class, event.getInstance());
		}
	}

	/**
	 * Verify case instance.
	 */
	@Test(enabled = false)
	public void verifyCaseInstance() {
		fail("Test not implemented");
	}

}
