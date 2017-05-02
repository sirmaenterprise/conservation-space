package com.sirma.itt.seip.configuration.convert;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;

/**
 * Tests for {@link ConverterClassInstance}
 *
 * @author BBonev
 */
public class ConverterClassInstanceTest {

	/** The bean manager. */
	@Mock
	BeanManager beanManager;

	/** The bean. */
	@Mock
	@SuppressWarnings("rawtypes")
	Bean bean;

	/** The creational context. */
	@Mock
	CreationalContext<?> creationalContext;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testConverter() throws ConverterException {
		Set beans = new HashSet<>();
		beans.add(bean);
		when(beanManager.getBeans(TestConverter.class)).thenReturn(beans);
		when(beanManager.createCreationalContext(bean)).thenReturn(creationalContext);
		when(bean.create(creationalContext)).thenReturn(new TestConverter());
		ConverterClassInstance<String> converter = new ConverterClassInstance<>(TestConverter.class, beanManager);

		ConverterContext context = new PropertyContext(null, null, null);

		assertNotNull(converter.convert(context));
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testConverter_noBeans() throws ConverterException {
		Set beans = new HashSet<>();
		when(beanManager.getBeans(TestConverter.class)).thenReturn(beans);
		when(beanManager.createCreationalContext(bean)).thenReturn(creationalContext);
		when(bean.create(creationalContext)).thenReturn(new TestConverter());
		ConverterClassInstance<String> converter = new ConverterClassInstance<>(TestConverter.class, beanManager);

		ConverterContext context = new PropertyContext(null, null, null);

		assertNull(converter.convert(context));
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testConverter_noNameMatch() throws ConverterException {
		Set beans = new HashSet<>();
		beans.add(bean);
		when(beanManager.getBeans(TestConverter.class)).thenReturn(beans);
		when(beanManager.createCreationalContext(bean)).thenReturn(creationalContext);

		when(bean.create(creationalContext)).thenReturn(new NamedConverter());
		ConverterClassInstance<String> converter = new ConverterClassInstance<>(NamedConverter.class, beanManager);

		ConfigurationInstance configurationInstance = mock(ConfigurationInstance.class);
		when(configurationInstance.getName()).thenReturn("configName");
		when(configurationInstance.getConverter()).thenReturn("converterName");
		ConverterContext context = new PropertyContext(configurationInstance, null, null);

		assertNull(converter.convert(context));
	}


	@Test(expected = IllegalArgumentException.class)
	public void invalidClass() throws Exception {
		new ConverterClassInstance<>(getClass(), beanManager);
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidClass_noAnnotation() throws Exception {
		new ConverterClassInstance<>(mock(ConfigurationValueConverter.class).getClass(), beanManager);
	}

	/**
	 * The Class TestConverter.
	 */
	@ConfigurationConverter
	class TestConverter implements ConfigurationValueConverter<String> {

		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		@Override
		public Class<String> getType() {
			return String.class;
		}

		/**
		 * Convert.
		 *
		 * @param converterContext
		 *            the converter context
		 * @return the string
		 * @throws ConverterException
		 *             the converter exception
		 */
		@Override
		public String convert(TypeConverterContext converterContext) throws ConverterException {
			return "testValue";
		}
	}

	/**
	 * The Class NamedConverter.
	 */
	@ConfigurationConverter("name")
	class NamedConverter implements ConfigurationValueConverter<String> {

		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		@Override
		public Class<String> getType() {
			return String.class;
		}

		/**
		 * Convert.
		 *
		 * @param converterContext
		 *            the converter context
		 * @return the string
		 * @throws ConverterException
		 *             the converter exception
		 */
		@Override
		public String convert(TypeConverterContext converterContext) throws ConverterException {
			return "testValue";
		}
	}
}
