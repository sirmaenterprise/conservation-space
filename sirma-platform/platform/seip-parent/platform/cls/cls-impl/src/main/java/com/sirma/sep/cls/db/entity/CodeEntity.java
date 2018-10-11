package com.sirma.sep.cls.db.entity;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Objects;

/**
 * Abstract database entity for common properties of {@link com.sirma.sep.cls.model.Code}.
 *
 * @author Mihail Radkov
 */
@MappedSuperclass
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
public abstract class CodeEntity extends BaseEntity {

	@Column(length = 255, name = "value")
	private String value;

	@Column(length = 1000, name = "extra1")
	private String extra1;

	@Column(length = 1000, name = "extra2")
	private String extra2;

	@Column(length = 1000, name = "EXTRA3")
	private String extra3;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getExtra1() {
		return extra1;
	}

	public void setExtra1(String extra1) {
		this.extra1 = extra1;
	}

	public String getExtra2() {
		return extra2;
	}

	public void setExtra2(String extra2) {
		this.extra2 = extra2;
	}

	public String getExtra3() {
		return extra3;
	}

	public void setExtra3(String extra3) {
		this.extra3 = extra3;
	}

	/**
	 * Used to determine if the code entity is existing or brand new.
	 *
	 * @return true if the entity exists or false if not
	 */
	public boolean exists() {
		return getId() != null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), value, extra1, extra2, extra3);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CodeEntity)) {
			return false;
		}

		CodeEntity entity = (CodeEntity) obj;
		return super.equals(obj) && Objects.equals(value, entity.value) && Objects.equals(extra1, entity.extra2) && Objects
				.equals(extra2, entity.extra2) && Objects.equals(extra3, entity.extra3);
	}
}
