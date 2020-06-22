package com.sirma.itt.seip.plugin;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.Nonbinding;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Locates all plugins mapped to extension points and keeps for use within the application.
 *
 * @author Adrian Mitev
 */
@Named
@ApplicationScoped
public class PluginRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private BeanManager beanManager;

	/**
	 * Holds CDI Bean information about the available plugins.
	 */
	private Map<String, List<Bean<?>>> beans;

	/**
	 * Holds information about the cached plugins for an extension point.
	 */
	private Map<String, List<Plugin>> cachedPlugins;

	/**
	 * Fetches all {@link Plugin} classes that are annotated with @Extension, performs a merging logic, stores them and.
	 */
	@PostConstruct
	public void init() {
		beans = new HashMap<>(64);
		cachedPlugins = new HashMap<>(64);

		// get all plugins that are annotated with @Extension
		Set<Bean<?>> pluginBeans = beanManager.getBeans(Plugin.class, ExtensionLiteral.INSTANCE);

		for (Bean<?> bean : pluginBeans) {
			Extension metadata = getExtensionMetadata(bean);
			List<Bean<?>> extensionPointBeans = beans.get(metadata.target());
			if (extensionPointBeans == null) {
				extensionPointBeans = new ArrayList<>();
				beans.put(metadata.target(), extensionPointBeans);
			}

			// look for a plugin with the same order
			Bean<?> sameOrderPlugin = null;
			for (Bean<?> current : extensionPointBeans) {
				Extension currentMetadata = getExtensionMetadata(current);
				if (Double.compare(currentMetadata.order(), metadata.order()) == 0) {
					sameOrderPlugin = current;
					break;
				}
			}

			// if there is a plugin with the same order as the current
			// check the priority. If the priority is the same an
			// exception should be thrown. If the current bean has a
			// greater priority, the previous should be removed, if the
			// previous has a greater priority, it should be kept
			if (sameOrderPlugin != null) {
				Extension sameOrderMetadata = getExtensionMetadata(sameOrderPlugin);
				if (metadata.priority() == sameOrderMetadata.priority()) {
					throw new IllegalStateException("There are two extensions with the same order and priority - '"
							+ bean.getBeanClass().getName() + "' and '" + sameOrderPlugin.getBeanClass().getName()
							+ "'");
				} else if (metadata.priority() > sameOrderMetadata.priority()) {
					// remove the plugin with lower priority
					extensionPointBeans.remove(sameOrderPlugin);
					extensionPointBeans.add(bean);
				}
			} else {
				extensionPointBeans.add(bean);
			}
		}

		cleanAndSortPlugins();

		beanManager.fireEvent(new AfterPluginRegistryInitializedEvent(beans), CDI.getDefaultLiteral());
	}

	/**
	 * Purge all disabled plugins and sort the plugins according to their order.
	 */
	private void cleanAndSortPlugins() {
		PluginComparator comparator = new PluginComparator();
		for (List<Bean<?>> pointBeans : beans.values()) {
			for (Iterator<Bean<?>> iterator = pointBeans.iterator(); iterator.hasNext();) {
				if (!getExtensionMetadata(iterator.next()).enabled()) {
					iterator.remove();
				}
			}

			Collections.sort(pointBeans, comparator);
		}
	}

	/**
	 * Provides a list of all plugins for a specific extension point. If the entries in this list are changed, the
	 * available list plugins for the particular extension point also gets changed.
	 *
	 * @param target
	 *            target for which to get the extensions
	 * @return a list of all plugins for a specific extension point.
	 */
	public List<Plugin> getPlugins(String target) {
		return instantiatePlugins(target, true, false);
	}

	/**
	 * Checks that there are plugins registered for a specific extension point.
	 *
	 * @param target
	 *            target for which to get the extensions
	 * @return true, if there are registered plugins
	 */
	public boolean hasPlugins(String target) {
		return !instantiatePlugins(target, true, false).isEmpty();
	}

	/**
	 * Provides all plugins for a particular extension point.
	 *
	 * @param injectionPoint
	 *            CDI injection point.
	 * @return list with the found extensions.
	 */
	@Produces
	@ExtensionPoint("")
	public <T extends Plugin> Plugins<T> producePlugins(InjectionPoint injectionPoint) {
		ExtensionPoint extensionPoint = injectionPoint.getAnnotated().getAnnotation(ExtensionPoint.class);
		if (extensionPoint == null) {
			return new Plugins<>(null, Collections.emptyList());
		}

		// collect all possible plugins for the injection point
		List<T> plugins = instantiatePlugins(extensionPoint.value(), extensionPoint.singleton(),
				extensionPoint.reverseOrder());

		// collect any qualifiers from the injection point
		Map<Class<? extends Annotation>, Annotation> qualifiers = getInjectionPointQualifiers(injectionPoint);
		// filter the plugins based on the additional qualifiers
		return new Plugins<>(extensionPoint.value(), filterPluginsByQualifiers(plugins, qualifiers));
	}

	/**
	 * Gets the injection point qualifiers.
	 *
	 * @param injectionPoint
	 *            the injection point
	 * @return the injection point qualifiers
	 */
	private static Map<Class<? extends Annotation>, Annotation> getInjectionPointQualifiers(
			InjectionPoint injectionPoint) {
		Set<Annotation> injectionQualifiers = injectionPoint.getQualifiers();
		// it's -1 because the the ExtensionPoint annotation that should be present
		// the check is for optimization not to create a map for nothing
		int size = injectionQualifiers.size() - 1;
		if (size <= 0) {
			return Collections.emptyMap();
		}
		// collect all qualifiers on the injection point
		Map<Class<? extends Annotation>, Annotation> qualifiers = CollectionUtils.createHashMap(size);
		for (Annotation qualifier : injectionQualifiers) {
			// we are not interested in the extension point annotation but from all other qualifiers
			if (!(qualifier instanceof ExtensionPoint)) {
				qualifiers.put(qualifier.annotationType(), qualifier);
			}
		}
		return qualifiers;
	}

	/**
	 * Filter plugins by qualifiers that are set on the injection point.
	 *
	 * @param plugins
	 *            the plugins
	 * @param qualifiers
	 *            the qualifiers
	 * @return the iterable
	 */
	private static <T> List<T> filterPluginsByQualifiers(List<T> plugins,
			Map<Class<? extends Annotation>, Annotation> qualifiers) {
		// nothing to filter return now
		if (qualifiers.isEmpty()) {
			return plugins;
		}
		List<T> filtered = new ArrayList<>(plugins.size());
		for (T plugin : plugins) {
			// check if the plugin qualifiers match the filter criteria
			if (matchPluginToQualifiers((Plugin) plugin, qualifiers)) {
				filtered.add(plugin);
			}
		}
		return filtered;
	}

	/**
	 * Checks if the plugin matches the list of qualifiers found on the injection point. The current implementation does
	 * not check the qualifiers binding methods, but only the presence of an annotation.
	 *
	 * @param plugin
	 *            the plugin to match
	 * @param qualifiers
	 *            the qualifiers to match the plugin against.
	 * @return true, if plugin matches the qualifiers given.
	 */
	private static boolean matchPluginToQualifiers(Plugin plugin,
			Map<Class<? extends Annotation>, Annotation> qualifiers) {
		Annotation[] annotations = plugin.getClass().getAnnotations();
		for (Annotation annotation : annotations) {
			// skip the extension qualifier and documentation - they should not be part of the
			// filtering
			if (annotation instanceof Extension) {
				continue;
			}
			// check if the annotation is qualifier at all
			Qualifier qualifier = annotation.annotationType().getAnnotation(Qualifier.class);
			if (qualifier != null) {
				// check if the qualifier is present on the plugin
				Annotation filter = qualifiers.get(annotation.annotationType());
				if (filter != null) {
					return matchQualifiers(annotation, filter);
				}
			}
		}
		return false;
	}

	/**
	 * Match qualifiers by their binding methods.
	 *
	 * @param annotation
	 *            the annotation
	 * @param filter
	 *            the filter
	 * @return true, if successful
	 */
	private static boolean matchQualifiers(Annotation annotation, Annotation filter) {
		// get only the declared methods on the annotation and not all methods inherited from Object
		Method[] methods = annotation.annotationType().getDeclaredMethods();
		// nothing to filter they match
		if (methods == null || methods.length == 0) {
			return true;
		}
		boolean methodsMatch = true;
		for (Method method : methods) {
			// we should check only binding methods and skip non binding
			if (!method.isAnnotationPresent(Nonbinding.class)) {
				Object currentValue = getAnnotationMethodValue(annotation, method);
				Object filterValue = getAnnotationMethodValue(filter, method);
				// if all methods values match then we are good to go
				methodsMatch &= EqualsHelper.nullSafeEquals(currentValue, filterValue);
			}
		}
		return methodsMatch;
	}

	/**
	 * Gets the annotation method value.
	 *
	 * @param annotation
	 *            the annotation
	 * @param method
	 *            the method
	 * @return the annotation method value or <code>null</code> if nothing was set or some error occur
	 */
	private static Object getAnnotationMethodValue(Annotation annotation, Method method) {
		try {
			return method.invoke(annotation);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// we are not so interested in these exceptions here but we still going to log them
			LOGGER.trace("Could not get the value of method {} from qualifier {} with instance {} due to {}",
					method.getName(), annotation.annotationType(), annotation, e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Instatiates all plugins for a given extension point.
	 *
	 * @param target
	 *            target for which to get the extensions
	 * @param singleton
	 *            if true and if all the plugins are in @Dependent, they will be cached. If even one plugin is in
	 *            non-dependent scope, the plugins won't be cached.
	 * @param inverse
	 *            if true, the plugins will be served in reverse order.
	 * @return list with the instantiated plugins.
	 */
	@SuppressWarnings("unchecked")
	private <T> List<T> instantiatePlugins(String target, boolean singleton, boolean inverse) {
		List<T> result = (List<T>) cachedPlugins.get(target);
		if (result == null) {
			List<Bean<?>> extensionPointBeans = beans.get(target);
			// return empty list of no beans are found for the particular
			// extension point
			if (extensionPointBeans == null) {
				return Collections.emptyList();
			}

			result = new ArrayList<>(extensionPointBeans.size());
			for (Bean<?> bean : extensionPointBeans) {
				CreationalContext<Plugin> creationalContext = (CreationalContext<Plugin>) beanManager
						.createCreationalContext(bean);
				T pluginInstance = (T) beanManager.getReference(bean, Plugin.class, creationalContext);
				result.add(pluginInstance);
			}

			if (isEligableForCache(singleton, extensionPointBeans)) {
				// should prevent the build plugins list from modifications
				cachedPlugins.put(target, (List<Plugin>) Collections.unmodifiableList(result));
			}
		}

		if (inverse) {
			// if the reverse order attribute is true, copy extensions
			// in a new list and invert it
			result = new ArrayList<>(result);
			Collections.reverse(result);
		}

		return result;
	}

	/**
	 * Examine all plugins for the particular extension point and see if they're is a bean that is not in @Dependent
	 * scope. If there issuch bean, the extension point will not be cached.
	 *
	 * @param singleton
	 *            is the extension point singleton
	 * @param extensionPointBeans
	 *            beans mapped to the specific extension point.
	 * @return true if the beans should be cached, false otherwise.
	 */
	private static boolean isEligableForCache(boolean singleton, List<Bean<?>> extensionPointBeans) {
		boolean shouldCache = singleton;
		if (singleton) {
			for (Bean<?> bean : extensionPointBeans) {
				if (bean.getScope() != Dependent.class) {
					shouldCache = false;
					break;
				}
			}
		}
		return shouldCache;
	}

	/**
	 * Provides the @Extension annotation object for an object.
	 *
	 * @param bean
	 *            bean which extionsion information should be retrieved.
	 * @return current instance of @Extension annotation for the class of the provided object.
	 */
	private static Extension getExtensionMetadata(Bean<?> bean) {
		return (Extension) bean.getQualifiers().stream().filter(qualifier -> qualifier instanceof Extension).findFirst().get();
	}

	/**
	 * Compares plugins based on their order.
	 *
	 * @author Adrian Mitev
	 */
	private static class PluginComparator implements Comparator<Bean<?>>, Serializable {
		private static final long serialVersionUID = -4921207201920789610L;

		@Override
		public int compare(Bean<?> plugin1, Bean<?> plugin2) {
			return Double.compare(getExtensionMetadata(plugin1).order(), getExtensionMetadata(plugin2).order());
		}
	}

}
