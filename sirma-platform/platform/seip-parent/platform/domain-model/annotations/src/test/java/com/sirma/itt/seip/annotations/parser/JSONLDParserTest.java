package com.sirma.itt.seip.annotations.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.repository.creator.RepositoryUtils;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.semantic.model.vocabulary.OA;

/**
 * Test for {@link JSONLDParser}
 *
 * @author BBonev
 */
public class JSONLDParserTest {

	@BeforeClass
	public static void init() {
		RDFParserRegistry.getInstance().add(new JsonLdParserFactory());
	}

	@Test
	public void testParser() throws Exception {
		String annotationData = readTestData();

		AnnotationCollectorHandler handler = new AnnotationCollectorHandler();

		RepositoryUtils.parseRDFFile(new StringReader(annotationData), RDFFormat.JSONLD, "",
				handler.addHandlerToParser());

		Collection<Annotation> annotations = handler.getAnnotations();
		assertNotNull(annotations);
		assertFalse(annotations.isEmpty());
		Annotation annotation = annotations.iterator().next();
		assertNull(annotation.getId());
		assertEquals(OA.COMMENTING, annotation.get(OA.MOTIVATED_BY.toString()));
		assertTrue(annotation.get(OA.HAS_BODY.toString()).toString().contains("<p>asdf</p>"));
	}

	@Test(expected = SemanticPersistenceException.class)
	public void testParser_noDefinedContext() throws Exception {
		String annotationData = "{ }";


		RepositoryUtils.parseRDFFile(new StringReader(annotationData), RDFFormat.JSONLD, "invalidNamespace",
				parser -> {
					JSONLDParser jsonldParser = (JSONLDParser) parser;
					jsonldParser.setContextBuilder((uri, options) -> null);
				});
	}

	@Test
	public void testParser_multipleItems() throws Exception {

		AnnotationCollectorHandler handler = new AnnotationCollectorHandler();

		RepositoryUtils.parseRDFFile(getMultipleStream(), RDFFormat.JSONLD, "", handler.addHandlerToParser());

		Collection<Annotation> annotations = handler.getAnnotations();
		assertNotNull(annotations);
		assertFalse(annotations.isEmpty());
		Annotation annotation = annotations.iterator().next();
		assertNull(annotation.getId());
		assertEquals(OA.COMMENTING, annotation.get(OA.MOTIVATED_BY.toString()));
		assertTrue(annotation.get(OA.HAS_BODY.toString()).toString().contains("<p>вегфвегвгвегвегвегвегвег</p>"));
	}

	@Test
	public void testParserFromStream() throws Exception {

		AnnotationCollectorHandler handler = new AnnotationCollectorHandler();

		RepositoryUtils.parseRDFFile(getSingleStream(), RDFFormat.JSONLD, "", handler.addHandlerToParser());

		Collection<Annotation> annotations = handler.getAnnotations();
		assertNotNull(annotations);
		assertFalse(annotations.isEmpty());
		Annotation annotation = annotations.iterator().next();
		assertNull(annotation.getId());
		assertEquals(OA.COMMENTING, annotation.get(OA.MOTIVATED_BY.toString()));
		assertTrue(annotation.get(OA.HAS_BODY.toString()).toString().contains("<p>asdf</p>"));
	}

	@Test(expected = SemanticPersistenceException.class)
	public void parseInvalidInputStream() throws Exception {
		RepositoryUtils.parseRDFFile(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)),
				RDFFormat.JSONLD, "");
	}

	@Test(expected = SemanticPersistenceException.class)
	public void parseInvalidInputReader() throws Exception {
		RepositoryUtils.parseRDFFile(new StringReader("test"), RDFFormat.JSONLD, "");
	}

	private static InputStream getSingleStream() {
		return JSONLDParserTest.class.getClassLoader().getResourceAsStream("annotations/create.json");
	}

	private static InputStream getMultipleStream() {
		return JSONLDParserTest.class.getClassLoader().getResourceAsStream("annotations/create-multiple.json");
	}

	private static String readTestData() throws IOException {
		String annotationData = null;
		try (InputStream inputData = getSingleStream()) {
			annotationData = IOUtils.toString(inputData, StandardCharsets.UTF_8);
		}
		return annotationData;
	}
}
