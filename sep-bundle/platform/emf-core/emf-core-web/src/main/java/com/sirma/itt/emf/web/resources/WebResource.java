package com.sirma.itt.emf.web.resources;

import java.util.Date;

/**
 * Holds an information for a given resource.
 * 
 * @author Adrian Mitev
 */
public class WebResource {

	private String name;
	private byte[] content;
	private String contentType;
	private String hash;
	private Date expires;
	private Date lastModified;
	private boolean cachable;

	/**
	 * Default constructor.
	 */
	public WebResource() {

	}

	/**
	 * Initializing constructor.
	 * 
	 * @param name
	 *            resource name.
	 * @param content
	 *            resource content.
	 * @param contentType
	 *            resource mimetype.
	 * @param hash
	 *            resource hash.
	 * @param cachable
	 *            if the resource is eligable for caching.
	 */
	public WebResource(String name, byte[] content, String contentType, String hash, boolean cachable) {
		this.name = name;
		this.content = content;
		this.contentType = contentType;
		this.hash = hash;
		this.cachable = cachable;
	}

	/**
	 * Getter method for content.
	 * 
	 * @return the content
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * Setter method for content.
	 * 
	 * @param content
	 *            the content to set
	 */
	public void setContent(byte[] content) {
		this.content = content;
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
	 * Getter method for cachable.
	 * 
	 * @return the cachable
	 */
	public boolean isCachable() {
		return cachable;
	}

	/**
	 * Setter method for cachable.
	 * 
	 * @param cachable
	 *            the cachable to set
	 */
	public void setCachable(boolean cachable) {
		this.cachable = cachable;
	}

	/**
	 * Getter method for hash.
	 * 
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * Setter method for hash.
	 * 
	 * @param hash
	 *            the hash to set
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}

	/**
	 * Getter method for contentType.
	 * 
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Setter method for contentType.
	 * 
	 * @param contentType
	 *            the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Getter method for expires.
	 * 
	 * @return the expires
	 */
	public Date getExpires() {
		return expires;
	}

	/**
	 * Setter method for expires.
	 * 
	 * @param expires
	 *            the expires to set
	 */
	public void setExpires(Date expires) {
		this.expires = expires;
	}

	/**
	 * Getter method for lastModified.
	 * 
	 * @return the lastModified
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * Setter method for lastModified.
	 * 
	 * @param lastModified
	 *            the lastModified to set
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

}
