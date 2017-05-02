package com.sirma.itt.seip.instance.relation;

import static com.sirma.itt.seip.collections.CollectionUtils.addValueToMap;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_ON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link AbstractLinkService}
 *
 * @author BBonev
 */
public class AbstractLinkServiceTest {

	@InjectMocks
	private AbstractLinkServiceDummy linkService;

	@Mock
	private DictionaryService dictionaryService;

	@Mock
	private SecurityContext securityContext;

	@Before
	public void beforeMethod() {
		linkService = new AbstractLinkServiceDummy();
		MockitoAnnotations.initMocks(this);

		when(securityContext.getAuthenticated()).thenReturn(new EmfUser("testUser"));
	}

	@Test
	public void getInstanceRelations() throws Exception {

		assertNotNull(linkService.getInstanceRelations(null));

		DefinitionModel model = new DefinitionMock();
		Instance instance = InstanceReferenceMock.createGeneric("emf:instance").toInstance();
		when(dictionaryService.getInstanceDefinition(instance)).thenReturn(model);

		assertNotNull(linkService.getInstanceRelations(instance));

		addObjectPropertyDefinition(model, "emf:hasParent");

		List<LinkReference> relations = linkService.getInstanceRelations(instance);
		assertNotNull(relations);
		assertFalse(relations.isEmpty());
	}

