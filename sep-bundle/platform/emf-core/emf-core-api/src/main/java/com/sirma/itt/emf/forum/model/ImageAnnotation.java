package com.sirma.itt.emf.forum.model;

import java.io.Serializable;

import com.sirma.itt.emf.forum.ForumProperties;
import com.sirma.itt.emf.instance.model.CommonInstance;

/**
 * Represents an image annotation that is connected with Comment.
 * 
 * @author kirq4e
 * @author BBonev
 */
public class ImageAnnotation extends CommonInstance implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -751447755354458345L;

	/** The topic. */
	private transient TopicInstance topic;

	/**
	 * Getter method for svgValue.
	 * 
	 * @return the svgValue
	 */
	public String getSvgValue() {
		return (String) getProperties().get(ForumProperties.IA_SVG_VALUE);
	}

	/**
	 * Setter method for svgValue.
	 * 
	 * @param svgValue
	 *            the svgValue to set
	 */
	public void setSvgValue(String svgValue) {
		getProperties().put(ForumProperties.IA_SVG_VALUE, svgValue);
	}

	/**
	 * Getter method for viewBox.
	 * 
	 * @return the viewBox
	 */
	public String getViewBox() {
		return (String) getProperties().get(ForumProperties.IA_VIEW_BOX);
	}

	/**
	 * Setter method for viewBox.
	 * 
	 * @param viewBox
	 *            the viewBox to set
	 */
	public void setViewBox(String viewBox) {
		getProperties().put(ForumProperties.IA_VIEW_BOX, viewBox);
	}

	/**
	 * Getter method for zoomLevel.
	 * 
	 * @return the zoomLevel
	 */
	public int getZoomLevel() {
		Serializable serializable = getProperties().get(ForumProperties.IA_ZOOM_LEVEL);
		if (serializable instanceof Integer) {
			return ((Integer) serializable).intValue();
		}
		return 0;
	}

	/**
	 * Setter method for zoomLevel.
	 * 
	 * @param zoomLevel
	 *            the zoomLevel to set
	 */
	public void setZoomLevel(int zoomLevel) {
		getProperties().put(ForumProperties.IA_ZOOM_LEVEL, zoomLevel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ImageAnnotation [id=");
		builder.append(getId());
		builder.append(", svgValue=");
		builder.append(getSvgValue());
		builder.append(", viewBox=");
		builder.append(getViewBox());
		builder.append(", zoomLevel=");
		builder.append(getZoomLevel());
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Sets the topic.
	 * 
	 * @param topic
	 *            the new topic
	 */
	public void setTopic(TopicInstance topic) {
		this.topic = topic;
	}

	/**
	 * Gets the topic.
	 * 
	 * @return the topic
	 */
	public TopicInstance getTopic() {
		return topic;
	}

}
