package com.sirma.itt.seip.tenant.semantic;

import java.io.Serializable;
import java.net.URI;

/**
 * Represents a semantic tenant initialization context data. Represents a semantic configurations after the provisioning
 *
 * @author BBonev
 */
public class TenantSemanticContext implements Serializable {

	/** serialVersionUID. */
	private static final long serialVersionUID = 818106457049101629L;
	/** The semantic address. */
	private URI semanticAddress;
	/** The repo name. */
	private String repoName;
	/** The solr core name. */
	private String solrCoreName;

	/**
	 * Instantiates a new tenant semantic context.
	 */
	public TenantSemanticContext() {
		// default
	}

	/**
	 * Gets the semantic address.
	 *
	 * @return the semantic address
	 */
	public URI getSemanticAddress() {
		return semanticAddress;
	}

	/**
	 * Sets the semantic address.
	 *
	 * @param semanticAddress
	 *            the new semantic address
	 */
	public void setSemanticAddress(URI semanticAddress) {
		this.semanticAddress = semanticAddress;
	}

	/**
	 * Gets the repo name.
	 *
	 * @return the repo name
	 */
	public String getRepoName() {
		return repoName;
	}

	/**
	 * Sets the repo name.
	 *
	 * @param repoName
	 *            the new repo name
	 */
	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	/**
	 * Gets the solr core name.
	 *
	 * @return the solr core name
	 */
	public String getSolrCoreName() {
		return solrCoreName;
	}

	/**
	 * Sets the solr core name.
	 *
	 * @param solrCoreName
	 *            the new solr core name
	 */
	public void setSolrCoreName(String solrCoreName) {
		this.solrCoreName = solrCoreName;
	}
}