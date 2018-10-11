package com.sirma.itt.seip.tenant.management;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.runtime.Component;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.OnTenantRemove;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Test the tenant operation collector.
 *
 * @author nvelkov
 */
public class TenantOperationCollectorTest {

	private List<Component> tenantAddedComponents = new ArrayList<>();

	private List<Component> tenantRemovedComponets = new ArrayList<>();

	private TenantOperationCollector collector = new TenantOperationCollector();

	/**
	 * Init the component collections.
	 */
	@Before
	public void init() {
		ReflectionUtils.setFieldValue(collector, "tenantAddedComponents", tenantAddedComponents);
		ReflectionUtils.setFieldValue(collector, "tenantRemovedComponets", tenantRemovedComponets);
	}

	@Test
	public void should_registerOnTenantOperations() {
		collector.registerDestination(mockProcessAnnotatedType(TenantOperationsDummy.class), mock(BeanManager.class));
		assertEquals(1, collector.getTenantAddedComponents().size());
		assertEquals(1, collector.getTenantRemovedComponets().size());
	}

	@Test
	public void should_SortByOrder_MultipleAddComponents() {
		collector.registerDestination(mockProcessAnnotatedType(TenantOperationsDummy.class), mock(BeanManager.class));
		collector.registerDestination(mockProcessAnnotatedType(TenantOperationLastOrder.class),
				mock(BeanManager.class));
		collector.registerDestination(mockProcessAnnotatedType(TenantOperationFirstOrder.class),
				mock(BeanManager.class));
		collector.registerDestination(mockProcessAnnotatedType(TenantOperationDefaultOrder.class),
				mock(BeanManager.class));

		List<Component> components = collector.getTenantAddedComponents();
		assertEquals(4, components.size());
		assertEquals(TenantOperationFirstOrder.class, components.get(0).getActualClass());
		assertEquals(TenantOperationLastOrder.class, components.get(3).getActualClass());

		List<Component> tenantRemovedComponets = collector.getTenantRemovedComponets();
		assertEquals(3, tenantRemovedComponets.size());
		assertEquals(TenantOperationFirstOrder.class, tenantRemovedComponets.get(0).getActualClass());
		assertEquals(TenantOperationLastOrder.class, tenantRemovedComponets.get(1).getActualClass());
		assertEquals(TenantOperationsDummy.class, tenantRemovedComponets.get(2).getActualClass());
	}

	private static ProcessAnnotatedType<?> mockProcessAnnotatedType(Class<?> clazz) {
		ProcessAnnotatedType<?> processAnnotatedType = mock(ProcessAnnotatedType.class);
		AnnotatedType<?> annotatedType = mock(AnnotatedType.class);

		doReturn(clazz).when(annotatedType).getJavaClass();
		doReturn(annotatedType).when(processAnnotatedType).getAnnotatedType();
		return processAnnotatedType;
	}

	/**
	 * Tenant operations dummy used only in this test.
	 *
	 * @author nvelkov
	 */
	private class TenantOperationsDummy {

		/**
		 * On tenant add method to be collected by the collector.
		 */
		@OnTenantAdd
		public void onTenantAdd() {
			// nothing
		}

		/**
		 * On tenant removed method to be collected by the collector.
		 */
		@OnTenantRemove
		public void onTenantRemove() {
			// nothing
		}
	}

	private class TenantOperationFirstOrder {

		@OnTenantAdd(order = -10)
		public void onTenantAdd() {
			// nothing
		}

		@OnTenantRemove(order = 1)
		public void onTenantRemove() {
			// nothing
		}

	}

	private class TenantOperationLastOrder {

		@OnTenantAdd(order = 10)
		public void onTenantAdd() {
			// nothing
		}

		/**
		 * On tenant removed method to be collected by the collector.
		 */
		@OnTenantRemove(order = 2)
		public void onTenantRemove2() {
			// nothing
		}
	}

	private class TenantOperationDefaultOrder {

		@OnTenantAdd
		public void onTenantAdd() {
			// nothing
		}

	}
}
