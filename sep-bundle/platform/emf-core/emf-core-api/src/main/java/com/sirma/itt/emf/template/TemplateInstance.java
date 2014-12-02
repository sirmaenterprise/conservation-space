package com.sirma.itt.emf.template;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sirma.itt.emf.instance.model.EmfInstance;

/**
 * The TemplateInstance represents a holder for templates. The content of the template is not loaded
 * by default when {@link TemplateInstance} is returned from the template service. To load the
 * content call the proper service method.
 * 
 * @author BBonev
 */
public class TemplateInstance extends EmfInstance {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 7804306020013687239L;

	/**
	 * Getter method for groupId.
	 *
	 * @return the groupId
	 */
	public String getGroupId() {
		return (String) getProperties().get(TemplateProperties.TYPE);
	}

	/**
	 * Setter method for groupId.
	 *
	 * @param groupId
	 *            the groupId to set
	 */
	public void setGroupId(String groupId) {
		getProperties().put(TemplateProperties.TYPE, groupId);
	}

	/**
	 * Getter method for visibleTo.
	 *
	 * @return the visibleTo
	 */
	public String getVisibleTo() {
		return (String) getProperties().get(TemplateProperties.CREATED_BY);
	}

	/**
	 * Setter method for visibleTo.
	 * 
	 * @param createdBy
	 *            the new visible to
	 */
	public void setVisibleTo(String createdBy) {
		getProperties().put(TemplateProperties.CREATED_BY, createdBy);
	}

	/**
	 * Getter method for primary.
	 *
	 * @return the primary
	 */
	public Boolean getPrimary() {
		return (Boolean) getProperties().get(TemplateProperties.PRIMARY);
	}

	/**
	 * Setter method for primary.
	 *
	 * @param primary
	 *            the primary to set
	 */
	public void setPrimary(Boolean primary) {
		getProperties().put(TemplateProperties.PRIMARY, primary);
	}

	/**
	 * Getter method for publicTemplate.
	 *
	 * @return the publicTemplate
	 */
	public Boolean getPublicTemplate() {
		return (Boolean) getProperties().get(TemplateProperties.PUBLIC);
	}

	/**
	 * Setter method for publicTemplate.
	 *
	 * @param publicTemplate
	 *            the publicTemplate to set
	 */
	public void setPublicTemplate(Boolean publicTemplate) {
		getProperties().put(TemplateProperties.PUBLIC, publicTemplate);
	}

	/**
	 * Gets the template digest.
	 * 
	 * @return the template digest
	 */
	public String getTemplateDigest() {
		return (String) getProperties().get(TemplateProperties.CONTENT_DIGEST);
	}

	/**
	 * Sets the template hash.
	 * 
	 * @param digest
	 *            the new template hash
	 */
	public void setTemplateDigest(String digest) {
		getProperties().put(TemplateProperties.CONTENT_DIGEST, digest);
	}

	@Override
	public Map<String, Serializable> getProperties() {
		Map<String, Serializable> map = super.getProperties();
		if (map == null) {
			map = new LinkedHashMap<>();
			setProperties(map);
		}
		return map;
	}

	@Override
	public Long getRevision() {
		Long current = super.getRevision();
		if (current == null) {
			return 0L;
		}
		return current;
	}

	/**
	 * Gets the template content
	 * 
	 * @return the content
	 */
	public String getContent() {
		return (String) getProperties().get(TemplateProperties.CONTENT);
	}

	/**
	 * Checks if is content loaded. If not loaded the method {@link #getContent()} will return
	 * <code>null</code>. By default the content is not loaded when instance is returned from the
	 * template service.
	 * 
	 * @return true, if is content loaded
	 */
	public boolean isContentLoaded() {
		Serializable property = getProperties().get(TemplateProperties.IS_CONTENT_LOADED);
		if (property instanceof Boolean) {
			return ((Boolean) property).booleanValue();
		}
		return false;
	}

	@Override
	public String getPath() {
		// disable path support
		return null;
	}
}
