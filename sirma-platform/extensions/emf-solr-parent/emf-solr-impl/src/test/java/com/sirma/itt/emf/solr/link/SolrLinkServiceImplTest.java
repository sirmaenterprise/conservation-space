package com.sirma.itt.emf.solr.link;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.emf.solr.configuration.SolrSearchConfiguration;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkSearchArguments;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * The Class SolrLinkServiceImplTest.
 *
 * @author BBonev
 * @author A. Kunchev
 */
public class SolrLinkServiceImplTest {

	private static final String EMF_LINK_ID = "emf:linkId";

	private static final HashSet<String> LINK_IDS = new HashSet<>(Arrays.asList(LinkConstants.REFERENCES_URI));

	private static final LinkSourceId TO = new LinkSourceId("emf:to", null);

	private static final LinkSourceId FROM = new LinkSourceId("emf:from", null);

	private static final Instance TO_INSTANCE = createInstance("emf:to");

	private static final Instance FROM_INSTANCE = createInstance("emf:from");

	private static final String PROJECTION = "uri,instanceType,compact_header,mimetype";

	@InjectMocks
	private SolrLinkServiceImpl service;

	@Mock
	private SearchService searchService;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Mock
	private LinkService semanticLinkService;

	@Mock
	private SolrSearchConfiguration searchConfiguration;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Before
	public void init() {
		initMocks(this);
		when(searchConfiguration.getRelationsRequestFields()).thenReturn(new ConfigurationPropertyMock<>(PROJECTION));
	}

	private static Instance createInstance(String string) {
		EmfInstance instance = new EmfInstance();
		instance.setId(string);
		return instance;
	}

	@Test
	public void testInvalidSearch() {
		assertNull(service.searchLinks(null));

		LinkSearchArguments arguments = new LinkSearchArguments();
		assertNotNull(service.searchLinks(arguments));
		assertNotNull(arguments.getResult());
		assertTrue(arguments.getResult().isEmpty());

		arguments = new LinkSearchArguments();
		arguments.setLinkId("somelinkId");
		assertNotNull(service.searchLinks(arguments));
		assertNotNull(arguments.getResult());
		assertTrue(arguments.getResult().isEmpty());

		arguments = new LinkSearchArguments();
		arguments.setFrom(new LinkSourceId());
		assertNotNull(service.searchLinks(arguments));
		assertNotNull(arguments.getResult());
		assertTrue(arguments.getResult().isEmpty());
	}

	/**
	 * Test search_ child2 parent.
	 */
	@Test
	public void testSearch_Child2Parent() {
		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setFrom(new LinkSourceId("emf:fromId", null));
		arguments.setLinkId(LinkConstants.TREE_CHILD_TO_PARENT);
		when(namespaceRegistryService.buildFullUri("emf:fromId")).thenReturn("http://fullEmfUri:fromId");
		when(namespaceRegistryService.getShortUri(LinkConstants.TREE_CHILD_TO_PARENT))
				.thenReturn(LinkConstants.TREE_CHILD_TO_PARENT);

		service.searchLinks(arguments);

		SearchArguments<Instance> search = new SearchArguments<>();
		search.setDialect(SearchDialects.SOLR);
		search.setProjection(PROJECTION);
		search.setPageSize(1000);
		search.setQuery(new Query("emfParentOf", "\"http://fullEmfUri:fromId\"", true));
		verify(searchService).searchAndLoad(eq(SearchInstance.class), eq(search));
	}

	/**
	 * Test search_ parent2 child.
	 */
	@Test
	public void testSearch_Parent2Child() {
		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setFrom(new LinkSourceId("emf:fromId", null));
		arguments.setLinkId(LinkConstants.TREE_PARENT_TO_CHILD);
		when(namespaceRegistryService.buildFullUri("emf:fromId")).thenReturn("http://fullEmfUri:fromId");
		when(namespaceRegistryService.getShortUri(LinkConstants.TREE_PARENT_TO_CHILD))
				.thenReturn(LinkConstants.TREE_PARENT_TO_CHILD);

		service.searchLinks(arguments);

		SearchArguments<Instance> search = new SearchArguments<>();
		search.setDialect(SearchDialects.SOLR);
		search.setProjection(PROJECTION);
		search.setPageSize(1000);
		search.setQuery(new Query("emfHasParent", "\"http://fullEmfUri:fromId\""));
		verify(searchService).searchAndLoad(eq(SearchInstance.class), eq(search));
	}

