package com.sirma.itt.emf.solr.link;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.testutil.CmfTest;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkSearchArguments;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.search.SearchDialects;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchInstance;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * The Class SolrLinkServiceImplTest.
 *
 * @author BBonev
 */
@Test
public class SolrLinkServiceImplTest extends CmfTest {

	/** The Constant EMF_LINK_ID. */
	private static final String EMF_LINK_ID = "emf:linkId";

	/** The Constant LINK_IDS. */
	private static final HashSet<String> LINK_IDS = new HashSet<String>(
			Arrays.asList(LinkConstants.REFERENCES_URI));

	/** The Constant TO. */
	private static final LinkSourceId TO = new LinkSourceId("emf:to", null);

	/** The Constant FROM. */
	private static final LinkSourceId FROM = new LinkSourceId("emf:from", null);

	/** The Constant TO_INSTANCE. */
	private static final Instance TO_INSTANCE = createInstance("emf:to");

	/** The Constant FROM_INSTANCE. */
	private static final Instance FROM_INSTANCE = createInstance("emf:from");

	/** The Constant PROJECTION. */
	private static final String PROJECTION = "uri,instanceType,compact_header,mimetype";

	/** The service. */
	private LinkService service;

	/** The search service. */
	private SearchService searchService;

	/** The namespace registry service. */
	private NamespaceRegistryService namespaceRegistryService;

	/** The semantic link service. */
	private LinkService semanticLinkService;

	/**
	 * Initializes the.
	 */
	@BeforeMethod
	public void init() {
		service = new SolrLinkServiceImpl();
		searchService = Mockito.mock(SearchService.class);
		namespaceRegistryService = Mockito.mock(NamespaceRegistryService.class);
		semanticLinkService = Mockito.mock(LinkService.class);
		ReflectionUtils.setField(service, "searchService", searchService);
		ReflectionUtils.setField(service, "namespaceRegistryService", namespaceRegistryService);
		ReflectionUtils.setField(service, "linkProjection", PROJECTION);
		ReflectionUtils.setField(service, "semanticLinkService", semanticLinkService);

	}

	/**
	 * Creates the instance.
	 *
	 * @param string
	 *            the string
	 * @return the instance
	 */
	private static Instance createInstance(String string) {
		EmfInstance instance = new EmfInstance();
		instance.setId(string);
		return instance;
	}

	/**
	 * Test invalid search.
	 */
	public void testInvalidSearch() {
		Assert.assertNull(service.searchLinks(null));

		LinkSearchArguments arguments = new LinkSearchArguments();
		Assert.assertNotNull(service.searchLinks(arguments));
		Assert.assertNotNull(arguments.getResult());
		Assert.assertTrue(arguments.getResult().isEmpty());

		arguments = new LinkSearchArguments();
		arguments.setLinkId("somelinkId");
		Assert.assertNotNull(service.searchLinks(arguments));
		Assert.assertNotNull(arguments.getResult());
		Assert.assertTrue(arguments.getResult().isEmpty());

		arguments = new LinkSearchArguments();
		arguments.setFrom(new LinkSourceId());
		Assert.assertNotNull(service.searchLinks(arguments));
		Assert.assertNotNull(arguments.getResult());
		Assert.assertTrue(arguments.getResult().isEmpty());
	}

	/**
	 * Test search_ child2 parent.
	 */
	public void testSearch_Child2Parent() {
		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setFrom(new LinkSourceId("emf:fromId", null));
		arguments.setLinkId(LinkConstants.TREE_CHILD_TO_PARENT);
		Mockito.when(namespaceRegistryService.buildFullUri("emf:fromId")).thenReturn(
				"http://fullEmfUri:fromId");
		Mockito.when(namespaceRegistryService.getShortUri(LinkConstants.TREE_CHILD_TO_PARENT))
				.thenReturn(LinkConstants.TREE_CHILD_TO_PARENT);

		service.searchLinks(arguments);

		SearchArguments<Instance> search = new SearchArguments<Instance>();
		search.setDialect(SearchDialects.SOLR);
		search.setProjection(PROJECTION);
		search.setPageSize(1000);
		search.setStringQuery("emfParentOf: \"http://fullEmfUri:fromId\"");
		Mockito.verify(searchService).search(Mockito.eq(SearchInstance.class), Mockito.eq(search));
	}

