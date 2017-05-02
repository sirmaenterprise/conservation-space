package com.sirma.itt.seip.template;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;

/**
 * The TemplateInstance represents a holder for templates. The content of the template is not loaded by default when
 * {@link TemplateInstance} is returned from the template service. To load the content call the proper service method.
 *
 * @author BBonev
 */
public class TemplateInstance extends EmfInstance {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 7804306020013687239L;

	/**
	 * Getter method for forType.
	 *
	 * @return the forType
	 */
	public String getForType() {
		return getString(DefaultProperties.TYPE);
	}

	/**
	 * Setter method for forType.
	 *
	 * @param forType
	 *            the groupId to set
	 */
	public void setForType(String forType) {
		getProperties().put(DefaultProperties.TYPE, forType);
	}

	/**
	 * Getter method for visibleTo.
	 *
	 * @return the visibleTo
	 */
	public String getVisibleTo() {
		return getString(DefaultProperties.CREATED_BY);
	}

	/**
	 * Setter method for visibleTo.
	 *
	 * @param createdBy
	 *            the new visible to
	 */
	public void setVisibleTo(String createdBy) {
		getProperties().put(DefaultProperties.CREATED_BY, createdBy);
	}

	/**
	 * Getter method for primary.
	 *
	 * @return the primary
	 */
	public Boolean getPrimary() {
		return getBoolean(TemplateProperties.PRIMARY);
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
		return getBoolean(TemplateProperties.PUBLIC);
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
		return getString(TemplateProperties.CONTENT_DIGEST);
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

	/**
	 * Sets the template purpose.
	 *
	 * @param purpose
	 *            the template purpose
	 */
	public void setPurpose(String purpose) {
		add(TemplateProperties.PURPOSE, purpose);
	}

	/**
	 * Gets the template purpose.
	 *
	 * @return the template purpose
	 */
	public String getPurpose() {
		return getString(TemplateProperties.PURPOSE);
	}

	public void setCorrespondingInstance(String correspondingInstance) {
		add(TemplateProperties.CORRESPONDING_INSTANCE, correspondingInstance);
	}

	public String getCorrespondingInstance() {
		return getString(TemplateProperties.CORRESPONDING_INSTANCE);
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
		return getString(CONTENT);
	}

	/**
	 * Set or update the content for the current template instance
	 * 
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		add(CONTENT, content);
	}

	/**
	 * Checks if is content loaded. If not loaded the method {@link #getContent()} will return <code>null</code>. By
	 * default the content is not loaded when instance is returned from the template service.
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
