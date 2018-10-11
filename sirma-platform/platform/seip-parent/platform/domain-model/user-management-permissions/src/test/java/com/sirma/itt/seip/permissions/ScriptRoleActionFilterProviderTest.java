package com.sirma.itt.seip.permissions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.permissions.action.ScriptRoleActionFilterProvider;
import com.sirma.itt.seip.permissions.model.RoleId;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleActionEvaluatorContext;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.script.ScriptEvaluator;
import com.sirma.itt.seip.script.ScriptException;
import com.sirma.itt.seip.script.ScriptInstance;
import com.sirma.itt.seip.testutil.mocks.ControlDefintionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link ScriptRoleActionFilterProvider}
 *
 * @author BBonev
 */
public class ScriptRoleActionFilterProviderTest {

	@InjectMocks
	private ScriptRoleActionFilterProvider filterProvider;

	@Mock
	private DefinitionService definitionService;
	@Mock
	private ScriptEvaluator scriptEvaluator;
	@Mock
	private TypeConverter typeConverter;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(typeConverter.convert(eq(ScriptInstance.class), any())).then(a -> mock(ScriptInstance.class));
		when(scriptEvaluator.createScriptedPredicate("script")).thenReturn(bindings -> true);
		when(scriptEvaluator.createScriptedPredicate("failing")).thenThrow(new ScriptException());
		when(scriptEvaluator.createScriptedPredicate("invalid")).thenReturn(bindings -> {
			throw new ScriptException();
		});
	}

	@Test
	public void validFilter() throws Exception {
		mockDefinition("script");
		Map<String, Predicate<RoleActionEvaluatorContext>> filters = filterProvider.provideFilters();
		assertFalse(filters.isEmpty());

		Predicate<RoleActionEvaluatorContext> predicate = filters.get("filter");
		assertNotNull(predicate);
		RoleActionEvaluatorContext ctx = new RoleActionEvaluatorContext(null, new EmfInstance(), new EmfUser(),
				new Role(new RoleId()));
		assertTrue(predicate.test(ctx));
	}

	@Test
	public void testValidFilterCaching() throws Exception {
		mockDefinition("script");
		Map<String, Predicate<RoleActionEvaluatorContext>> filters = filterProvider.provideFilters();
		assertFalse(filters.isEmpty());

		Predicate<RoleActionEvaluatorContext> predicate = filters.get("filter");
		assertNotNull(predicate);
		RoleActionEvaluatorContext ctx = new RoleActionEvaluatorContext(null, new EmfInstance(), new EmfUser(),
				new Role(new RoleId()));
		assertTrue(predicate.test(ctx));
		assertTrue(predicate.test(ctx));

		assertTrue(ctx.containsKey("filter/filter"));
	}

	@Test
	public void failinkgFilter() throws Exception {
		mockDefinition("failing");
		Map<String, Predicate<RoleActionEvaluatorContext>> filters = filterProvider.provideFilters();
		assertFalse(filters.isEmpty());

		Predicate<RoleActionEvaluatorContext> predicate = filters.get("filter");
		assertNotNull(predicate);
		RoleActionEvaluatorContext ctx = new RoleActionEvaluatorContext(null, new EmfInstance(), new EmfUser(),
				new Role(new RoleId()));
		assertFalse(predicate.test(ctx));
	}

	@Test
	public void invalidFilter() throws Exception {
		mockDefinition("invalid");
		Map<String, Predicate<RoleActionEvaluatorContext>> filters = filterProvider.provideFilters();
		assertFalse(filters.isEmpty());

		Predicate<RoleActionEvaluatorContext> predicate = filters.get("filter");
		assertNotNull(predicate);
		RoleActionEvaluatorContext ctx = new RoleActionEvaluatorContext(null, new EmfInstance(), new EmfUser(),
				new Role(new RoleId()));
		assertFalse(predicate.test(ctx));
	}

	private void mockDefinition(String scriptId) {
		DefinitionMock definitionMock = new DefinitionMock();
		PropertyDefinitionMock scriptProperty = new PropertyDefinitionMock();
		scriptProperty.setValue(scriptId);
		scriptProperty.setIdentifier("filter");
		ControlDefintionMock controlDefinition = new ControlDefintionMock();
		controlDefinition.setIdentifier("action_filter");
		scriptProperty.setControlDefinition(controlDefinition);
		definitionMock.getFields().add(scriptProperty);
		definitionMock.setType("script");

		when(definitionService.getAllDefinitions(any(Class.class))).thenReturn(Arrays.asList(definitionMock));
	}
}