	/**
	 * Test search_ part of.
	 */
	@Test
	public void testSearch_PartOf() {
		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setFrom(new LinkSourceId("emf:fromId", null));
		arguments.setLinkId(LinkConstants.PART_OF_URI);
		when(namespaceRegistryService.buildFullUri("emf:fromId")).thenReturn("http://fullEmfUri:fromId");
		when(namespaceRegistryService.getShortUri(LinkConstants.PART_OF_URI)).thenReturn(LinkConstants.PART_OF_URI);

		service.searchLinks(arguments);

		SearchArguments<Instance> search = new SearchArguments<>();
		search.setDialect(SearchDialects.SOLR);
		search.setProjection(PROJECTION);
		search.setPageSize(1000);
		search.setQuery(new Query("hasChild", "\"http://fullEmfUri:fromId\""));
		verify(searchService).searchAndLoad(eq(SearchInstance.class), eq(search));
	}

	/**
	 * Test search_ has child.
	 */
	@Test
	public void testSearch_HasChild() {
		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setFrom(new LinkSourceId("emf:fromId", null));
		arguments.setLinkId(LinkConstants.HAS_CHILD_URI);
		when(namespaceRegistryService.buildFullUri("emf:fromId")).thenReturn("http://fullEmfUri:fromId");
		when(namespaceRegistryService.getShortUri(LinkConstants.HAS_CHILD_URI)).thenReturn(LinkConstants.HAS_CHILD_URI);

		service.searchLinks(arguments);

		SearchArguments<Instance> search = new SearchArguments<>();
		search.setDialect(SearchDialects.SOLR);
		search.setProjection(PROJECTION);
		search.setPageSize(1000);
		search.setQuery(new Query("partOfRelation", "\"http://fullEmfUri:fromId\""));
		verify(searchService).searchAndLoad(eq(SearchInstance.class), eq(search));
	}

	/**
	 * Link simple_ from to link.
	 */
	@Test
	public void linkSimple_FromToLink() {
		service.linkSimple(FROM, TO, LinkConstants.REFERENCES_URI);
		verify(semanticLinkService, only()).linkSimple(FROM, TO, LinkConstants.REFERENCES_URI);
	}

	/**
	 * Link simple_ from to link link.
	 */
	@Test
	public void linkSimple_FromToLinkLink() {
		service.linkSimple(FROM, TO, LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI);
		verify(semanticLinkService, only()).linkSimple(FROM, TO, LinkConstants.REFERENCES_URI,
				LinkConstants.REFERENCES_URI);
	}

	/**
	 * Link simple_ from tos link.
	 */
	@Test
	public void linkSimple_FromTosLink() {
		ArrayList<InstanceReference> arrayList = new ArrayList<>(Arrays.asList(TO));
		service.linkSimple(FROM, arrayList, LinkConstants.REFERENCES_URI);
		verify(semanticLinkService, only()).linkSimple(FROM, arrayList, LinkConstants.REFERENCES_URI);
	}

	/**
	 * Gets the simple links to_ to link.
	 */
	@Test
	public void getSimpleLinksTo_ToLink() {
		service.getSimpleLinksTo(TO, LinkConstants.REFERENCES_URI);
		verify(semanticLinkService, only()).getSimpleLinksTo(TO, LinkConstants.REFERENCES_URI);
	}

	/**
	 * Gets the simple links_ from link.
	 */
	@Test
	public void getSimpleLinks_FromLink() {
		service.getSimpleLinks(FROM, LinkConstants.REFERENCES_URI);
		verify(semanticLinkService, only()).getSimpleLinks(FROM, LinkConstants.REFERENCES_URI);
	}

