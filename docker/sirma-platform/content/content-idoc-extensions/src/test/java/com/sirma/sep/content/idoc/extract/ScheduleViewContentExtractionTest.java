package com.sirma.sep.content.idoc.extract;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.jglue.cdiunit.AdditionalPackages;
import org.jglue.cdiunit.CdiRunner;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentPersister;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.export.renders.IdocRenderer;

/**
 * Test for {@link ScheduleViewContentExtraction}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ IdocWidgetContentExtractor.class })
@AdditionalPackages({ InstanceContentService.class })
@AdditionalClasspaths({ Extension.class })
public class ScheduleViewContentExtractionTest {

	private static final String EMPTY_IDOC_FILE_PATH = "idoc-empty.html";
	private static final String CONTENT_ONLY_IDOC_FILE_PATH = "idoc-content-only.html";
	private static final String WIDGETS_ONLY_IDOC_FILE_PATH = "idoc-widgets-only.html";
	private static final String CONTENT_AND_WIDGETS_IDOC_FILE_PATH = "idoc-content-and-widgets.html";

	private static final String EMF_ID = "emf-id";

	private static final Pattern WIDGET_CONTENT_EXTRACTION_PATTERN = Pattern.compile("datatable-widget|object-data-widget|object-link");

	@Mock
	@Produces
	private InstanceContentService instanceContentService;

	@Mock
	@Produces
	private ContentPersister contentPersisterService;

	@Produces
	@Configuration
	private ConfigurationPropertyMock<Pattern> widgetForContentExtractionPattern = new ConfigurationPropertyMock<>();

	@Spy
	@Produces
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Inject
	private ScheduleViewContentExtraction scheduleViewContentExtraction;

	/**
	 * Tests method execute scenario with exceptions.
	 */
	@Test
	public void executeWithExceptionsTest() throws IOException {
		SchedulerContext context = new SchedulerContext();
		context.put(ScheduleViewContentExtraction.INSTANCE_ID, EMF_ID);
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		when(contentInfo.asString()).thenThrow(new IOException());
		when(contentInfo.getMimeType()).thenReturn("mimeType");
		when(contentInfo.isIndexable()).thenReturn(Boolean.TRUE);

		when(instanceContentService.getContent(EMF_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
		widgetForContentExtractionPattern.setValue(WIDGET_CONTENT_EXTRACTION_PATTERN);

		scheduleViewContentExtraction.execute(context);

		Mockito.verify(contentPersisterService).savePrimaryView(EMF_ID, "");
		Mockito.verify(contentPersisterService).saveWidgetsContent(EMF_ID, "");
	}

	/**
	 * Tests method execute scenario with content and data table widget only.
	 */
	@Test
	public void executeWithContentAndDataTabelWidgetOnlyTest() throws IOException {
		SchedulerContext context = new SchedulerContext();
		context.put(ScheduleViewContentExtraction.INSTANCE_ID, EMF_ID);
		withContent(CONTENT_AND_WIDGETS_IDOC_FILE_PATH);

		widgetForContentExtractionPattern.setValue(Pattern.compile("datatable-widget"));

		scheduleViewContentExtraction.execute(context);

		Mockito.verify(contentPersisterService).savePrimaryView(EMF_ID, "content of section one content of section two");
		Mockito.verify(contentPersisterService).saveWidgetsContent(EMF_ID, "datatable-widget text");
	}

	/**
	 * Tests method execute scenario with content and widgets.
	 */
	@Test
	public void executeWithContentAndWidgetsTest() throws IOException {
		SchedulerContext context = new SchedulerContext();
		context.put(ScheduleViewContentExtraction.INSTANCE_ID, EMF_ID);
		withContent(CONTENT_AND_WIDGETS_IDOC_FILE_PATH);
		widgetForContentExtractionPattern.setValue(WIDGET_CONTENT_EXTRACTION_PATTERN);

		scheduleViewContentExtraction.execute(context);

		Mockito.verify(contentPersisterService).savePrimaryView(EMF_ID, "content of section one content of section two");
		Mockito.verify(contentPersisterService).saveWidgetsContent(EMF_ID, "object-data-widget text object-link text datatable-widget text");
	}

	private void withContent(String contentLocation) throws IOException {
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		when(contentInfo.asString()).thenReturn(loadIdocContent(contentLocation));
		when(contentInfo.getMimeType()).thenReturn("mimeType");
		when(contentInfo.isIndexable()).thenReturn(Boolean.TRUE);

		when(instanceContentService.getContent(EMF_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
	}

	/**
	 * Tests method execute scenario with widgets only.
	 */
	@Test
	public void executeWithWidgetsOnlyTest() throws IOException {
		SchedulerContext context = new SchedulerContext();
		context.put(ScheduleViewContentExtraction.INSTANCE_ID, EMF_ID);
		withContent(WIDGETS_ONLY_IDOC_FILE_PATH);
		widgetForContentExtractionPattern.setValue(WIDGET_CONTENT_EXTRACTION_PATTERN);

		scheduleViewContentExtraction.execute(context);

		Mockito.verify(contentPersisterService).savePrimaryView(EMF_ID, "");
		Mockito.verify(contentPersisterService).saveWidgetsContent(EMF_ID, "object-data-widget text object-link text datatable-widget text");

	}

	/**
	 * Tests method execute scenario with content only.
	 */
	@Test
	public void executeWithContentOnlyTest() throws IOException {
		SchedulerContext context = new SchedulerContext();
		context.put(ScheduleViewContentExtraction.INSTANCE_ID, EMF_ID);
		withContent(CONTENT_ONLY_IDOC_FILE_PATH);
		widgetForContentExtractionPattern.setValue(WIDGET_CONTENT_EXTRACTION_PATTERN);

		scheduleViewContentExtraction.execute(context);

		Mockito.verify(contentPersisterService).savePrimaryView(EMF_ID, "test content");
		Mockito.verify(contentPersisterService).saveWidgetsContent(EMF_ID, "");

	}

	/**
	 * Tests method execute scenario without content and widgets.
	 */
	@Test
	public void executeWithoutContentAndWidgetsTest() throws IOException {
		SchedulerContext context = new SchedulerContext();
		context.put(ScheduleViewContentExtraction.INSTANCE_ID, EMF_ID);
		withContent(EMPTY_IDOC_FILE_PATH);
		widgetForContentExtractionPattern.setValue(WIDGET_CONTENT_EXTRACTION_PATTERN);

		scheduleViewContentExtraction.execute(context);

		Mockito.verify(contentPersisterService).savePrimaryView(EMF_ID, "");
		Mockito.verify(contentPersisterService).saveWidgetsContent(EMF_ID, "");

	}

	/**
	 * Tests method execute scenario with instance which have no valid content for extraction.
	 */
	@Test
	public void executeWithNonValidContentForExtractionTest() {
		SchedulerContext context = Mockito.mock(SchedulerContext.class);

		scheduleViewContentExtraction.execute(context);

		// Mockito.verify(contentPersister, Mockito.never()).get();

	}

	private String loadIdocContent(String pathToFile) throws IOException {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(pathToFile)) {
			return IOUtils.toString(is);
		}
	}

	@Produces
	@Extension(target = IdocRenderer.PLUGIN_NAME)
	static IdocRenderer createObjectDataRenderer() {
		return createIdocRender("object-data-widget", "object-data-widget text");
	}
	@Produces
	@Extension(target = IdocRenderer.PLUGIN_NAME, order = 1)
	static IdocRenderer createDataTableRenderer() {
		return createIdocRender("datatable-widget", "datatable-widget text");
	}
	@Produces
	@Extension(target = IdocRenderer.PLUGIN_NAME, order = 2)
	static IdocRenderer createAggregatedTableRenderer() {
		return createIdocRender("aggregated-table", "aggregated-table text");
	}
	@Produces
	@Extension(target = IdocRenderer.PLUGIN_NAME, order = 3)
	static IdocRenderer createContentViewRenderer() {
		return createIdocRender("content-viewer", "content-viewer text");
	}
	@Produces
	@Extension(target = IdocRenderer.PLUGIN_NAME, order = 4)
	static IdocRenderer createCommnetsRenderer() {
		return createIdocRender("comments-widget", "comments-widget text");
	}
	@Produces
	@Extension(target = IdocRenderer.PLUGIN_NAME, order = 5)
	static IdocRenderer createImageWidgetRenderer() {
		return createIdocRender("image-widget", "image-widget text");
	}

	@Produces
	@Extension(target = IdocRenderer.PLUGIN_NAME, order = 6)
	static IdocRenderer createObjectLinkRenderer() {
		return createIdocRender("object-link", "object-link text");
	}
	@Produces
	@Extension(target = IdocRenderer.PLUGIN_NAME, order = 7)
	static IdocRenderer createRecentActivitiesRenderer() {
		return createIdocRender("recent-activities", "recent-activities text");
	}

	private static IdocRenderer createIdocRender(String widgetId, String textOfWindget) {
		return new IdocRenderer() {
			@Override
			public boolean accept(ContentNode node) {
				return node != null && node.isWidget() && widgetId.equals(((Widget) node).getName());
			}

			@Override
			public Element render(String currentInstanceId, ContentNode node) {
				Element widgetContent = new Element(Tag.valueOf("div"), "");
				widgetContent.html(textOfWindget);
				return widgetContent;
			}

			@Override
			public void afterRender(Element newElement, ContentNode node) {
				// do nothing
			}
		};
	}
}
