package com.sirma.itt.seip.eai.cs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sirma.itt.seip.eai.model.ServiceResponse;

/**
 * Represent an item element in the response containing the record data and additional data as described in the IDC for
 * CS.
 * 
 * @author bbanchev
 */
public class CSResultItem implements ServiceResponse {
	/** record grouping. **/
	@JsonProperty("record")
	private CSItemRecord record;
	@JsonProperty(value = "description")
	private String description;
	@JsonProperty(value = "url")
	private String url;
	@JsonProperty(value = "thumbnail")
	private String thumbnail;

	/**
	 * Getter method for record data.
	 *
	 * @return the record data
	 */
	public CSItemRecord getRecord() {
		return record;
	}

	/**
	 * Setter method for record data.
	 *
	 * @param record
	 *            the record to set
	 */
	public void setRecord(CSItemRecord record) {
		this.record = record;
	}

	/**
	 * Getter method for description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Setter method for description.
	 *
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Getter method for url.
	 *
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Setter method for url.
	 *
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Getter method for thumbnail.
	 *
	 * @return the thumbnail
	 */
	public String getThumbnail() {
		return thumbnail;
	}

	/**
	 * Setter method for thumbnail.
	 *
	 * @param thumbnail
	 *            the thumbnail to set
	 */
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName());
		builder.append("[record=");
		builder.append(record);
		builder.append(", description=");
		builder.append(description);
		builder.append(", url=");
		builder.append(url);
		builder.append(", thumbnail=");
		builder.append(thumbnail);
		builder.append("]");
		return builder.toString();
	}

}
