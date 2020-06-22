package com.sirma.itt.seip.permissions.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;

/**
 * Tests for {@link SystemActionsProvider}.
 *
 * @author smustafov
 */
public class SystemActionsProviderTest {

	private SystemActionsProvider provider;

	@Before
	public void before() {
		provider = new SystemActionsProvider();
	}

	@Test
	public void should_ProvideOnlyCreateAction() {
		Map<String, Action> actions = provider.provide();

		assertEquals(1, actions.size());
		assertTrue(actions.containsKey(ActionTypeConstants.CREATE));
		assertNotNull(actions.get(ActionTypeConstants.CREATE));
	}

}
