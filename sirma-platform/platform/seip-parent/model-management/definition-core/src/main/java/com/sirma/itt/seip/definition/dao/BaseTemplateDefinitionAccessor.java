package com.sirma.itt.seip.definition.dao;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.BaseDefinitionAccessor;
import com.sirma.itt.seip.definition.DefinitionAccessor;
import com.sirma.itt.seip.definition.MutableDefinitionService;
import com.sirma.itt.seip.definition.TopLevelDefinition;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.domain.BidirectionalMapping;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.sep.definition.db.DefinitionEntry;

/**
 * The Class BaseTemplateDefinitionAccessor.
 *
 * @author BBonev
 */
public abstract class BaseTemplateDefinitionAccessor extends BaseDefinitionAccessor {

	protected static final Long ZERO = Long.valueOf(0);

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	protected HashCalculator hashCalculator;

	@Inject
	private Instance<MutableDefinitionService> mutableDefinitionService;

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
			EntityLookupCache<String, E, Serializable> cache) {
		DataTypeDefinition type = getDataTypeDefinition(typeClass);
		if (type == null) {
			return Collections.emptyList();
		}
		List<TopLevelDefinition> list = getDbDao().fetchWithNamed(DefinitionEntry.QUERY_DEFINITION_BY_ID_TYPE_KEY,
				Collections.singletonList(new Pair<>("type", type.getId())));

		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		List<E> result = new ArrayList<>(list.size());
		Set<String> handled = CollectionUtils.createHashSet(list.size());
		List<DefinitionModel> definitions = convertDefinitions(list.stream());
		for (DefinitionModel definition : definitions) {
			if (definition instanceof TopLevelDefinition) {
				String key = definition.getIdentifier();
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
	protected <E extends DefinitionModel> E saveTemplate(E src, EntityLookupCache<String, E, Serializable> cache,
			DefinitionAccessor accessor) {
		String key = src.getIdentifier();
		// REVIEW: we have a potential NPE here 'key' may be null
		E definition = getDefinitionFromCache(key, cache);
		DefinitionEntry entry = null;
		if (definition != null) {
			if (mutableDefinitionService.get().isDefinitionEquals(src, definition)) {
				return definition;
			}
			LOGGER.debug("Found changes in template: {}", key);
			// find non converted definition if any and update the internal model without
			// deleting the old one but replacing it
			List<DefinitionModel> list = getDefinitionsInternal(DefinitionEntry.QUERY_DEFINITION_BY_ID_REVISION_KEY,
					key, ZERO, detectDataTypeDefinition(src), null, false, false, false);
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

	@Override
	protected void updateCache(DefinitionModel definition, boolean propertiesOnly, boolean isMaxRevision) {
		injectLabelProvider(definition);
	}
}
