/**
 *
 */
package com.sirma.itt.seip.collections;

import static org.testng.Assert.assertEquals;

import java.util.function.Supplier;

import org.testng.annotations.Test;

import com.sirma.itt.seip.collections.BiComputationChain;

/**
 * @author BBonev
 */
public class ComputationChainTest {

	/**
	 * Test_chain.
	 */
	@Test
	public void test_chain() {
		BiComputationChain<String, Integer, Supplier<String>> chain = new BiComputationChain<>();
		chain.addStep((s, i) -> s == null && i == null, () -> "isNull");
		chain.addStep((s, i) -> ("test".equals(s) && i == 1), () -> "isTest");

		assertEquals(chain.execute(null, null).get(), "isNull");
		assertEquals(chain.execute("test", 1).get(), "isTest");
		assertEquals(chain.execute("test", 0), null);
	}

	/**
	 * Test_chain with default.
	 */
	@Test
	public void test_chainWithDefault() {
		BiComputationChain<String, Integer, Supplier<String>> chain = new BiComputationChain<>();
		chain.addStep((s, i) -> s == null && i == null, () -> "isNull");
		chain.addStep((s, i) -> ("test".equals(s) && i == 1), () -> "isTest");
		chain.addDefault(() -> "defaultValue");

		assertEquals(chain.execute(null, null).get(), "isNull");
		assertEquals(chain.execute("test", 1).get(), "isTest");
		assertEquals(chain.execute("test", 0).get(), "defaultValue");
	}
}
