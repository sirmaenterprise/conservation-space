package com.sirma.itt.seip.resources.script;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.script.ScriptTest;

/**
 * Test {@link UsersScriptProvider}
 *
 * @author BBonev
 */
public class UsersScriptProviderTest extends ScriptTest {

	@InjectMocks
	private UsersScriptProvider scriptProvider;

	@Mock
	private ResourceService resourceService;

	@Test
	public void test_isCurrentUser() {
		when(resourceService.areEqual(any(), any())).thenReturn(Boolean.TRUE);
		Object object = eval("isCurrentUser('emf:admin')");
		Assert.assertTrue(Boolean.TRUE.equals(object));
	}

	@Override
	protected void provideBindings(List<GlobalBindingsExtension> bindingsExtensions) {
		super.provideBindings(bindingsExtensions);
		bindingsExtensions.add(scriptProvider);
	}
}
