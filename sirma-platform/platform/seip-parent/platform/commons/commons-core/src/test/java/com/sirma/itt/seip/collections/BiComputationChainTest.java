/**
 *
 */
package com.sirma.itt.seip.collections;

import static org.testng.Assert.assertEquals;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.testng.annotations.Test;

import com.sirma.itt.seip.collections.ComputationChain;

/**
 * @author BBonev
 */
public class BiComputationChainTest {

	@Test
	public void test_chain() {
		ComputationChain<String, Supplier<String>> chain = new ComputationChain<>();
		chain.addStep(Objects::isNull, () -> "isNull");
		chain.addStep(Predicate.isEqual("test"), () -> "isTest");

		assertEquals(chain.execute(null).get(), "isNull");
		assertEquals(chain.execute("test").get(), "isTest");
		assertEquals(chain.execute("test2"), null);
	}

	@Test
	public void test_chainWithDefault() {
		ComputationChain<String, Supplier<String>> chain = new ComputationChain<>();
		chain.addStep(Objects::isNull, () -> "isNull");
		chain.addStep(Predicate.isEqual("test"), () -> "isTest");
		chain.addDefault(() -> "defaultValue");

		assertEquals(chain.execute(null).get(), "isNull");
		assertEquals(chain.execute("test").get(), "isTest");
		assertEquals(chain.execute("test2").get(), "defaultValue");
	}
}