	/**
	 * Test search_ parent2 child.
	 */
	public void testSearch_Parent2Child() {
		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setFrom(new LinkSourceId("emf:fromId", null));
		arguments.setLinkId(LinkConstants.TREE_PARENT_TO_CHILD);
		Mockito.when(namespaceRegistryService.buildFullUri("emf:fromId")).thenReturn(
				"http://fullEmfUri:fromId");
		Mockito.when(namespaceRegistryService.getShortUri(LinkConstants.TREE_PARENT_TO_CHILD))
				.thenReturn(LinkConstants.TREE_PARENT_TO_CHILD);

		service.searchLinks(arguments);

		SearchArguments<Instance> search = new SearchArguments<Instance>();
		search.setDialect(SearchDialects.SOLR);
		search.setProjection(PROJECTION);
		search.setPageSize(1000);
		search.setStringQuery("emfHasParent: \"http://fullEmfUri:fromId\"");
		Mockito.verify(searchService).search(Mockito.eq(SearchInstance.class), Mockito.eq(search));
	}

	/**
	 * Test search_ part of.
	 */
	public void testSearch_PartOf() {
		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setFrom(new LinkSourceId("emf:fromId", null));
		arguments.setLinkId(LinkConstants.PART_OF_URI);
		Mockito.when(namespaceRegistryService.buildFullUri("emf:fromId")).thenReturn(
				"http://fullEmfUri:fromId");
		Mockito.when(namespaceRegistryService.getShortUri(LinkConstants.PART_OF_URI)).thenReturn(
				LinkConstants.PART_OF_URI);

		service.searchLinks(arguments);

		SearchArguments<Instance> search = new SearchArguments<Instance>();
		search.setDialect(SearchDialects.SOLR);
		search.setProjection(PROJECTION);
		search.setPageSize(1000);
		search.setStringQuery("hasChild: \"http://fullEmfUri:fromId\"");
		Mockito.verify(searchService).search(Mockito.eq(SearchInstance.class), Mockito.eq(search));
	}

	/**
	 * Test search_ has child.
	 */
	public void testSearch_HasChild() {
		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setFrom(new LinkSourceId("emf:fromId", null));
		arguments.setLinkId(LinkConstants.HAS_CHILD_URI);
		Mockito.when(namespaceRegistryService.buildFullUri("emf:fromId")).thenReturn(
				"http://fullEmfUri:fromId");
		Mockito.when(namespaceRegistryService.getShortUri(LinkConstants.HAS_CHILD_URI)).thenReturn(
				LinkConstants.HAS_CHILD_URI);

		service.searchLinks(arguments);

		SearchArguments<Instance> search = new SearchArguments<Instance>();
		search.setDialect(SearchDialects.SOLR);
		search.setProjection(PROJECTION);
		search.setPageSize(1000);
		search.setStringQuery("partOfRelation: \"http://fullEmfUri:fromId\"");
		Mockito.verify(searchService).search(Mockito.eq(SearchInstance.class), Mockito.eq(search));
	}

	/**
	 * Link simple_ from to link.
	 */
	public void linkSimple_FromToLink() {
		service.linkSimple(FROM, TO, LinkConstants.REFERENCES_URI);
		Mockito.verify(semanticLinkService, Mockito.only()).linkSimple(FROM, TO,
				LinkConstants.REFERENCES_URI);
	}

	/**
	 * Link simple_ from to link link.
	 */
	public void linkSimple_FromToLinkLink() {
		service.linkSimple(FROM, TO, LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI);
		Mockito.verify(semanticLinkService, Mockito.only()).linkSimple(FROM, TO,
				LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI);
	}

