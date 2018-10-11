package com.sirma.itt.sep.instance.unique.persistence;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entity represent table "sep_unique_values".
 * <pre>
 *     <table border="1">
 *         <tr>
 *             <th colspan="4">sep_unique_values</th>
 *         </tr>
 *         <tr>
 *             <th>id</th>
 *             <th>instance_id</th>
 *             <th>value</th>
 *             <th>unique_field_id</th>
 *         </tr>
 *         <tr>
 *             <td>1</td>
 *             <td>emf:0001</td>
 *             <td>value of property</td>
 *             <td>2</td>
 *         </tr>
 *     </table>
 * </pre>
 *
 * @author Boyan Tonchev.
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "sep_unique_values")
@NamedQueries(value = { @NamedQuery(
        name = UniqueValueEntity.QUERY_NAME_SELECT_UNIQUE_VALUE_BY_VALUE_DEFINITION_ID_AND_FIELD_URI,
        query = UniqueValueEntity.QUERY_SELECT_UNIQUE_VALUE_BY_VALUE_DEFINITION_ID_AND_FIELD_URI),
        @NamedQuery(name = UniqueValueEntity.QUERY_NAME_SELECT_UNIQUE_VALUE_BY_VALUE_DEFINITION_ID_AND_FIELD_URI_CASE_INSENSITIVE,
                query = UniqueValueEntity.QUERY_SELECT_UNIQUE_VALUE_BY_VALUE_DEFINITION_ID_AND_FIELD_URI_CASE_INSENSITIVE),
        @NamedQuery(name = UniqueValueEntity.QUERY_NAME_DELETE_UNIQUE_VALUES_BY_INSTANCE_ID,
                query = UniqueValueEntity.QUERY_DELETE_UNIQUE_VALUES_BY_INSTANCE_ID),
        @NamedQuery(name = UniqueValueEntity.QUERY_NAME_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_AND_UNIQUE_FIELD_ID,
                query = UniqueValueEntity.QUERY_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_UNIQUE_AND_FIELD_ID),
        @NamedQuery(
                name = UniqueValueEntity.QUERY_NAME_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_DEFINITION_ID_AND_FIELD_URI,
                query = UniqueValueEntity.QUERY_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_DEFINITION_ID_AND_FIELD_URI) })
class UniqueValueEntity extends BaseEntity {

    private static final long serialVersionUID = 5996551440019097323L;

    private static final String SELECT_VALUE_ENTITY = "SELECT uniqueValueEntity FROM UniqueValueEntity uniqueValueEntity ";

    static final String QUERY_NAME_SELECT_UNIQUE_VALUE_BY_VALUE_DEFINITION_ID_AND_FIELD_URI = "QUERY_SELECT_UNIQUE_VALUE_BY_VALUE_DEFINITION_ID_AND_FIELD_URI";
    static final String QUERY_SELECT_UNIQUE_VALUE_BY_VALUE_DEFINITION_ID_AND_FIELD_URI =
            SELECT_VALUE_ENTITY
                    + "WHERE uniqueValueEntity.value = :value "
                    + "AND uniqueValueEntity.uniqueField.definitionId = :definitionId "
                    + "AND uniqueValueEntity.uniqueField.fieldUri = :fieldUri";

    static final String QUERY_NAME_SELECT_UNIQUE_VALUE_BY_VALUE_DEFINITION_ID_AND_FIELD_URI_CASE_INSENSITIVE = "QUERY_SELECT_UNIQUE_VALUE_BY_VALUE_DEFINITION_ID_AND_FIELD_URI_CASE_INSENSITIVE";
    static final String QUERY_SELECT_UNIQUE_VALUE_BY_VALUE_DEFINITION_ID_AND_FIELD_URI_CASE_INSENSITIVE =
            SELECT_VALUE_ENTITY
                    + "WHERE trim(lower(uniqueValueEntity.value)) = trim(lower(:value)) "
                    + "AND uniqueValueEntity.uniqueField.definitionId = :definitionId "
                    + "AND uniqueValueEntity.uniqueField.fieldUri = :fieldUri";

    static final String QUERY_NAME_DELETE_UNIQUE_VALUES_BY_INSTANCE_ID = "QUERY_DELETE_UNIQUE_VALUES_BY_INSTANCE_ID";
    static final String QUERY_DELETE_UNIQUE_VALUES_BY_INSTANCE_ID =
            "DELETE FROM UniqueValueEntity " + "WHERE instanceId = :instanceId ";

    static final String QUERY_NAME_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_AND_UNIQUE_FIELD_ID = "QUERY_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_UNIQUE_AND_FIELD_ID";
    static final String QUERY_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_UNIQUE_AND_FIELD_ID =
            SELECT_VALUE_ENTITY
                    + "WHERE uniqueValueEntity.instanceId = :instanceId "
                    + "AND uniqueValueEntity.uniqueField.id = :uniqueFieldId";

    static final String QUERY_NAME_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_DEFINITION_ID_AND_FIELD_URI = "QUERY_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_DEFINITION_ID_AND_FIELD_URI";
    static final String QUERY_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_DEFINITION_ID_AND_FIELD_URI =
            SELECT_VALUE_ENTITY
                    + "WHERE uniqueValueEntity.instanceId = :instanceId "
                    + "AND uniqueValueEntity.uniqueField.definitionId = :definitionId "
                    + "AND uniqueValueEntity.uniqueField.fieldUri = :fieldUri ";

    /**
     * Registered unique field. It hold couples definitionId and fieldUri.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unique_field_id")
    private UniqueFieldEntity uniqueField;

    @Column(name = "value", nullable = false)
    private String value;

    /**
     * Instance id for which <code>value</code> is set.
     */
    @Column(name = "instance_id", nullable = false)
    private String instanceId;

    /**
     * getter for registered unique field for which value is set.
     *
     * @return the registered unique field.
     */
    UniqueFieldEntity getUniqueField() {
        return uniqueField;
    }

    /**
     * Setter for registered unique field.
     *
     * @param uniqueField
     *         - the unique field.
     */
    void setUniqueField(UniqueFieldEntity uniqueField) {
        this.uniqueField = uniqueField;
    }

    /**
     * Getter for value.
     *
     * @return the value.
     */
    String getValue() {
        return value;
    }

    /**
     * Setter for value.
     *
     * @param value
     *         - the value.
     */
    void setValue(String value) {
        this.value = value;
    }

    /**
     * Getter of instance id for which value is set.
     *
     * @return - the id of instance.
     */
    String getInstanceId() {
        return instanceId;
    }

    /**
     * Setter for instance id.
     *
     * @param instanceId
     *         - the instance id.
     */
    void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        UniqueValueEntity that = (UniqueValueEntity) o;

        if (!uniqueField.equals(that.uniqueField)) {
            return false;
        }
        if (!value.equals(that.value)) {
            return false;
        }
        return instanceId.equals(that.instanceId);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + uniqueField.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + instanceId.hashCode();
        return result;
    }
}
