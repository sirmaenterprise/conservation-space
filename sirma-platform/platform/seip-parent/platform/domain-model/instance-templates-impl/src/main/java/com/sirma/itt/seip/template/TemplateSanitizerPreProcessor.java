package com.sirma.itt.seip.template;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.idoc.sanitizer.IdocSanitizer;

/**
 * Default first pre processor that sanitizes the template content
 *
 * @author BBonev
 */
@Extension(target = TemplatePreProcessor.PLUGIN_NAME, order = 0)
public class TemplateSanitizerPreProcessor implements TemplatePreProcessor {

	@Inject
	private IdocSanitizer sanitizer;

	@Override
	public void process(TemplateContext context) {
		String content = context.getTemplate().getContent();
		if (StringUtils.isNotBlank(content)) {
			String result = sanitizer.sanitizeTemplate(content, null);
			context.getTemplate().setContent(result);
		}
	}

}
