package com.sirma.itt.seip.instance;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.PropertiesChanges;
import com.sirma.itt.seip.PropertyChange;
import com.sirma.itt.seip.Trackable;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.Lockable;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.util.LoggingUtil;

/**
 * Instance implementation that represents a business object.
 *
 * @author BBonev
 */
public class ObjectInstance extends EmfInstance implements Lockable, Trackable<Serializable> {

	private static final long serialVersionUID = 2949311977301845446L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private transient PropertiesChanges<Serializable> propertiesChanges;

	@Override
	@SuppressWarnings("unchecked")
	public void enableChangesTracking() {
		if (isTracked()) {
			throw new IllegalStateException("Already tracking instance");
		}
		if (propertiesChanges == null) {
			propertiesChanges = new PropertiesChanges<>();
		}
		Map<String, Serializable> properties = getOrCreateProperties();
		properties.replaceAll((key, value) -> {
			if (value instanceof Collection) {
				return (Serializable) propertiesChanges.trackChanges(key, (Collection<Serializable>) value);
			} else if (value instanceof Trackable) {
				Trackable.enableTracking(value);
			}
			return value;
		});
		super.setProperties(propertiesChanges.trackChanges(properties));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void disableChangesTracking() {
		if (!isTracked()) {
			throw new IllegalStateException("Not tracking the current instance");
		}
		Map<String, Serializable> properties = getProperties();
		properties.replaceAll((key, value) -> {
			if (value instanceof Collection) {
				return (Serializable) PropertiesChanges.unTrackChanges((Collection<Serializable>) value);
			} else if (value instanceof Trackable) {
				Trackable.disableTracking(value);
			}
			return value;
		});
		super.setProperties(PropertiesChanges.unTrackChanges(properties));
	}

	@Override
	public boolean isTracked() {
		return propertiesChanges != null && propertiesChanges.isTracking(getProperties());
	}

	@Override
	public void clearChanges() {
		if (propertiesChanges != null) {
			propertiesChanges.clear();
		}
	}

	@Override
	public Stream<PropertyChange<Serializable>> changes() {
		if (propertiesChanges == null) {
			throw new IllegalStateException("Tracking not enabled, yet.");
		}
		return propertiesChanges.changes();
	}

	@Override
	public void applyChanges(Stream<PropertyChange<Serializable>> newChanges) {
		newChanges.forEach(this::apply);
	}

	@SuppressWarnings("unchecked")
	private void apply(PropertyChange<Serializable> change) {
		switch (change.getChangeType()) {
			case ADD:
				LOGGER.trace("Apply change to {} add {}={}", getId(), change.getProperty(), change.getNewValue());
				add(change.getProperty(), change.getNewValue());
				break;
			case APPEND:
				LOGGER.trace("Apply change to {} append {}={}", getId(), change.getProperty(), change.getNewValue());
				appendInternal(change);
				break;
			case UPDATE:
				LOGGER.trace("Apply change to {} update {}={} was {}", getId(), change.getProperty(), change.getNewValue(), change.getOldValue());
				removeOrRemoveAll(change);
				if (change.getNewValue() instanceof Collection) {
					appendAll(change.getProperty(), (Collection<Serializable>) change.getNewValue());
				} else {
					add(change.getProperty(), change.getNewValue());
				}
				// getProperties().rep
				break;
			case REMOVE:
				LOGGER.trace("Apply change to {} remove {}={}", getId(), change.getProperty(), change.getOldValue());
				removeOrRemoveAll(change);
		}
	}

	@SuppressWarnings("unchecked")
	private void removeOrRemoveAll(PropertyChange<Serializable> change) {
		if (change.getOldValue() instanceof Collection) {
			((Collection<Serializable>) change.getOldValue()).forEach(v -> remove(change.getProperty(), v));
		} else {
			remove(change.getProperty(), change.getOldValue());
		}
	}

	@SuppressWarnings("unchecked")
	private void appendInternal(PropertyChange<Serializable> change) {
		if (change.getNewValue() instanceof Collection) {
			appendAll(change.getProperty(), (Collection<Serializable>) change.getNewValue());
		} else {
			append(change.getProperty(), change.getNewValue());
		}
	}

	@Override
	public void setProperties(Map<String, Serializable> properties) {
		Map<String, Serializable> map = properties;
		if (isTracked()) {
			// if current instance is tracked, setting new map means we should track it as well
			map = propertiesChanges.trackChanges(map);
			propertiesChanges.clear();
		} else if (propertiesChanges == null || !propertiesChanges.isTracking(properties)) {
			// if we are not currently tracking or the given map is not tracked by the current instance
			// we should remove any tracking from somewhere else
			map = PropertiesChanges.unTrackChanges(properties);
		}
		super.setProperties(map);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(512);
		builder.append(getClass().getSimpleName()).append(" [id=");
		builder.append(getId());
		builder.append(", identifier=");
		builder.append(getIdentifier());
		builder.append(", container=");
		builder.append(getContainer());
		builder.append(", dmsId=");
		builder.append(getDmsId());
		builder.append(", contentManagementId=");
		builder.append(getContentManagementId());
		builder.append(", properties=");
		builder.append(LoggingUtil.toString(getProperties()));
		builder.append("]");
		return builder.toString();
	}

	@Override
	public boolean isLocked() {
		return getLockedBy() != null;
	}

	@Override
	public String getLockedBy() {
		return getString(DefaultProperties.LOCKED_BY);
	}

	@Override
	public void setLockedBy(String lockedBy) {
		add(DefaultProperties.LOCKED_BY, lockedBy);
	}

	@Override
	public Map<String, Serializable> getProperties() {
		Map<String, Serializable> map = super.getProperties();
		if (map == null) {
			map = new LinkedHashMap<>();
			super.setProperties(map);
		}
		return map;
	}

	// override the method in order to have consistent behavior for the created maps as the super method creates plain HashMap
	@Override
	public Map<String, Serializable> getOrCreateProperties() {
		return this.getProperties();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		// this should make the current instance to be distinguishable from the parent instance hash
		return super.hashCode() * prime;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || super.equals(obj) && getClass() == obj.getClass();
	}

	@Override
	public Instance createCopy() {
		ObjectInstance clone = new ObjectInstance();
		clone.setId(getId());
		clone.setIdentifier(getIdentifier());
		clone.setDmsId(getDmsId());
		clone.setContentManagementId(getContentManagementId());
		clone.setRevision(getRevision());
		clone.setContainer(getContainer());
		if (isDeleted()) {
			clone.markAsDeleted();
		}
		clone.setType(type());
		clone.setVersion(getVersion());
		clone.setReference(toReference());

		clone.setProperties(PropertiesUtil.cloneProperties(getProperties()));
		return clone;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Serializable> Serializable add(String name, T value) {
		if (isTracked()) {
			if (value instanceof Collection) {
				return super.add(name,
						(Serializable) propertiesChanges.trackChanges(name, (Collection<Serializable>) value));
			} else if (value instanceof Trackable) {
				Trackable.enableTracking(value);
			}
		}
		return super.add(name, value);
	}

	@Override
	public void addAllProperties(Map<String, ? extends Serializable> newProperties) {
		if (newProperties != null) {
			newProperties.forEach(this::add);
		}
	}
}
