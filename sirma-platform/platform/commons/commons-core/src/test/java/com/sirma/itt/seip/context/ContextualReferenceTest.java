/**
 *
 */
package com.sirma.itt.seip.context;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

/**
 * @author BBonev
 */
@Test
public class ContextualReferenceTest {

	public void test() {

		ContextSupplier supplier = new ContextSupplier("test");

		ContextualReference<String> reference = new ContextualReference<>(supplier::getId);
		assertNull(reference.getContextValue());
		assertEquals(reference.getContextId(), "test");
		assertNull(reference.getFromStore());

		assertNull(reference.replaceContextValue("newValue"));
		assertEquals(reference.getContextValue(), "newValue");

		supplier.id = "newContext";

		assertNull(reference.getContextValue());

		supplier.id = "test";
		assertEquals(reference.getContextValue(), "newValue");

		reference.initializeWith(() -> "initialier");

		supplier.id = "otherContext";
		assertFalse(reference.isSet());

		assertEquals(reference.getContextValue(), "initialier");

		assertEquals(reference.replaceContextValue("changedInitializedValue"), "initialier");

		assertEquals(reference.getContextValue(), "changedInitializedValue");

		reference.reset();

		assertEquals(reference.getContextValue(), "initialier");
	}

	class ContextSupplier {
		String id;

		public ContextSupplier(String id) {
			this.id = id;
		}

		String getId() {
			return id;
		}
	}
}