	/**
	 * Unlink simple_ from link.
	 */
	@Test
	public void unlinkSimple_FromLink() {
		service.unlinkSimple(FROM, LinkConstants.REFERENCES_URI);
		verify(semanticLinkService, only()).unlinkSimple(FROM, LinkConstants.REFERENCES_URI);
	}

	/**
	 * Unlink simple_ from to link.
	 */
	@Test
	public void unlinkSimple_FromToLink() {
		service.unlinkSimple(FROM, TO, LinkConstants.REFERENCES_URI);
		verify(semanticLinkService, only()).unlinkSimple(FROM, TO, LinkConstants.REFERENCES_URI);
	}

	/**
	 * Unlink simple_ from to link link.
	 */
	@Test
	public void unlinkSimple_FromToLinkLink() {
		service.unlinkSimple(FROM, TO, LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI);
		verify(semanticLinkService, only()).unlinkSimple(FROM, TO, LinkConstants.REFERENCES_URI,
				LinkConstants.REFERENCES_URI);
	}

	/**
	 * Link_ inst from to link prop.
	 */
	@Test
	public void link_InstFromToLinkProp() {
		service.link(FROM_INSTANCE, TO_INSTANCE, LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI,
				Collections.<String, Serializable> emptyMap());
		verify(semanticLinkService, only()).link(FROM_INSTANCE, TO_INSTANCE, LinkConstants.REFERENCES_URI,
				LinkConstants.REFERENCES_URI, Collections.<String, Serializable> emptyMap());
	}

	/**
	 * Link_ from to link prop.
	 */
	@Test
	public void link_FromToLinkProp() {
		service.link(FROM, TO, LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI,
				Collections.<String, Serializable> emptyMap());
		verify(semanticLinkService, only()).link(FROM, TO, LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI,
				Collections.<String, Serializable> emptyMap());
	}

	/**
	 * Gets the links_ from link.
	 */
	@Test
	public void getLinks_FromLink() {
		service.getLinks(FROM, LinkConstants.REFERENCES_URI);
		verify(semanticLinkService, only()).getLinks(FROM, LinkConstants.REFERENCES_URI);
	}

	/**
	 * Gets the links_ from links.
	 */
	@Test
	public void getLinks_FromLinks() {
		service.getLinks(FROM, LINK_IDS);
		verify(semanticLinkService, only()).getLinks(FROM, LINK_IDS);
	}

	/**
	 * Gets the links_ from.
	 */
	@Test
	public void getLinks_From() {
		service.getLinks(FROM);
		verify(semanticLinkService, only()).getLinks(FROM);
	}

	/**
	 * Gets the links to_ to.
	 */
	@Test
	public void getLinksTo_TO() {
		service.getLinksTo(TO);
		verify(semanticLinkService, only()).getLinksTo(TO);
	}

	/**
	 * Gets the links to_ to link.
	 */
	@Test
	public void getLinksTo_ToLink() {
		service.getLinksTo(TO, LinkConstants.REFERENCES_URI);
		verify(semanticLinkService, only()).getLinksTo(TO, LinkConstants.REFERENCES_URI);
	}

	/**
	 * Gets the link instance.
	 */
	@Test
	public void getLinkInstance() {
		service.getLinkInstance("emf:id");
		verify(semanticLinkService, only()).getLinkInstance("emf:id");
	}

	/**
	 * Gets the link reference.
	 */
	@Test
	public void getLinkReference() {
		service.getLinkReference(FROM);
		verify(semanticLinkService, only()).getLinkReference(FROM);
	}

	/**
	 * Gets the links to_ to links.
	 */
	@Test
	public void getLinksTo_ToLinks() {
		service.getLinksTo(FROM, LINK_IDS);
		verify(semanticLinkService, only()).getLinksTo(FROM, LINK_IDS);
	}

