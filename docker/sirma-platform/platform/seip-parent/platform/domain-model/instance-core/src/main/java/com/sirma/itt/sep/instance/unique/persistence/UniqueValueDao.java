package com.sirma.itt.sep.instance.unique.persistence;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.PersistenceUnits;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Dao implementation for interaction with unique tables.
 *
 * @author Boyan Tonchev.
 */
@ApplicationScoped
public class UniqueValueDao {

    private static final String QUERY_PARAMETER_DEFINITION_ID = "definitionId";
    private static final String QUERY_PARAMETER_FIELD_URI = "fieldUri";
    private static final String QUERY_PARAMETER_VALUE = "value";
    private static final String QUERY_PARAMETER_INSTANCE_ID = "instanceId";
    private static final String QUERY_PARAMETER_UNIQUE_FIELD_ID = "uniqueFieldId";

    private static final String INSERT_QUERY_PREFIX = "INSERT INTO sep_unique_values (value, instance_id, unique_field_id)  VALUES";
    private static final String VALUE_PATTERN_PREFIX = "(?{0}, ?{1}, ";
    private static final String VALUE_PATTERN_SUFFIX = ")";

    @Inject
    private DbDao dbDao;

    @PersistenceContext(unitName = PersistenceUnits.PRIMARY)
    private EntityManager entityManager;

    /**
     * Fetches instance id for which has registered value for property with <code>definitionId</code> and
     * <code>fieldUri</code>.
     *
     * @param definitionId
     *         - the definition id where property is defined.
     * @param fieldUri
     *         - the field uri of property.
     * @param value
     *         - value of property.
     * @return the instance or null if no instance registered for this value.
     */
    public String getInstanceId(String definitionId, String fieldUri, String value) {
        List<Pair<String, Object>> params = new QueryParametersBuilder()
                .addParameter(QUERY_PARAMETER_DEFINITION_ID, definitionId)
                .addParameter(QUERY_PARAMETER_FIELD_URI, fieldUri)
                .addParameter(QUERY_PARAMETER_VALUE, value)
                .build();

        List<UniqueValueEntity> uniqueValues = dbDao.fetchWithNamed(
                UniqueValueEntity.QUERY_NAME_SELECT_UNIQUE_VALUE_BY_VALUE_DEFINITION_ID_AND_FIELD_URI_CASE_INSENSITIVE, params);

        return uniqueValues.isEmpty() ? null : uniqueValues.get(0).getInstanceId();
    }

    /**
     * Fetches all registered unique fields from table "sep_unique_fields".
     *
     * @return all registered unique fields.
     */
    public List<UniqueField> getAllUniqueFields() {
        return dbDao.fetchWithNamed(UniqueFieldEntity.QUERY_NAME_SELECT_ALL_UNIQUE_FIELDS, Collections.emptyList());
    }

    /**
     * Unregisters all <code>toBeUnregistered</code> unique fields from table "sep_unique_fields". All registered values for them will be removed too.
     *
     * @param toBeUnregistered
     *         - the unique fields which have to be unregistered.
     */
    public void unRegister(Collection<UniqueField> toBeUnregistered) {
        toBeUnregistered.forEach(uniqueField -> {
            List<Pair<String, Object>> params = new QueryParametersBuilder()
                    .addParameter(QUERY_PARAMETER_DEFINITION_ID, uniqueField.getDefinitionId())
                    .addParameter(QUERY_PARAMETER_FIELD_URI, uniqueField.getFieldUri())
                    .build();
            dbDao.executeUpdate(UniqueFieldEntity.QUERY_NAME_DELETE_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI,
                                params);
        });
    }

    /**
     * Registers all <code>toBeRegistered</code> unique fields to table "sep_unique_fields".
     *
     * @param toBeRegistered
     *         - the unique fields which have to be registered.
     */
    public void register(Collection<UniqueField> toBeRegistered) {
        for (UniqueField uniqueField: toBeRegistered) {
            UniqueFieldEntity uniqueFieldEntity = new UniqueFieldEntity();
            uniqueFieldEntity.setDefinitionId(uniqueField.getDefinitionId());
            uniqueFieldEntity.setFieldUri(uniqueField.getFieldUri());
            dbDao.saveOrUpdate(uniqueFieldEntity);
        }
    }

