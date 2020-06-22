package com.sirma.itt.seip.annotations.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.OutputStream;

import javax.ws.rs.core.MediaType;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.domain.instance.EmfInstance;

/**
 * Test for {@link AnnotationBodyWriter}
 *
 * @author BBonev
 */
public class AnnotationBodyWriterTest {

	private static final MediaType MEDIA_TYPE = MediaType.valueOf(RDFFormat.JSONLD.getDefaultMIMEType());
	@InjectMocks
	private AnnotationBodyWriter bodyWriter;
	@Mock
	private AnnotationWriter writer;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void isWritable() throws Exception {
		assertTrue(bodyWriter.isWriteable(Annotation.class, null, null, MEDIA_TYPE));
		assertFalse(bodyWriter.isWriteable(Annotation.class, null, null, null));
		assertFalse(bodyWriter.isWriteable(EmfInstance.class, null, null, MEDIA_TYPE));
	}

	@Test
	public void writeAnnotation() throws Exception {

		OutputStream outputStream = mock(OutputStream.class);
		bodyWriter.writeTo(new Annotation(), null, null, null, null, null, outputStream);

		verify(writer).writeTo(any(Annotation.class), eq(outputStream), eq(true));
	}
}
