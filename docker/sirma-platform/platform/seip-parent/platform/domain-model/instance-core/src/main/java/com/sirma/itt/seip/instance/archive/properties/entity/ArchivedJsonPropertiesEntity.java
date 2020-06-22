package com.sirma.itt.seip.instance.archive.properties.entity;

import static com.sirma.itt.seip.instance.archive.properties.entity.ArchivedJsonPropertiesEntity.QUERY_BATCH_ARCHIVED_PROPERTY_VALUE_BY_VERSION_IDS;
import static com.sirma.itt.seip.instance.archive.properties.entity.ArchivedJsonPropertiesEntity.QUERY_BATCH_ARCHIVED_PROPERTY_VALUE_BY_VERSION_IDS_KEY;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.instance.properties.entity.JsonPropertiesEntity;

/**
 * Entity used to store data in <code><b>sep_archivedjsonproperties</b></code> table. It will store version properties
 * as JSON. Contains named queries for load/delete of single entity and batch load of entities.
 *
 * @author A. Kunchev
 */
@Entity
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Table(name = "sep_archivedjsonproperties")
@NamedQueries(@NamedQuery(name = QUERY_BATCH_ARCHIVED_PROPERTY_VALUE_BY_VERSION_IDS_KEY, query = QUERY_BATCH_ARCHIVED_PROPERTY_VALUE_BY_VERSION_IDS))
public class ArchivedJsonPropertiesEntity extends JsonPropertiesEntity {

	private static final long serialVersionUID = 703429776691942688L;

	/** Query batch ArchivedJsonPropertyValueEntity by <code>versionId</code>s. */
	public static final String QUERY_BATCH_ARCHIVED_PROPERTY_VALUE_BY_VERSION_IDS_KEY = "QUERY_BATCH_ARCHIVED_PROPERTY_VALUE_BY_VERSION_IDS";
	static final String QUERY_BATCH_ARCHIVED_PROPERTY_VALUE_BY_VERSION_IDS = "SELECT p FROM ArchivedJsonPropertiesEntity p WHERE p.versionId in (:versionId)";

	@Id
	private String versionId;

	public ArchivedJsonPropertiesEntity() {
		// required
	}

	public ArchivedJsonPropertiesEntity(String versionId, Map<String, Serializable> properties) {
		this.versionId = versionId;
		setProperties(properties);
	}

	@Override
	public String getId() {
		return versionId;
	}

	@Override
	public void setId(String id) {
		versionId = id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(versionId, getProperties());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		if (!super.equals(obj)) {
			return false;
		}
		ArchivedJsonPropertiesEntity that = ArchivedJsonPropertiesEntity.class.cast(obj);
		return Objects.equals(versionId, that.versionId);
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append("ArchivedJsonPropertiesEntity [versionId=").append(versionId)
					.append(", properties=").append(getProperties())
					.append("]").toString();
	}
}