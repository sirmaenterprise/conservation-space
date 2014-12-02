package com.sirma.itt.emf.forum.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.sirma.itt.emf.entity.BaseStringIdEntity;

/**
 * Entity table to represents a single image annotation to a comment
 * 
 * @author BBonev
 */
@Entity
@Table(name = "emf_imageannotation")
@org.hibernate.annotations.Table(appliesTo = "emf_imageannotation")
public class ImageAnnotationEntity extends BaseStringIdEntity {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -4607447626262035969L;

	/** The svg value. */
	@Column(name = "svg_value", length = 2048)
	private String svgValue;

	/** The view box. */
	@Column(name = "view_box", length = 50)
	private String viewBox;

	/** The zoom level. */
	@Column(name = "zoom_level")
	private int zoomLevel;

	/**
	 * Getter method for svgValue.
	 * 
	 * @return the svgValue
	 */
	public String getSvgValue() {
		return svgValue;
	}

	/**
	 * Setter method for svgValue.
	 * 
	 * @param svgValue
	 *            the svgValue to set
	 */
	public void setSvgValue(String svgValue) {
		this.svgValue = svgValue;
	}

	/**
	 * Getter method for viewBox.
	 * 
	 * @return the viewBox
	 */
	public String getViewBox() {
		return viewBox;
	}

	/**
	 * Setter method for viewBox.
	 * 
	 * @param viewBox
	 *            the viewBox to set
	 */
	public void setViewBox(String viewBox) {
		this.viewBox = viewBox;
	}

	/**
	 * Getter method for zoomLevel.
	 * 
	 * @return the zoomLevel
	 */
	public int getZoomLevel() {
		return zoomLevel;
	}

	/**
	 * Setter method for zoomLevel.
	 * 
	 * @param zoomLevel
	 *            the zoomLevel to set
	 */
	public void setZoomLevel(int zoomLevel) {
		this.zoomLevel = zoomLevel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ImageAnnotationEntity [id=");
		builder.append(getId());
		builder.append(", svgValue=");
		builder.append(svgValue);
		builder.append(", viewBox=");
		builder.append(viewBox);
		builder.append(", zoomLevel=");
		builder.append(zoomLevel);
		builder.append("]");
		return builder.toString();
	}

}
