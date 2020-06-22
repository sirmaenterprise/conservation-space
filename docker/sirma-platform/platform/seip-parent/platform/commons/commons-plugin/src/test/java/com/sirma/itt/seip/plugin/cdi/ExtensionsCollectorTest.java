package com.sirma.itt.seip.plugin.cdi;

import java.lang.reflect.Type;
import java.util.Arrays;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.plugin.Extensions;

/**
 * Test the extensions collector.
 * 
 * @author nvelkov
 */
public class ExtensionsCollectorTest {

	@Test
	public void should_collectTypesWithMultipleExtensions() {
		ProcessAnnotatedType<?> processAnnotatedType = Mockito.mock(ProcessAnnotatedType.class);
		AnnotatedType<?> annotatedType = Mockito.mock(AnnotatedType.class);

		Mockito.doReturn(annotatedType).when(processAnnotatedType).getAnnotatedType();
		Mockito.doReturn(Mockito.mock(Extensions.class)).when(annotatedType).getAnnotation(Extensions.class);

		ExtensionsCollector collector = new ExtensionsCollector();
		collector.onAnnotatedType(processAnnotatedType);
		Mockito.verify(processAnnotatedType, Mockito.times(1)).veto();
	}

	@Test
	public void should_registerBeans_forEachExtension() {
		ExtensionsCollector collector = new ExtensionsCollector();

		AnnotatedType<?> annotatedType = Mockito.mock(AnnotatedType.class);
		Mockito.doReturn(Type.class).when(annotatedType).getBaseType();
		ReflectionUtils.setFieldValue(collector, "annotatedTypes", Arrays.asList(annotatedType));
		Extensions extensions = Mockito.mock(Extensions.class);
		Mockito.when(extensions.value())
				.thenReturn(new Extension[] { Mockito.mock(Extension.class), Mockito.mock(Extension.class) });
		Mockito.when(annotatedType.getAnnotation(Extensions.class)).thenReturn(extensions);

		AfterBeanDiscovery afterBeanDiscovery = Mockito.mock(AfterBeanDiscovery.class);
		collector.onAfterBeanDiscovery(afterBeanDiscovery, Mockito.mock(BeanManager.class));
		Mockito.verify(afterBeanDiscovery, Mockito.times(2)).addBean(Matchers.any(Bean.class));
	}
}
