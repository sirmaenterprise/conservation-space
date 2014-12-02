package com.sirma.itt.emf.audit.command;

import java.io.Serializable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.event.PropertiesChangeEvent;

/**
 * Provides common logic for all classes that extends this one.
 * 
 * @author Mihail Radkov
 */
public abstract class AuditAbstractCommand implements AuditCommand {

	protected static final Logger LOGGER = LoggerFactory.getLogger(AuditAbstractCommand.class);

	/**
	 * Retrieves an instance from given event. Works only if the event is
	 * {@link PropertiesChangeEvent}, otherwise returns null.
	 * 
	 * @param event
	 *            the event
	 * @return the instance or null
	 */
	protected Instance getInstance(EmfEvent event) {
		if (event instanceof PropertiesChangeEvent) {
			PropertiesChangeEvent changeEvent = (PropertiesChangeEvent) event;
			if (changeEvent.getEntity() instanceof Instance) {
				return (Instance) changeEvent.getEntity();
			}
		} else if (event instanceof AbstractInstanceEvent<?>) {
			return (Instance) ((AbstractInstanceEvent<?>) event).getInstance();
		}
		return null;
	}

	/**
	 * Extracts specific value by given key from a Map of properties.
	 * 
	 * @param properties
	 *            the map with properties
	 * @param key
	 *            the property's key
	 * @return the property or null
	 */
	protected String getProperty(Map<String, Serializable> properties, String key) {
		if (properties != null) {
			Serializable value = properties.get(key);
			if (value != null) {
				return value.toString();
			} else {
				return null;
			}
		}
		return null;
	}
}
