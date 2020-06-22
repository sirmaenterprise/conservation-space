package com.sirma.itt.seip.definition.label;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.exceptions.DatabaseException;
import com.sirma.itt.seip.definition.model.LabelImpl;
import com.sirma.itt.seip.model.SerializableValue;

/**
 * Default implementation for label service
 *
 * @author BBonev
 */
@ApplicationScoped
public class LabelServiceImpl implements LabelService {

	@CacheConfiguration(eviction = @Eviction(maxEntries = 2000), doc = @Documentation(""
			+ "Cache used to store the definition label entries. There is an entry for every unique defined label in all definitions. "
			+ "<br>Minimal value expression: labels * 1.2"))
	private static final String LABEL_CACHE = "LABEL_CACHE";

	@Inject
	private DbDao dbDao;

	@Inject
	private EntityLookupCacheContext cacheContext;

	@PostConstruct
	void init() {
		if (!cacheContext.containsCache(LABEL_CACHE)) {
			cacheContext.createCache(LABEL_CACHE, new LabelLookup(dbDao));
		}
	}

	@Override
	public LabelDefinition getLabel(String name) {
		Pair<String, LabelDefinition> pair = getLabelCache().getByKey(name);
		if (pair != null) {
			return pair.getSecond();
		}
		return null;
	}

	@Override
	public List<LabelDefinition> getLabelsDefinedIn(String identifier) {
		List<LabelImpl> labels = queryLabelsByDefinedIn(identifier);
		labels.forEach(LabelServiceImpl::deserializeLabels);
		return new ArrayList<>(labels);
	}

	@Override
	public boolean saveLabel(LabelDefinition labelDefinition) {
		if (labelDefinition == null) {
			return false;
		}
		LabelDefinition definition = getLabel(labelDefinition.getIdentifier());
		if (definition instanceof LabelImpl) {
			LabelImpl second = (LabelImpl) definition;
			mergeAndSaveLabels(second, labelDefinition);
			return true;
		}
		return persistNewLabel(labelDefinition);
	}

	private void copyDefinedIn(LabelDefinition from, LabelImpl to) {
		Set<String> definedIn = from.getDefinedIn();
		if (definedIn != null) {
			definedIn.forEach(to::addDefinedIn);
		}
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean saveLabels(List<LabelDefinition> definitions) {
		if (CollectionUtils.isEmpty(definitions)) {
			return false;
		}
		Map<String, LabelDefinition> labelIds = definitions.stream()
				.collect(CollectionUtils.toIdentityMap(LabelDefinition::getIdentifier));
		List<LabelImpl> list = queryLabelsByIds(labelIds);

		// update the labels that are in the DB
		for (LabelImpl labelImpl : list) {
			LabelDefinition definition = labelIds.remove(labelImpl.getIdentifier());
			if (definition == null) {
				// should not happen or something is very wrong!
				continue;
			}
			mergeAndSaveLabels(labelImpl, definition);
		}

		// persist new labels
		for (LabelDefinition labelDefinition : labelIds.values()) {
			persistNewLabel(labelDefinition);
		}
		return true;
	}

	private List<LabelImpl> queryLabelsByIds(Map<String, LabelDefinition> labelIds) {
		return dbDao.fetchWithNamed(LabelImpl.QUERY_LABELS_BY_ID_KEY,
				Collections.singletonList(new Pair<>("labelId", labelIds.keySet())));
	}

	private List<LabelImpl> queryLabelsByDefinedIn(String identifier) {
		String wildCardIdentifier = "%" + identifier + "%";
		return dbDao.fetchWithNamed(LabelImpl.QUERY_LABELS_BY_DEFINED_IN_KEY,
				Collections.singletonList(new Pair<>("definedIn", new LinkedHashSet<>(Collections.singletonList(wildCardIdentifier)))));
	}

	private boolean persistNewLabel(LabelDefinition labelDefinition) {
		Map<String, String> labels = labelDefinition.getLabels();
		if (labels instanceof Serializable) {
			LabelImpl impl = (LabelImpl) labelDefinition;
			if (impl.getValue() == null) {
				impl.setValue(new SerializableValue());
			}
			impl.getValue().setSerializable((Serializable) labels);
			copyDefinedIn(labelDefinition, impl);

			impl = dbDao.saveOrUpdate(impl);
			getLabelCache().setValue(impl.getIdentifier(), impl);
			return true;
		}
		return false;
	}

	private void mergeAndSaveLabels(LabelImpl labelImpl, LabelDefinition definition) {
		labelImpl.setLabels(Collections.unmodifiableMap(definition.getLabels()));
		labelImpl.getValue().setSerializable((Serializable) definition.getLabels());
		copyDefinedIn(definition, labelImpl);

		dbDao.saveOrUpdate(labelImpl);
		getLabelCache().setValue(labelImpl.getIdentifier(), labelImpl);
	}

	private static void deserializeLabels(LabelImpl impl) {
		SerializableValue serializableValue = impl.getValue();
		if (serializableValue != null && serializableValue.getSerializable() instanceof Map) {
			Map<String, String> labels = (Map<String, String>) serializableValue.getSerializable();
			impl.setLabels(Collections.unmodifiableMap(labels));
		}
	}

	private EntityLookupCache<String, LabelDefinition, Serializable> getLabelCache() {
		return cacheContext.getCache(LABEL_CACHE);
	}

	@Override
	public void clearCache() {
		getLabelCache().clear();
	}

	/**
	 * The Class LabelLookup.
	 *
	 * @author BBonev
	 */
	private static class LabelLookup extends EntityLookupCallbackDAOAdaptor<String, LabelDefinition, Serializable> {

		private final DbDao dbDao;

		LabelLookup(DbDao dbDao) {
			this.dbDao = dbDao;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Pair<String, LabelDefinition> findByKey(String key) {
			List<LabelImpl> list = dbDao.fetchWithNamed(LabelImpl.QUERY_LABEL_BY_ID_KEY,
					Collections.singletonList(new Pair<>("labelId", key)));
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() > 1) {
				throw new DatabaseException("More then one record found for label: " + key);
			}
			LabelImpl impl = list.get(0);
			deserializeLabels(impl);
			return new Pair<>(key, impl);
		}

		@Override
		public Pair<String, LabelDefinition> createValue(LabelDefinition value) {
			throw new UnsupportedOperationException("Labels are persisted externally");
		}

	}

}
