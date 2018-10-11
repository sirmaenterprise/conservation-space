package com.sirma.itt.seip.permissions.db.patches;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.permissions.role.EntityPermission;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for {@link InsertMissingParentPermissionsPatch}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 21/09/2017
 */
public class InsertMissingParentPermissionsPatchTest {
	@InjectMocks
	private InsertMissingParentPermissionsPatch patch;
	@Mock
	private SearchService searchService;
	@Mock
	private NamespaceRegistryService registryService;
	@Mock
	private DbDao dbDao;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(registryService.getShortUri(anyString())).then(a -> a.getArgumentAt(0, String.class));
	}

	@Test
	public void execute() throws Exception {
		when(searchService.stream(argThat(CustomMatcher.ofPredicate(
				arg -> arg != null && arg.getStringQuery().contains("isPartOfObjectLibrary"))), any())).then(
				a -> Stream.of(EMF.CASE.toString(), EMF.TEMPLATE));

		when(searchService.stream(argThat(CustomMatcher.ofPredicate(arg -> isType(arg, "Case"))), any())).then(
				a -> {
					String[] cases = new String[2222];
					Arrays.parallelSetAll(cases, index -> "emf:case-" + index);
					return Arrays.stream(cases);
				});
		when(searchService.stream(argThat(CustomMatcher.ofPredicate(arg -> isType(arg, "Template"))), any())).then(
				a -> Stream.of("emf:template1"));
		when(searchService.stream(argThat(CustomMatcher.ofPredicate(arg -> isType(arg, "User"))), any())).then(
				a -> Stream.of("emf:admin", "emf:test"));
		when(searchService.stream(argThat(CustomMatcher.ofPredicate(arg -> isType(arg, "Group"))), any())).then(
				a -> Stream.of("emf:GROUP_test"));

		patch.execute(null);

		ArgumentCaptor<List<Pair<String, Object>>> argumentCaptor = ArgumentCaptor.forClass(List.class);
		verify(dbDao, times(6)).executeUpdateInNewTx(eq(EntityPermission.QUERY_UPDATE_PARENT_FOR_TARGET_KEY),
				argumentCaptor.capture());
		verify(dbDao, times(6)).executeUpdateInNewTx(eq(EntityPermission.QUERY_UPDATE_PARENT_INHERITANCE_FOR_TARGET_KEY),
				argumentCaptor.capture());
	}

	private boolean isType(SearchArguments<? extends Instance> arg, String typeName) {
		return arg != null && arg.getArguments().containsKey("parent") && arg.getArguments()
				.get("parent").toString().contains(typeName);
	}

}
