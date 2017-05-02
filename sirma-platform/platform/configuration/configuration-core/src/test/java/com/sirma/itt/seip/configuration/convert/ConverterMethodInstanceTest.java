package com.sirma.itt.seip.configuration.convert;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Collections;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;

/**
 * The Class ConverterInstanceTest.
 *
 * @author BBonev
 */
@Test
public class ConverterMethodInstanceTest {

	/**
	 * Convert.
	 *
	 * @param context
	 *            the context
	 * @return the string
	 */
	@ConfigurationConverter
	public static String convert(ConverterContext context) {
		return context.getRawValue();
	}

	/**
	 * Convert non static.
	 *
	 * @param context
	 *            the context
	 * @return the string
	 */
	@ConfigurationConverter
	@SuppressWarnings("static-method")
	String convertNonStatic(ConverterContext context) {
		return context.getRawValue();
	}

	/**
	 * Convert with filter.
	 *
	 * @param context
	 *            the context
	 * @return the string
	 */
	@ConfigurationConverter("test")
	public static String convertWithFilter(ConverterContext context) {
		return context.getRawValue();
	}

	/**
	 * Convert with param
	 *
	 * @param context
	 *            the context
	 * @param param
	 *            the param
	 * @return the string
	 */
	@ConfigurationConverter
	public static String convertWithParams(ConverterContext context, ConverterMethodInstanceTest param) {
		return context.getRawValue();
	}

	/**
	 * Convert void.
	 *
	 * @param context
	 *            the context
	 */
	@Test(enabled = false)
	@ConfigurationConverter
	static void convertVoid(ConverterContext context) {
		// no result
	}

	/**
	 * Convert no args.
	 *
	 * @return the string
	 */
	@ConfigurationConverter
	public static String convertNoArgs() {
		return null;
	}

	/**
	 * Convert wrong arg.
	 *
	 * @param arg
	 *            the arg
	 * @return the string
	 */
	@ConfigurationConverter
	public static String convertWrongArg(String arg) {
		return arg;
	}

	/**
	 * Test invalid arguments.
	 *
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 * @throws SecurityException
	 *             the security exception
	 * @throws ConverterException
	 *             the converter exception
	 */
	public void testInvalidArguments() throws NoSuchMethodException, SecurityException, ConverterException {
		ConverterMethodInstance<String> instance = new ConverterMethodInstance<>(getClass(),
				getClass().getMethod("convert", ConverterContext.class), null);
		GroupConverterContext context = new GroupContext(null, null, null, null, null);
		assertNull(instance.convert(context));
	}

	/**
	 * Test converter filtering.
	 *
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 * @throws SecurityException
	 *             the security exception
	 * @throws ConverterException
	 *             the converter exception
	 */
	public void testConverterFiltering() throws NoSuchMethodException, SecurityException, ConverterException {
		ConverterMethodInstance<String> instance = new ConverterMethodInstance<>(getClass(),
				getClass().getMethod("convertWithFilter", ConverterContext.class), null);

		ConfigurationInstance configurationInstance = mock(ConfigurationInstance.class);
		when(configurationInstance.getName()).thenReturn("notMatch", "test");
		RawConfigurationAccessor configurationAccessor = mock(RawConfigurationAccessor.class);

		ConverterContext context = new PropertyContext(configurationInstance, "test", configurationAccessor);
		assertNull(instance.convert(context));
		assertEquals(instance.convert(context), "test");
	}

