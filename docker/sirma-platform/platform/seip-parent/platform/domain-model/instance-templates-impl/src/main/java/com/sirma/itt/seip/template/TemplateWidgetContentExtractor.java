package com.sirma.itt.seip.template;

import java.util.Optional;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import javax.interceptor.Interceptor;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.sep.content.idoc.extract.WidgetContentExtractor;

/**
 * Templates does not support search by widget contents. So we disable the widget content extraction for all templates.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/09/2018
 */
@Decorator
@Priority(Interceptor.Priority.APPLICATION)
public abstract class TemplateWidgetContentExtractor implements WidgetContentExtractor {

	@Inject
	@Delegate
	private WidgetContentExtractor delegate;

	@Inject
	private TemplateService templateService;

	@Override
	public Optional<String> extractWidgetsContent(String currentInstanceId, FileDescriptor descriptor) {
		Template template = templateService.getTemplate(currentInstanceId);
		if (template == null) {
			return delegate.extractWidgetsContent(currentInstanceId, descriptor);
		}
		return Optional.empty();
	}
}
