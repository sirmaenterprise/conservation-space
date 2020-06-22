package com.sirma.itt.seip.instance.properties.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.instance.properties.RichtextPropertiesDao;
import com.sirma.sep.content.idoc.sanitizer.IdocSanitizer;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides functionality to retrieve, persist and manage richtext properties in the relational DB.
 *
 * @author S.Djulgerova
 */
@Singleton
public class RichtextPropertiesDaoImpl implements RichtextPropertiesDao {

	private static final String INSTANCE_ID = "instanceId";
	private static final String PROPERTY_ID = "propertyId";

	@Inject
	DefinitionService definitionService;

	@Inject
	private IdocSanitizer sanitizer;

	@Inject
	private DbDao dbDao;

	@Override
	public void saveOrUpdate(String instanceId, Long propertyId, String value) {
		String sanitized = value;
		if(!StringUtils.isBlank(value)) {
			sanitized = sanitizer.sanitize(value);
		}
		RichtextPropertyEntity entity = new RichtextPropertyEntity(instanceId, propertyId, sanitized);
		Optional<RichtextPropertyEntity> existingEntity = fetchRichtextProperty(instanceId, propertyId);
		existingEntity.ifPresent(existing -> entity.setId(existing.getId()));
		dbDao.saveOrUpdate(entity);
	}

	/**
	 * Fetch property value by given ids
	 * 
	 * @param instanceId
	 *            instance id
	 * @param propertyId
	 *            property id
	 * @return richtext property entity or empty optional if no such record exist
	 */
	private Optional<RichtextPropertyEntity> fetchRichtextProperty(String instanceId, Long propertyId) {
		String query = RichtextPropertyEntity.QUERY_RICHTEXT_PROPERTY_KEY;

		List<Pair<String, Object>> params = new ArrayList<>(2);
		params.add(new Pair<String, Object>(INSTANCE_ID, instanceId));
		params.add(new Pair<String, Object>(PROPERTY_ID, propertyId));
		List<RichtextPropertyEntity> fetched = dbDao.fetchWithNamed(query, params);

		if (!fetched.isEmpty()) {
			return Optional.of(fetched.get(0));
		}
		return Optional.empty();
	}

	@Override
	public Map<String, Serializable> fetchByInstanceId(String instanceId) {

		Map<String, Serializable> properties = new HashMap<>();
		String query = RichtextPropertyEntity.QUERY_RICHTEXT_PROPERTIES_KEY;

		List<Pair<String, Object>> params = new ArrayList<>(1);
		params.add(new Pair<String, Object>(INSTANCE_ID, instanceId));
		List<RichtextPropertyEntity> resultList = dbDao.fetchWithNamed(query, params);

		for (RichtextPropertyEntity property : resultList) {
			PrototypeDefinition currentPropertyDef = definitionService.getProperty(property.getPropertyId());
			properties.put(currentPropertyDef.getIdentifier(), property.getContent());
		}

		return properties;
	}

	@Override
	public <S extends Serializable> Map<String, Map<String, Serializable>> fetchByInstanceIds(List<S> ids) {
		String query = RichtextPropertyEntity.QUERY_RICHTEXT_PROPERTIES_BY_IDS_KEY;
		List<RichtextPropertyEntity> resultList = dbDao.fetchWithNamed(query,
				Collections.singletonList(new Pair<>("ids", ids)));

		Map<String, Map<String, Serializable>> instanceProperties = new HashMap<>();
		Map<Long, PrototypeDefinition> propertyDef = new HashMap<>();
		for (RichtextPropertyEntity property : resultList) {
			Map<String, Serializable> properties = instanceProperties.computeIfAbsent(property.getInstanceId(),
					k -> new HashMap<>());
			PrototypeDefinition currentPropertyDef = propertyDef.computeIfAbsent(property.getPropertyId(),
					definitionService::getProperty);
			properties.put(currentPropertyDef.getIdentifier(), property.getContent());
		}
		return instanceProperties;
	}

	@Override
	public void delete(String instanceId) {
		List<Pair<String, Object>> args = new ArrayList<>(1);
		args.add(new Pair<String, Object>(INSTANCE_ID, instanceId));
		dbDao.executeUpdate(RichtextPropertyEntity.DELETE_RICHTEXT_PROPERTIES_KEY, args);
	}
}
