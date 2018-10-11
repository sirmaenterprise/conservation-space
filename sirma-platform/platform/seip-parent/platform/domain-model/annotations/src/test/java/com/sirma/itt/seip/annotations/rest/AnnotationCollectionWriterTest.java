package com.sirma.itt.seip.annotations.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.annotations.model.Annotation;

/**
 * Test for {@link AnnotationCollectionWriter}
 *
 * @author BBonev
 */
public class AnnotationCollectionWriterTest {
	private static final MediaType MEDIA_TYPE = MediaType.valueOf(RDFFormat.JSONLD.getDefaultMIMEType());

	@InjectMocks
	private AnnotationCollectionWriter bodyWriter;
	@Mock
	private AnnotationWriter writer;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void writeAnnotation() throws Exception {

		OutputStream outputStream = mock(OutputStream.class);
		bodyWriter.writeTo(Arrays.asList(new Annotation()), null, null, null, null, null, outputStream);

		verify(writer).writeTo(any(Collection.class), eq(outputStream), eq(true));
	}

	@Test
	public void isCompatible() throws Exception {
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { Annotation.class });
		assertTrue(bodyWriter.isWriteable(Collection.class, type, null, MEDIA_TYPE));
		assertTrue(bodyWriter.isWriteable(List.class, type, null, MEDIA_TYPE));
		assertFalse(bodyWriter.isWriteable(Annotation.class, type, null, MEDIA_TYPE));
		assertFalse(bodyWriter.isWriteable(Collection.class, type, null, null));
	}
}
