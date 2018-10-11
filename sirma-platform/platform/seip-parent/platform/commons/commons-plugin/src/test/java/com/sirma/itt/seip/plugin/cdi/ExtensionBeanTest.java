package com.sirma.itt.seip.plugin.cdi;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.plugin.Extensions;

/**
 * Test the extension bean.
 * 
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtensionBeanTest {

	@Test
	@SuppressWarnings("boxing")
	public void should_setCorrectScope() {
		AnnotatedType<?> type = Mockito.mock(AnnotatedType.class);

		Annotation scope = mockScope(type);
		Mockito.when(type.getAnnotations()).thenReturn(new HashSet<>(Arrays.asList(scope)));

		BeanManager beanManager = Mockito.mock(BeanManager.class);
		Mockito.when(beanManager.isScope(Matchers.any(Class.class))).thenReturn(true);

		Bean<?> extensionBean = new ExtensionBean<>(beanManager, Mockito.mock(Annotation.class), type, "beanname");
		Assert.assertTrue(ApplicationScoped.class.isAssignableFrom(extensionBean.getScope()));
	}

	@Test
	public void should_returnDefaultScope_when_notSpecified() {
		AnnotatedType<?> type = Mockito.mock(AnnotatedType.class);

		Bean<?> extensionBean = new ExtensionBean<>(Mockito.mock(BeanManager.class), Mockito.mock(Annotation.class),
				type, "beanname");
		Assert.assertTrue(Dependent.class.isAssignableFrom(extensionBean.getScope()));
	}

	@Test
	public void should_getNonExtensionQualifiers() {
		AnnotatedType<?> type = Mockito.mock(AnnotatedType.class);

		// Mock an extension qualifier
		Annotation extensionAnnotation = Mockito.mock(Extensions.class);
		Annotation scope = mockScope(type);
		Mockito.when(type.getAnnotations()).thenReturn(new HashSet<>(Arrays.asList(scope, extensionAnnotation)));

		Bean<?> extensionBean = new ExtensionBean<>(Mockito.mock(BeanManager.class), Mockito.mock(Annotation.class),
				type, "beanname");
		Set<Annotation> qualifiers = extensionBean.getQualifiers();

		Assert.assertEquals(1, qualifiers.size());
	}

	private static Annotation mockScope(AnnotatedType<?> type) {
		Annotation annotation = Mockito.mock(ApplicationScoped.class);
		Mockito.doReturn(ApplicationScoped.class).when(annotation).annotationType();
		return annotation;
	}
}
