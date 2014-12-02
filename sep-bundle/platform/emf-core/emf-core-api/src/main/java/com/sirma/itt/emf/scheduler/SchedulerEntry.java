package com.sirma.itt.emf.scheduler;

import java.io.Serializable;

import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.Identity;

/**
 * Represents a single scheduler entry.
 * 
 * @author BBonev
 */
public class SchedulerEntry implements Entity<Long>, Identity, Serializable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -788323478052588088L;
	/** The id. */
	private Long id;
	/** The status. */
	private SchedulerEntryStatus status;
	/** The configuration. */
	private SchedulerConfiguration configuration;
	/** The context. */
	private SchedulerContext context;
	/** The action. */
	private SchedulerAction action;
	/** The identifier. */
	private String identifier;
	/**
	 * Getter method for id.
	 *
	 * @return the id
	 */
	@Override
	public Long getId() {
		return id;
	}
	/**
	 * Setter method for id.
	 *
	 * @param id the id to set
	 */
	@Override
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * Getter method for status.
	 *
	 * @return the status
	 */
	public SchedulerEntryStatus getStatus() {
		return status;
	}
	/**
	 * Setter method for status.
	 *
	 * @param status the status to set
	 */
	public void setStatus(SchedulerEntryStatus status) {
		this.status = status;
	}
	/**
	 * Getter method for configuration.
	 *
	 * @return the configuration
	 */
	public SchedulerConfiguration getConfiguration() {
		return configuration;
	}
	/**
	 * Setter method for configuration.
	 *
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(SchedulerConfiguration configuration) {
		this.configuration = configuration;
	}
	/**
	 * Getter method for context.
	 *
	 * @return the context
	 */
	public SchedulerContext getContext() {
		return context;
	}
	/**
	 * Setter method for context.
	 *
	 * @param context the context to set
	 */
	public void setContext(SchedulerContext context) {
		this.context = context;
	}
	/**
	 * Getter method for action.
	 *
	 * @return the action
	 */
	public SchedulerAction getAction() {
		return action;
	}
	/**
	 * Setter method for action.
	 *
	 * @param action the action to set
	 */
	public void setAction(SchedulerAction action) {
		this.action = action;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((id == null) ? 0 : id.hashCode());
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SchedulerEntry other = (SchedulerEntry) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SchedulerEntry [id=");
		builder.append(id);
		builder.append(", status=");
		builder.append(status);
		builder.append(", configuration=");
		builder.append(configuration);
		builder.append(", context=");
		builder.append(context);
		builder.append(", action=");
		builder.append(action);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

}
