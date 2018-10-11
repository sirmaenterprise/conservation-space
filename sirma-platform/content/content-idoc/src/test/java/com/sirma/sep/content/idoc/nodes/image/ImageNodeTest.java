package com.sirma.sep.content.idoc.nodes.image;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import com.sirma.sep.content.idoc.nodes.image.Dimension;
import com.sirma.sep.content.idoc.nodes.image.ImageNode;

/**
 * Test for {@link ImageNode}
 *
 * @author BBonev
 */
public class ImageNodeTest {

	@Test
	public void should_reportForPrensetDimentions_whenHightAndWidthArePresent() throws Exception {
		Attributes attributes = new Attributes();

		Element element = new Element(Tag.valueOf("img"), "", attributes);
		ImageNode node = new ImageNode(element);
		assertFalse(node.hasImageDimensions());
		attributes.put(ImageNode.ATTR_HEIGHT, "10");
		assertFalse(node.hasImageDimensions());
		attributes.put(ImageNode.ATTR_WIDTH, "10");
		assertTrue(node.hasImageDimensions());
		attributes.remove(ImageNode.ATTR_HEIGHT);
		assertFalse(node.hasImageDimensions());
	}

	@Test
	public void should_returnDimentions_ifPresent() throws Exception {
		Attributes attributes = new Attributes();

		Element element = new Element(Tag.valueOf("img"), "", attributes);
		ImageNode node = new ImageNode(element);
		assertFalse(node.getImageDimensions().isPresent());
		attributes.put(ImageNode.ATTR_HEIGHT, "10");
		assertFalse(node.getImageDimensions().isPresent());
		attributes.put(ImageNode.ATTR_WIDTH, "11");
		assertTrue(node.getImageDimensions().isPresent());
		Dimension dimension = node.getImageDimensions().get();
		assertEquals(10, dimension.getHeight());
		assertEquals(11, dimension.getWidth());
		attributes.remove(ImageNode.ATTR_HEIGHT);
		assertFalse(node.getImageDimensions().isPresent());
	}

	@Test
	public void should_updateCorrectDimentions_whenSet() throws Exception {
		Attributes attributes = new Attributes();

		Element element = new Element(Tag.valueOf("img"), "", attributes);
		ImageNode node = new ImageNode(element);
		assertFalse(node.getImageDimensions().isPresent());
		node.setImageDimensions(11, 10);
		assertTrue(node.getImageDimensions().isPresent());
		Dimension dimension = node.getImageDimensions().get();
		assertEquals(10, dimension.getHeight());
		assertEquals(11, dimension.getWidth());
	}

	@Test
	public void should_returnTrue_whenSrcContainsBase64Data() throws Exception {
		Attributes attributes = new Attributes();

		Element element = new Element(Tag.valueOf("img"), "", attributes);
		ImageNode node = new ImageNode(element);

		assertFalse(node.isEmbedded());
		attributes.put(ImageNode.ATTR_SRC, "data:image/png;base64,====");
		assertTrue(node.isEmbedded());
	}

	@Test
	public void should_ProvideEmbeddedMimetype_whenSrcContainsBase64Data() throws Exception {
		Attributes attributes = new Attributes();

		Element element = new Element(Tag.valueOf("img"), "", attributes);
		ImageNode node = new ImageNode(element);

		assertFalse(node.getEmbeddedImageMimetype().isPresent());
		attributes.put(ImageNode.ATTR_SRC, "data:image/png;base64,====");
		assertEquals("image/png", node.getEmbeddedImageMimetype().get());
	}

	@Test
	public void should_GetEmeddedData_whenSrcContainsBase64Data() throws Exception {
		Attributes attributes = new Attributes();

		Element element = new Element(Tag.valueOf("img"), "", attributes);
		ImageNode node = new ImageNode(element);

		assertFalse(node.getEmbeddedData().isPresent());
		attributes.put(ImageNode.ATTR_SRC, "data:image/png;base64,AA==");
		assertNotNull(node.getEmbeddedData().get());
	}
}
