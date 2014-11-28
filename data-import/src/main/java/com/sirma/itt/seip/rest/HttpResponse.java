/**
 * Copyright (c) 2014 29.05.2014 , Sirma ITT. /* /**
 */
package com.sirma.itt.seip.rest;

import org.apache.http.Header;

import com.google.gson.JsonObject;

/**
 * Response returned when calling EMF restful services.
 * 
 * @author Adrian Mitev
 */
public class HttpResponse {

	private String text;

	private JsonObject json;

	private String cookie;

	private Header[] headers;

	private Integer status;

	/**
	 * Default constructor.
	 */
	public HttpResponse() {

	}

	/**
	 * Initializes properties
	 * 
	 * @param status
	 *            status code
	 * @param text
	 *            result text.
	 * @param json
	 *            json if available
	 * @param cookie
	 *            cookie if applied
	 * @param headers
	 *            all http headers.
	 */
	public HttpResponse(Integer status, String text, JsonObject json, String cookie,
			Header[] headers) {
		this.status = status;
		this.text = text;
		this.json = json;
		this.cookie = cookie;
		this.headers = headers;
	}

	/**
	 * Getter method for text.
	 * 
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Setter method for text.
	 * 
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Getter method for json.
	 * 
	 * @return the json
	 */
	public JsonObject getJson() {
		return json;
	}

	/**
	 * Setter method for json.
	 * 
	 * @param json
	 *            the json to set
	 */
	public void setJson(JsonObject json) {
		this.json = json;
	}

	/**
	 * Getter method for cookie.
	 * 
	 * @return the cookie
	 */
	public String getCookie() {
		return cookie;
	}

	/**
	 * Setter method for cookie.
	 * 
	 * @param cookie
	 *            the cookie to set
	 */
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	/**
	 * Getter method for headers.
	 * 
	 * @return the headers
	 */
	public Header[] getHeaders() {
		return headers;
	}

	/**
	 * Setter method for headers.
	 * 
	 * @param headers
	 *            the headers to set
	 */
	public void setHeaders(Header[] headers) {
		this.headers = headers;
	}

	/**
	 * Getter method for status.
	 * 
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * Setter method for status.
	 * 
	 * @param status
	 *            the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

}
