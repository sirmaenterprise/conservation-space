package com.sirma.itt.seip.template.script;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.template.TemplateService;

/**
 * Extension to provide means to work with content.
 *
 * @author BBonev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 120)
public class ContentScriptsProvider implements GlobalBindingsExtension {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContentScriptsProvider.class);
	@Inject
	private javax.enterprise.inject.Instance<TemplateService> templateService;
	/** Default script name */
	private static final String CONTENT_ACTIONS_JS = "content-actions.js";
	private boolean enabled = true;

	/**
	 * Check if the provider is enabled
	 */
	public void initialize() {
		enabled = !templateService.isUnsatisfied();
		if (!enabled) {
			LOGGER.info("Template content service implementation not found. No content integertion in script api");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<String> getScripts() {
		if (!enabled) {
			return Collections.emptyList();
		}
		return ResourceLoadUtil.loadResources(getClass(), CONTENT_ACTIONS_JS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getBindings() {
		if (!enabled) {
			return Collections.emptyMap();
		}
		return Collections.<String, Object> singletonMap("content", this);
	}

	/**
	 * Copy the content from the given template id if found to the given instance.
	 *
	 * @param templateId
	 *            the template id to load
	 * @return the template content or <code>null</code> if not found
	 */
	public String getTemplateContent(String templateId) {
		if (StringUtils.isBlank(templateId)) {
			LOGGER.warn("Missing required argument for setting a content: templateId[{}]", templateId);
			return null;
		}

		String content = templateService.get().getContent(templateId);

		if (content == null) {
			LOGGER.warn("Could not find template with id [{}]", templateId);
			return null;
		}
		return content;
	}
}
