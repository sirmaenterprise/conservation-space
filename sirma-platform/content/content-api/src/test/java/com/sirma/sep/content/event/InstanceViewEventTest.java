/**
 *
 */
package com.sirma.sep.content.event;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentValidationException;
import com.sirma.sep.content.descriptor.ByteArrayFileDescriptor;
import com.sirma.sep.content.event.InstanceViewEvent;

/**
 * Tests for {@link InstanceViewEvent}.
 * 
 * @author BBonev
 */
public class InstanceViewEventTest {

	@Test
	public void getParsedView() throws Exception {
		InstanceViewEvent event = new InstanceViewEvent(null, Content.create(null,
				new ByteArrayFileDescriptor("test.html", "<xml/>".getBytes(StandardCharsets.UTF_8)))) {
			// nothing to add
		};

		assertNotNull(event.getView());
		assertNotNull(event.getView());
	}

	@Test(expected = ContentValidationException.class)
	public void testIOException() throws Exception {
		FileDescriptor descriptor = mock(FileDescriptor.class);
		InputStream inputStream = getClass().getResourceAsStream(this.getClass().getSimpleName() + ".class");
		// force IOException for closed stream
		inputStream.close();
		when(descriptor.getInputStream()).thenReturn(inputStream);
		InstanceViewEvent event = new InstanceViewEvent(null, Content.create(null, descriptor)) {
			// nothing to add
		};

		assertNotNull(event.getView());
	}
}
