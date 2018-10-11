package com.sirmaenterprise.sep.roles.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.testutil.fakes.EntityLookupCacheContextFake;
import com.sirmaenterprise.sep.roles.ActionDefinition;
import com.sirmaenterprise.sep.roles.RoleManagement;

/**
 * Test for {@link ActionRegistryImpl}
 *
 * @since 2017-03-28
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
public class ActionRegistryImplTest {

	private static final ActionDefinition ACTION1 = new ActionDefinition()
			.setId("action1")
				.setActionType("serverAction1")
				.setEnabled(true);
	private static final ActionDefinition ACTION2 = new ActionDefinition()
			.setId("action2")
				.setActionType("serverAction2");

	@InjectMocks
	private ActionRegistryImpl actionRegistry;

	@Mock
	private RoleManagement roleManagement;
	@Mock
	private LabelProvider labelProvider;
	@Spy
	private EntityLookupCacheContext cacheContext = EntityLookupCacheContextFake.createNoCache();

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(labelProvider.getLabel(anyString())).then(a -> a.getArgumentAt(0, String.class));

		when(roleManagement.getActions()).then(a -> Stream.of(ACTION1, ACTION2));
		when(roleManagement.getAction("action1")).thenReturn(Optional.of(ACTION1));
		when(roleManagement.getAction("action2")).thenReturn(Optional.of(ACTION2));
		when(roleManagement.getAction("action3")).thenReturn(Optional.empty());
	}

	@Test
	public void should_provideActiveActionKeys() throws Exception {
		when(roleManagement.getActions()).then(a -> Stream.of(ACTION1, ACTION2));
		Set<String> keys = actionRegistry.getKeys();
		assertEquals(1, keys.size());
		assertTrue(keys.contains("action1"));
	}

	@Test
	public void should_returnActiveActions() throws Exception {
		assertNotNull(actionRegistry.find("action1"));
	}

	@Test
	public void shouldNot_returnInactiveActions() throws Exception {
		assertNull(actionRegistry.find("action2"));
	}

	@Test
	public void shouldNot_returnUnkownActions() throws Exception {
		assertNull(actionRegistry.find("action3"));
	}

	@Test
	public void should_resetCache_onReload() throws Exception {
		actionRegistry.reload();
	}
}
