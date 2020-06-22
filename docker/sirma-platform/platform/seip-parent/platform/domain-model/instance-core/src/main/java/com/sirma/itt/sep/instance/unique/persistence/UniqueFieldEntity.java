package com.sirma.itt.sep.instance.unique.persistence;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * Entity of table "sep_unique_fields".
 * <pre>
 *     <table border="1">
 *         <tr><th colspan="4">sep_unique_fields</th></tr>
 *         <tr>
 *             <th>id</th>
 *             <th>definition_id</th>
 *             <th>field_uri</th>
 *         </tr>
 *         <tr>
 *             <td>1</td>
 *             <td>DT0007</td>
 *             <td>dcterms:description</td>
 *         </tr>
 *     </table>
 * </pre>
 *
 * @author Boyan Tonchev.
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "sep_unique_fields")
@NamedQueries(value = {
        @NamedQuery(name = UniqueFieldEntity.QUERY_NAME_SELECT_ALL_UNIQUE_FIELDS, query = UniqueFieldEntity.QUERY_SELECT_ALL_UNIQUE_FIELDS),
        @NamedQuery(name = UniqueFieldEntity.QUERY_NAME_DELETE_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI,
                query = UniqueFieldEntity.QUERY_DELETE_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI),
        @NamedQuery(name = UniqueFieldEntity.QUERY_NAME_SELECT_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI,
                query = UniqueFieldEntity.QUERY_SELECT_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI), })
class UniqueFieldEntity extends BaseEntity implements UniqueField {

    private static final long serialVersionUID = 4220902068142821663L;

    static final String QUERY_NAME_SELECT_ALL_UNIQUE_FIELDS = "QUERY_SELECT_ALL_UNIQUE_FIELDS";
    static final String QUERY_SELECT_ALL_UNIQUE_FIELDS = "SELECT uniqueField FROM UniqueFieldEntity uniqueField";

    static final String QUERY_NAME_DELETE_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI = "QUERY_DELETE_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI";
    static final String QUERY_DELETE_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI =
            "DELETE FROM UniqueFieldEntity uniqueField " + "WHERE uniqueField.definitionId=:definitionId "
                    + "AND uniqueField.fieldUri=:fieldUri";

    static final String QUERY_NAME_SELECT_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI = "QUERY_SELECT_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI";
    static final String QUERY_SELECT_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI =
            "SELECT uniqueField FROM UniqueFieldEntity uniqueField " + "WHERE uniqueField.definitionId=:definitionId "
                    + "AND uniqueField.fieldUri=:fieldUri";

    /**
     * Definition id where unique field is defined.
     */
    @Column(name = "definition_id", nullable = false, updatable = false)
    private String definitionId;

    /**
     * Field uri of the unique field.
     */
    @Column(name = "field_uri", nullable = false, updatable = false)
    private String fieldUri;

    /**
     * All registered values for current unique field.
     */
    @OneToMany(mappedBy = "uniqueField", orphanRemoval = true)
    private List<UniqueValueEntity> uniqueValues;

    /**
     * Getter for definition id where property is defined.
     *
     * @return definition id where property is defined.
     */
    public String getDefinitionId() {
        return definitionId;
    }

    /**
     * Setter for definition id where property is defined.
     *
     * @param definitionId
     *         - the definition id where property is defined.
     */
    void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    /**
     * Getter for filed uri.
     *
     * @return the field uri.
     */
    public String getFieldUri() {
        return fieldUri;
    }

    /**
     * Setter for field uri.
     *
     * @param fieldUri
     *         the field uri.
     */
    void setFieldUri(String fieldUri) {
        this.fieldUri = fieldUri;
    }

    /**
     * Getter for all registered values.
     *
     * @return list with all registered values.
     */
    List<UniqueValueEntity> getUniqueValues() {
        return uniqueValues;
    }

    /**
     * Setter for all registered unique values.
     *
     * @param uniqueValues
     *         all registered unique values.
     */
    void setUniqueValues(List<UniqueValueEntity> uniqueValues) {
        this.uniqueValues = uniqueValues;
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

        UniqueFieldEntity that = (UniqueFieldEntity) o;

        if (!getDefinitionId().equals(that.getDefinitionId())) {
            return false;
        }
        return getFieldUri().equals(that.getFieldUri());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getDefinitionId().hashCode();
        result = 31 * result + getFieldUri().hashCode();
        return result;
    }
}
