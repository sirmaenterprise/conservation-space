/**
 * Copyright (c) 2013 23.07.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.seip.plugin;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.Nonbinding;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.plugin.MockPlugins.MyBindingQualifier;
import com.sirma.itt.seip.plugin.MockPlugins.Plugin1;
import com.sirma.itt.seip.plugin.MockPlugins.Plugin2;
import com.sirma.itt.seip.plugin.MockPlugins.Plugin2DefaultPriority;
import com.sirma.itt.seip.plugin.MockPlugins.Plugin2Priority2;
import com.sirma.itt.seip.plugin.MockPlugins.Plugin2Priority2Disabled;
import com.sirma.itt.seip.plugin.MockPlugins.PluginWith2Qualifiers;
import com.sirma.itt.seip.plugin.MockPlugins.PluginWithBindingQualifier1;
import com.sirma.itt.seip.plugin.MockPlugins.PluginWithBindingQualifier2;
import com.sirma.itt.seip.plugin.MockPlugins.PluginWithQualifier;
import com.sirma.itt.seip.plugin.MockPlugins.PluginWithQualifier2;
import com.sirma.itt.seip.plugin.MockPlugins.PluginWithoutQualifier;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Tests for {@link PluginRegistry}.
 *
 * @author Adrian Mitev
 */
@Test
public class PluginRegistryTest {

	/** The plugin registry. */
	private PluginRegistry pluginRegistry;

	/** The bean manager. */
	private BeanManager beanManager;

	/**
	 * Initializes CUT and dependencies.
	 */
	@BeforeMethod
	public void init() {
		pluginRegistry = new PluginRegistry();

		beanManager = Mockito.mock(BeanManager.class);
		ReflectionUtils.setFieldValue(pluginRegistry, "beanManager", beanManager);
	}

	/**
	 * Tests {@link PluginRegistry#init()} method by providing a perfectly valid plugins.
	 */
	public void testInitWithValidPlugins() {
		withExtensions(Plugin1.class, Plugin2.class);

		expectExtensions(Plugin1.class, Plugin2.class);
	}

	/**
	 * Tests {@link PluginRegistry#init()} method by providing two plugins with the same order but different priority.
	 * The one with higher priority should only be discovered.
	 */
	public void testInitForPluginsWithSameOrderAndDifferentPriority() {
		withExtensions(Plugin2.class, Plugin2Priority2.class);

		expectExtensions(Plugin2Priority2.class);
	}

	/**
	 * Tests {@link PluginRegistry#init()} method by providing two plugins with the same order but different priority.
	 * The one with higher priority should only be discovered. <br/>
	 * Needed to cover all the code branches.
	 */
	public void testInitForPluginsWithSameOrderAndDifferentPriorityInDifferentOrder() {
		withExtensions(Plugin2Priority2.class, Plugin2.class);

		expectExtensions(Plugin2Priority2.class);
	}

	/**
	 * Tests {@link PluginRegistry#init()} method by providing two plugins with the same order but with the same
	 * priority. IllegalStateException should be thrown in this case.
	 */
	@Test(expectedExceptions = IllegalStateException.class)
	public void testInitForPluginsWithSameOrderAndPriority() {
		withExtensions(Plugin2.class, Plugin2DefaultPriority.class);
		expectExtensions();
	}

	/**
	 * Tests {@link PluginRegistry#init()} method by providing two plugins with the same order and and the same priority
	 * but one of the is not enabled.
	 */
	public void testInitForPluginsWithSameOrderAndDisabled() {
		withExtensions(Plugin2.class, Plugin2Priority2Disabled.class);

		expectExtensions();
	}

	/**
	 * Tests plugin instantiation by calling {@link PluginRegistry#getPlugins(String)} and verifying that the created
	 * instances are of the corresponding classes.
	 */
	public void testInstantiate() {
		withExtensions(Plugin1.class, Plugin2.class);

		List<Plugin> instances = pluginRegistry.getPlugins(MockPlugins.EXTENSION_POINT);

		expectPluginInstances(instances, Plugin1.class, Plugin2.class);
	}

	/**
	 * Tests plugin instantiation by calling {@link PluginRegistry#getPlugins(String)} and verifying that the created
	 * instances are of the corresponding classes.
	 */
	public void testInstantiateUsingExtensionPoint() {
		withExtensions(Plugin1.class, Plugin2.class);

		InjectionPoint injectionPoint = Mockito.mock(InjectionPoint.class);
		Mockito.when(injectionPoint.getQualifiers()).thenReturn(buildExtensionPoint(true, false, false));
		Annotated annotated = Mockito.mock(Annotated.class);
		Mockito.when(annotated.getAnnotation(ExtensionPoint.class)).thenReturn(
				(ExtensionPoint) buildExtensionPoint(true, false, false).iterator().next());
		Mockito.when(injectionPoint.getAnnotated()).thenReturn(annotated);

		Iterable<Plugin> instances = pluginRegistry.producePlugins(injectionPoint);

		expectPluginInstances(CollectionUtils.toList(instances.iterator()), Plugin1.class, Plugin2.class);
	}

