package com.sirma.itt.seip.permissions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.action.RoleActionDefaultFiltersProvider;
import com.sirma.itt.seip.permissions.action.RoleActionEvaluatorServiceImpl;
import com.sirma.itt.seip.permissions.action.RoleActionFilterProvider;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleActionEvaluatorContext;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;

/**
 * Test class for {@link RoleActionEvaluatorServiceImpl}.
 *
 * @author siliev
 *
 */
public class RoleActionEvaluatorServiceImplTest {

	private static final User USER_ME = new EmfUser("Me");
	private static final User USER_NOT_ME = new EmfUser("NotMe");

	private static final Action ACTION_ONE = new EmfAction("Approve");
	private static final Action ACTION_TWO = new EmfAction("Reject");

	@Mock
	private ResourceService resourceService;

	@InjectMocks
	private RoleActionEvaluatorServiceImpl evaluatorService;

	@Mock
	private RoleActionEvaluatorContext context;

	@Spy
	private RoleActionDefaultFiltersProvider provider;

	@Mock
	private Instance instance;

	@Mock
	private Role role;

	List<RoleActionFilterProvider> filters = new ArrayList<>();
	@Spy
	private Plugins<RoleActionFilterProvider> filterProviders = new Plugins<>("", filters);

	@Spy
	private ContextualMap<String, Predicate<RoleActionEvaluatorContext>> predefinedFilters = ContextualMap.create();

	private Set<Action> actionSet;

	@BeforeTest
	public void beforeTest() {
		MockitoAnnotations.initMocks(this);

		actionSet = new HashSet<>();
		fillSetWithActions(actionSet);
		context = new RoleActionEvaluatorContext(evaluatorService, instance, USER_ME, role);

		ReflectionUtils.setFieldValue(provider, "resourceService", resourceService);

		filters.clear();
		filters.add(provider);
		evaluatorService.initialize();
	}

	/**
	 * Basic action filtering.
	 */
	@Test
	public void filterActions() {
		Set<Action> result = evaluatorService.filter(actionSet, context);
		Assert.assertTrue(actionSet.containsAll(result));
	}

	/**
	 * Filter out action.
	 */
	@Test
	public void filterFiltarableActions() {
		Filterable filterable = (Filterable) actionSet.iterator().next();
		setFilters(filterable);
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.CREATED_BY, USER_NOT_ME);
		Mockito.when(instance.getProperties()).thenReturn(properties);
		Set<Action> result = evaluatorService.filter(actionSet, context);
		Assert.assertFalse(result.contains(filterable));
	}

	private static void setFilters(Filterable action) {
		List<String> filters = Arrays.asList("CREATEDBY", "LOCKEDBY");
		action.setFilters(filters);
	}

	/**
	 * Fill a set with the predefined actions for the test.
	 *
	 * @param actionSet
	 *            the set to fill.
	 */
	private static void fillSetWithActions(Set<Action> actionSet) {
		actionSet.add(ACTION_ONE);
		actionSet.add(ACTION_TWO);
	}
}
