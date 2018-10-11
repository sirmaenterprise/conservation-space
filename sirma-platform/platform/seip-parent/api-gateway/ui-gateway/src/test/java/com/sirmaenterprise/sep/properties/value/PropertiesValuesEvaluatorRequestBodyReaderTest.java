package com.sirmaenterprise.sep.properties.value;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Test for {@link PropertiesValuesEvaluatorRequestBodyReader} class.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertiesValuesEvaluatorRequestBodyReaderTest {

	private static final String FILE_PROPERTY_WITH_DOT = "property-with-dot.json";
	private static final String FILE_PROPERTY_WITHOUT_DOT = "property-without-dot.json";

	private PropertiesValuesEvaluatorRequestBodyReader reader = new PropertiesValuesEvaluatorRequestBodyReader();

	@Test
	public void should_ReturnCorrectExpressionTemplateRequest_When_PropertyNameIsWithoutDot() throws IOException {
		try (InputStream stream = this.getClass().getResourceAsStream(FILE_PROPERTY_WITHOUT_DOT)) {
			PropertiesValuesEvaluatorRequest expressionTemplateRequest = reader.readFrom(null, null, null, null, null,
																						 stream);

			Optional<Map.Entry<String, List<PropertiesValuesEvaluatorProperty>>> entry = expressionTemplateRequest.getExpressionTemplateModelAsStream()
					.findFirst();

			List<PropertiesValuesEvaluatorProperty> value = entry.get().getValue();
			Assert.assertEquals("selected-report-type-instance-id", entry.get().getKey());
			Assert.assertEquals(value.size(), 1);
			PropertiesValuesEvaluatorProperty propertiesValuesEvaluatorProperty = value.get(0);

			Assert.assertEquals("identifier", propertiesValuesEvaluatorProperty.getInstancePropertyName());
			Assert.assertEquals("generatedField", propertiesValuesEvaluatorProperty.getNewInstancePropertyName());
			Assert.assertEquals("identifier", propertiesValuesEvaluatorProperty.getReturnInstancePropertyName());
			Assert.assertEquals("new-instance-definition-id", expressionTemplateRequest.getNewInstanceDefinitionId());
		}
	}

	@Test
	public void should_ReturnCorrectExpressionTemplateRequest_When_PropertyNameIsWithDot() throws IOException {
		try (InputStream stream = this.getClass().getResourceAsStream(FILE_PROPERTY_WITH_DOT)) {
			PropertiesValuesEvaluatorRequest expressionTemplateRequest = reader.readFrom(null, null, null, null, null,
																						 stream);

			Optional<Map.Entry<String, List<PropertiesValuesEvaluatorProperty>>> entry = expressionTemplateRequest.getExpressionTemplateModelAsStream()
					.findFirst();

			List<PropertiesValuesEvaluatorProperty> value = entry.get().getValue();
			Assert.assertEquals("selected-report-type-instance-id", entry.get().getKey());
			Assert.assertEquals(value.size(), 1);
			PropertiesValuesEvaluatorProperty propertiesValuesEvaluatorProperty = value.get(0);

			Assert.assertEquals("emf:email", propertiesValuesEvaluatorProperty.getInstancePropertyName());
			Assert.assertEquals("generatedField", propertiesValuesEvaluatorProperty.getNewInstancePropertyName());
			Assert.assertEquals("emf:createdBy.emf:email", propertiesValuesEvaluatorProperty.getReturnInstancePropertyName());
			Assert.assertEquals("new-instance-definition-id", expressionTemplateRequest.getNewInstanceDefinitionId());
		}
	}

	@Test(expected = BadRequestException.class)
	public void should_throwBadRequestException_When_JsonIsEmpty() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8))) {
			reader.readFrom(null, null, null, null, null, stream);
		}
	}

	@Test
	public void should_CanNotRead_When_ClassIsNotExpressionTemplateRequest() {
		Assert.assertFalse(reader.isReadable(PropertiesValuesEvaluatorRequestBodyReaderTest.class, null, null, null));
	}

	@Test
	public void should_CanRead_When_ClassIsExpressionTemplateRequest() {
		Assert.assertTrue(reader.isReadable(PropertiesValuesEvaluatorRequest.class, null, null, null));
	}
}