    /**
     * Checks if there is registered value for property of instance with id <code>instanceId</code>,
     * <code>definitionId</code> and <code>fieldUri</code>.
     * If found it will be updated with <code>propertyValue</code>.
     * If not <code>propertyValue</code> will be registered as new one.
     *
     * @param instanceId
     *         - the instance id for which <code>propertyValue</code> will be register/update.
     * @param definitionId
     *         - the definition id where property is defined.
     * @param fieldUri
     *         - the field uri of property.
     * @param propertyValue
     *         - the property value which have to be registered.
     */
    public void registerOrUpdateUniqueValue(String instanceId, String definitionId, String fieldUri,
            String propertyValue) {
        UniqueFieldEntity uniqueFieldEntity = getUniqueField(definitionId, fieldUri);
        if (uniqueFieldEntity == null) {
            //nothing to do we have not registered unique field with such definitionId and fieldUri.
            return;
        }
        UniqueValueEntity uniqueValueEntity = fetchUniqueValue(instanceId, uniqueFieldEntity.getId());

        if (uniqueValueEntity == null) {
            registerUniqueValue(uniqueFieldEntity, instanceId, propertyValue);
        } else {
            updateUniqueValue(uniqueValueEntity, propertyValue);
        }
    }

    private void updateUniqueValue(UniqueValueEntity uniqueValueEntity, String newValue) {
        uniqueValueEntity.setValue(newValue);
        dbDao.saveOrUpdate(uniqueValueEntity);
    }

    private void registerUniqueValue(UniqueFieldEntity uniqueFieldEntity, String instanceId, String propertyValue) {
        UniqueValueEntity uniqueValueEntity = new UniqueValueEntity();
        uniqueValueEntity.setValue(propertyValue);
        uniqueValueEntity.setInstanceId(instanceId);
        uniqueValueEntity.setUniqueField(uniqueFieldEntity);
        dbDao.saveOrUpdate(uniqueValueEntity);
    }

    /**
     * Fetches registered {@link UniqueValueEntity} for <code>instanceId</code> and <code>uniqueFieldEntityId</code>
     *
     * @param instanceId
     *         - the instance id.
     * @param uniqueFieldEntityId
     *         - the id of {@link UniqueFieldEntity} for which value is registered.
     * @return the found value or null.
     */
    private UniqueValueEntity fetchUniqueValue(String instanceId, Long uniqueFieldEntityId) {
        List<Pair<String, Object>> params = new QueryParametersBuilder()
                .addParameter(QUERY_PARAMETER_INSTANCE_ID, instanceId)
                .addParameter(QUERY_PARAMETER_UNIQUE_FIELD_ID, uniqueFieldEntityId)
                .build();

        List<UniqueValueEntity> uniqueValues = dbDao.fetchWithNamed(
                UniqueValueEntity.QUERY_NAME_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_AND_UNIQUE_FIELD_ID, params);

        return uniqueValues.isEmpty() ? null : uniqueValues.get(0);
    }

    /**
     * Fetches {@link UniqueFieldEntity} with <code>definitionId</code> and <code>fieldUri</code>.
     *
     * @param definitionId
     *         - the definition id where property is defined.
     * @param fieldUri
     *         - the property field uri.
     * @return found unique field or null if not found.
     */
    private UniqueFieldEntity getUniqueField(String definitionId, String fieldUri) {
        List<Pair<String, Object>> params = new QueryParametersBuilder()
                .addParameter(QUERY_PARAMETER_DEFINITION_ID, definitionId)
                .addParameter(QUERY_PARAMETER_FIELD_URI, fieldUri)
                .build();

        List<UniqueFieldEntity> uniqueFields = dbDao.fetchWithNamed(
                UniqueFieldEntity.QUERY_NAME_SELECT_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI, params);

        return uniqueFields.isEmpty() ? null : uniqueFields.get(0);
    }

    /**
     * Fetches id of couple <code>definitionId</code> and <code>fieldUri</code>.
     *
     * @param definitionId
     *         - the definition id.
     * @param fieldUri
     *         - the field uri.
     * @return return id or null if couple <code>definitionId</code> and <code>fieldUri</code> is not registered.
     */
    public Long getUniqueFieldId(String definitionId, String fieldUri) {
        UniqueFieldEntity uniqueField = getUniqueField(definitionId, fieldUri);
        if (uniqueField == null) {
            return null;
        }
        return uniqueField.getId();
    }

