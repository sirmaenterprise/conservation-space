package com.sirma.itt.seip.db;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Extension definition for plugin query parsing for virtual {@link DbDao}. The implementation should extract from the
 * given arguments only the entity id that need to be loaded.
 *
 * @author BBonev
 */
public interface VirtualDbQueryParser extends Plugin {

	/** The plugin name. */
	String PLUGIN_NAME = "virtualDbQueryParser";

	/**
	 * Parses query and arguments passed to the methods {@link DbDao#fetch(String, List)} and
	 * {@link DbDao#fetch(String, List, int, int)}
	 *
	 * @param <R>
	 *            the result entity id type
	 * @param <E>
	 *            the arguments type
	 * @param query
	 *            the query
	 * @param params
	 *            the parameters
	 * @return the optional containing the ids if any.
	 */
	<R, E extends Pair<String, Object>> Optional<Collection<R>> parse(String query, List<E> params);

	/**
	 * Parses the query name and arguments passed to the methods {@link DbDao#fetchWithNamed(String, List)}
	 * {@link DbDao#fetchWithNamed(String, List, int, int)}
	 *
	 * @param <R>
	 *            the result entity id type
	 * @param <E>
	 *            the arguments type
	 * @param namedQuery
	 *            the named query
	 * @param params
	 *            the parameters
	 * @return the optional containing the ids if any.
	 */
	<R, E extends Pair<String, Object>> Optional<Collection<R>> parseNamed(String namedQuery, List<E> params);
}
