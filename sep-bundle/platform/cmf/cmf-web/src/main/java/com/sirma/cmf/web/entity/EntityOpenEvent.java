package com.sirma.cmf.web.entity;

import com.sirma.itt.emf.domain.model.Entity;

/**
 * EntityOpenEvent is fired when an entity object is opened trough user interface.
 * 
 * @author svelikov
 */
public class EntityOpenEvent {

	private Entity entity;

	/**
	 * Instantiates a new entity open event.
	 * 
	 * @param entity
	 *            the entity
	 */
	public EntityOpenEvent(Entity entity) {
		this.entity = entity;
	}

	/**
	 * Getter method for entity.
	 * 
	 * @return the entity
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Setter method for entity.
	 * 
	 * @param entity
	 *            the entity to set
	 */
	public void setEntity(Entity entity) {
		this.entity = entity;
	}

}