	/**
	 * Tests plugin instantiation by calling {@link PluginRegistry#getPlugins(String)} and verifying that the created
	 * instances are of the corresponding classes.
	 */
	public void testInstantiateUsingExtensionPointAndReverseOrder() {
		withExtensions(Plugin1.class, Plugin2.class);

		InjectionPoint injectionPoint = Mockito.mock(InjectionPoint.class);
		Mockito.when(injectionPoint.getQualifiers()).thenReturn(buildExtensionPoint(false, true, false));
		Annotated annotated = Mockito.mock(Annotated.class);
		Mockito.when(annotated.getAnnotation(ExtensionPoint.class)).thenReturn(
				(ExtensionPoint) buildExtensionPoint(false, true, false).iterator().next());
		Mockito.when(injectionPoint.getAnnotated()).thenReturn(annotated);

		Iterable<Plugin> instances = pluginRegistry.producePlugins(injectionPoint);

		expectPluginInstances(CollectionUtils.toList(instances.iterator()), Plugin2.class, Plugin1.class);
	}

	/**
	 * Test with custom qualifier filtering.
	 */
	public void testWithCustomQualifierFiltering() {
		withExtensions(PluginWithQualifier.class, PluginWithoutQualifier.class);

		InjectionPoint injectionPoint = Mockito.mock(InjectionPoint.class);
		Mockito.when(injectionPoint.getQualifiers()).thenReturn(buildExtensionPoint(true, false, true));
		Annotated annotated = Mockito.mock(Annotated.class);
		Mockito.when(annotated.getAnnotation(ExtensionPoint.class)).thenReturn(
				(ExtensionPoint) buildExtensionPoint(true, false, true).iterator().next());
		Mockito.when(injectionPoint.getAnnotated()).thenReturn(annotated);

		Iterable<Plugin> instances = pluginRegistry.producePlugins(injectionPoint);

		expectPluginInstances(CollectionUtils.toList(instances.iterator()), PluginWithQualifier.class);
	}

	/**
	 * Test with mixed custom qualifier filtering.
	 */
	public void testWithMixedCustomQualifierFiltering() {
		withExtensions(PluginWithQualifier.class, PluginWithoutQualifier.class, PluginWithQualifier2.class,
				PluginWith2Qualifiers.class);

		InjectionPoint injectionPoint = Mockito.mock(InjectionPoint.class);
		Mockito.when(injectionPoint.getQualifiers()).thenReturn(buildExtensionPoint(true, false, true));
		Annotated annotated = Mockito.mock(Annotated.class);
		Mockito.when(annotated.getAnnotation(ExtensionPoint.class)).thenReturn(
				(ExtensionPoint) buildExtensionPoint(true, false, true).iterator().next());
		Mockito.when(injectionPoint.getAnnotated()).thenReturn(annotated);

		Iterable<Plugin> instances = pluginRegistry.producePlugins(injectionPoint);

		expectPluginInstances(CollectionUtils.toList(instances.iterator()), PluginWithQualifier.class,
				PluginWith2Qualifiers.class);
	}

	/**
	 * Test without custom qualifier filtering.
	 */
	public void testWithoutCustomQualifierFiltering() {
		withExtensions(PluginWithQualifier.class, PluginWithoutQualifier.class);

		InjectionPoint injectionPoint = Mockito.mock(InjectionPoint.class);
		Mockito.when(injectionPoint.getQualifiers()).thenReturn(buildExtensionPoint(true, false, false));
		Annotated annotated = Mockito.mock(Annotated.class);
		Mockito.when(annotated.getAnnotation(ExtensionPoint.class)).thenReturn(
				(ExtensionPoint) buildExtensionPoint(true, false, false).iterator().next());
		Mockito.when(injectionPoint.getAnnotated()).thenReturn(annotated);

		Iterable<Plugin> instances = pluginRegistry.producePlugins(injectionPoint);

		expectPluginInstances(CollectionUtils.toList(instances.iterator()), PluginWithQualifier.class,
				PluginWithoutQualifier.class);
	}

	/**
	 * Test with binding qualifier filtering.
	 */
	public void testWithBindingQualifierFiltering() {
		withExtensions(PluginWithBindingQualifier1.class, PluginWithBindingQualifier2.class);

		InjectionPoint injectionPoint = Mockito.mock(InjectionPoint.class);
		Mockito.when(injectionPoint.getQualifiers()).thenReturn(buildExtensionPoint("1"));
		Annotated annotated = Mockito.mock(Annotated.class);
		Mockito.when(annotated.getAnnotation(ExtensionPoint.class)).thenReturn(
				(ExtensionPoint) buildExtensionPoint("1").iterator().next());
		Mockito.when(injectionPoint.getAnnotated()).thenReturn(annotated);

		Iterable<Plugin> instances = pluginRegistry.producePlugins(injectionPoint);

		expectPluginInstances(CollectionUtils.toList(instances.iterator()), PluginWithBindingQualifier1.class);
	}

