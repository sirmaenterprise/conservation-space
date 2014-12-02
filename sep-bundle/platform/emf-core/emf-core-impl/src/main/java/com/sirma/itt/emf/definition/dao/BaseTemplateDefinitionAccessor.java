package com.sirma.itt.emf.definition.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.definition.DefinitionIdentityUtil;
import com.sirma.itt.emf.definition.MutableDictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.DefinitionEntry;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.hash.HashCalculator;
import com.sirma.itt.emf.security.context.SecurityContextManager;

/**
 * The Class BaseTemplateDefinitionAccessor.
 *
 * @author BBonev
 */
public abstract class BaseTemplateDefinitionAccessor extends BaseDefinitionAccessor {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8109357274106244882L;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(BaseTemplateDefinitionAccessor.class);

	@Inject
	protected HashCalculator hashCalculator;

	@Inject
	private Instance<MutableDictionaryService> mutableDictionaryService;

	/**
	 * Gets the template definition internal.
	 *
	 * @param <E>
	 *            the element type
	 * @param typeClass
	 *            the type class
	 * @param cache
	 *            the cache to update
	 * @return the template definition internal
	 */
	@SuppressWarnings("unchecked")
	protected <E extends DefinitionModel> List<E> getTemplateDefinitionInternal(Class<E> typeClass,
			EntityLookupCache<Pair<String, String>, E, Serializable> cache) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(2);
		args.add(new Pair<String, Object>("container", getCurrentContainer()));
		DataTypeDefinition type = getDataTypeDefinition(typeClass);
		args.add(new Pair<String, Object>("type", type.getId()));
		List<TopLevelDefinition> list = getDbDao().fetchWithNamed(
				EmfQueries.QUERY_DEFINITION_BY_ID_TYPE_KEY, args);
		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		List<E> result = new LinkedList<E>();
		Set<Pair<String, String>> handled = new HashSet<Pair<String, String>>(
				(int) (result.size() * 1.1), 0.95f);
		List<DefinitionModel> definitions = convertDefinitions(list);
		for (DefinitionModel definition : definitions) {
			if (definition instanceof TopLevelDefinition) {
				Pair<String, String> key = DefinitionIdentityUtil
						.createDefinitionPair((TopLevelDefinition) definition);
				if (handled.contains(key)) {
					continue;
				}
				if (definition instanceof BidirectionalMapping) {
					((BidirectionalMapping) definition).initBidirection();
				}
				handled.add(key);
				updateCache(definition, true, false);
				cache.setValue(key, (E) definition);
				result.add((E) definition);
			}
		}
		return result;
	}

	/**
	 * Saves definition template to database, but first remove any current.
	 *
	 * @param <E>
	 *            the element type
	 * @param src
	 *            the src
	 * @param cache
	 *            the cache
	 * @param accessor
	 *            the accessor
	 * @return the e
	 */
	protected <E extends DefinitionModel> E saveTemplate(E src,
			EntityLookupCache<Pair<String, String>, E, Serializable> cache,
			DefinitionAccessor accessor) {
		Pair<String, String> key = null;
		if (src instanceof TopLevelDefinition) {
			TopLevelDefinition topLevelDefinition = (TopLevelDefinition) src;
			key = DefinitionIdentityUtil.createDefinitionPair(topLevelDefinition);
			if (StringUtils.isNullOrEmpty(topLevelDefinition.getContainer())) {
				topLevelDefinition.setContainer(SecurityContextManager.NO_CONTAINER);
			}
		}
		// REVIEW: we have a potential NPE here 'key' may be null
		E definition = getDefinitionFromCache(key, cache);
		DefinitionEntry entry = null;
		if (definition != null) {
			if (mutableDictionaryService.get().isDefinitionEquals(src, definition)) {
				return definition;
			}
			LOGGER.debug("Found changes in template: " + key);
			// find non converted definition if any and update the internal model without
			// deleting the old one but replacing it
			List<DefinitionModel> list = getDefinitionsInternal(
					EmfQueries.QUERY_DEFINITION_BY_ID_CONTAINER_REVISION_KEY, key.getFirst(), 0L,
					key.getSecond(), detectDataTypeDefinition(src), null, false, false, false);
			if (!list.isEmpty()) {
				DefinitionModel model = list.get(0);
				if (model instanceof DefinitionEntry) {
					entry = (DefinitionEntry) model;
					entry.setTarget(src);
				}
			}
		}
		if (entry == null) {
			entry = createEntity((TopLevelDefinition) src, accessor);
		}
		getDbDao().saveOrUpdate(entry);
		cache.setValue(key, src);
		// NOTE: we should not update the task definitions Refs that has
		// a reference to this particular definition but to call an update
		// of the workflow definitions. The changes will be detected on
		// workflow definition compiling
		return src;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateCache(DefinitionModel definition, boolean propertiesOnly,
			boolean isMaxRevision) {

		injectLabelProvider(definition);
	}

}
