package com.sirma.itt.seip.content.processing;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link IdocContentItemWriter}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 08/02/2019
 */
public class IdocContentItemWriterTest {

	@InjectMocks
	private IdocContentItemWriter writer;

	@Mock
	private InstanceContentService instanceContentService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void writeItems() throws Exception {
		Content content1 = mock(Content.class);
		when(content1.getContentId()).thenReturn("content-1");
		Content content2 = mock(Content.class);
		when(content2.getContentId()).thenReturn("content-2");
		when(instanceContentService.updateContent(anyString(), any(), any())).thenReturn(mock(ContentInfo.class));

		writer.writeItems(Arrays.asList(content1, content2));

		verify(instanceContentService, times(2)).updateContent(anyString(), eq(null), any());
	}
}