package com.sirma.itt.idoc.web.util.sanitize;

import java.io.Serializable;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.cmf.event.template.BeforeTemplatePersistEvent;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.template.TemplateProperties;
import com.sirma.itt.emf.util.sanitize.ContentSanitizer;

/**
 * Sanitizes idoc template.
 * 
 * @author Adrian Mitev
 */
@ApplicationScoped
public class IdocTemplateSanitizer {

	@Inject
	private ContentSanitizer sanitizer;

	/**
	 * Sanitizes the template html.
	 * 
	 * @param event
	 *            BeforeTemplatePersistEvent
	 */
	public void onBeforeTemplatePersist(@Observes BeforeTemplatePersistEvent event) {
		Map<String, Serializable> properties = event.getInstance().getProperties();
		String content = (String) properties.get(TemplateProperties.CONTENT);
		if (StringUtils.isNotNullOrEmpty(content)) {
			String result = sanitizer.sanitizeTemplate(content, null);
			properties.put(TemplateProperties.CONTENT, result);
		}
	}

}
