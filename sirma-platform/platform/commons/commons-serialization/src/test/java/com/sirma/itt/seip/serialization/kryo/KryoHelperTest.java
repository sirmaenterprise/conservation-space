/**
 *
 */
package com.sirma.itt.seip.serialization.kryo;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.Pair;

/**
 * @author BBonev
 */
public class KryoHelperTest {

	@InjectMocks
	KryoHelper kryoHelper;

	@Mock
	KryoInitializer kryoInitializer;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(kryoInitializer.getClassesToRegister()).thenReturn(Arrays.asList(new Pair<>(ToSerialize.class, 1)));
		kryoHelper.initializeKryoRegister();
	}

	/**
	 * Test_serialize_deserialize.
	 */
	@Test
	public void test_serialize_deserialize() {

		Serializable stored = kryoHelper.getPooled().serialize(new ToSerialize("value"));

		Object deserialized = kryoHelper.getPooled().deserialize(stored);
		assertTrue(deserialized instanceof ToSerialize);
		assertEquals(((ToSerialize) deserialized).fieldToStore, "value");
	}

	@Test
	public void test_getClassRegistration() {
		assertNull(kryoHelper.getClassRegistration(null));
		assertNull(kryoHelper.getClassRegistration(KryoHelperTest.class));
		// there is an offset for the user indexes
		assertEquals(kryoHelper.getClassRegistration(ToSerialize.class), Integer.valueOf(101));
	}

	@Test
	@SuppressWarnings({ "boxing" })
	public void test_getRegisteredClass() {
		assertNull(kryoHelper.getRegisteredClass(null));
		assertNull(kryoHelper.getRegisteredClass(102));
		assertEquals(kryoHelper.getRegisteredClass(101), ToSerialize.class);
	}

	static class ToSerialize {

		/**
		 * Instantiates a new to serialize.
		 */
		public ToSerialize() {
			// implement me
		}

		/**
		 * Instantiates a new to serialize.
		 *
		 * @param toStore
		 *            the to store
		 */
		public ToSerialize(String toStore) {
			fieldToStore = toStore;
		}

		/** The field to store. */
		@Tag(1)
		public String fieldToStore;
	}
}