	/**
	 * Removes the links for_ ref.
	 */
	@Test
	public void removeLinksFor_Ref() {
		service.removeLinksFor(FROM);
		verify(semanticLinkService, only()).removeLinksFor(FROM);
	}

	/**
	 * Removes the links for_ ref links.
	 */
	@Test
	public void removeLinksFor_RefLinks() {
		service.removeLinksFor(FROM, LINK_IDS);
		verify(semanticLinkService, only()).removeLinksFor(FROM, LINK_IDS);
	}

	/**
	 * Unlink_ from to.
	 */
	@Test
	public void unlink_FromTo() {
		service.unlink(FROM, TO);
		verify(semanticLinkService, only()).unlink(FROM, TO);
	}

	/**
	 * Unlink_ from to link link.
	 */
	@Test
	public void unlink_FromToLinkLink() {
		service.unlink(FROM, TO, LinkConstants.REFERENCES_URI, LinkConstants.REFERENCES_URI);
		verify(semanticLinkService, only()).unlink(FROM, TO, LinkConstants.REFERENCES_URI,
				LinkConstants.REFERENCES_URI);
	}

	/**
	 * Removes the link_ inst.
	 */
	@Test
	public void removeLink_Inst() {
		LinkInstance instance = new LinkInstance();
		instance.setId(EMF_LINK_ID);
		service.removeLink(instance);
		verify(semanticLinkService, only()).removeLink(instance);
	}

	/**
	 * Removes the link_ ref.
	 */
	@Test
	public void removeLink_Ref() {
		LinkReference instance = new LinkReference();
		instance.setId(EMF_LINK_ID);
		service.removeLink(instance);
		verify(semanticLinkService, only()).removeLink(instance);
	}

	/**
	 * Removes the link by id.
	 */
	@Test
	public void removeLinkById() {
		service.removeLinkById(EMF_LINK_ID);
		verify(semanticLinkService, only()).removeLinkById(EMF_LINK_ID);
	}

	/**
	 * Update link properties_ id map.
	 */
	@Test
	public void updateLinkProperties_IdMap() {
		service.updateLinkProperties(EMF_LINK_ID, Collections.<String, Serializable> emptyMap());
		verify(semanticLinkService, only()).updateLinkProperties(EMF_LINK_ID,
				Collections.<String, Serializable> emptyMap());
	}

	/**
	 * Checks if is linked.
	 */
	@Test
	public void isLinked() {
		service.isLinked(FROM, TO, LinkConstants.REFERENCES_URI);
		verify(semanticLinkService, only()).isLinked(FROM, TO, LinkConstants.REFERENCES_URI);
	}

	/**
	 * Checks if is linked simple.
	 */
	@Test
	public void isLinkedSimple() {
		service.isLinkedSimple(FROM, TO, LinkConstants.REFERENCES_URI);
		verify(semanticLinkService, only()).isLinkedSimple(FROM, TO, LinkConstants.REFERENCES_URI);
	}

	@Test
	public void getSimpleLinks_collectionOfLinks_semanticLinkServiceCalled() {
		InstanceReferenceMock reference = new InstanceReferenceMock();
		HashSet<String> links = new HashSet<>(Arrays.asList(LinkConstants.HAS_FAVOURITE));
		service.getSimpleLinks(reference, links);
		verify(semanticLinkService).getSimpleLinks(reference, links);
	}

	@Test
	public void getReverseLinkType_nullRelationInstance() {
		assertNull(service.getReverseLinkType(null));
		verify(semanticDefinitionService).getRelation(any());
	}

	@Test
	public void getReverseLinkType_notNullRelationInstance() {
		PropertyInstance propertyInstance = new PropertyInstance();
		propertyInstance.add("inverseRelation", "noitaleResrevni");
		when(semanticDefinitionService.getRelation(LinkConstants.HAS_ATTACHMENT)).thenReturn(propertyInstance);
		String type = service.getReverseLinkType(LinkConstants.HAS_ATTACHMENT);
		assertNotNull(type);
		assertEquals("noitaleResrevni", type);
	}

}
