package com.sirma.itt.seip.plugin.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.inject.Qualifier;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.plugin.Extensions;

/**
 * Extension bean that will be registered for each {@link Extension} annotation on types with
 * multiple extensions.
 * 
 * @author nvelkov
 * @param <T>
 *            the generic type
 */
final class ExtensionBean<T> implements Bean<T>, PassivationCapable {

	private final Set<Annotation> qualifiers;

	private final Set<Type> types;

	private final InjectionTarget<T> target;

	private final String name;

	private final Class<T> clazz;

	private final Class<? extends Annotation> scope;

	/**
	 * Create a new Extension Bean.
	 * 
	 * @param manager
	 *            the bean manager
	 * @param extensionQualifier
	 *            the extension qualifier. This needs to be passed explicitly in the constructor,
	 *            unlike the other qualifiers, because we need to create an instance of this class
	 *            per extension qualifier, but that instance needs to have all other qualifiers of
	 *            the annotated type.
	 * @param annotatedType
	 *            the annotated type
	 * @param name
	 *            the name of the bean
	 */
	@SuppressWarnings("unchecked")
	ExtensionBean(BeanManager manager, Annotation extensionQualifier, AnnotatedType<T> annotatedType, String name) {
		types = annotatedType.getTypeClosure();
		target = manager.createInjectionTarget(annotatedType);
		clazz = (Class<T>) annotatedType.getBaseType();
		this.name = name;

		Set<Annotation> nonExtensionQualifiers = getNonExtensionQualifiers(annotatedType);
		qualifiers = CollectionUtils.createHashSet(nonExtensionQualifiers.size() + 1);
		qualifiers.add(extensionQualifier);
		qualifiers.addAll(nonExtensionQualifiers);
		scope = getScope(manager, annotatedType);
	}

	@Override
	public Class<? extends Annotation> getScope() {
		if (scope == null) {
			// return default scope by specification.
			return Dependent.class;
		}
		return scope;
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return Collections.unmodifiableSet(qualifiers);
	}

	@Override
	public void destroy(T instance, CreationalContext<T> context) {
		target.preDestroy(instance);
		target.dispose(instance);
		context.release();
	}

	@Override
	public T create(CreationalContext<T> context) {
		T builder = target.produce(context);
		target.inject(builder, context);
		target.postConstruct(builder);
		context.push(builder);
		return builder;
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return Collections.emptySet();
	}

	@Override
	public Class<T> getBeanClass() {
		return clazz;
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
	public String getName() {
		return name;
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public boolean isAlternative() {
		return false;
	}

	@Override
	public String getId() {
		return clazz.getName();
	}

	private static Set<Annotation> getNonExtensionQualifiers(AnnotatedType<?> annotatedType){
		return annotatedType
				.getAnnotations()
					.stream()
					.filter(annotation -> !(annotation instanceof Extensions))
					.filter(annotation -> annotation.annotationType().getAnnotationsByType(Qualifier.class).length > 0)
					.collect(Collectors.toSet());
	}
	
	private static Class<? extends Annotation> getScope(BeanManager manager, AnnotatedType<?> annotatedType) {
		return annotatedType
				.getAnnotations()
					.stream()
					.filter(annotation -> manager.isScope(annotation.getClass()))
					.map(Annotation::getClass)
					.findFirst()
					.orElse(null);
	}
}