	/**
	 * Creates mock data by filling the BeanManager with beans of the provided extension types so they get available
	 * when BeanManager#getBeans() is called.
	 *
	 * @param extensions
	 *            bean types to fill.
	 */
	@SuppressWarnings("rawtypes")
	private void withExtensions(Class<?>... extensions) {
		Set<Bean<?>> beans = new HashSet<>();
		for (Class<?> extension : extensions) {
			Bean bean = Mockito.mock(Bean.class);
			Mockito.when(bean.getBeanClass()).thenReturn(extension);
			Mockito.when(bean.getScope()).thenReturn(Dependent.class);
			Mockito.doReturn(new HashSet<>(Arrays.asList(extension.getAnnotation(Extension.class)))).when(bean).getQualifiers();
			beans.add(bean);

			try {
				Mockito.when(beanManager.getReference(bean, Plugin.class, null)).thenReturn(extension.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("Error during class instantiation", e);
			}
		}

		Mockito.when(beanManager.getBeans(Plugin.class, ExtensionLiteral.INSTANCE)).thenReturn(beans);

		pluginRegistry.init();
	}

	/**
	 * Used to assert that a specific set of extensions are being discovered by the PluginRegistry in an order-specific
	 * way. The order of the expected extension classes should match the order of the discovered extensions.
	 *
	 * @param extensions
	 *            types of the expected discovered extensions.
	 */
	@SuppressWarnings("unchecked")
	private void expectExtensions(Class<?>... extensions) {
		Map<String, List<Bean<?>>> beansCache = (Map<String, List<Bean<?>>>) ReflectionUtils.getFieldValue(pluginRegistry,
				"beans");
		List<Bean<?>> beans = beansCache.get(MockPlugins.EXTENSION_POINT);
		if (beans != null && !beans.isEmpty()) {
			if (extensions.length != beans.size()) {
				Assert.fail("Number of found extensions doesn't match the number of expected");
			}

			for (int i = 0; i < beans.size(); i++) {
				if (!beans.get(i).getBeanClass().equals(extensions[i])) {
					Assert.fail("Expected extension of type '" + extensions[i] + "' but found '"
							+ beans.get(i).getBeanClass() + "'");
				}
			}
		} else if (extensions.length != 0) {
			Assert.fail("No beans have been discovered");
		}
	}

	/**
	 * Asserts the instantiated plugins for a given extension point. The order of the expected extension classes should
	 * match the order of the instantiated extension classes.
	 *
	 * @param instances
	 *            the instantiates plugins.
	 * @param extensions
	 *            plugin classes that are expected.
	 */
	private void expectPluginInstances(List<Plugin> instances, Class<?>... extensions) {
		if (instances != null && !instances.isEmpty()) {
			if (extensions.length != instances.size()) {
				Assert.fail("Number of found extensions doesn't match the number of expected");
			}

			for (int i = 0; i < instances.size(); i++) {
				if (!instances.get(i).getClass().equals(extensions[i])) {
					Assert.fail("Expected extension of type '" + extensions[i] + "' but found '"
							+ instances.get(i).getClass() + "'");
				}
			}
		} else if (extensions.length != 0) {
			Assert.fail("No beans have been discovered");
		}
	}

	/**
	 * Constructs a set with an instance of {@link ExtensionPoint} annotation with predefined paramters.
	 *
	 * @param singleton
	 *            singleton paramter.
	 * @param reverseOrder
	 *            reverse order paramter.
	 * @param customQualifer
	 *            the custom qualifer
	 * @return constructed set/
	 */
	private Set<Annotation> buildExtensionPoint(final boolean singleton, final boolean reverseOrder,
			boolean customQualifer) {
		Set<Annotation> qualifiers = new LinkedHashSet<>();
		qualifiers.add(new ExtensionPoint() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return ExtensionPoint.class;
			}

			@Override
			public String value() {
				return MockPlugins.EXTENSION_POINT;
			}

			@Override
			public boolean singleton() {
				return singleton;
			}

			@Override
			public boolean reverseOrder() {
				return reverseOrder;
			}
		});
		if (customQualifer) {
			qualifiers.add(() -> MockPlugins.MyQualifier.class);
		}
		return qualifiers;
	}

	/**
	 * Constructs a set with an instance of {@link ExtensionPoint} annotation with predefined paramters.
	 *
	 * @param value
	 *            the value
	 * @return constructed set/
	 */
	private Set<Annotation> buildExtensionPoint(final String value) {
		Set<Annotation> qualifiers = new LinkedHashSet<>();
		qualifiers.add(new ExtensionPoint() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return ExtensionPoint.class;
			}

			@Override
			public String value() {
				return MockPlugins.EXTENSION_POINT;
			}

			@Override
			public boolean singleton() {
				return false;
			}

			@Override
			public boolean reverseOrder() {
				return false;
			}
		});
		qualifiers.add(new MockPlugins.MyBindingQualifier() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return MyBindingQualifier.class;
			}

			@Override
			public String value() {
				return value;
			}

			@Override
			@Nonbinding
			public String nonBindingMethod() {
				return "some value";
			}
		});
		return qualifiers;
	}

}
