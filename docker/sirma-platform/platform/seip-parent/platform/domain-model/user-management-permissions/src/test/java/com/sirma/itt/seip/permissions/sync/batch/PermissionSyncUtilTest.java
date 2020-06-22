package com.sirma.itt.seip.permissions.sync.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.permissions.role.RoleService;
import com.sirma.itt.seip.search.ResultItem;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.SimpleResultItem;

/**
 * Test for {@link PermissionSyncUtil}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 21/07/2017
 */
public class PermissionSyncUtilTest {
	@InjectMocks
	private PermissionSyncUtil syncUtil;

	@Mock
	private SearchService searchService;
	@Mock
	private RoleService roleService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(roleService.isManagerRole("MANAGER")).thenReturn(Boolean.TRUE);
		when(searchService.getFilter(anyString(), any(), any())).then(a -> new SearchArguments<>());
	}

	@Test
	public void getRoleTypesMapping_shouldGetTheMappingAndCacheIt() throws Exception {
		when(searchService.stream(any(), any())).then(a -> Stream.of(createItem("CONSUMER", "conc:SecurityRoleTypes-Read")));

		assertNotNull(syncUtil.getRoleTypesMapping());
		assertEquals("conc:SecurityRoleTypes-Read", syncUtil.getRoleTypesMapping().get("CONSUMER"));

		verify(searchService).stream(any(), any());
	}

	private static ResultItem createItem(String sepRole, String roleType) {
		return SimpleResultItem.create().add("sepRoleId", sepRole).add("instance", roleType);
	}

	@Test
	public void isManagerRoleType() throws Exception {
		when(searchService.stream(any(), any())).then(a -> Stream.of(
				createItem("CONSUMER", "conc:SecurityRoleTypes-Read"),
				createItem("MANAGER", "conc:SecurityRoleTypes-Manager")));

		assertTrue(syncUtil.isManagerRoleType("conc:SecurityRoleTypes-Manager"));
		assertFalse(syncUtil.isManagerRoleType("conc:SecurityRoleTypes-Read"));
	}

	@Test
	public void prepareSearchArguments() throws Exception {
		SearchArguments<CommonInstance> arguments = syncUtil.prepareSearchArguments("query", true);

		assertEquals(SearchArguments.QueryResultPermissionFilter.NONE, arguments.getPermissionsType());
		assertEquals(0, arguments.getMaxSize());
		assertEquals(0, arguments.getPageSize());
		assertEquals(SearchDialects.SPARQL, arguments.getDialect());
		assertEquals(Boolean.TRUE, arguments.getQueryConfigurations().get(SPARQLQueryHelper.INCLUDE_INFERRED_CONFIGURATION));
	}

}
