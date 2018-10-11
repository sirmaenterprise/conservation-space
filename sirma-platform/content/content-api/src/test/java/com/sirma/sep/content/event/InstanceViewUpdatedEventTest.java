/**
 *
 */
package com.sirma.sep.content.event;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentValidationException;
import com.sirma.sep.content.event.InstanceViewUpdatedEvent;

/**
 * Tests for {@link InstanceViewUpdatedEvent}.
 * 
 * @author BBonev
 */
public class InstanceViewUpdatedEventTest {
	
	@Test
	public void getParsedView() throws Exception {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.getInputStream())
				.thenReturn(new ByteArrayInputStream("<xml/>".getBytes(StandardCharsets.UTF_8)));
		InstanceViewUpdatedEvent event = new InstanceViewUpdatedEvent(null, null, contentInfo);

		assertNotNull(event.getOldViewParsed());
		assertNotNull(event.getOldViewParsed());
	}

	@Test(expected = ContentValidationException.class)
	public void testIOException() throws Exception {
		ContentInfo contentInfo = mock(ContentInfo.class);
		InputStream inputStream = getClass().getResourceAsStream(this.getClass().getSimpleName() + ".class");
		// force IOException for closed stream
		inputStream.close();
		when(contentInfo.getInputStream()).thenReturn(inputStream);
		InstanceViewUpdatedEvent event = new InstanceViewUpdatedEvent(null, null, contentInfo);

		assertNotNull(event.getOldViewParsed());
	}
}
