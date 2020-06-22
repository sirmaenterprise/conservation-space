package com.sirma.itt.seip.instance.properties.entity;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Convert;
import javax.persistence.MappedSuperclass;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

/**
 * Base entity that could be used when specific data should be stored as JSON in DB. Contains one attribute of type
 * {@link Map}, which has custom converter that will transform it to JSON that could be stored directly in the DB.
 *
 * @author A. Kunchev
 * @see JsonPropertiesConverter
 */
@MappedSuperclass
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
public abstract class JsonPropertiesEntity implements Entity<String>, Serializable {

	private static final long serialVersionUID = 970450338772870164L;

	@Convert(converter = JsonPropertiesConverter.class)
	private Map<String, Serializable> properties;

	public Map<String, Serializable> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Serializable> properties) {
		this.properties = properties;
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
		JsonPropertiesEntity that = JsonPropertiesEntity.class.cast(obj);
		return Objects.equals(getId(), that.getId()) && Objects.equals(properties, that.properties);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId(), properties);
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append("JsonPropertiesEntity [id=").append(getId())
				.append(", properties=").append(properties)
				.append("]").toString();
	}
}