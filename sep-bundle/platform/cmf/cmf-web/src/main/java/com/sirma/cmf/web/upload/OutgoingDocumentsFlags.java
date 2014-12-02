package com.sirma.cmf.web.upload;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;

/**
 * Holds a flag in request scope to keep track if outgoing documents links have been already
 * processed.
 * 
 * @author svelikov
 */
@RequestScoped
public class OutgoingDocumentsFlags {

	/** The processed outgoing documents links. */
	private boolean processedLinks;

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		processedLinks = false;
	}

	/**
	 * Getter method for processedLinks.
	 * 
	 * @return the processedLinks
	 */
	public boolean isProcessedLinks() {
		return processedLinks;
	}

	/**
	 * Setter method for processedLinks.
	 * 
	 * @param processedLinks
	 *            the processedLinks to set
	 */
	public void setProcessedLinks(boolean processedLinks) {
		this.processedLinks = processedLinks;
	}
}