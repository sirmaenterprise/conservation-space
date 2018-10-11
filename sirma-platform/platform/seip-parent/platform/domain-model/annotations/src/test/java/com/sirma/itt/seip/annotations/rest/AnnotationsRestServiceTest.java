package com.sirma.itt.seip.annotations.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;

import com.sirma.itt.seip.annotations.AnnotationService;
import com.sirma.itt.seip.annotations.mention.AnnotationMentionService;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests the annotations rest service
 *
 * @author kirq4e
 */
public class AnnotationsRestServiceTest {

	@InjectMocks
	private AnnotationsRestService restService;
	@Mock
	private AnnotationService annotationService;
	@Mock
	private NamespaceRegistryService namespaceRegistryService;
	@Mock
	private AnnotationMentionService annotationMentionService;
	@Mock
	private SecurityContext securityContext;

	/**
	 * Initializes the mocks
	 */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(annotationService.saveAnnotation(any(Annotation.class))).thenAnswer(
				a -> a.getArgumentAt(0, Annotation.class));
		Mockito.when(annotationService.saveAnnotation(any(Collection.class))).thenAnswer(
				a -> a.getArgumentAt(0, Collection.class));
		Mockito.when(annotationService.saveAnnotation((Collection<Annotation>) null)).thenReturn(
				Collections.emptyList());
		Mockito.when(annotationService.saveAnnotation((Annotation) null)).thenReturn(null);

		Mockito.when(annotationService.searchAnnotation(any(), any(), any(Integer.class))).thenAnswer(
				invocationMock -> Collections.singletonList(new Annotation()));
		ReflectionUtils.setFieldValue(restService, "annotationService", annotationService);
		EmfUser emfUser = mock(EmfUser.class);
		when(emfUser.getSystemId()).thenReturn("emf:testUserId");
		when(securityContext.getAuthenticated()).thenReturn(emfUser);
	}

	@Test
	public void testSaveAnnotation() {
		Annotation createAnnotationResponse = restService.createAnnotation(new Annotation());
		assertNotNull(createAnnotationResponse);
		verify(annotationService).saveAnnotation(any(Annotation.class));
	}

	@Test
	public void testSaveAnnotation_with_mentioned_user() {
		Annotation annotation = mock(Annotation.class);
		when(annotation.getTargetId()).thenReturn("emf:targetId");
		when(annotation.getId()).thenReturn("annotationId");
		when(annotation.getCommentsOn()).thenReturn("emf:commentedOnId");
		when(annotation.isSomeoneMentioned()).thenReturn(Boolean.TRUE);
		Set<Serializable> mentionedUsers = Collections.singleton("emf:testUserId");
		when(annotation.getMentionedUsers()).thenReturn(mentionedUsers);
		when(namespaceRegistryService.getShortUri(eq("emf:testUserId"))).thenReturn("emf:testUserId");
		when(annotation.isNew()).thenReturn(Boolean.FALSE);
		when(annotationMentionService.loadMentionedUsers(eq("annotationId"))).thenReturn(Collections.emptyList());
		Annotation createAnnotationResponse = restService.createAnnotation(annotation);
		assertNotNull(createAnnotationResponse);
		verify(annotationService).saveAnnotation(any(Annotation.class));
		verify(annotationMentionService).sendNotifications(eq(mentionedUsers), eq("emf:targetId"), eq("emf:commentedOnId"), eq("emf:testUserId"));
	}

	@Test
	public void saveMultipleAnnotations() throws Exception {
		Collection<Annotation> response = restService.createAnnotation(Arrays.asList(new Annotation()));
		assertNotNull(response);
		verify(annotationService).saveAnnotation(anyCollectionOf(Annotation.class));
	}

	@Test
	public void testUpdateAnnotation() {
		Annotation updated = restService.updateAnnotation("testId", new Annotation());
		assertNotNull(updated);
		assertEquals("testId", updated.getId());
		verify(annotationService).saveAnnotation(any(Annotation.class));

		updated = restService.updateAnnotation("newId", updated);
		assertNotNull(updated);
		assertEquals("testId", updated.getId());
		verify(annotationService, times(2)).saveAnnotation(any(Annotation.class));
	}

	@Test
	public void test_UpdateAnnotation_with_mentionedUsers() {
		Annotation annotation = mock(Annotation.class);
		when(annotation.getTargetId()).thenReturn("emf:targetId");
		when(annotation.getId()).thenReturn("annotationId");
		when(annotation.getCommentsOn()).thenReturn("emf:commentedOnId");
		when(annotation.isSomeoneMentioned()).thenReturn(Boolean.TRUE);
		Set<Serializable> mentionedUsers = Collections.singleton("emf:testUserId");
		when(annotation.getMentionedUsers()).thenReturn(mentionedUsers);
		when(namespaceRegistryService.getShortUri(eq("emf:testUserId"))).thenReturn("emf:testUserId");
		when(annotation.isNew()).thenReturn(Boolean.FALSE);
		when(annotationMentionService.loadMentionedUsers(eq("annotationId"))).thenReturn(Collections.emptyList());

		Annotation updated = restService.updateAnnotation("annotationId", annotation);
		assertNotNull(updated);
		assertEquals("annotationId", updated.getId());
		verify(annotationService, times(1)).saveAnnotation(any(Annotation.class));
		verify(annotationMentionService).sendNotifications(eq(mentionedUsers), eq("emf:targetId"), eq("emf:commentedOnId"), eq("emf:testUserId"));
	}

	@Test
	public void testSearchAllAnnotations() {
		Mockito.when(annotationService.loadAnnotations("imageId", 10)).thenReturn(Collections.emptyList());
		restService.loadAllAnnotations("imageId", 10);
		verify(annotationService, times(1)).loadAnnotations("imageId", 10);
	}

	/**
	 * Tests deleteAnnotation method
	 */
	@Test
	public void testDeleteAnnotation() {
		Response createAnnotationResponse = restService.deleteAnnotation("testAnnotation");
		checkResponse(createAnnotationResponse, AnnotationsRestService.EMPTY_JSON);
	}

	/**
	 * Tests performSearch method
	 */
	@Test
	public void testPerformSearch() {
		Collection<Annotation> annotations = restService.performSearch("testAnnotation","tabId", 0);
		assertNotNull(annotations);
		assertFalse(annotations.isEmpty());

		verify(annotationService).searchAnnotation("testAnnotation", "tabId", 0);
	}

	@Test
	public void test_CountReplays() {
		restService.countReplies("emf:testInstanceId");
		verify(annotationService).countAnnotationReplies("emf:testInstanceId");
	}

	@Test
	public void test_countAnnotations() {
		restService.count("emf:testInstanceId", "testTabId");
		verify(annotationService).countAnnotations("emf:testInstanceId", "testTabId");
	}

	@Test
	public void test_loadReplies() {
		when(annotationService.loadAnnotation(eq("testAnnotation"))).thenReturn(Optional.empty());
		Annotation testAnnotation = restService.loadReplies("testAnnotation");
		assertTrue(testAnnotation.isNew());
		verify(annotationService).loadAnnotation("testAnnotation");
	}

	@Test
	public void test_deleteAllForTarget() {
		restService.deleteAllForTarget("emf:testInstanceId", "testTabId");
		verify(annotationService).deleteAllAnnotations("emf:testInstanceId", "testTabId");
	}

	private static void checkResponse(Response response, String expectedResult) {
		Object entity = response.getEntity();
		Assert.assertTrue(entity instanceof String);
		Assert.assertEquals(entity, expectedResult);
	}

}
