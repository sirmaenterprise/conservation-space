/**
 *
 */
package com.sirma.itt.seip.context;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.sirma.itt.seip.context.RuntimeContext.CurrentRuntimeConfiguration;

/**
 * @author BBonev
 */
@Test
public class RuntimeContextTest {

	/**
	 * Test transfer.
	 */
	public void testTransfer() {
		Option option = RuntimeContext.createOption("option1");
		Config transferrableConfig = RuntimeContext.createConfig("config1", true);
		Config nonTransferrableConfig = RuntimeContext.createConfig("config2", false);

		option.enable();
		transferrableConfig.set("value");
		nonTransferrableConfig.set("value2");

		assertTrue(option.isEnabled());
		assertNotNull(transferrableConfig.get());
		assertNotNull(nonTransferrableConfig.get());

		CurrentRuntimeConfiguration currentConfiguration = RuntimeContext.replaceConfiguration(null);

		assertFalse(option.isEnabled());
		assertNull(transferrableConfig.get());
		assertNotNull(nonTransferrableConfig.get());

		RuntimeContext.replaceConfiguration(currentConfiguration);

		assertTrue(option.isEnabled());
		assertNotNull(transferrableConfig.get());
		assertNotNull(nonTransferrableConfig.get());
	}

}
