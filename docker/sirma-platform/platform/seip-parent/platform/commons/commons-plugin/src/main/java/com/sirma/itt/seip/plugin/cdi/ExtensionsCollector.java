package com.sirma.itt.seip.plugin.cdi;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import com.sirma.itt.seip.plugin.Extensions;
import com.sirma.itt.seip.plugin.PluginRegistry;

/**
 * Collect all types, annotated with multiple {@link com.sirma.itt.seip.plugin.Extension}
 * annotations, and for each such annotation, register a new bean. This is done because weld doesn't
 * yet support repeatable annotations discovery (It will in weld 3.0), so the {@link PluginRegistry}
 * can't collect them properly if we don't explicitly collect them before-hand and register a new
 * bean for each Extension annotation. Each discovered type with multiple extension annotations will
 * be vetoed.
 * 
 * @author nvelkov
 */
public class ExtensionsCollector implements Extension {

	private List<AnnotatedType<?>> annotatedTypes = new ArrayList<>(256);

	/**
	 * Process all annotated types and f the annotated type is annotated with multiple extension
	 * annotations, veto it and add it for later processing in the onAfterBeanDiscovery method.
	 * 
	 * @param annotatedType
	 *            the annotated type
	 */
	<X> void onAnnotatedType(@Observes ProcessAnnotatedType<X> annotatedType) {
		Extensions extensions = annotatedType.getAnnotatedType().getAnnotation(Extensions.class);
		if (extensions != null) {
			annotatedType.veto();
			annotatedTypes.add(annotatedType.getAnnotatedType());
		}
	}

	/**
	 * Process all collected in the onAnnotatedType method types. For each type, register an
	 * {@link ExtensionBean} for each {@link com.sirma.itt.seip.plugin.Extension} annotation it's
	 * annotated with.
	 * 
	 * @param afterBeanDiscovery
	 *            the after bean discovery event
	 * @param manager
	 *            the bean manager
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void onAfterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager manager) {
		for (AnnotatedType<?> annotatedType : annotatedTypes) {
			Extensions extensions = annotatedType.getAnnotation(Extensions.class);
			if (extensions != null) {
				for (com.sirma.itt.seip.plugin.Extension extension : extensions.value()) {
					// The bean name must be unique so we will generate it by the class name and the
					// extension target. We can't have two extension annotations with the same
					// target though.
					afterBeanDiscovery.addBean(new ExtensionBean(manager, extension, annotatedType,
							annotatedType.getBaseType().getTypeName() + extension.target()));
				}
			}
		}
	}

}
