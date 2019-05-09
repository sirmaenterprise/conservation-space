package com.sirma.sep.content.idoc.extract;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.export.renders.IdocRenderer;

/**
 * Load content select only widgets of interest load data in it by {@link IdocRenderer}
 * and extract only text from it.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/09/2018
 */
@Singleton
public class IdocWidgetContentExtractor implements WidgetContentExtractor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "viewcontent.extratctor.widgets.regex", type = Pattern.class,
			defaultValue = "datatable-widget|object-data-widget|object-link",
			label = "Regex with widget ids which contents have to be extracted.")
	private ConfigurationProperty<Pattern> widgetForContentExtractionPattern;

	@Inject
	@ExtensionPoint(IdocRenderer.PLUGIN_NAME)
	private Plugins<IdocRenderer> idocRenders;

	@Override
	public Optional<String> extractWidgetsContent(String currentInstanceId, FileDescriptor descriptor) {
		try {
			String idocContent = descriptor.asString();
			Idoc document = Idoc.parse(idocContent);
			Pattern widgetNamePattern = widgetForContentExtractionPattern.get();
			return Optional.of(document.widgets()
					.filter(widget -> widgetNamePattern.matcher(widget.getName()).matches())
					.map(widget -> extractWidgetContent(currentInstanceId, widget))
					.collect(Collectors.joining(" ")));
		} catch (IOException e) {
			LOGGER.warn("Failed to load content of instance with id : " + currentInstanceId, e);
		}
		return Optional.empty();
	}

	/**
	 * Load <code>widget</code> data and extract it as text.
	 *
	 * @param targetId id of instance to be updated.
	 * @param widget the widget data to be extracted.
	 * @return widget data as text.
	 */
	private String extractWidgetContent(String targetId, Widget widget) {
		return idocRenders.stream()
				.filter(render -> render.accept(widget))
				.findFirst()
				.map(render -> render.render(targetId, widget).text())
				.orElse("");
	}
}