	/**
	 * Test converter filtering.
	 *
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 * @throws SecurityException
	 *             the security exception
	 * @throws ConverterException
	 *             the converter exception
	 */
	public void testConverterWihtNonStaticMethod() throws NoSuchMethodException, SecurityException, ConverterException {
		BeanManager beanManager = mock(BeanManager.class);
		Bean bean = mock(Bean.class);
		when(beanManager.getBeans(getClass())).thenReturn(Collections.singleton(bean));

		CreationalContext cc = mock(CreationalContext.class);
		when(beanManager.createCreationalContext(bean)).thenReturn(cc);
		when(beanManager.getReference(bean, getClass(), cc)).thenReturn(this);

		ConverterMethodInstance<String> instance = new ConverterMethodInstance<>(getClass(),
				getClass().getDeclaredMethod("convertNonStatic", ConverterContext.class), beanManager);

		ConfigurationInstance configurationInstance = mock(ConfigurationInstance.class);
		when(configurationInstance.getName()).thenReturn("notMatch", "test");
		RawConfigurationAccessor configurationAccessor = mock(RawConfigurationAccessor.class);

		ConverterContext context = new PropertyContext(configurationInstance, "test", configurationAccessor);
		assertEquals(instance.convert(context), "test");
		// just test for NPEs in the method
		assertNotNull(instance.toString());
	}

	/**
	 * Test converter filtering.
	 *
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 * @throws SecurityException
	 *             the security exception
	 * @throws ConverterException
	 *             the converter exception
	 */
	public void testConverterWihtParam() throws NoSuchMethodException, SecurityException, ConverterException {
		BeanManager beanManager = mock(BeanManager.class);
		Bean bean = mock(Bean.class);
		when(beanManager.getBeans(getClass())).thenReturn(Collections.singleton(bean));

		CreationalContext cc = mock(CreationalContext.class);
		when(beanManager.createCreationalContext(bean)).thenReturn(cc);
		when(beanManager.getReference(bean, getClass(), cc)).thenReturn(this);

		ConverterMethodInstance<String> instance = new ConverterMethodInstance<>(getClass(),
				getClass().getMethod("convertWithParams", ConverterContext.class, getClass()), beanManager);

		ConfigurationInstance configurationInstance = mock(ConfigurationInstance.class);
		when(configurationInstance.getName()).thenReturn("notMatch", "test");
		RawConfigurationAccessor configurationAccessor = mock(RawConfigurationAccessor.class);

		ConverterContext context = new PropertyContext(configurationInstance, "test", configurationAccessor);
		assertEquals(instance.convert(context), "test");
		// just test for NPEs in the method
		assertNotNull(instance.toString());
	}

	/**
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 * @throws SecurityException
	 *             the security exception
	 * @throws ConverterException
	 *             the converter exception
	 */
	@Test(expectedExceptions = ConverterException.class)
	public void testConverterWihtParam_notFound() throws NoSuchMethodException, SecurityException, ConverterException {
		BeanManager beanManager = mock(BeanManager.class);
		Bean bean = mock(Bean.class);
		when(beanManager.getBeans(getClass())).thenReturn(Collections.emptySet());

		ConverterMethodInstance<String> instance = new ConverterMethodInstance<>(getClass(),
				getClass().getMethod("convertWithParams", ConverterContext.class, getClass()), beanManager);

		ConfigurationInstance configurationInstance = mock(ConfigurationInstance.class);
		when(configurationInstance.getName()).thenReturn("notMatch", "test");
		RawConfigurationAccessor configurationAccessor = mock(RawConfigurationAccessor.class);

		ConverterContext context = new PropertyContext(configurationInstance, "test", configurationAccessor);
		assertEquals(instance.convert(context), "test");
	}

	@SuppressWarnings("unused")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testConverterWihtNoReturn() throws NoSuchMethodException, SecurityException, ConverterException {

		new ConverterMethodInstance<>(getClass(), getClass().getDeclaredMethod("convertVoid", ConverterContext.class),
				null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testConverterWihtNoArgs() throws NoSuchMethodException, SecurityException, ConverterException {

		new ConverterMethodInstance<>(getClass(), getClass().getMethod("convertNoArgs"), null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testConverterWihtNoWrongArg() throws NoSuchMethodException, SecurityException, ConverterException {

		new ConverterMethodInstance<>(getClass(), getClass().getMethod("convertWrongArg", String.class),
				null);
	}

}
