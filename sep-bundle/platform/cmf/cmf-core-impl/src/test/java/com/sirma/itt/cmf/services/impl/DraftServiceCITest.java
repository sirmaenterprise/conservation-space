package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.ByteArrayFileDescriptor;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.compile.TemplateDefinitionCompilerCallback;
import com.sirma.itt.cmf.beans.definitions.compile.TemplateDefinitionCompilerCallbackProxy;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefProxy;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.DraftInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.services.DraftService;
import com.sirma.itt.cmf.services.impl.dao.TemplateInstanceDao;
import com.sirma.itt.cmf.testutil.BaseArquillianCITest;
import com.sirma.itt.cmf.testutil.CmfTestResourcePackager;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * The DraftServiceCITest is testing the {@link DraftService} related methods
 *
 * @author bbanchev
 */
public class DraftServiceCITest extends BaseArquillianCITest {

	@Inject
	private DraftService draftService;

	/** The authentication service. */
	@Inject
	private AuthenticationService authenticationService;

	/** The document service. */
	@Inject
	private DocumentService documentService;

	/** The case service. */
	@Inject
	private CaseService caseService;

	/** The resource service. */
	@Inject
	private ResourceService resourceService;
	/** The intance service. */
	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> intanceService;

	/** The testable i doc. */
	private static DocumentInstance testableIDoc;

	/** The current draft title. */
	private static String currentDraftTitle;

	/** The current draft content. */
	private static String currentDraftContent;

	/** The current user. */
	private static EmfUser currentUser;

	/** The current date. */
	private static Date currentDate;

	/**
	 * Creates the deployment.
	 *
	 * @return the web archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		System.out.println("Starting test: " + DraftServiceCITest.class);
		return defaultBuilder(new CmfTestResourcePackager()).addClasess(
				DocumentTemplateServiceImpl.class, TemplateDefinitionCompilerCallback.class,
				TemplateDefinitionCompilerCallbackProxy.class, TemplateInstanceDao.class)
				.packageWar();
	}

	/**
	 * Test create draft instance.
	 */
	@Test(enabled = true)
	public void testCreateDraftInstance() {
		CaseInstance parentCase = createTestableCase();
		testableIDoc = createIdoc(parentCase);
		currentUser = resourceService.getResource(authenticationService.getCurrentUserId(),
				ResourceType.USER);
		authenticationService.setAuthenticatedUser(currentUser);
		DraftInstance create = draftService.create(testableIDoc, currentUser);
		currentDate = create.getCreatedOn();
		Assert.assertNotNull(currentDate);
		assertTrue(create != null, "Should have draft!");
		List<DraftInstance> drafts = draftService.getDrafts(testableIDoc);
		assertEquals(drafts.size(), 1, "Should have 1 draft!");
	}

	/**
	 * Test draft update.
	 */
	@Test(enabled = true, dependsOnMethods = "testCreateDraftInstance")
	public void testDraftInstanceData() {
		// test the initial draft
		List<DraftInstance> drafts = draftService.getDrafts(testableIDoc);
		assertEquals(drafts.size(), 1, "Should have 1 draft!");
		DraftInstance currentDraft = drafts.get(0);
		assertEquals(currentDraft.getDraftContent(), currentDraftContent, "Data should be stored");
		assertTrue(currentDraft.getDraftProperties().size() == 1, "Data should be stored");
		assertEquals(currentDraft.getCreatedOn(), currentDate, "Data should be stored");
		assertEquals(currentDraft.getCreator(), currentUser, "Data should be stored");
		assertEquals(currentDraft.getInstanceId(), testableIDoc.getId(), "Data should be stored");
		String changedDraftContent = "my second data";
		testableIDoc.getProperties().put(
				DocumentProperties.FILE_LOCATOR,
				new ByteArrayFileDescriptor(UUID.randomUUID().toString(), changedDraftContent
						.getBytes()));
		testableIDoc.getProperties().put(DefaultProperties.TITLE, "my title");
		documentService.save(testableIDoc, new Operation(ActionTypeConstants.EDIT_DETAILS));
		// after save 0 drafts
		drafts = draftService.getDrafts(testableIDoc);
		assertEquals(drafts.size(), 0, "Should have 0 drafts after save!");
		// create a new draft with changed title and content
		DraftInstance created = draftService.create(testableIDoc, currentUser);
		currentDraftContent = changedDraftContent;
		currentDate = created.getCreatedOn();
		Assert.assertNotNull(currentDate);
		drafts = draftService.getDrafts(testableIDoc);
		assertEquals(drafts.size(), 1, "Should have 1 draft!");
		currentDraft = drafts.get(0);
		assertEquals(currentDraft.getDraftContent(), currentDraftContent, "Data should be stored");
		assertTrue(currentDraft.getDraftProperties().size() == 1, "Data should be stored");
		assertEquals(currentDraft.getDraftProperties().get(DefaultProperties.TITLE), "my title",
				"Data should be stored");
		assertEquals(currentDraft.getCreatedOn(), currentDate, "Data should be stored");
		assertEquals(currentDraft.getCreator(), currentUser, "Data should be stored");
		assertEquals(currentDraft.getInstanceId(), testableIDoc.getId(), "Data should be stored");

		testableIDoc.getProperties().put(DefaultProperties.TITLE, "my title 2");
		documentService.save(testableIDoc, new Operation(ActionTypeConstants.EDIT_DETAILS));

		// assertEquals(currentDraft.getDraftContent(), currentDraftContent,
		// "Data should be stored");
		created = draftService.create(testableIDoc, currentUser);
		currentDate = created.getCreatedOn();
		Assert.assertNotNull(currentDate);
		drafts = draftService.getDrafts(testableIDoc);
		assertEquals(drafts.size(), 1, "Should have 1 draft!");
		currentDraft = drafts.get(0);
		assertEquals(currentDraft.getDraftContent(), currentDraftContent, "Data should be stored");
		assertTrue(currentDraft.getDraftProperties().size() == 1, "Data should be stored");
		assertEquals(currentDraft.getDraftProperties().get(DefaultProperties.TITLE), "my title 2",
				"Data should be stored");
		assertEquals(currentDraft.getCreatedOn(), currentDate, "Data should be stored");
		assertEquals(currentDraft.getCreator(), currentUser, "Data should be stored");
		assertEquals(currentDraft.getInstanceId(), testableIDoc.getId(), "Data should be stored");
	}

