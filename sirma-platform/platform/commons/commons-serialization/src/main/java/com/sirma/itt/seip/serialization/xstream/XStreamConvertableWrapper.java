package com.sirma.itt.seip.serialization.xstream;

import com.sirma.itt.seip.serialization.convert.OutputFormat;

/**
 * Wrapper class used for some conversions in {@link com.sirma.itt.seip.convert.TypeConverter}.
 *
 * @author BBonev
 */
public class XStreamConvertableWrapper implements Cloneable {

	private OutputFormat outputFormat;
	private Object target;

	/**
	 * Instantiates a new convertable wrapper for XML format.
	 *
	 * @param target
	 *            the target
	 */
	public XStreamConvertableWrapper(Object target) {
		this(OutputFormat.XML, target);
	}

	/**
	 * Instantiates a new convertable wrapper.
	 *
	 * @param outputFormat
	 *            the output format
	 * @param target
	 *            the target
	 */
	public XStreamConvertableWrapper(OutputFormat outputFormat, Object target) {
		setOutputFormat(outputFormat);
		this.target = target;
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object clone() { // NOSONAR
		return new XStreamConvertableWrapper(outputFormat, target);
	}

	/**
	 * Getter method for outputFormat.
	 *
	 * @return the outputFormat
	 */
	public OutputFormat getOutputFormat() {
		return outputFormat;
	}

	/**
	 * Setter method for outputFormat.
	 *
	 * @param outputFormat
	 *            the outputFormat to set
	 */
	public void setOutputFormat(OutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}

}
