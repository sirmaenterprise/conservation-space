/**
 *
 */
package com.sirma.sep.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ViewPreProcessorContext;

/**
 * @author BBonev
 *
 */
public class ViewPreProcessorContextTest {

	@Test
	public void isViewPresent() throws Exception {
		assertFalse(new ViewPreProcessorContext(null, null).isViewPresent());
		assertTrue(new ViewPreProcessorContext(null, mockContent("<test>")).isViewPresent());
	}

	@Test
	public void getView() throws Exception {
		Content view = mock(Content.class);

		assertNull(new ViewPreProcessorContext(null, null).getView());

		Content descriptor = new ViewPreProcessorContext(null, view).getView();
		assertNotNull(descriptor);
		assertEquals(view, descriptor);
	}

	@Test
	public void getParsedView() throws Exception {
		assertNull(new ViewPreProcessorContext(null, null).getParsedView());

		ViewPreProcessorContext context = new ViewPreProcessorContext(null, mockContent("<xml/>"));
		Document document = context.getParsedView();
		assertNotNull(document);
	}

	@Test
	public void getOwner() throws Exception {
		ViewPreProcessorContext context = new ViewPreProcessorContext(mock(Serializable.class), null);
		assertNotNull(context.getOwner());
	}

	@Test
	public void modifyContent() throws Exception {
		ViewPreProcessorContext context = new ViewPreProcessorContext(null, mockContent("<xml id=\"someId\"></xml>"));
		context.getParsedView().getElementsByAttribute("id").first().appendText("test");
		context.setViewUpdated();
		String string = context.getView().getContent().asString();
		assertTrue(string.contains("test"));
	}

	@Test
	public void updateView() throws Exception {
		ViewPreProcessorContext context = new ViewPreProcessorContext(null, mockContent("<xml/>"));
		assertNotNull(context.getParsedView());
		String parsedOld = context.getParsedView().toString();
		assertFalse(parsedOld.contains("test"));

		context.updateView(Jsoup.parse("<test/>"));
		String updated = context.getView().getContent().asString();
		assertTrue(updated.contains("test"));
	}

	@Test
	public void setView() throws Exception {
		ViewPreProcessorContext context = new ViewPreProcessorContext(null, Content.createEmpty());
		assertNotNull(context.getView());
		assertNull(context.getView().getContent());

		context.setView(mockDescriptor("test"));

		assertNotNull(context.getView().getContent());
	}

	@Test
	public void getOldView() throws Exception {
		assertNull(new ViewPreProcessorContext(null, null).getOldView());
		assertNotNull(new ViewPreProcessorContext(null, null, mockDescriptor("<old>")).getOldView());
	}

	@Test
	public void getOldViewParsed() throws Exception {
		assertNull(new ViewPreProcessorContext(null, null).getOldViewParsed());
		assertNotNull(new ViewPreProcessorContext(null, null, mockDescriptor("<old>")).getOldViewParsed());
	}

	static FileDescriptor mockDescriptor(String content) {
		FileDescriptor descriptor = mock(FileDescriptor.class);
		when(descriptor.getInputStream())
				.thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		return descriptor;
	}

	static Content mockContent(String content) {
		return Content.create(null, mockDescriptor(content));
	}

}
