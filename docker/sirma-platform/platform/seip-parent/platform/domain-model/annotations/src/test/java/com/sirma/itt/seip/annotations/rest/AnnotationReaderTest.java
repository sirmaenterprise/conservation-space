package com.sirma.itt.seip.annotations.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import javax.ws.rs.core.MediaType;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.annotations.parser.JsonLdParserFactory;
import com.sirma.itt.seip.domain.instance.EmfInstance;

/**
 * Test for {@link AnnotationReader}
 *
 * @author BBonev
 */
public class AnnotationReaderTest {
	private static final MediaType MEDIA_TYPE = MediaType.valueOf(RDFFormat.JSONLD.getDefaultMIMEType());
	/**
	 * runs the parser once so it could load it's context so other method not to load it again
	 */
	@BeforeClass
	public static void initContext() throws Exception {
		RDFParserRegistry.getInstance().add(new JsonLdParserFactory());

		AnnotationReader reader = new AnnotationReader();
		reader.readFrom(null, null, null, null, null,
				AnnotationReaderTest.class.getClassLoader().getResourceAsStream("annotations/create.json"));
	}

	@Test
	public void convertSingle() throws Exception {

		AnnotationReader reader = new AnnotationReader();
		Annotation annotation = reader.readFrom(null, null, null, null, null,
				getClass().getClassLoader().getResourceAsStream("annotations/create.json"));
		assertNotNull(annotation);
		assertNotNull(annotation.getContent());
	}

	@Test
	public void convertWithReplies() throws Exception {

		AnnotationReader reader = new AnnotationReader();
		Annotation annotation = reader.readFrom(null, null, null, null, null,
				getClass().getClassLoader().getResourceAsStream("annotations/createWithReplies.json"));
		assertNotNull(annotation);
		assertNotNull(annotation.getContent());
		Collection<Annotation> replies = annotation.getReplies();
		assertNotNull(replies);
		assertEquals(2, replies.size());
		for (Annotation reply : replies) {
			assertNotNull(reply.getContent());
		}
	}

	@Test
	public void convertWithManyReplies() throws Exception {

		AnnotationReader reader = new AnnotationReader();
		Annotation annotation = reader.readFrom(null, null, null, null, null,
				getClass().getClassLoader().getResourceAsStream("annotations/createWithManyReplies.json"));
		assertNotNull(annotation);
		assertNotNull(annotation.getContent());
		Collection<Annotation> replies = annotation.getReplies();
		assertNotNull(replies);
		assertEquals(104, replies.size());
		for (Annotation reply : replies) {
			assertNotNull(reply.getContent());
		}
	}

	@Test
	public void isReadable() throws Exception {

		AnnotationReader reader = new AnnotationReader();
		assertTrue(reader.isReadable(Annotation.class, null, null, MEDIA_TYPE));
		assertFalse(reader.isReadable(Annotation.class, null, null, null));
		assertFalse(reader.isReadable(EmfInstance.class, null, null, MEDIA_TYPE));
	}
}
