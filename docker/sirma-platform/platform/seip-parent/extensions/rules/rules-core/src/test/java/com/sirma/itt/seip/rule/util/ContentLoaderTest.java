package com.sirma.itt.seip.rule.util;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceReferenceImpl;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.search.SearchService;

/**
 * Test for {@link ContentLoader}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 30/04/2018
 */
public class ContentLoaderTest {
	@InjectMocks
	private ContentLoader contentLoader;
	@Mock
	private SearchService searchService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(searchService.getFilter(anyString(), any(), any())).thenReturn(new SearchArguments<>());
	}

	@Test
	public void loadContent() throws Exception {
		when(searchService.stream(any(), any())).thenReturn(Stream.of("content 1", "content 2"));

		String content = contentLoader.loadContent("emf:instanceId");
		assertEquals("content 1content 2", content);
	}

	@Test
	public void loadContent_returnNullOnNoLoadedContent() throws Exception {
		when(searchService.stream(any(), any())).thenReturn(Stream.empty());

		assertNull(contentLoader.loadContent("emf:instanceId"));
	}

	@Test
	public void loadContent_returnNullOnNullId() throws Exception {
		assertNull(contentLoader.loadContent(null));
	}

	@Test
	public void loadContent_forInstance() throws Exception {
		when(searchService.stream(any(), any())).thenReturn(Stream.of("content 1", "content 2"));

		String content = contentLoader.loadContent(new EmfInstance("emf:instanceId"));
		assertEquals("content 1content 2", content);
	}

	@Test
	public void loadContent_forReference() throws Exception {
		when(searchService.stream(any(), any())).thenReturn(Stream.of("content 1", "content 2"));

		String content = contentLoader.loadContent(new InstanceReferenceImpl("emf:instanceId", null));
		assertEquals("content 1content 2", content);
	}
}
