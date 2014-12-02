package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.testutil.BaseArquillianCITest;
import com.sirma.itt.cmf.testutil.CmfTestResourcePackager;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Test the {@link com.sirma.itt.emf.link.LinkServiceImpl} cmf implementation in various scenarios.
 *
 * @author bbanchev
 */
public class LinkServiceImplCITest extends BaseArquillianCITest {
	/** The manual case to case link id. */
	private static final String MANUAL_CASE_TO_CASE_REV_LINK_ID = "emf:RevReferences";
	@Inject
	private LinkService linkService;
	@Inject
	private CaseService caseService;
	/** The cache context. */
	@Inject
	private EntityLookupCacheContext cacheContext;
	private static Pair<Serializable, Serializable> lastLink;
	private static CaseInstance case1;
	private static CaseInstance case2;

	/**
	 * Creates the deployment.
	 *
	 * @return the web archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		System.out.println("Starting test: " + LinkServiceImplCITest.class);
		return defaultBuilder(new CmfTestResourcePackager()).packageWar();
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.LinkService#associate(com.sirma.itt.emf.instance.model.Instance, com.sirma.itt.emf.instance.model.Instance, java.lang.String)}
	 * .
	 */
	@Test(enabled = true)
	public final void testAssociate() throws Exception {
		boolean associate = linkService.associate(null, null, null);
		Assert.assertFalse(associate, "Null instances should not be linked!");
		CaseInstance mock1 = Mockito.mock(CaseInstance.class);
		DocumentInstance mock2 = Mockito.mock(DocumentInstance.class);
		Mockito.when(mock1.getDmsId()).thenReturn(UUID.randomUUID().toString());
		Mockito.when(mock2.getDmsId()).thenReturn(UUID.randomUUID().toString());
		associate = linkService.associate(mock1, mock2, "case_documents");
		Assert.assertTrue(associate, "Instances should be linked!");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.LinkServiceImpl#removeLinksFor(com.sirma.itt.emf.instance.model.InstanceReference)}
	 * .
	 */
	@Test(enabled = false)
	public void testRemoveLinksForInstanceReference() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.LinkServiceImpl#getLinks(com.sirma.itt.emf.instance.model.InstanceReference, java.util.Set)}
	 * .
	 */
	@Test(enabled = true, dependsOnMethods = "testLink")
	public void testGetLinks() throws Exception {
		clearCaches();
		List<LinkReference> links = linkService.getLinks(case1.toReference());
		// TODO in case of change test update number
		// number of sections * (emf:case_to_section + emf:hasChild + emf:parentOf-simple)
		int expextedLinks = case1.getSections().size() * 3 + 1;
		int case1links = links.size();
		assertEquals(case1links, expextedLinks, "Should have " + expextedLinks + " links: " + links);
		expextedLinks = case2.getSections().size() * 3 + 1;
		links = linkService.getLinks(case2.toReference());
		assertEquals(links.size(), case1links, "Should have "
				+ expextedLinks + " links: " + links);

		/**
		 * {@link LinkService#getLinks(com.sirma.itt.emf.instance.model.InstanceReference,
		 * Set<String>)}
		 */
		links = linkService.getLinks(case1.toReference(),
				Collections.singleton(LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID));
		assertEquals(links.size(), 1, "Should have 1 link: " + links);

		links = linkService.getLinks(case2.toReference(),
				Collections.singleton(LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID));
		assertEquals(links.size(), 0, "Should have 0 link: " + links);
		/** {@link LinkService#getLinks(com.sirma.itt.emf.instance.model.InstanceReference, String)} */
		links = linkService
				.getLinks(case1.toReference(), LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID);
		assertEquals(links.size(), 1, "Should have 1 link: " + links);

		links = linkService
				.getLinks(case2.toReference(), LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID);
		assertEquals(links.size(), 0, "Should have 0 link: " + links);

		links = linkService.getLinks(case1.toReference(),
				LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID, CaseInstance.class);
		assertEquals(links.size(), 1, "Should have 1 link: " + links);

		links = linkService.getLinks(case1.toReference(),
				LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID, DocumentInstance.class);
		assertEquals(links.size(), 0, "Should have 0 link: " + links);

		links = linkService.getLinks(case2.toReference(), MANUAL_CASE_TO_CASE_REV_LINK_ID,
				CaseInstance.class);
		assertEquals(links.size(), 1, "Should have 1 link: " + links);

		links = linkService.getLinks(case2.toReference(), MANUAL_CASE_TO_CASE_REV_LINK_ID,
				DocumentInstance.class);
		assertEquals(links.size(), 0, "Should have 0 link: " + links);

		links = linkService.getLinks(case2.toReference(),
				LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID, CaseInstance.class);
		assertEquals(links.size(), 0, "Should have 0 link: " + links);
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.LinkServiceImpl#unlink(com.sirma.itt.emf.instance.model.InstanceReference, com.sirma.itt.emf.instance.model.InstanceReference)}
	 * .
	 */
	@Test(enabled = false)
	public void testUnlinkInstanceReferenceInstanceReference() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.LinkServiceImpl#unlink(com.sirma.itt.emf.instance.model.InstanceReference, com.sirma.itt.emf.instance.model.InstanceReference, java.lang.String, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public void testUnlinkInstanceReferenceInstanceReferenceStringString() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.LinkServiceImpl#getLinksTo(com.sirma.itt.emf.instance.model.InstanceReference)}
	 * .
	 */
	@Test(enabled = false)
	public void testGetLinksToInstanceReference() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.LinkServiceImpl#linkSimple(com.sirma.itt.emf.instance.model.InstanceReference, com.sirma.itt.emf.instance.model.InstanceReference, java.lang.String)}
	 * .
	 */
	@Test(enabled = true, dependsOnMethods = "testRemoveLink")
	public void testLinkSimple() throws Exception {
		case1 = createTestableCase(Collections.singletonMap(CaseProperties.TITLE,
				(Serializable) "case 1 "));
		case2 = createTestableCase(Collections.singletonMap(CaseProperties.TITLE,
				(Serializable) "case 2 "));
		boolean linkSimple = linkService.linkSimple(case1.toReference(), case2.toReference(),
				LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID);
		assertTrue(linkSimple, "Should be linked");

	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.LinkServiceImpl#getSimpleLinks(com.sirma.itt.emf.instance.model.InstanceReference, java.lang.String)}
	 * .
	 */
	@Test(enabled = true, dependsOnMethods = "testLinkSimple")
	public void testGetSimpleLinks() throws Exception {
		List<LinkReference> simpleLinks = linkService.getSimpleLinks(case1.toReference(),
				LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID);
		assertTrue(simpleLinks != null && simpleLinks.size() == 1, "Should have 1 link: "
				+ simpleLinks);

		simpleLinks = linkService.getSimpleLinks(case2.toReference(),
				LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID);
		assertTrue(simpleLinks != null && simpleLinks.size() == 1, "Should have 1 link: "
				+ simpleLinks);
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.LinkServiceImpl#unlinkSimple(com.sirma.itt.emf.instance.model.InstanceReference, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public void testUnlinkSimpleInstanceReferenceString() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.LinkServiceImpl#unlinkSimple(com.sirma.itt.emf.instance.model.InstanceReference, com.sirma.itt.emf.instance.model.InstanceReference, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public void testUnlinkSimpleInstanceReferenceInstanceReferenceString() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.LinkServiceImpl#getSimpleLinksTo(com.sirma.itt.emf.instance.model.InstanceReference, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public void testGetSimpleLinksTo() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.AbstractLinkService#link(com.sirma.itt.emf.instance.model.Instance, com.sirma.itt.emf.instance.model.Instance, java.lang.String, java.lang.String, java.util.Map)}
	 * .
	 */
	@Test(enabled = true)
	public void testLink() throws Exception {
		case1 = createTestableCase(Collections.singletonMap(CaseProperties.TITLE,
				(Serializable) "case 1 "));
		case2 = createTestableCase(Collections.singletonMap(CaseProperties.TITLE,
				(Serializable) "case 2 "));
		Map<String, Serializable> properties = new LinkedHashMap<String, Serializable>();
		properties.put(LinkConstants.LINK_DESCRIPTION, "Manual");

		lastLink = linkService.link(case1, case2, LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID, null,
				properties);
		assertNotNull(lastLink, "Link should be created");
		assertNotNull(lastLink.getFirst(), "Link should be created");
		assertNull(lastLink.getSecond(), "Link should not be created");
		lastLink = linkService.link(case1, case2, LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID,
				MANUAL_CASE_TO_CASE_REV_LINK_ID, properties);
		assertNotNull(lastLink, "Link should be created");
		assertNotNull(lastLink.getFirst(), "Link should be created");
		assertNotNull(lastLink.getSecond(), "Link should be created");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.AbstractLinkService#dissociate(com.sirma.itt.emf.instance.model.Instance, com.sirma.itt.emf.instance.model.Instance, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public void testDissociate() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.AbstractLinkService#reassociate(com.sirma.itt.emf.instance.model.Instance, com.sirma.itt.emf.instance.model.Instance, com.sirma.itt.emf.instance.model.Instance, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public void testReassociate() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.AbstractLinkService#convertToLinkInstance(com.sirma.itt.emf.link.LinkReference)}
	 * .
	 */
	@Test(enabled = false)
	public void testConvertToLinkInstance() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.AbstractLinkService#isLinked(com.sirma.itt.emf.instance.model.InstanceReference, com.sirma.itt.emf.instance.model.InstanceReference, java.lang.String)}
	 * .
	 */
	@Test(enabled = true, dependsOnMethods = "testGetLinkInstance")
	public void testIsLinked() throws Exception {
		boolean linked = linkService.isLinked(case1.toReference(), case2.toReference(),
				LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID);
		assertTrue(linked, "Should be linked");

		linked = linkService.isLinked(case1.toReference(), case2.toReference(),
				MANUAL_CASE_TO_CASE_REV_LINK_ID);
		Assert.assertFalse(linked, "Should not be linked");
		// check the oposite
		linked = linkService.isLinked(case2.toReference(), case1.toReference(),
				LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID);
		Assert.assertFalse(linked, "Should not be linked");

		linked = linkService.isLinked(case2.toReference(), case1.toReference(),
				MANUAL_CASE_TO_CASE_REV_LINK_ID);
		assertTrue(linked, "Should be linked");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.AbstractLinkService#isLinkedSimple(com.sirma.itt.emf.instance.model.InstanceReference, com.sirma.itt.emf.instance.model.InstanceReference, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public void testIsLinkedSimple() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.AbstractLinkService#getLinkInstance(java.io.Serializable)}.
	 */
	@Test(enabled = true, dependsOnMethods = "testLink")
	public void testGetLinkInstance() throws Exception {
		LinkInstance linkInstance = linkService.getLinkInstance(null);
		Assert.assertNull(linkInstance);

		linkInstance = linkService.getLinkInstance("emf:someid");
		Assert.assertNull(linkInstance);

		LinkReference linkReference = linkService.getLinkReference(null);
		Assert.assertNull(linkReference);

		linkInstance = linkService.getLinkInstance(lastLink.getFirst());
		assertNotNull(linkInstance, "Link should be loaded");
		assertEquals(linkInstance.getIdentifier(), LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID,
				"Id should be " + LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID);
		linkInstance = linkService.getLinkInstance(lastLink.getSecond());
		assertNotNull(linkInstance, "Link should be loaded");
		assertEquals(linkInstance.getIdentifier(), MANUAL_CASE_TO_CASE_REV_LINK_ID, "Id should be "
				+ MANUAL_CASE_TO_CASE_REV_LINK_ID);
		// check again
		clearCaches();
		linkInstance = linkService.getLinkInstance(lastLink.getFirst());
		assertNotNull(linkInstance, "Link should be loaded");
		assertEquals(linkInstance.getIdentifier(), LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID,
				"Id should be " + LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID);
		linkInstance = linkService.getLinkInstance(lastLink.getSecond());
		assertNotNull(linkInstance, "Link should be loaded");
		assertEquals(linkInstance.getIdentifier(), MANUAL_CASE_TO_CASE_REV_LINK_ID, "Id should be "
				+ MANUAL_CASE_TO_CASE_REV_LINK_ID);
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.AbstractLinkService#removeLink(com.sirma.itt.emf.link.LinkInstance)}
	 * .
	 */
	@Test(enabled = true, dependsOnMethods = { "testIsLinked" })
	public void testRemoveLink() throws Exception {
		LinkInstance linkInstance = linkService.getLinkInstance(lastLink.getFirst());
		linkService.removeLink(linkInstance);
		linkInstance = linkService.getLinkInstance(lastLink.getFirst());
		assertNull(linkInstance, "Link should not exist");
		linkInstance = linkService.getLinkInstance(lastLink.getSecond());
		assertNotNull(linkInstance, "Link should exists");
		this.clearCaches();
		linkInstance = linkService.getLinkInstance(lastLink.getFirst());
		assertNull(linkInstance, "Link should not exist");
		linkInstance = linkService.getLinkInstance(lastLink.getSecond());
		assertNotNull(linkInstance, "Link should exists");

		linkInstance = linkService.getLinkInstance(lastLink.getSecond());
		linkService.removeLink(linkInstance);
		linkInstance = linkService.getLinkInstance(lastLink.getFirst());
		assertNull(linkInstance, "Link should not exist");
		this.clearCaches();
		linkInstance = linkService.getLinkInstance(lastLink.getFirst());
		assertNull(linkInstance, "Link should not exist");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.AbstractLinkService#removeLinkById(java.io.Serializable)}.
	 */
	@Test(enabled = false)
	public void testRemoveLinkById() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.AbstractLinkService#updateLinkProperties(java.io.Serializable, java.util.Map)}
	 * .
	 */
	@Test(enabled = false)
	public void testUpdateLinkProperties() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.link.AbstractLinkService#updateCache(java.io.Serializable, java.lang.String, com.sirma.itt.emf.instance.model.InstanceReference, com.sirma.itt.emf.instance.model.InstanceReference, java.util.Map)}
	 * .
	 */
	@Test(enabled = false)
	public void testUpdateCache() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Creates the testable case to start task/wf on.
	 *
	 * @param additionalProps
	 *            are some more props or to override
	 * @return the case instance
	 */
	private CaseInstance createTestableCase(Map<String, Serializable> additionalProps) {
		Map<String, Serializable> caseProperties = new HashMap<>();
		caseProperties.put(DefaultProperties.TITLE, "task holder");
		caseProperties.put(DefaultProperties.DESCRIPTION, "created for holder of tasks");
		if (additionalProps != null) {
			caseProperties.putAll(additionalProps);
		}
		CaseInstance createCase = createCase(null, DEFAULT_DEFINITION_ID_CASE, caseProperties);
		return createCase;
	}

	/**
	 * Check not null.
	 *
	 * @param object
	 *            is the object to check
	 * @param message
	 *            the custom message
	 */
	private void assertNotNull(Object object, String message) {
		Assert.assertNotNull(object, message);
	}

	/**
	 * Check is null.
	 *
	 * @param object
	 *            is the object to check
	 * @param message
	 *            the custom message
	 */
	private void assertNull(Object object, String message) {
		Assert.assertNull(object, message);
	}

	/**
	 * Clears the cache internal. Based on WS impl
	 */
	private void clearCaches() {
		Set<String> activeCaches = cacheContext.getActiveCaches();
		for (String cacheName : activeCaches) {
			if (cacheName.toLowerCase().contains("entity")) {
				EntityLookupCache<Serializable, Object, Serializable> cache = cacheContext
						.getCache(cacheName);
				cache.clear();
			}
		}
	}

	@Override
	protected CaseService getCaseService() {
		return caseService;
	}
}
