/**
 *
 */
package com.sirma.itt.seip.configuration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.sync.NoOpSynchronizer;
import com.sirma.itt.seip.tenant.context.TenantInfo;

/**
 * @author BBonev
 */
@Test
public class MultiTenantConfigurationPropertyTest {

	public void test() {
		ContextSupplier valueSupplier = new ContextSupplier("value1");
		ConfigurationInstance instance = mock(ConfigurationInstance.class);
		when(instance.getName()).thenReturn("test");
		Supplier<ConfigurationProperty<String>> propertySupplier = () -> new Property<>(instance,
				NoOpSynchronizer.INSTANCE, valueSupplier::getId);
		Consumer<Consumer<TenantInfo>> tenantRemoveObserverRegister = System.out::println;
		ContextSupplier contextSupplier = new ContextSupplier("context1");
		ConfigurationProperty<String> property = new MultiTenantConfigurationProperty<>(instance,
				contextSupplier::getId,
				tenantRemoveObserverRegister, NoOpSynchronizer.INSTANCE, propertySupplier);

		assertFalse(property.isInitialized());

		assertTrue(property.isSet());

		assertEquals(property.get(), "value1");

		contextSupplier.id = "context2";
		assertEquals(property.get(), "value1");

		valueSupplier.id = "value2";

		property.addValueDestroyListener(s -> assertEquals(s, "value1"));
		property.addConfigurationChangeListener((p) -> assertEquals(p.get(), "value2"));

		property.valueUpdated();
		assertEquals(property.get(), "value2");
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
