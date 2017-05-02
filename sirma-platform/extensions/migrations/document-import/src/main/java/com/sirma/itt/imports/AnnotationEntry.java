package com.sirma.itt.imports;

import java.io.Serializable;

import com.sirma.itt.emf.instance.model.Instance;

/**
 * The Class AnnotationEntry.
 * 
 * @author BBonev
 */
public class AnnotationEntry implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4133138008564790896L;

	/** The options. */
	private String options;

	/** The original value. */
	private String originalValue;

	/** The updated value. */
	private Serializable updatedValue;

	/** The owning instance. */
	private Instance owningInstance;

	/** The selector. */
	private String selector;

	/**
	 * Instantiates a new annotation entry.
	 */
	public AnnotationEntry() {
	}

	/**
	 * Instantiates a new annotation entry.
	 * 
	 * @param options
	 *            the options
	 * @param originalValue
	 *            the original value
	 * @param updatedValue
	 *            the updated value
	 * @param owningInstance
	 *            the owning instance
	 * @param selector
	 *            the selector
	 */
	public AnnotationEntry(String options, String originalValue, Serializable updatedValue,
			Instance owningInstance, String selector) {
		super();
		this.options = options;
		this.originalValue = originalValue;
		this.updatedValue = updatedValue;
		this.owningInstance = owningInstance;
		this.selector = selector;
	}

	/**
	 * Getter method for originalValue.
	 * 
	 * @return the originalValue
	 */
	public String getOriginalValue() {
		return originalValue;
	}

	/**
	 * Setter method for originalValue.
	 * 
	 * @param originalValue
	 *            the originalValue to set
	 * @return the annotation entry
	 */
	public AnnotationEntry setOriginalValue(String originalValue) {
		this.originalValue = originalValue;
		return this;
	}

	/**
	 * Getter method for updatedValue.
	 * 
	 * @return the updatedValue
	 */
	public Serializable getUpdatedValue() {
		return updatedValue;
	}

	/**
	 * Setter method for updatedValue.
	 * 
	 * @param updatedValue
	 *            the updatedValue to set
	 * @return the annotation entry
	 */
	public AnnotationEntry setUpdatedValue(Serializable updatedValue) {
		this.updatedValue = updatedValue;
		return this;
	}

	/**
	 * Getter method for owningInstance.
	 * 
	 * @return the owningInstance
	 */
	public Instance getOwningInstance() {
		return owningInstance;
	}

	/**
	 * Setter method for owningInstance.
	 * 
	 * @param owningInstance
	 *            the owningInstance to set
	 * @return the annotation entry
	 */
	public AnnotationEntry setOwningInstance(Instance owningInstance) {
		this.owningInstance = owningInstance;
		return this;
	}

	/**
	 * Getter method for selector.
	 * 
	 * @return the selector
	 */
	public String getSelector() {
		return selector;
	}

	/**
	 * Setter method for selector.
	 * 
	 * @param selector
	 *            the selector to set
	 * @return the annotation entry
	 */
	public AnnotationEntry setSelector(String selector) {
		this.selector = selector;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AnnotationEntry [options=");
		builder.append(options);
		builder.append(", originalValue=");
		builder.append(originalValue);
		builder.append(", updatedValue=");
		builder.append(updatedValue);
		builder.append(", selector=");
		builder.append(selector);
		builder.append(", owningInstance=");
		builder.append(owningInstance);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for options.
	 * 
	 * @return the options
	 */
	public String getOptions() {
		return options;
	}

	/**
	 * Setter method for options.
	 * 
	 * @param options
	 *            the options to set
	 * @return the annotation entry
	 */
	public AnnotationEntry setOptions(String options) {
		this.options = options;
		return this;
	}

}
