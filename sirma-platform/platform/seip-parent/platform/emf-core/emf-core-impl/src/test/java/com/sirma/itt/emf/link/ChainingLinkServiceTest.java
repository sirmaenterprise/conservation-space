package com.sirma.itt.emf.link;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.util.ReflectionUtils;

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

	private void prepareLinkServicesField(LinkService... linkServices) {
		ReflectionUtils.setFieldValue(service, "linkServices", Arrays.asList(linkServices));
	}

}
