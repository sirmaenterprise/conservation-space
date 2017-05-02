package com.sirma.itt.seip.plugin;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * {@link Plugins} is an injection wrapper object that provides access to set of plugin instances for iteration or
 * querying by plugin identity. For the identity resolving to work the plugins must implement the {@link Named}
 * interface or the user to provide custom identity resolver via the method {@link #setIdentityResolver(Function)}.
 *
 * @param <T>
 *            the plugin type
 * @author BBonev
 */
public class Plugins<T extends Plugin> implements Iterable<T>, Named, Serializable {

	private static final long serialVersionUID = 4910226748189367192L;

	/** The plugin instances represented by this wrapper */
	private Collection<T> pluginInstances;

	/** A temporary plugin mapping build using the provided resolver via {@link #setIdentityResolver(Function)} */
	private final Map<Object, T> pluginMapping = new HashMap<>();

	/** The resolver function to use for extracting unique identity from a plugin instance. */
	private Function<T, Object> resolver;

	/** The identity comparator used for plugin identity comparison. */
	private BiPredicate<Object, Object> identityComparator = getDefaultIdentityComparator();

	private final String pluginName;

	/**
	 * Instantiates a new plugins target.
	 *
	 * @param name
	 *            the plugin extension name
	 * @param plugins
	 *            the plugins
	 */
	public Plugins(String name, Collection<T> plugins) {
		pluginName = name;
		this.pluginInstances = plugins;
	}

	/**
	 * Sets the identity resolver. The resolver function will be used to resolve the plugin identifier when the method
	 * {@link #get(Object)} is called. The default identity resolving is {@link Named#getName()} if the plugin
	 * implements the {@link Named} interface. If no resolver is set and if the plugin does not implement {@link Named}
	 * interface then the {@link #get(Object)} will always return <code>null</code>.
	 *
	 * @param resolver
	 *            the resolver function to use for identity resolving of the plugins
	 */
	public void setIdentityResolver(Function<T, Object> resolver) {
		this.resolver = resolver;
		// reset the mapping for the new resolver
		pluginMapping.clear();
	}

	/**
	 * Sets the identity comparator function. The {@link BiPredicate} will be used to test if the current plugin id and
	 * the requested plugin id match. The predicate will be called with first argument the one passed to the method
	 * {@link #get(Object)} and with second argument the identity resolved by the identity resolver for the plugin being
	 * tested.
	 *
	 * @param predicate
	 *            the to use. If <code>null</code> is passed the default comparator will be used.
	 */
	public void setIdentityComparator(BiPredicate<Object, Object> predicate) {
		this.identityComparator = predicate == null ? getDefaultIdentityComparator() : predicate;
		// reset the mapping for the new comparator
		pluginMapping.clear();
	}

	/**
	 * Gets the default identity comparator used when comparing plugin identities.
	 *
	 * @return the default identity comparator
	 */
	@SuppressWarnings("static-method")
	protected BiPredicate<Object, Object> getDefaultIdentityComparator() {
		return EqualsHelper::nullSafeEquals;
	}

	/**
	 * Gets a plugin instance that reports the given identifier. The identifier is extracted from the plugin using the
	 * provided resolver via {@link #setIdentityResolver(Function)}
	 *
	 * @param identity
	 *            the identity to search for
	 * @return the plugin or empty {@link Optional}. The method may return empty {@link Optional} if the plugin is not
	 *         found or no correct resolver is set and the plugin identifier could not be identified.
	 */
	public Optional<T> get(Object identity) {
		if (identity == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(pluginMapping.computeIfAbsent(identity, this::resolvePlugin));
	}

	/**
	 * Select the first plugin that satisfies the given predicate
	 *
	 * @param predicate
	 *            the predicate to test against the plugins
	 * @return the optional object containing the plugin that matches the predicate
	 */
	public Optional<T> select(Predicate<T> predicate) {
		Objects.requireNonNull(predicate, "Cannot select with null predicate");
		return stream().filter(predicate).findFirst();
	}

	T resolvePlugin(Object key) {
		for (T plugin : this) {
			Object identifier = resolvePluginIdentifier(plugin, resolver);
			if (identifier != null && identityComparator.test(key, identifier)) {
				return plugin;
			}
		}
		return null;
	}

	/**
	 * Resolve plugin identifier using the set resolver if any or if the plugin implements {@link Named} interface.
	 *
	 * @param plugin
	 *            the plugin
	 * @param customResolver
	 *            the custom resolver to use
	 * @return the object
	 */
	protected Object resolvePluginIdentifier(T plugin, Function<T, Object> customResolver) {
		if (customResolver != null) {
			return customResolver.apply(plugin);
		} else if (plugin instanceof Named) {
			return ((Named) plugin).getName();
		}
		return null;
	}

	@Override
	public Iterator<T> iterator() {
		return pluginInstances.iterator();
	}

	@Override
	public Spliterator<T> spliterator() {
		return pluginInstances.spliterator();
	}

	/**
	 * Stream of all plugins represented by the current instance.
	 *
	 * @return the stream of plugin instances
	 */
	public Stream<T> stream() {
		return pluginInstances.stream();
	}

	/**
	 * The extension point name
	 *
	 * @return the name
	 */
	@Override
	public String getName() {
		return pluginName;
	}

	/**
	 * The number of available plugins
	 *
	 * @return the plugins count
	 */
	public int count() {
		return pluginInstances.size();
	}
}
