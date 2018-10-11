package com.sirma.itt.seip.annotations.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import javax.json.stream.JsonParsingException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sirma.itt.seip.annotations.model.Annotation;

/**
 * Tests for {@link AnnotationParser}
 *
 * @author BBonev
 */
public class AnnotationParserTest {

	@BeforeClass
	public static void testName() throws Exception {
		new JsonLdParserFactory().registerOnStartup();
	}

	@Test
	public void parseSingle() throws Exception {
		Annotation annotation = AnnotationParser
				.parseSingle(this.getClass().getClassLoader().getResourceAsStream("annotations/create.json"));
		assertNotNull(annotation);
		assertNotNull(annotation.getContent());
	}

	@Test
	public void parseMultiple_Single() throws Exception {
		Collection<Annotation> annotations = AnnotationParser
				.parse(this.getClass().getClassLoader().getResourceAsStream("annotations/create.json"));
		assertNotNull(annotations);
		assertFalse(annotations.isEmpty());
		assertEquals(1, annotations.size());
		assertNotNull(annotations.iterator().next().getContent());
	}

	@Test
	public void parseMultiple() throws Exception {
		Collection<Annotation> annotations = AnnotationParser
				.parse(this.getClass().getClassLoader().getResourceAsStream("annotations/create-multiple.json"));
		assertNotNull(annotations);
		assertFalse(annotations.isEmpty());
		assertEquals(2, annotations.size());
		assertNotNull(annotations.iterator().next().getContent());
	}

	@Test
	public void parseMultipleWithReplies() throws Exception {
		Collection<Annotation> annotations = AnnotationParser.parse(
				this.getClass().getClassLoader().getResourceAsStream("annotations/createMultipleWithManyReplies.json"));
		assertNotNull(annotations);
		assertFalse(annotations.isEmpty());
		assertEquals(21, annotations.size());
		assertNotNull(annotations.iterator().next().getContent());
	}

	@Test(expected = JsonParsingException.class)
	public void parseSingle_invalidData() throws Exception {
		AnnotationParser.parseSingle(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));
	}

	@Test(expected = JsonParsingException.class)
	public void parseMultiple_invalidData() throws Exception {
		AnnotationParser.parse(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));
	}

}