    /**
     * Unregisters all registered unique values for instance with id <code>instanceId</code>.
     *
     * @param instanceId
     *         - the instance id.
     */
    public void unregisterAllUniqueValuesForInstance(String instanceId) {
        List<Pair<String, Object>> params = new QueryParametersBuilder()
                .addParameter(QUERY_PARAMETER_INSTANCE_ID, instanceId)
                .build();
        dbDao.executeUpdate(UniqueValueEntity.QUERY_NAME_DELETE_UNIQUE_VALUES_BY_INSTANCE_ID, params);
    }

    /**
     * Unregisters a registered value.
     *
     * @param instanceId
     *         - the instance id of instance for which value is registered.
     * @param definitionId
     *         - the definition id where property for value is defined.
     * @param fieldUri
     *         - the property field uri.
     */
    public void unregisterValue(String instanceId, String definitionId, String fieldUri) {
        List<Pair<String, Object>> params = new QueryParametersBuilder()
                .addParameter(QUERY_PARAMETER_INSTANCE_ID, instanceId)
                .addParameter(QUERY_PARAMETER_DEFINITION_ID, definitionId)
                .addParameter(QUERY_PARAMETER_FIELD_URI, fieldUri)
                .build();

        List<UniqueValueEntity> objects = dbDao.fetchWithNamed(
                UniqueValueEntity.QUERY_NAME_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_DEFINITION_ID_AND_FIELD_URI, params);

        if (!objects.isEmpty()) {
            dbDao.delete(UniqueValueEntity.class, objects.get(0).getId());
        }
    }

    /**
     * Generates native insert multi values query.
     * For example if:<br>
     * <b><code>rowCount</code> = 3<br>
     * <code>uniqueFieldId</code> = 720</b><br>
     * Generated query will be:
     * <pre>
     *     <b>INSERT INTO</b> sep_unique_values (value, instance_id, unique_field_id)  <b>VALUES</b>
     *     (?1, ?2, 720),
     *     (?3, ?4, 720),
     *     (?5, ?6, 720)
     * </pre>
     *
     * @param rowCount
     *         - how many rows will be inserted.
     * @param uniqueFieldId
     *         - the id of couple definitionId and fieldUri see {@link com.sirma.itt.sep.instance.unique.persistence.UniqueFieldEntity}
     * @return created query.
     */
    public Query buildNativeInsertUniqueValuesQuery(int rowCount, Long uniqueFieldId) {
        String valuePattern = buildValuePattern(uniqueFieldId);
        StringBuilder valuesQuery = new StringBuilder(INSERT_QUERY_PREFIX);
        int parameterIndex = 1;
        for (int index = 0; index < rowCount; index++) {
            valuesQuery.append(MessageFormat.format(valuePattern, parameterIndex++, parameterIndex++)).append(",");
        }
        //After update DB to PostgreSQL 9.5 add "ON CONFLICT DO NOTHING" after query. This will prevent constraint exception to be thrown
        //from DB. It will insert only rows which have not conflict.
        String query = valuesQuery.substring(0, valuesQuery.length() - 1);
        return  entityManager.createNativeQuery(query);
    }

    /**
     * Builds string pattern for value row. If<br>
     * <b><code>uniqueField</code> = 720</b><br>
     * Result will be<br>
     * <pre><b>
     *         (?{0}, ?{1}, 729)
     *     </pre></b>
     *
     * @param uniqueFieldId
     *         - the id of couple definitionId and fieldUri see {@link com.sirma.itt.sep.instance.unique.persistence.UniqueFieldEntity}
     * @return value pattern.
     */
    private String buildValuePattern(Long uniqueFieldId) {
        return VALUE_PATTERN_PREFIX + uniqueFieldId + VALUE_PATTERN_SUFFIX;
    }

    /**
     * Helper class for building query parameters.
     */
    private class QueryParametersBuilder {

        List<Pair<String, Object>> parameters = new LinkedList<>();

        private QueryParametersBuilder addParameter(String parameterName, Object parameterValue) {
            parameters.add(new Pair<>(parameterName, parameterValue));
            return this;
        }

        /**
         * Build added query parameters.
         *
         * @return the list with query parameters.
         */
        private List<Pair<String, Object>> build() {
            return parameters;
        }
    }
}
