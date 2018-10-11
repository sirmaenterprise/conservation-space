package com.sirma.sep.content.idoc.extensions.widgets.comments;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.annotations.AnnotationSearchRequest;
import com.sirma.itt.seip.annotations.AnnotationService;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.annotations.model.AnnotationProperties;
import com.sirma.itt.seip.annotations.rest.AnnotationWriter;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.sep.content.idoc.extensions.widgets.recentactivities.RecentActivitiesWidgetSearchHandlerTest;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetMock;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerResult;
import com.sirma.sep.content.idoc.nodes.widgets.comments.CommentsWidget;

/**
 * Test for {@link CommentsWidgetSearchHandler}.
 *
 * @author A. Kunchev
 */
public class CommentsWidgetSearchHandlerTest {

	private static final String TEST_WIDGET_CONFIG = "comments-widget-configuration-manually-select.txt";

	@InjectMocks
	private CommentsWidgetSearchHandler handler;

	@Mock
	private SearchService searchService;

	@Mock
	private JsonToConditionConverter conditionConverter;

	@Mock
	private AnnotationService annotationService;

	@Mock
	private DateConverter dateConverter;

	@Mock
	private AnnotationWriter annotationWriter;

	@Before
	public void setup() {
		handler = new CommentsWidgetSearchHandler();
		MockitoAnnotations.initMocks(this);
		ReflectionUtils.setFieldValue(handler, "searchService", searchService);
		ReflectionUtils.setFieldValue(handler, "jsonToConditionConverter", conditionConverter);
	}

	@Test
	public void accept_incorrectWidgetType_false() {
		boolean result = handler.accept(new WidgetMock());
		assertFalse(result);
	}

	@Test
	public void accept_correctWidgetType_false() {
		boolean result = handler.accept(mock(CommentsWidget.class));
		assertTrue(result);
	}

	@Test
	public void handle_mainSearchNoResults() {
		Element node = new Element(Tag.valueOf("div"), "");
		node.attr("config", "e30=");
		CommentsWidget widget = new CommentsWidget(node);
		HandlerResult handlerResult = handler.handle(widget, new HandlerContext());
		assertFalse(handlerResult.getResult().isPresent());
	}

	@Test
	public void handle_withMainSearchResultsWithVersionDate() throws IOException {
		when(annotationService.searchAnnotations(any(AnnotationSearchRequest.class))).thenReturn(buildAnnotations(4));
		when(annotationService.searchAnnotationsCountOnly(any(AnnotationSearchRequest.class))).thenReturn(4);
		when(dateConverter.parseDate(anyString())).thenReturn(new Date());
		when(annotationWriter.convert(anyCollection())).thenReturn(new Object());
		try (InputStream stream = RecentActivitiesWidgetSearchHandlerTest.class
				.getClassLoader()
					.getResourceAsStream(TEST_WIDGET_CONFIG)) {
			Element node = new Element(Tag.valueOf("div"), "");
			node.attr("config", IOUtils.toString(stream));
			CommentsWidget widget = new CommentsWidget(node);
			HandlerContext handlerContext = new HandlerContext();
			handlerContext.put("versionCreationDate", new Date());
			HandlerResult handlerResult = handler.handle(widget, handlerContext);
			Optional<Map<String, Object>> optional = handlerResult.getResult();
			assertTrue(optional.isPresent());
			Map<String, Object> resultMap = optional.get();
			assertFalse(resultMap.isEmpty());
		}
	}

	@Test
	public void handle_withMainSearchResultsWithoutVersionDate() throws IOException {
		when(annotationService.searchAnnotations(any(AnnotationSearchRequest.class))).thenReturn(buildAnnotations(4));
		when(annotationService.searchAnnotationsCountOnly(any(AnnotationSearchRequest.class))).thenReturn(4);
		when(dateConverter.parseDate(anyString())).thenReturn(new Date());
		when(annotationWriter.convert(anyCollection())).thenReturn(new Object());
		try (InputStream stream = RecentActivitiesWidgetSearchHandlerTest.class
				.getClassLoader()
					.getResourceAsStream(TEST_WIDGET_CONFIG)) {
			Element node = new Element(Tag.valueOf("div"), "");
			node.attr("config", IOUtils.toString(stream));
			CommentsWidget widget = new CommentsWidget(node);
			HandlerResult handlerResult = handler.handle(widget, new HandlerContext());
			Optional<Map<String, Object>> optional = handlerResult.getResult();
			assertTrue(optional.isPresent());
			Map<String, Object> resultMap = optional.get();
			assertFalse(resultMap.isEmpty());
		}
	}

	@Test(expected = EmfRuntimeException.class)
	public void handle_errorWhileConvertionAnnotations() throws IOException {
		when(annotationService.searchAnnotations(any(AnnotationSearchRequest.class))).thenReturn(buildAnnotations(4));
		when(annotationService.searchAnnotationsCountOnly(any(AnnotationSearchRequest.class))).thenReturn(4);
		when(dateConverter.parseDate(anyString())).thenReturn(new Date());
		when(annotationWriter.convert(anyCollection())).thenThrow(new IOException());
		try (InputStream stream = RecentActivitiesWidgetSearchHandlerTest.class
				.getClassLoader()
					.getResourceAsStream(TEST_WIDGET_CONFIG)) {
			Element node = new Element(Tag.valueOf("div"), "");
			node.attr("config", IOUtils.toString(stream));
			CommentsWidget widget = new CommentsWidget(node);
			handler.handle(widget, new HandlerContext());
		}
	}

	private static Collection<Annotation> buildAnnotations(int numberOfAnnotations) {
		List<Annotation> annotations = new ArrayList<>(numberOfAnnotations);
		for (int i = 0; i < numberOfAnnotations; i++) {
			Annotation annotation = new Annotation();
			annotation.setId("annotation-id-" + i);
			annotation.add(AnnotationProperties.HAS_TARGET, "target-id-" + i);
			annotations.add(annotation);
		}

		return annotations;
	}

}
