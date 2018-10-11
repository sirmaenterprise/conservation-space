package com.sirma.itt.seip.permissions.action.external;

import java.util.ArrayList;
import java.util.List;

import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.permissions.action.EmfAction;

/**
 * Action is intermediate class to help conversion between jaxb mapping and {@link EmfAction} class
 */
public class Action implements Copyable<Action> {
	private List<String> filters;
	private String name;
	private String bind;
	private boolean local;

	/**
	 * Getter method for filters.
	 *
	 * @return the filters
	 */
	public List<String> getFilters() {
		return filters;
	}

	/**
	 * Setter method for filters.
	 *
	 * @param filters
	 *            the filters to set
	 */
	public void setFilters(List<String> filters) {
		this.filters = filters;
	}

	/**
	 * Getter method for name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter method for name.
	 *
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter method for bind.
	 *
	 * @return the bind
	 */
	public String getBind() {
		return bind;
	}

	/**
	 * Setter method for bind.
	 *
	 * @param bind
	 *            the bind to set
	 */
	public void setBind(String bind) {
		this.bind = bind;
	}

	/**
	 * Getter method for local.
	 *
	 * @return the local
	 */
	public boolean isLocal() {
		return local;
	}

	/**
	 * Setter method for local.
	 *
	 * @param local
	 *            the local to set
	 */
	public void setLocal(boolean local) {
		this.local = local;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Action [name=");
		builder.append(name);
		builder.append(", bind=");
		builder.append(bind);
		builder.append(", local=");
		builder.append(local);
		builder.append(", filters=");
		builder.append(filters);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public Action createCopy() {
		Action clone = new Action();

		clone.name = name;
		clone.bind = bind;
		clone.local = local;
		clone.filters = new ArrayList<>(filters);

		return clone;
	}

}