	/**
	 * Link simple_ from tos link.
	 */
	public void linkSimple_FromTosLink() {
		ArrayList<InstanceReference> arrayList = new ArrayList<InstanceReference>(Arrays.asList(TO));
		service.linkSimple(FROM, arrayList, LinkConstants.REFERENCES_URI);
		Mockito.verify(semanticLinkService, Mockito.only()).linkSimple(FROM, arrayList,
				LinkConstants.REFERENCES_URI);
	}

	/**
	 * Gets the simple links to_ to link.
	 */
	public void getSimpleLinksTo_ToLink() {
		service.getSimpleLinksTo(TO, LinkConstants.REFERENCES_URI);
		Mockito.verify(semanticLinkService, Mockito.only()).getSimpleLinksTo(TO,
				LinkConstants.REFERENCES_URI);
	}

	/**
	 * Gets the simple links_ from link.
	 */
	public void getSimpleLinks_FromLink() {
		service.getSimpleLinks(FROM, LinkConstants.REFERENCES_URI);
		Mockito.verify(semanticLinkService, Mockito.only()).getSimpleLinks(FROM,
				LinkConstants.REFERENCES_URI);
	}

	/**
	 * Unlink simple_ from link.
	 */
	public void unlinkSimple_FromLink() {
		service.unlinkSimple(FROM, LinkConstants.REFERENCES_URI);
		Mockito.verify(semanticLinkService, Mockito.only()).unlinkSimple(FROM,
				LinkConstants.REFERENCES_URI);
	}

	/**
	 * Unlink simple_ from to link.
	 */
	public void unlinkSimple_FromToLink() {
		service.unlinkSimple(FROM, TO, LinkConstants.REFERENCES_URI);
		Mockito.verify(semanticLinkService, Mockito.only()).unlinkSimple(FROM, TO,
				LinkConstants.REFERENCES_URI);
	}

	/**
	 * Unlink simple_ from to link link.
	 */
	public void unlinkSimple_FromToLinkLink() {
		service.unlinkSimple(FROM, TO, LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI);
		Mockito.verify(semanticLinkService, Mockito.only()).unlinkSimple(FROM, TO,
				LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI);
	}

	/**
	 * Link_ inst from to link prop.
	 */
	public void link_InstFromToLinkProp() {
		service.link(FROM_INSTANCE, TO_INSTANCE, LinkConstants.REFERENCES_URI,
				LinkConstants.REFERENCES_URI, Collections.<String, Serializable> emptyMap());
		Mockito.verify(semanticLinkService, Mockito.only()).link(FROM_INSTANCE, TO_INSTANCE,
				LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI,
				Collections.<String, Serializable> emptyMap());
	}

	/**
	 * Link_ from to link prop.
	 */
	public void link_FromToLinkProp() {
		service.link(FROM, TO, LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI,
				Collections.<String, Serializable> emptyMap());
		Mockito.verify(semanticLinkService, Mockito.only()).link(FROM, TO,
				LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI,
				Collections.<String, Serializable> emptyMap());
	}

	/**
	 * Gets the links_ from link.
	 */
	public void getLinks_FromLink() {
		service.getLinks(FROM, LinkConstants.REFERENCES_URI);
		Mockito.verify(semanticLinkService, Mockito.only()).getLinks(FROM,
				LinkConstants.REFERENCES_URI);
	}

	/**
	 * Gets the links_ from links.
	 */
	public void getLinks_FromLinks() {
		service.getLinks(FROM, LINK_IDS);
		Mockito.verify(semanticLinkService, Mockito.only()).getLinks(FROM, LINK_IDS);
	}

	/**
	 * Gets the links_ from.
	 */
	public void getLinks_From() {
		service.getLinks(FROM);
		Mockito.verify(semanticLinkService, Mockito.only()).getLinks(FROM);
	}

	/**
	 * Gets the links to_ to.
	 */
	public void getLinksTo_TO() {
		service.getLinksTo(TO);
		Mockito.verify(semanticLinkService, Mockito.only()).getLinksTo(TO);
	}

	/**
	 * Gets the links to_ to link.
	 */
	public void getLinksTo_ToLink() {
		service.getLinksTo(TO, LinkConstants.REFERENCES_URI);
		Mockito.verify(semanticLinkService, Mockito.only()).getLinksTo(TO,
				LinkConstants.REFERENCES_URI);
	}

