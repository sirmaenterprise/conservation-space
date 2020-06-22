package com.sirma.itt.seip.domain.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;

/**
 * Tests {@link ValidationReport} and the related classes/utilities.
 *
 * @author Mihail Radkov
 */
public class ValidationReportTest {

	private LabelProvider labelProvider;

	@Before
	public void init() {
		labelProvider = Mockito.mock(LabelProvider.class);
		when(labelProvider.getLabel("parametrized.message")).thenReturn("Error {0} occurred {1} for {2}");
		when(labelProvider.getLabel("parametrized.message", "en")).thenReturn("Error {0} occurred {1} for {2}");
		when(labelProvider.getLabel("parametrized.message", "bg")).thenReturn("Грешка {0} се случи {1} за {2}");
	}

	@Test
	public void shouldBuildAndTranslateReports() {
		TestMessageBuilder testMessageBuilder = new TestMessageBuilder();
		testMessageBuilder.addSimpleMessage();
		testMessageBuilder.addParametrizedMessage();
		testMessageBuilder.addNodeMessage();
		testMessageBuilder.addNodeWarning();

		ValidationReport report = testMessageBuilder.build();
		assertNotNull(report);
		assertFalse(report.isValid());
		assertEquals(4, report.getValidationMessages().size());

		report.addMessage(ValidationMessage.error("entity", "Another error occurred!"));
		assertEquals(5, report.getValidationMessages().size());

		report.addError("Some generic error!");
		assertEquals(6, report.getValidationMessages().size());

		// Wrap the produced report in the translator utility
		ValidationReportTranslator reportTranslator = new ValidationReportTranslator(labelProvider, report);

		// Should provide generic errors
		List<String> genericErrors = reportTranslator.getGenericErrors();
		assertEquals(2, genericErrors.size());
		assertTrue(genericErrors.contains("Error occurred!"));
		assertTrue(genericErrors.contains("Some generic error!"));

		// Should map node ids
		Map<String, List<String>> nodeErrors = reportTranslator.getNodeErrors();
		assertEquals(1, nodeErrors.size());
		assertTrue(nodeErrors.containsKey("entity"));
		assertEquals(3, nodeErrors.get("entity").size());
		assertTrue(nodeErrors.get("entity").contains("Error occurred for entity!"));
		assertTrue(nodeErrors.get("entity").contains("Error one occurred true for three"));
		assertTrue(nodeErrors.get("entity").contains("Another error occurred!"));

		Map<String, List<String>> nodeWarnings = reportTranslator.getNodeMessages(ValidationMessage.MessageSeverity.WARNING);
		assertEquals(1, nodeWarnings.size());
		assertTrue(nodeWarnings.containsKey("entity"));
		assertEquals(1, nodeWarnings.get("entity").size());
		assertTrue(nodeWarnings.get("entity").contains("Some entity warning"));

		// Should translate and provide all messages as single collection
		List<String> translatedErrors = reportTranslator.getErrors();
		assertEquals(5, translatedErrors.size());
		assertTrue(translatedErrors.contains("Error occurred!"));
		assertTrue(translatedErrors.contains("Error one occurred true for three"));
		assertTrue(translatedErrors.contains("Error occurred for entity!"));
		assertTrue(translatedErrors.contains("Some generic error!"));
		assertTrue(translatedErrors.contains("Another error occurred!"));

		// Should return translated messages in requested language (if there are labels for that lang)
		translatedErrors = reportTranslator.getErrors("bg");
		assertEquals(5, reportTranslator.getErrors().size());
		assertTrue(translatedErrors.contains("Грешка one се случи true за three"));
		assertTrue(translatedErrors.contains("Error occurred for entity!"));
	}

	@Test
	public void shouldInterpolateEvenWithoutNeededParams() {
		when(labelProvider.getLabel("parametrized.message")).thenReturn("Error {0} occurred {1} for {2} and {3} + {4}");

		TestMessageBuilder testMessageBuilder = new TestMessageBuilder();
		testMessageBuilder.addParametrizedMessage();

		ValidationReport report = testMessageBuilder.build();

		ValidationReportTranslator reportTranslator = new ValidationReportTranslator(labelProvider, report);
		List<String> translatedErrors = reportTranslator.getErrors();
		assertEquals(1, translatedErrors.size());
		assertTrue(translatedErrors.contains("Error one occurred true for three and {3} + {4}"));
	}

	@Test
	public void shouldFallBackToEnglish() {
		TestMessageBuilder testMessageBuilder = new TestMessageBuilder();
		testMessageBuilder.addParametrizedMessage();

		ValidationReport report = testMessageBuilder.build();

		ValidationReportTranslator reportTranslator = new ValidationReportTranslator(labelProvider, report);

		List<String> translatedErrors = reportTranslator.getErrors("fi");
		assertEquals(1, translatedErrors.size());
		assertTrue(translatedErrors.contains("Error one occurred true for three"));
	}

	private class TestMessageBuilder extends ValidationMessageBuilder {

		private void addSimpleMessage() {
			error(null, "Error occurred!");
		}

		private void addParametrizedMessage() {
			error("entity", "parametrized.message").setParams("one", true, "three");
		}

		private void addNodeMessage() {
			error("entity", "Error occurred for entity!");
		}

		private void addNodeWarning() {
			warning("entity", "Some entity warning");
		}
	}
}
