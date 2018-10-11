package com.sirma.itt.seip.configuration.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.build.DefaultConfigurationBuilder;
import com.sirma.itt.seip.configuration.build.DefaultRawConfigurationAccessor;

/**
 * Bean used to register the default raw configuration accessor. This is used for lazy configuration accessor
 * registration so that if there is external raw configuration accessor it will be used instead of the default one.
 *
 * @author BBonev
 */
final class DefaultRawConfigurationAccessorBean implements Bean<DefaultRawConfigurationAccessor>, PassivationCapable {

	@SuppressWarnings("serial")
	private final Set<Annotation> qualifiers = new HashSet<>(Arrays.<Annotation> asList(new AnnotationLiteral<Any>() {// nothing
																														// to
																														// add
	}, new AnnotationLiteral<Default>() {// nothing to add
	}));

	private final Set<Type> types;

	private final InjectionTarget<DefaultRawConfigurationAccessor> target;

	/**
	 * Instantiates a new default raw configuration accessor bean.
	 *
	 * @param manager
	 *            the manager
	 */
	DefaultRawConfigurationAccessorBean(BeanManager manager) {
		AnnotatedType<DefaultRawConfigurationAccessor> annotatedType = manager
				.createAnnotatedType(DefaultRawConfigurationAccessor.class);
		types = annotatedType.getTypeClosure();
		target = manager.createInjectionTarget(annotatedType);
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return Singleton.class;
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return Collections.unmodifiableSet(qualifiers);
	}

	@Override
	public DefaultRawConfigurationAccessor create(CreationalContext<DefaultRawConfigurationAccessor> context) {
		DefaultRawConfigurationAccessor builder = target.produce(context);
		target.inject(builder, context);
		target.postConstruct(builder);
		context.push(builder);
		return builder;
	}

	@Override
	public void destroy(DefaultRawConfigurationAccessor instance,
			CreationalContext<DefaultRawConfigurationAccessor> context) {
		target.preDestroy(instance);
		target.dispose(instance);
		context.release();
	}

	@Override
	public Class<DefaultConfigurationBuilder> getBeanClass() {
		return DefaultConfigurationBuilder.class;
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		// this is not valid because the class has injection points
		return Collections.emptySet();
	}

	@Override
	public String getName() {
		return "raw-configuration-accessor";
	}

	@Override
	public String toString() {
		return "Default Raw Configuration Accessor Bean";
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		return Collections.emptySet();
	}

	@Override
	public Set<Type> getTypes() {
		return Collections.unmodifiableSet(types);
	}

	@Override
	public boolean isAlternative() {
		return false;
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public String getId() {
		return getClass().getName();
	}

}