	/**
	 * Gets the link instance.
	 */
	public void getLinkInstance() {
		service.getLinkInstance("emf:id");
		Mockito.verify(semanticLinkService, Mockito.only()).getLinkInstance("emf:id");
	}

	/**
	 * Gets the link reference.
	 */
	public void getLinkReference() {
		service.getLinkReference(FROM);
		Mockito.verify(semanticLinkService, Mockito.only()).getLinkReference(FROM);
	}

	/**
	 * Gets the links_ from link class.
	 */
	public void getLinks_FromLinkClass() {
		service.getLinks(FROM, LinkConstants.REFERENCES_URI, CaseInstance.class);
		Mockito.verify(semanticLinkService, Mockito.only()).getLinks(FROM,
				LinkConstants.REFERENCES_URI, CaseInstance.class);
	}

	/**
	 * Gets the links to_ to links.
	 */
	public void getLinksTo_ToLinks() {
		service.getLinksTo(FROM, LINK_IDS);
		Mockito.verify(semanticLinkService, Mockito.only()).getLinksTo(FROM, LINK_IDS);
	}

	/**
	 * Removes the links for_ ref.
	 */
	public void removeLinksFor_Ref() {
		service.removeLinksFor(FROM);
		Mockito.verify(semanticLinkService, Mockito.only()).removeLinksFor(FROM);
	}

	/**
	 * Removes the links for_ ref links.
	 */
	public void removeLinksFor_RefLinks() {
		service.removeLinksFor(FROM, LINK_IDS);
		Mockito.verify(semanticLinkService, Mockito.only()).removeLinksFor(FROM, LINK_IDS);
	}

	/**
	 * Unlink_ from to.
	 */
	public void unlink_FromTo() {
		service.unlink(FROM, TO);
		Mockito.verify(semanticLinkService, Mockito.only()).unlink(FROM, TO);
	}

	/**
	 * Unlink_ from to link link.
	 */
	public void unlink_FromToLinkLink() {
		service.unlink(FROM, TO, LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI);
		Mockito.verify(semanticLinkService, Mockito.only()).unlink(FROM, TO,
				LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI);
	}

	/**
	 * Removes the link_ inst.
	 */
	public void removeLink_Inst() {
		LinkInstance instance = new LinkInstance();
		instance.setId(EMF_LINK_ID);
		service.removeLink(instance);
		Mockito.verify(semanticLinkService, Mockito.only()).removeLink(instance);
	}

	/**
	 * Removes the link_ ref.
	 */
	public void removeLink_Ref() {
		LinkReference instance = new LinkReference();
		instance.setId(EMF_LINK_ID);
		service.removeLink(instance);
		Mockito.verify(semanticLinkService, Mockito.only()).removeLink(instance);
	}

	/**
	 * Removes the link by id.
	 */
	public void removeLinkById() {
		service.removeLinkById(EMF_LINK_ID);
		Mockito.verify(semanticLinkService, Mockito.only()).removeLinkById(EMF_LINK_ID);
	}

	/**
	 * Update link properties_ id map.
	 */
	public void updateLinkProperties_IdMap() {
		service.updateLinkProperties(EMF_LINK_ID, Collections.<String, Serializable> emptyMap());
		Mockito.verify(semanticLinkService, Mockito.only()).updateLinkProperties(EMF_LINK_ID,
				Collections.<String, Serializable> emptyMap());
	}

	/**
	 * Checks if is linked.
	 */
	public void isLinked() {
		service.isLinked(FROM, TO, LinkConstants.REFERENCES_URI);
		Mockito.verify(semanticLinkService, Mockito.only()).isLinked(FROM, TO,
				LinkConstants.REFERENCES_URI);
	}


	/**
	 * Checks if is linked simple.
	 */
	public void isLinkedSimple() {
		service.isLinkedSimple(FROM, TO, LinkConstants.REFERENCES_URI);
		Mockito.verify(semanticLinkService, Mockito.only()).isLinkedSimple(FROM, TO,
				LinkConstants.REFERENCES_URI);
	}

}
