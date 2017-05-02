package com.sirma.itt.emf.extensions;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import com.sirma.itt.seip.annotation.Documentation;

/**
 * CDI extension that collects all classes annotated with {@link Documentation} annotation.
 *
 * @author BBonev
 */
public class DocumentationExtension implements Extension {

	/**
	 * Comparator for sorting classes by fill class name.
	 *
	 * @author BBonev
	 */
	private static class ClassNameComparator implements Comparator<Class> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(Class o1, Class o2) {
			return o1.getCanonicalName().compareToIgnoreCase(o2.getCanonicalName());
		}
	}

	/** The documented classes. */
	private static Set<Class<?>> documentedClasses = new TreeSet<Class<?>>(new ClassNameComparator());

	/**
	 * Process annotated type.
	 *
	 * @param <D>
	 *            the generic type
	 * @param pat
	 *            the pat
	 */
	public <D> void processAnnotatedType(@Observes ProcessAnnotatedType<D> pat) {
		AnnotatedType<D> type = pat.getAnnotatedType();
		if (type.isAnnotationPresent(Documentation.class)) {
			documentedClasses.add(type.getJavaClass());
		}
	}

	/**
	 * Gets a list of classes that are annotated with {@link Documentation} annotation and implement the given interface
	 * or extend the given class.
	 *
	 * @param <T>
	 *            the generic type
	 * @param clazz
	 *            the clazz
	 * @return the typed classes
	 */
	public <T> List<Class<?>> getTypedClasses(Class<T> clazz) {
		if (clazz == null) {
			return Collections.emptyList();
		}
		List<Class<?>> list = new LinkedList<>();
		for (Class<?> c : documentedClasses) {
			if (clazz.isAssignableFrom(c)) {
				list.add(c);
			}
		}
		return list;
	}

	/**
	 * Gets all registered classes.
	 *
	 * @return the all classes
	 */
	public List<Class<?>> getAllClasses() {
		return Collections.unmodifiableList(new LinkedList<>(documentedClasses));
	}

}
