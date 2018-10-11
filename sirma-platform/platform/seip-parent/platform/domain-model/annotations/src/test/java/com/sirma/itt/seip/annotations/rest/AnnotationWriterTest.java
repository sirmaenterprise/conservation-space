package com.sirma.itt.seip.annotations.rest;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link AnnotationWriter}
 *
 * @author BBonev
 */
public class AnnotationWriterTest {

	@InjectMocks
	private AnnotationWriter writer;
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private NamespaceRegistryService namespaceRegistryService;
	private ValueFactory factory = SimpleValueFactory.getInstance();

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(typeConverter.convert(eq(String.class), any(Object.class))).then(a -> {
			Object arg = a.getArgumentAt(1, Object.class);
			if (arg instanceof Date) {
				return ISO8601DateFormat.format((Date) arg);
			}
			if (arg instanceof Calendar) {
				return ISO8601DateFormat.format((Calendar) arg);
			}
			return arg.toString();
		});
		when(namespaceRegistryService.getShortUri(any(IRI.class))).then(a -> {
			IRI uri = a.getArgumentAt(0, IRI.class);
			if (EMF.NAMESPACE.equals(uri.getNamespace())) {
				return EMF.PREFIX + ":" + uri.getLocalName();
			}
			return uri.toString();
		});
		when(namespaceRegistryService.getShortUri(anyString())).then(a -> {
			String arg = a.getArgumentAt(0, String.class);
			if (arg.startsWith(EMF.PREFIX)) {
				return arg;
			} else if (arg.startsWith(EMF.NAMESPACE)) {
				return EMF.PREFIX + ":" + arg.substring(EMF.NAMESPACE.length());
			}
			return arg;
		});
	}

	@Test
	public void writeAnnotation() throws Exception {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		writer.writeTo(buildAnnotation(), outputStream, true);

		String response = new String(outputStream.toByteArray());
		assertFalse(StringUtils.isBlank(response));
		JsonAssert.assertJsonEquals(readContent("annotations/response.json"), response);
	}

	@Test
	public void writeAnnotation_noAutoClose() throws Exception {
		ByteArrayOutputStream outputStream = spy(new ByteArrayOutputStream());
		writer.writeTo(buildAnnotation(), outputStream, false);
		String response = new String(outputStream.toByteArray());
		verify(outputStream, never()).close();
		assertFalse(StringUtils.isBlank(response));
		JsonAssert.assertJsonEquals(readContent("annotations/response.json"), response);
	}

	@Test
	public void writeAnnotationWithReplies() throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Annotation annotation = buildAnnotation();
		annotation.getReplies().add(buildAnnotation());
		annotation.getReplies().add(buildAnnotation());

		writer.writeTo(annotation, outputStream, true);

		String response = new String(outputStream.toByteArray());
		assertFalse(StringUtils.isBlank(response));
		JsonAssert.assertJsonEquals(readContent("annotations/responseWithReplies.json"), response);
	}

	@Test
	public void writeMultiple() throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		writer.writeTo(Arrays.asList(buildAnnotation(), buildAnnotation()), outputStream, true);
		String response = new String(outputStream.toByteArray());
		assertFalse(StringUtils.isBlank(response));
		JsonAssert.assertJsonEquals(readContent("annotations/responseMultiple.json"), response);
	}

	@Test(expected = IllegalArgumentException.class)
	public void write_NotSupportedData() throws Exception {

		writer.writeTo(new EmfInstance(), null, true);
	}

	private Annotation buildAnnotation() throws IOException, ParseException {
		Annotation annotation = new Annotation();
		annotation.setId(EMF.NAMESPACE + "annotationId");
		annotation.setContent(readContent("annotations/writerData.json"));
		EmfUser user = new EmfUser();
		user.setId(EMF.PREFIX +":admin");
		user.setName("admin");
		user.setDisplayName("Admin");
		annotation.add("emf:modifiedBy", user);
		annotation.add("emf:createdBy", user);

		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.setTime(ISO8601DateFormat.parse("2016-03-20T10:00:00.000Z"));
		annotation.add("emf:modifiedOn", calendar);
		annotation.add("emf:createdOn", factory.createLiteral("2016-03-20T10:00:00.000Z"));
		annotation.add("emf:status", "OPEN");
		annotation.add("emf:isDeleted", Boolean.FALSE);
		return annotation;
	}

	private String readContent(String resource) throws IOException {
		try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(resource)) {
			return IOUtils.toString(stream);
		}
	}
}
