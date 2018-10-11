package com.sirma.itt.seip.annotations.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.annotations.parser.JsonLdParserFactory;

/**
 * Test for {@link AnnotationCollectionReader}
 *
 * @author BBonev
 */
public class AnnotationCollectionReaderTest {
	private static final MediaType MEDIA_TYPE = MediaType.valueOf(RDFFormat.JSONLD.getDefaultMIMEType());
	/**
	 * runs the parser once so it could load it's context so other method not to load it again
	 */
	@BeforeClass
	public static void initContext() throws Exception {
		RDFParserRegistry.getInstance().add(new JsonLdParserFactory());

		AnnotationCollectionReader reader = new AnnotationCollectionReader();
		reader.readFrom(null, null, null, null, null,
				AnnotationCollectionReaderTest.class.getClassLoader().getResourceAsStream("annotations/create.json"));
	}

	@Test
	public void isCompatible() throws Exception {
		AnnotationCollectionReader reader = new AnnotationCollectionReader();
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { Annotation.class });
		assertTrue(reader.isReadable(Collection.class, type, null, MEDIA_TYPE));
		assertTrue(reader.isReadable(List.class, type, null, MEDIA_TYPE));
		assertFalse(reader.isReadable(Annotation.class, type, null, MEDIA_TYPE));
		assertFalse(reader.isReadable(Collection.class, type, null, null));
	}

	@Test
	public void convertSingle() throws Exception {

		AnnotationCollectionReader reader = new AnnotationCollectionReader();
		Collection<Annotation> collection = reader.readFrom(null, null, null, null, null,
				getClass().getClassLoader().getResourceAsStream("annotations/create.json"));
		assertNotNull(collection);
		assertEquals(1, collection.size());
		for (Annotation annotation : collection) {
			assertNotNull(annotation.getContent());
		}
	}

	@Test
	public void convertMultiple() throws Exception {

		AnnotationCollectionReader reader = new AnnotationCollectionReader();
		Collection<Annotation> collection = reader.readFrom(null, null, null, null, null,
				getClass().getClassLoader().getResourceAsStream("annotations/create-multiple.json"));
		assertNotNull(collection);
		assertEquals(2, collection.size());
		for (Annotation annotation : collection) {
			assertNotNull(annotation.getContent());
		}
	}

	@Test
	public void convertWithReplies() throws Exception {

		AnnotationCollectionReader reader = new AnnotationCollectionReader();
		Collection<Annotation> collection = reader.readFrom(null, null, null, null, null,
				getClass().getClassLoader().getResourceAsStream("annotations/createWithReplies.json"));
		assertNotNull(collection);
		assertEquals(1, collection.size());
		Annotation annotation = collection.iterator().next();
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

		AnnotationCollectionReader reader = new AnnotationCollectionReader();
		Collection<Annotation> collection = reader.readFrom(null, null, null, null, null,
				getClass().getClassLoader().getResourceAsStream("annotations/createWithManyReplies.json"));
		assertNotNull(collection);
		assertEquals(1, collection.size());
		Annotation annotation = collection.iterator().next();
		assertNotNull(annotation.getContent());
		Collection<Annotation> replies = annotation.getReplies();
		assertNotNull(replies);
		assertEquals(104, replies.size());
		for (Annotation reply : replies) {
			assertNotNull(reply.getContent());
		}
	}

	@Test
	public void convertMultipleWithManyReplies() throws Exception {

		AnnotationCollectionReader reader = new AnnotationCollectionReader();
		Collection<Annotation> collection = reader.readFrom(null, null, null, null, null,
				getClass().getClassLoader().getResourceAsStream("annotations/createMultipleWithManyReplies.json"));
		assertNotNull(collection);
		assertEquals(21, collection.size());
		for (Annotation annotation : collection) {
			assertNotNull(annotation.getContent());
			Collection<Annotation> replies = annotation.getReplies();
			assertNotNull(replies);
			assertEquals(104, replies.size());
			for (Annotation reply : replies) {
				assertNotNull(reply.getContent());
			}
		}
	}
}
