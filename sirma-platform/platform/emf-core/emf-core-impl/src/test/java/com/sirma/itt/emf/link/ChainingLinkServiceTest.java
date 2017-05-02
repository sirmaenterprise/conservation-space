package com.sirma.itt.emf.link;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Tests for {@link ChainingLinkService}.
 *
 * @author A. Kunchev
 */
public class ChainingLinkServiceTest {

	private ChainingLinkService service;

	@Before
	public void setup() {
		service = new ChainingLinkService();
	}

	@Test
	public void getSimpleLinksReferenceAndSet_oneLinkService() {
		InstanceReferenceMock from = new InstanceReferenceMock();
		Set<String> linkIds = Collections.singleton("relation");

		LinkService linkService1 = mock(LinkService.class);
		prepareLinkServicesField(linkService1);

		service.getSimpleLinks(from, linkIds);
		verify(linkService1).getSimpleLinks(from, linkIds);
	}

	@Test
	public void getSimpleLinksReferenceAndSet_twoLinkServices() {
		InstanceReferenceMock from = new InstanceReferenceMock();
		Set<String> linkIds = Collections.singleton("relation");

		LinkService linkService1 = mock(LinkService.class);
		LinkService linkService2 = mock(LinkService.class);
		prepareLinkServicesField(linkService1, linkService2);

		service.getSimpleLinks(from, linkIds);
		verify(linkService2).getSimpleLinks(from, linkIds);
	}

	@Test
	public void getInstanceRelations_oneLinkService() {
		Instance instance = mock(Instance.class);

		LinkService linkService1 = mock(LinkService.class);
		prepareLinkServicesField(linkService1);

		service.getInstanceRelations(instance);
		verify(linkService1).getInstanceRelations(instance);
	}

	@Test
	public void getInstanceRelations_twoLinkServices() {
		Instance instance = mock(Instance.class);

		LinkService linkService1 = mock(LinkService.class);
		LinkService linkService2 = mock(LinkService.class);
		prepareLinkServicesField(linkService1, linkService2);

		service.getInstanceRelations(instance);
		verify(linkService2).getInstanceRelations(instance);
	}

	@Test
	public void getInstanceRelationsWithFilter_oneLinkService() {
		Instance instance = mock(Instance.class);

		LinkService linkService1 = mock(LinkService.class);
		prepareLinkServicesField(linkService1);

		service.getInstanceRelations(instance, k -> true);
		verify(linkService1).getInstanceRelations(eq(instance), any(Predicate.class));
	}

	@Test
	public void getInstanceRelationsWithFilter_twoLinkServices() {
		Instance instance = mock(Instance.class);

		LinkService linkService1 = mock(LinkService.class);
		LinkService linkService2 = mock(LinkService.class);
		prepareLinkServicesField(linkService1, linkService2);

		service.getInstanceRelations(instance, k -> true);
		verify(linkService2).getInstanceRelations(eq(instance), any(Predicate.class));
	}

	@Test
	public void getRelationsDiff_oneLinkService() {
		InstanceReferenceMock reference = new InstanceReferenceMock();
		Map<String, ? extends Collection<InstanceReference>> relations = new HashMap<>();

		LinkService linkService1 = mock(LinkService.class);
		prepareLinkServicesField(linkService1);

		service.getRelationsDiff(reference, relations);
		verify(linkService1).getRelationsDiff(reference, relations);
	}

	@Test
	public void getRelationsDiff_twoLinkServices() {
		InstanceReferenceMock reference = new InstanceReferenceMock();
		Map<String, ? extends Collection<InstanceReference>> relations = new HashMap<>();

		LinkService linkService1 = mock(LinkService.class);
		LinkService linkService2 = mock(LinkService.class);
		prepareLinkServicesField(linkService1, linkService2);

		service.getRelationsDiff(reference, relations);
		verify(linkService2).getRelationsDiff(reference, relations);
	}

	@Test
	public void saveRelations_twoLinkServices_calledBoth() {
		List<LinkReference> toAdd = Arrays.asList(new LinkReference());
		List<LinkReference> toRemove = Arrays.asList(new LinkReference(), new LinkReference());

		LinkService linkService1 = mock(LinkService.class);
		LinkService linkService2 = mock(LinkService.class);
		prepareLinkServicesField(linkService1, linkService2);

		service.saveRelations(toAdd, toRemove, null, null);
		verify(linkService1).saveRelations(eq(toAdd), eq(toRemove), eq(null), eq(null));
		verify(linkService2).saveRelations(eq(toAdd), eq(toRemove), eq(null), eq(null));
	}

	private void prepareLinkServicesField(LinkService... linkServices) {
		ReflectionUtils.setField(service, "linkServices", Arrays.asList(linkServices));
	}

}
