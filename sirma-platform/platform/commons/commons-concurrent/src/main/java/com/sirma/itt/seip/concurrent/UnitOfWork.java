package com.sirma.itt.seip.concurrent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Represents a single unit of work. The unit could contains one or more elements. All elements in a unit are processed
 * successfully or none of them are. Each unit has unique identifier to distinguish it from other units.
 *
 * @author BBonev
 * @param <T>
 *            the generic type
 */
public class UnitOfWork<T> implements Serializable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -741489172144684097L;

	/** The Constant CANNOT_CREATE_WORK_UNIT_WITHOUT_ID. */
	private static final String CANNOT_CREATE_WORK_UNIT_WITHOUT_ID = "Cannot create work unit without ID";

	/** The Constant EMPTY_UNIT. */
	public static final UnitOfWork<?> EMPTY_UNIT = new UnitOfWork<>("EMPTY_UNIT", Collections.emptyList())
			.setStatus(WorkStatus.SUCCESS);

	/** The unit data. */
	protected final List<T> unitData;

	/** The unit id. */
	protected final Serializable unitId;

	/** The status. */
	private WorkStatus status;

	/**
	 * Instantiates a new upload unit.
	 *
	 * @param unitId
	 *            the unit id
	 * @param instance
	 *            the instance
	 * @param elements
	 *            the document instances
	 */
	@SuppressWarnings("unchecked")
	public UnitOfWork(Serializable unitId, T instance, T... elements) {
		this.unitId = Objects.requireNonNull(unitId, CANNOT_CREATE_WORK_UNIT_WITHOUT_ID);
		if (elements == null || elements.length == 0) {
			if (instance == null) {
				unitData = Collections.emptyList();
			} else {
				unitData = Collections.singletonList(instance);
			}
		} else {
			unitData = new ArrayList<>(elements.length + 1);
			unitData.add(instance);
			// using for operation not to create new list from the array
			for (int i = 0; i < elements.length; i++) {
				unitData.add(elements[i]);
			}
		}
	}

	/**
	 * Instantiates a new upload unit.
	 *
	 * @param unitId
	 *            the unit id
	 * @param unitData
	 *            the unit data
	 */
	public UnitOfWork(Serializable unitId, List<T> unitData) {
		this.unitId = Objects.requireNonNull(unitId, CANNOT_CREATE_WORK_UNIT_WITHOUT_ID);
		this.unitData = unitData;
	}

	/**
	 * Gets the unit id.
	 *
	 * @return the unit id
	 */
	public Serializable getUnitId() {
		return unitId;
	}

	/**
	 * Gets the unit data.
	 *
	 * @return the unit data
	 */
	public List<T> getUnitData() {
		return unitData;
	}

	/**
	 * Getter method for status.
	 *
	 * @return the status
	 */
	public WorkStatus getStatus() {
		return status;
	}

	/**
	 * Setter method for status.
	 *
	 * @param status
	 *            the status to set
	 * @return the same instance
	 */
	public UnitOfWork<T> setStatus(WorkStatus status) {
		this.status = status;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (unitId == null ? 0 : unitId.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UnitOfWork)) {
			return false;
		}
		UnitOfWork<?> other = (UnitOfWork<?>) obj;
		return EqualsHelper.nullSafeEquals(unitId, other.unitId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UnitOfWork [unitId=");
		builder.append(unitId);
		builder.append(", status=");
		builder.append(status);
		builder.append(", unitData=");
		builder.append(unitData.size());
		builder.append(" elements]");
		return builder.toString();
	}

	/**
	 * Defines the outcome for the given unit of work.
	 *
	 * @author BBonev
	 */
	public enum WorkStatus {

		/** The success. */
		SUCCESS, /** The failed. */
		FAILED;
	}
}
