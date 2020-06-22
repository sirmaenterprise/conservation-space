package com.sirmaenterprise.sep.object.browser;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.relation.LinkSearchArguments;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.script.ScriptEvaluator;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Tests for {@link TreeRestService}.
 *
 * @author smustafov
 */
public class TreeRestServiceTest {

	@InjectMocks
	private TreeRestService treeService;

	@Mock
	private ConfigurationProperty<Integer> treePageSize;
	@Mock
	private ConfigurationProperty<Set<String>> treeSortFields;
	@Mock
	private ScriptEvaluator scriptEvaluator;
	@Mock
	private InstanceContextService instanceContextInitializer;
	@Mock
	private LinkService linkService;
	@Mock
	private SearchService searchService;
	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Before
	public void before() {
		initMocks(this);

		when(treePageSize.get()).thenReturn(Integer.valueOf(10));
		when(treeSortFields.get()).thenReturn(new HashSet<>());
	}

	@Test
	public void should_ReturnBadRequest_When_RootIsNull() {
		Response response = treeService.tree(null, "emf:child1", false, null, true, false, true, null, null);

		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	@Test
	public void should_ReturnBadRequest_When_RootHasNoReference() {
		when(instanceTypeResolver.resolveReference("emf:root")).thenReturn(Optional.empty());

		Response response = treeService.tree("emf:root", "emf:child1", false, null, true, false, true, null, null);

		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	@Test
	public void should_MakeSearch_With_ReadPermissionFilter() {
		InstanceReferenceMock instanceReference = InstanceReferenceMock.createGeneric("emf:root");
		when(instanceTypeResolver.resolveReference("emf:root")).thenReturn(Optional.of(instanceReference));

		treeService.tree("emf:root", "emf:child1", false, null, true, false, true, Integer.valueOf(1), null);

		ArgumentCaptor<LinkSearchArguments> searchArgsCaptor = ArgumentCaptor.forClass(LinkSearchArguments.class);
		verify(linkService).searchLinks(searchArgsCaptor.capture());

		assertEquals(QueryResultPermissionFilter.READ, searchArgsCaptor.getValue().getPermissionsType());
	}

}
