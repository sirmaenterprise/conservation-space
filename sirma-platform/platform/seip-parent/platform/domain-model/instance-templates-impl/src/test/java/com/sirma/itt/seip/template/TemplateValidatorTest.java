package com.sirma.itt.seip.template;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * Tests {@link TemplateValidator}.
 * 
 * @author Vilizar Tsonev
 */
public class TemplateValidatorTest {

	@Test
	public void should_Detect_Duplicated_Corresponding_Instances() {
		Template template1 = constructTemplate("template1", "Template 1", "test content", "emf:Instance",
				"sampleForType", true, "");
		Template template2 = constructTemplate("template2", "Template 2", "test content", "emf:Instance",
				"sampleForType2", true, "");

		verifyNotValid(TemplateValidator.validate(Arrays.asList(template1, template2)));
	}

	@Test
	public void should_Detect_Missing_Title() {
		Template template1 = constructTemplate("template1", null, "test content", "emf:Instance",
				"sampleForType", true, "");
		verifyNotValid(TemplateValidator.validate(Arrays.asList(template1)));
	}

	@Test
	public void should_Detect_Missing_Type() {
		Template template1 = constructTemplate("template1", "Template 1", "test content", "emf:Instance", null, true,
				"");
		verifyNotValid(TemplateValidator.validate(Arrays.asList(template1)));
	}

	@Test
	public void should_Detect_Missing_Purpose() {
		Template template1 = constructTemplate("template1", "Template 1", "test content", "emf:Instance",
				"sampleForType", true, "");
		template1.setPurpose(null);
		verifyNotValid(TemplateValidator.validate(Arrays.asList(template1)));
	}

	@Test
	public void should_Detect_Missing_Content() {
		Template template1 = constructTemplate("template1", "Template 1", null, "emf:Instance",
				"sampleForType", true, "");
		verifyNotValid(TemplateValidator.validate(Arrays.asList(template1)));
	}

	@Test
	public void should_Allow_Email_Templates_With_Missing_Purpose() {
		Template template1 = constructTemplate("template1", "Template 1", "test content", "emf:Instance",
				"emailTemplate", true, "");
		template1.setPurpose(null);
		verifyValid(TemplateValidator.validate(Arrays.asList(template1)));
	}

	@Test
	public void should_Allow_Email_Templates_Where_Title_Does_Not_Correspond_To_Identifier() {
		Template template1 = constructTemplate("template1", "Some random title", "test content", "emf:Instance",
				"emailTemplate", true, "");
		verifyValid(TemplateValidator.validate(Arrays.asList(template1)));
	}

	@Test
	public void should_Detect_When_Title_Does_Not_Correspond_To_Identifier() {
		Template template1 = constructTemplate("template1", "Random title", "test content", "emf:Instance",
				"sampleForType", true, "");

		verifyNotValid(TemplateValidator.validate(Arrays.asList(template1)));
	}

	@Test
	public void should_Detect_When_No_Primary_Template_For_Rule() {
		Template template1 = constructTemplate("template1", "Template 1", "test content", "emf:Instance",
				"Case1", false, "(department == \"DEV\" || department == \"BA\")");
		Template template2 = constructTemplate("template2", "Template 2", "test content", "emf:Instance2",
				"Case1", false, "(department == \"BA\" || department == \"DEV\")");
		// have one primary, but with different rule. Doesn't count...
		Template template3 = constructTemplate("template3", "Template 3", "test content", "emf:Instance3",
				"Case1", true, "(department == \"BA\" || department == \"ENG\")");

		verifyNotValid(TemplateValidator.validate(Arrays.asList(template1, template2, template3)));
	}

	@Test
	public void should_Detect_When_No_Primary_Template_For_Same_Type_Purpose_Without_Rule() {
		Template template1 = constructTemplate("template1", "Template 1", "test content", "emf:Instance", "Case1",
				false, null);
		Template template2 = constructTemplate("template2", "Template 2", "test content", "emf:Instance2", "Case1",
				false, null);
		// have one primary, but with a rule. Doesn't count...
		Template template3 = constructTemplate("template3", "Template 3", "test content", "emf:Instance3", "Case1",
				true, "(department == \"BA\" || department == \"ENG\")");

		verifyNotValid(TemplateValidator.validate(Arrays.asList(template1, template2, template3)));
	}

	@Test
	public void should_Detect_When_Two_Primary_Templates_For_Same_Rule() {
		Template template1 = constructTemplate("template1", "Template 1", "test content", "emf:Instance", "Case1",
				true, "(department == \"DEV\" || department == \"BA\")");
		Template template2 = constructTemplate("template2", "Template 2", "test content", "emf:Instance2", "Case1",
				true, "(department == \"BA\" || department == \"DEV\")");

		verifyNotValid(TemplateValidator.validate(Arrays.asList(template1, template2)));
	}

	@Test
	public void should_Allow_When_One_Primary_Per_Rule_Or_Group() {
		Template template1 = constructTemplate("template1", "Template 1", "test content", "emf:Instance", "Case1",
				true, "(department == \"DEV\" || department == \"BA\")");
		Template template2 = constructTemplate("template2", "Template 2", "test content", "emf:Instance2", "Case1",
				false, "(department == \"BA\" || department == \"DEV\")");
		Template template3 = constructTemplate("template3", "Template 3", "test content", "emf:Instance3", "Case10",
				false, null);
		Template template4 = constructTemplate("template4", "Template 4", "test content", "emf:Instance4", "Case10",
				true, null);

		verifyValid(TemplateValidator.validate(Arrays.asList(template1, template2, template3, template4)));
	}

	@Test
	public void should_Detect_When_Two_Primary_Templates_For_Same_Type_And_Purpose() {
		Template template1 = constructTemplate("template1", "Template 1", "test content", "emf:Instance", "Case1", true,
				null);
		Template template2 = constructTemplate("template2", "Template 2", "test content", "emf:Instance2", "Case1",
				true, null);

		verifyNotValid(TemplateValidator.validate(Arrays.asList(template1, template2)));
	}

	private static void verifyValid(List<String> errors) {
		assertTrue(errors.isEmpty());
	}

	private static void verifyNotValid(List<String> errors) {
		assertFalse(errors.isEmpty());
	}

	private static Template constructTemplate(Serializable id, String title, String content,
			String correspondingInstance, String forType, boolean primary, String rule) {
		Template template = new Template();
		template.setId((String) id);
		template.setContent(content);
		if (StringUtils.isNotBlank(correspondingInstance)) {
			template.setCorrespondingInstance(correspondingInstance);
		}
		template.setForType(forType);
		template.setTitle(title);
		template.setPurpose(TemplatePurposes.CREATABLE);
		template.setPrimary(Boolean.valueOf(primary));
		template.setRule(rule);

		return template;
	}

}
