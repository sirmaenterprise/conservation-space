package com.sirma.sep.content.idoc.extract;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentPersister;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.extract.ContentExtractor;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.export.renders.IdocRenderer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Scheduler operation for update two properties of iDoc (emf:viewContent and emf:viewWidgetsContent).
 * <pre>
 * 1. Update flow of "emf:viewContent".
 *    Load content of instance remove all widgets of content and extract text only.
 *    Update property "emf:viewContent" by {@link ContentPersister}
 *
 * 2. Update flow of "emf:viewWidgetsContent".
 *    Load content of instance select only widgets of interest. Populate widgets content by {@link IdocRenderer}.
 *    Extract text from populated widgets and update "emf:viewWidgetsContent" by {@link ContentPersister}. The whigets of
 *    interest can be configured by "content.extratctor.widgets.regex" default value is "datatable-widget|object-data-widget|object-link"
 *    regex is widget-id-one|widget-id-two ....
 *
 * </pre>
 */
@Named(ScheduleViewContentExtraction.NAME)
public class ScheduleViewContentExtraction extends SchedulerActionAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleViewContentExtraction.class);

	public static final String NAME = "scheduleViewContentExtraction";

	public static final String INSTANCE_ID = "instanceId";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "viewcontent.extratctor.widgets.regex", type = Pattern.class,
			defaultValue = "datatable-widget|object-data-widget|object-link",
			label = "Regex with widget ids which contents have to be extracted.")
	private ConfigurationProperty<Pattern> widgetForContentExtractionPattern;

	@Inject
	private javax.enterprise.inject.Instance<ContentPersister> contentPersister;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private Statistics statistics;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	@ExtensionPoint(IdocRenderer.PLUGIN_NAME)
	private Plugins<IdocRenderer> idocRenders;

	@Override
	public void execute(SchedulerContext context) {
		Serializable instanceId = context.get(INSTANCE_ID);
		ContentInfo contentInfo = transactionSupport.invokeInTx(
				() -> instanceContentService.getContent(instanceId, Content.PRIMARY_VIEW));
		if (!ContentExtractor.isContentValidForExtraction(contentInfo)) {
			return;
		}
		TimeTracker tracker = statistics.createTimeStatistics(getClass(), "idocContentExtractTime").begin();
		try {
			String extractedContent = extractContent(contentInfo);
			String widgtsContent = extractWidgtsContent((String) instanceId, contentInfo);
			ContentPersister persister = this.contentPersister.get();
			transactionSupport.invokeInTx(() -> {
				persister.savePrimaryView(instanceId, extractedContent);
				persister.saveWidgetsContent(instanceId, widgtsContent);
				return null;
			});
		} finally {
			LOGGER.debug("iDoc content extraction took {} s", tracker.stopInSeconds());
		}

	}

	/**
	 * Extract content without widgets as text. Load content remove all widgets and extract only text from it.
	 *
	 * @param contentInfo
	 * 		the content info of instance.
	 * @return content without widgets as text.
	 */
	private static String extractContent(ContentInfo contentInfo) {
		try {
			String idocContent = contentInfo.asString();
			Document parse = Jsoup.parse(idocContent);
			parse.select("div[widget]").remove();
			return parse.text();
		} catch (IOException e) {
			LOGGER.warn("Failed to load content of instance with id : " + contentInfo.getInstanceId(), e);
		}
		return "";
	}

	/**
	 * Extract content of widgets only. Load content select only widgets of interest load data in it by {@link IdocRenderer}
	 * and extract only text from it.
	 *
	 * @param targetId
	 * 		id of instance to be updated.
	 * @param contentInfo
	 * 		the content info of instance.
	 * @return content of widgets as text.
	 */
	private String extractWidgtsContent(String targetId, ContentInfo contentInfo) {
		try {
			String idocContent = contentInfo.asString();
			Idoc document = Idoc.parse(idocContent);
			Pattern widgetNamePattern = widgetForContentExtractionPattern.get();
			return document.widgets()
					.filter(widget -> widgetNamePattern.matcher(widget.getName()).matches())
					.map(widget -> extractWidgetContent(targetId, widget))
					.collect(Collectors.joining(" "));
		} catch (IOException e) {
			LOGGER.warn("Failed to load content of instance with id : " + contentInfo.getInstanceId(), e);
		}
		return "";
	}

	/**
	 * Load <code>widget</code> data and extract it as text.
	 *
	 * @param targetId
	 * 		id of instance to be updated.
	 * @param widget
	 * 		the widget data to be extracted.
	 * @return widget data as text.
	 */
	private String extractWidgetContent(String targetId, Widget widget) {
		return idocRenders.stream()
				.filter(render -> render.accept(widget))
				.findFirst()
				.map(render -> render.render(targetId, widget).text())
				.orElse("");
	}

	/**
	 * Create context of action.
	 *
	 * @param instanceId
	 * 		of instance which have to be updated.
	 * @return the created configuration.
	 */
	public static final SchedulerContext createContext(Serializable instanceId) {
		SchedulerContext context = new SchedulerContext();
		context.put(ScheduleViewContentExtraction.INSTANCE_ID, instanceId);
		return context;
	}
}
