package com.sirma.sep.model.management.operation;

import static java.util.Objects.requireNonNull;

import com.sirma.sep.model.management.Path;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a change request for the target model
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/07/2018
 */
public class ModelChangeSet {
	private String selector;
	private String operation;
	private Object oldValue;
	private Object newValue;
	@JsonIgnore
	private Path resolvedPath;

	/**
	 * The path to the affected model.
	 *
	 * @return the path to the affected model
	 */
	public String getSelector() {
		return selector;
	}

	/**
	 * Parsed path to the affected model
	 *
	 * @return the path
	 */
	@JsonIgnore
	public Path getPath() {
		if (resolvedPath == null) {
			resolvedPath = Path.parsePath(requireNonNull(getSelector(), "Model change set id is not set!"));
		}
		return resolvedPath;
	}

	/**
	 * Set the path to the affected model. The given path should be resolvable using a {@link Path#parsePath(String)}
	 *
	 * @param selector the path to the model
	 */
	public ModelChangeSet setSelector(String selector) {
		this.selector = selector;
		return this;
	}

	/**
	 * Optional custom operation for the change
	 *
	 * @return the operation id
	 */
	public String getOperation() {
		return operation;
	}

	public ModelChangeSet setOperation(String operation) {
		this.operation = operation;
		return this;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public ModelChangeSet setOldValue(Object oldValue) {
		this.oldValue = oldValue;
		return this;
	}

	public Object getNewValue() {
		return newValue;
	}

	public ModelChangeSet setNewValue(Object newValue) {
		this.newValue = newValue;
		return this;
	}
}