	/**
	 * Test draft delete.
	 */
	@Test(enabled = true, dependsOnMethods = "testDraftInstanceData")
	public void testDraftDelete() {
		// test the initial draft
		List<DraftInstance> drafts = draftService.getDrafts(testableIDoc);
		assertEquals(drafts.size(), 1, "Should have 1 draft!");
		draftService.delete(testableIDoc);
		drafts = draftService.getDrafts(testableIDoc);
		assertEquals(drafts.size(), 0, "Should have 0 draft!");
		// create a new draft
		draftService.create(testableIDoc, currentUser);
		drafts = draftService.getDrafts(testableIDoc);
		assertEquals(drafts.size(), 1, "Should have 1 draft!");
		draftService.delete(testableIDoc, currentUser);

		// test possibility to work with multiple
		draftService.create(testableIDoc, currentUser);
		draftService.create(testableIDoc,
				(EmfUser) resourceService.getResource("banchev", ResourceType.USER));
		draftService.create(testableIDoc,
				(EmfUser) resourceService.getResource("Consumer", ResourceType.USER));
		drafts = draftService.getDrafts(testableIDoc);
		assertEquals(drafts.size(), 3, "Should have 3 draft!");
		draftService.delete(testableIDoc, currentUser);
		drafts = draftService.getDrafts(testableIDoc);
		assertEquals(drafts.size(), 2, "Should have 2 draft!");
		draftService.delete(testableIDoc);
		drafts = draftService.getDrafts(testableIDoc);
		assertEquals(drafts.size(), 0, "Should have 0 draft!");
	}

	/**
	 * Creates the idoc.
	 *
	 * @param caseInstance
	 *            the case instance
	 * @return the document instance
	 */
	private DocumentInstance createIdoc(CaseInstance caseInstance) {
		String definitionsId = "DT210011";
		String[] sectionsIds = new String[2];
		sectionsIds[0] = "official";
		sectionsIds[1] = "workflow";
		SectionInstance[] sections = new SectionInstance[sectionsIds.length];
		for (SectionInstance section : caseInstance.getSections()) {
			if (sectionsIds[0].equals(section.getIdentifier())) {
				sections[0] = section;
			} else if (sectionsIds[1].equals(section.getIdentifier())) {
				sections[1] = section;
			}
		}
		SectionInstance targetInstance = sections[(int) (Math.random() * sections.length)];
		DocumentDefinitionTemplate definition = getDefinition(DocumentDefinitionTemplate.class,
				definitionsId);

		DocumentInstance createDocumentInstance = documentService.createInstance(
				new DocumentDefinitionRefProxy(definition), targetInstance, new Operation(
						ActionTypeConstants.CREATE_IDOC));
		createDocumentInstance.setPurpose("iDoc");
		createDocumentInstance.setStandalone(true);
		currentDraftTitle = new Date().toString();
		createDocumentInstance.getProperties().put(DefaultProperties.TITLE, currentDraftTitle);
		currentDraftContent = "my initial data";
		createDocumentInstance.getProperties().put(
				DocumentProperties.FILE_LOCATOR,
				new ByteArrayFileDescriptor(UUID.randomUUID().toString(), currentDraftContent
						.getBytes()));

		getDocumentService().upload(targetInstance, true, createDocumentInstance);
		intanceService.attach(targetInstance, new Operation(ActionTypeConstants.UPLOAD),
				createDocumentInstance);
		return createDocumentInstance;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DocumentService getDocumentService() {
		return documentService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected CaseService getCaseService() {
		return caseService;
	}

	/**
	 * Creates the testable case to start task/wf on.
	 *
	 * @return the case instance
	 */
	private CaseInstance createTestableCase() {
		Map<String, Serializable> caseProperties = new HashMap<>();
		caseProperties.put(DefaultProperties.TITLE, "draft document holder");
		caseProperties.put(DefaultProperties.DESCRIPTION, "created for holder of drafts");
		CaseInstance createCase = createCase(null, "GEC20001", caseProperties);
		return createCase;
	}
}