	private static void addObjectPropertyDefinition(DefinitionModel model, String... propName) {
		for (String name : propName) {
			PropertyDefinitionMock property = new PropertyDefinitionMock();
			property.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.URI));
			property.setDisplayType(DisplayType.EDITABLE);
			property.setName(name);
			model.getFields().add(property);
		}
	}

	@Test
	public void getRelationsDiff() throws Exception {
		InstanceReference source = InstanceReferenceMock.createGeneric("emf:source");

		// initial state
		linkService.linksToReturn = Arrays.asList(createLink(source, "emf:link1", "emf:instance-1"), // link1 with 2
																										// instances
				createLink(source, "emf:link1", "emf:instance-4"), // will be changed and added one more
				createLink(source, "emf:link2", "emf:instance-3"), // will not change
				createLink(source, "emf:link3", "emf:instance-2"), // will be changed
				createLink(source, "emf:link4", "emf:instance-1")); // will be removed at all

		Map<String, List<InstanceReference>> changes = new HashMap<>();
		addValueToMap(changes, "emf:link1", InstanceReferenceMock.createGeneric("emf:instance-1")); // not changed
		addValueToMap(changes, "emf:link1", InstanceReferenceMock.createGeneric("emf:instance-2")); // new (changed)
		addValueToMap(changes, "emf:link1", InstanceReferenceMock.createGeneric("emf:instance-3")); // new
		addValueToMap(changes, "emf:link2", InstanceReferenceMock.createGeneric("emf:instance-3")); // not changed
		addValueToMap(changes, "emf:link3", InstanceReferenceMock.createGeneric("emf:instance-4")); // changed
		addValueToMap(changes, "emf:link5", InstanceReferenceMock.createGeneric("emf:instance-4")); // added new
		changes.put("emf:link4", new LinkedList<>()); // removed relation

		DefinitionModel model = new DefinitionMock();
		addObjectPropertyDefinition(model, "emf:link1", "emf:link2", "emf:link3", "emf:link4", "emf:link5");
		when(dictionaryService.getInstanceDefinition(source.toInstance())).thenReturn(model);

		Pair<List<LinkReference>, List<LinkReference>> relationsDiff = linkService.getRelationsDiff(source, changes);
		assertNotNull(relationsDiff);
		assertNotNull(relationsDiff.getFirst());
		assertNotNull(relationsDiff.getSecond());

		List<LinkReference> toAdd = relationsDiff.getFirst();
		List<LinkReference> toRemove = relationsDiff.getSecond();

		assertEquals(4, toAdd.size());
		assertEquals(3, toRemove.size());
	}

	private static LinkReference createLink(InstanceReference from, String linkId, String to) {
		LinkReference link = new LinkReference();
		link.setFrom(from);
		link.setIdentifier(linkId);
		link.setTo(InstanceReferenceMock.createGeneric(to));
		return link;
	}

	private static LinkReference createLink(String from, String linkId, String to) {
		return createLink(InstanceReferenceMock.createGeneric(from), linkId, to);
	}

	@Test
	public void saveRelations() throws Exception {
		Consumer<LinkReference> addConsumer = mock(Consumer.class);
		Consumer<LinkReference> removeConsumer = mock(Consumer.class);

		linkService.saveRelations(
				Arrays.asList(createLink("emf:source", "emf:link", "emf:destination1"),
						createLink("emf:source", "emf:link", "emf:destination2")),
				Arrays.asList(createLink("emf:source", "emf:link", "emf:destination3")), addConsumer, removeConsumer);

		verify(addConsumer, times(2)).accept(any());
		verify(removeConsumer, times(1)).accept(any());
	}

	/**
	 * Class for testing
	 *
	 * @author BBonev
	 */
	private static class AbstractLinkServiceDummy extends AbstractLinkService {

		private static final long serialVersionUID = 1474341996984173511L;

		List<LinkReference> linksToReturn;

		@Override
		public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId) {
			Assert.fail("Should not call this method");
			return false;
		}

		@Override
		public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId, String reverseId) {
			Assert.fail("Should not call this method");
			return false;
		}

		@Override
		public List<LinkReference> getSimpleLinksTo(InstanceReference to, String linkId) {
			Assert.fail("Should not call this method");
			return null;
		}

		@Override
		public List<LinkReference> getSimpleLinks(InstanceReference from, String linkId) {
			Assert.fail("Should not call this method");
			return null;
		}

		@Override
		public List<LinkReference> getSimpleLinks(InstanceReference from, Set<String> linkIds) {
			List<LinkReference> links = linksToReturn;
			if (links != null) {
				return links;
			}
			links = new LinkedList<>();
			for (String linkId : linkIds) {
				links.add(createLink(from, linkId, "emf:instance-to-" + linkId));
			}
			return links;
		}

		@Override
		public void unlinkSimple(InstanceReference from, String linkId) {
			Assert.fail("Should not call this method");
		}

		@Override
		public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId) {
			Assert.fail("Should not call this method");
		}

		@Override
		public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId, String reverseId) {
			Assert.fail("Should not call this method");
		}

		@Override
		public List<LinkReference> getLinks(InstanceReference from, Set<String> linkIds) {
			Assert.fail("Should not call this method");
			return null;
		}

		@Override
		public List<LinkReference> getLinksTo(InstanceReference to) {
			Assert.fail("Should not call this method");
			return null;
		}

		@Override
		public List<LinkReference> getLinksTo(InstanceReference to, Set<String> linkIds) {
			Assert.fail("Should not call this method");
			return null;
		}

		@Override
		public boolean removeLinksFor(InstanceReference instance, Set<String> linkIds) {
			Assert.fail("Should not call this method");
			return false;
		}

		@Override
		public boolean unlink(InstanceReference from, InstanceReference to) {
			Assert.fail("Should not call this method");
			return false;
		}

		@Override
		public boolean unlink(InstanceReference from, InstanceReference to, String linkId, String reverseLinkid) {
			return true;
		}

		@Override
		protected boolean updatePropertiesInternal(Serializable id, Map<String, Serializable> properties,
				Map<String, Serializable> oldProperties) {
			Assert.fail("Should not call this method");
			return false;
		}

		@Override
		protected void removeLinkInternal(LinkReference second) {
			Assert.fail("Should not call this method");
		}

		@Override
		protected LinkReference getLinkReferenceById(Serializable id, boolean loadProperties) {
			Assert.fail("Should not call this method");
			return null;
		}

		@Override
		protected String shrinkLinkIdentifier(String identifier) {
			Assert.fail("Should not call this method");
			return null;
		}

		@Override
		protected String expandLinkIdentifier(String identifier) {
			Assert.fail("Should not call this method");
			return null;
		}

		@Override
		protected List<LinkReference> getLinksInternal(Object from, Object to, Collection<String> linkids) {
			Assert.fail("Should not call this method");
			return null;
		}

		@Override
		protected Pair<Serializable, Serializable> linkInternal(Object from, Object to, String mainLinkId,
				String reverseLinkId, Map<String, Serializable> properties) {
			assertNotNull(properties);
			assertTrue(properties.containsKey(CREATED_ON));
			assertTrue(properties.containsKey(CREATED_BY));
			return new Pair<>(mainLinkId, reverseLinkId);
		}

		@Override
		protected String getMiddleLevelCacheName() {
			Assert.fail("Should not call this method");
			return null;
		}

		@Override
		protected String getTopLevelCacheName() {
			Assert.fail("Should not call this method");
			return null;
		}

		@Override
		protected String getReverseLinkType(String relationType) {
			return relationType;
		}
	}
}
