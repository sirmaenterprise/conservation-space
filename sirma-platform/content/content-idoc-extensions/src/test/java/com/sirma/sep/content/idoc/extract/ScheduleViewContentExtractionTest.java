package com.sirma.sep.content.idoc.extract;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentPersister;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.export.renders.IdocRenderer;

import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Boyan Tonchev.
 */
public class ScheduleViewContentExtractionTest {

	private static final String EMPTY_IDOC_FILE_PATH = "idoc-empty.html";
	private static final String CONTENT_ONLY_IDOC_FILE_PATH = "idoc-content-only.html";
	private static final String WIDGETS_ONLY_IDOC_FILE_PATH = "idoc-widgets-only.html";
	private static final String CONTENT_AND_WIDGETS_IDOC_FILE_PATH = "idoc-content-and-widgets.html";

	private static final String EMF_ID = "emf-id";

	private static final Pattern WIDGET_CONTENT_EXTRACTION_PATTERN = Pattern.compile("datatable-widget|object-data-widget|object-link");

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private javax.enterprise.inject.Instance<ContentPersister> contentPersister;

	@Mock
	private ContentPersister contentPersisterService;

	@Mock
	private ConfigurationProperty<Pattern> widgetForContentExtractionPattern;

	@Mock
	private Plugins<IdocRenderer> idocRenders;

	@Mock
	private Statistics statistics;
	
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@InjectMocks
	private ScheduleViewContentExtraction scheduleViewContentExtraction;

	/**
	 * Runs before each method and setup mockito.
	 */
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(contentPersister.get()).thenReturn(contentPersisterService);
		TimeTracker timeTracker = Mockito.mock(TimeTracker.class);
		Mockito.when(statistics.createTimeStatistics(Matchers.any(), Matchers.any())).thenReturn(
				timeTracker);
		Mockito.when(timeTracker.begin()).thenReturn(timeTracker);
		List<IdocRenderer> renders = getIdocRenders();
	}

	/**
	 * Tests method execute scenario with exceptions.
	 */
	@Test
	public void executeWithExceptionsTest() throws IOException {
		SchedulerContext context = new SchedulerContext();
		context.put(ScheduleViewContentExtraction.INSTANCE_ID, EMF_ID);
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.asString()).thenThrow(IOException.class);
		Mockito.when(contentInfo.getMimeType()).thenReturn("mimeType");
		Mockito.when(contentInfo.isIndexable()).thenReturn(Boolean.TRUE);

		Mockito.when(instanceContentService.getContent(EMF_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
		Mockito.when(widgetForContentExtractionPattern.get()).thenReturn(WIDGET_CONTENT_EXTRACTION_PATTERN);

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
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.asString()).thenReturn(loadIdocContent(CONTENT_AND_WIDGETS_IDOC_FILE_PATH));
		Mockito.when(contentInfo.getMimeType()).thenReturn("mimeType");
		Mockito.when(contentInfo.isIndexable()).thenReturn(Boolean.TRUE);

		Mockito.when(instanceContentService.getContent(EMF_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
		Mockito.when(idocRenders.stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream());
		Mockito.when(widgetForContentExtractionPattern.get()).thenReturn(Pattern.compile("datatable-widget"));

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
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.asString()).thenReturn(loadIdocContent(CONTENT_AND_WIDGETS_IDOC_FILE_PATH));
		Mockito.when(contentInfo.getMimeType()).thenReturn("mimeType");
		Mockito.when(contentInfo.isIndexable()).thenReturn(Boolean.TRUE);

		Mockito.when(instanceContentService.getContent(EMF_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
		Mockito.when(idocRenders.stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream());
		Mockito.when(widgetForContentExtractionPattern.get()).thenReturn(WIDGET_CONTENT_EXTRACTION_PATTERN);

		scheduleViewContentExtraction.execute(context);

		Mockito.verify(contentPersisterService).savePrimaryView(EMF_ID, "content of section one content of section two");
		Mockito.verify(contentPersisterService).saveWidgetsContent(EMF_ID, "object-data-widget text object-link text datatable-widget text");
	}

	/**
	 * Tests method execute scenario with widgets only.
	 */
	@Test
	public void executeWithWidgetsOnlyTest() throws IOException {
		SchedulerContext context = new SchedulerContext();
		context.put(ScheduleViewContentExtraction.INSTANCE_ID, EMF_ID);
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.asString()).thenReturn(loadIdocContent(WIDGETS_ONLY_IDOC_FILE_PATH));
		Mockito.when(contentInfo.getMimeType()).thenReturn("mimeType");
		Mockito.when(contentInfo.isIndexable()).thenReturn(Boolean.TRUE);

		Mockito.when(instanceContentService.getContent(EMF_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
		Mockito.when(idocRenders.stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream())
				.thenReturn(getIdocRenders().stream());
		Mockito.when(widgetForContentExtractionPattern.get()).thenReturn(WIDGET_CONTENT_EXTRACTION_PATTERN);

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
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.asString()).thenReturn(loadIdocContent(CONTENT_ONLY_IDOC_FILE_PATH));
		Mockito.when(contentInfo.getMimeType()).thenReturn("mimeType");
		Mockito.when(contentInfo.isIndexable()).thenReturn(Boolean.TRUE);

		Mockito.when(instanceContentService.getContent(EMF_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
		Mockito.when(widgetForContentExtractionPattern.get()).thenReturn(WIDGET_CONTENT_EXTRACTION_PATTERN);

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
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.asString()).thenReturn(loadIdocContent(EMPTY_IDOC_FILE_PATH));
		Mockito.when(contentInfo.getMimeType()).thenReturn("mimeType");
		Mockito.when(contentInfo.isIndexable()).thenReturn(Boolean.TRUE);

		Mockito.when(instanceContentService.getContent(EMF_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
		Mockito.when(widgetForContentExtractionPattern.get()).thenReturn(WIDGET_CONTENT_EXTRACTION_PATTERN);

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

		Mockito.verify(contentPersister, Mockito.never()).get();

	}

	private String loadIdocContent(String pathToFile) {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(pathToFile)) {
			return IOUtils.toString(is);
		} catch (IOException e) {

		}
		return "";
	}

	private List<IdocRenderer> getIdocRenders() {
		List<IdocRenderer> renders = new LinkedList<>();
		renders.add(createIdocRender("object-data-widget", "object-data-widget text"));
		renders.add(createIdocRender("datatable-widget", "datatable-widget text"));
		renders.add(createIdocRender("aggregated-table", "aggregated-table text"));
		renders.add(createIdocRender("content-viewer", "content-viewer text"));
		renders.add(createIdocRender("comments-widget", "comments-widget text"));
		renders.add(createIdocRender("image-widget", "image-widget text"));
		renders.add(createIdocRender("object-link", "object-link text"));
		renders.add(createIdocRender("recent-activities", "recent-activities text"));
		return renders;
	}

	private IdocRenderer createIdocRender(String widgetId, String textOfWindget) {
		return new IdocRenderer() {
			@Override
			public boolean accept(ContentNode node) {
				if (node != null && node.isWidget() && widgetId.equals(((Widget) node).getName())) {
					return true;
				}
				return false;
			}

			@Override
			public Element render(String currentInstanceId, ContentNode node) {
				Element widgetContent = new Element(Tag.valueOf("div"), "");
				widgetContent.html(textOfWindget);
				return widgetContent;
			}

			@Override
			public void afterRender(Element newElement, ContentNode node) {

			}
		};
	}
}
