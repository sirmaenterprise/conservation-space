package com.sirma.itt.seip.configuration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.sync.NoOpSynchronizer;

/**
 * The Class PropertyTest.
 *
 * @author BBonev
 */
@Test
public class PropertyTest {

	private static ConfigurationInstance INSTANCE = mock(ConfigurationInstance.class);

	static {
		when(INSTANCE.getName()).thenReturn("name");
	}

	/**
	 * Test get.
	 */
	public void testGet() {
		Property<String> property = new Property<>(INSTANCE, NoOpSynchronizer.INSTANCE, () -> "value");
		assertEquals(property.get(), "value");
		assertEquals(property.get(), "value");
	}

	/**
	 * Test is set.
	 */
	public void testIsSet() {
		assertEquals(new Property<>(INSTANCE, NoOpSynchronizer.INSTANCE, () -> "value").isSet(), true);
	}

	public void testIsInitialized() {
		Property<String> property = new Property<>(INSTANCE, NoOpSynchronizer.INSTANCE, () -> "value");
		assertFalse(property.isInitialized());
		assertEquals(property.get(), "value");
		assertTrue(property.isInitialized());

		property = new Property<>(INSTANCE, NoOpSynchronizer.INSTANCE, () -> null);
		assertFalse(property.isInitialized());
		assertNull(property.get());
		assertFalse(property.isInitialized());
	}

	/**
	 * Sets the change listener.
	 */
	public void testSetChangeListener() {
		Supplier<String> supplier = mock(Supplier.class);
		when(supplier.get()).thenReturn("value");

		Property<String> property = new Property<>(INSTANCE, NoOpSynchronizer.INSTANCE, supplier);
		ConfigurationChangeListener<String> listener = mock(ConfigurationChangeListener.class);
		property.addConfigurationChangeListener(listener);

		assertEquals(property.get(), "value");

		when(supplier.get()).thenReturn("newValue");

		property.valueUpdated();

		verify(listener).onConfigurationChange(property);
		assertEquals(property.get(), "newValue");
	}

	/**
	 * Sets the destroy listener.
	 */
	public void testSetDestroyListener() {
		Supplier<String> supplier = mock(Supplier.class);
		when(supplier.get()).thenReturn("value");

		Property<String> property = new Property<>(INSTANCE, NoOpSynchronizer.INSTANCE, supplier);
		Consumer<String> listener = mock(Consumer.class);
		property.addValueDestroyListener(listener);

		assertEquals(property.get(), "value");

		when(supplier.get()).thenReturn("newValue");

		property.valueUpdated();

		assertEquals(property.get(), "newValue");
		verify(listener).accept("value");
	}
}
