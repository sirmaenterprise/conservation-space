/**
 *
 */
package com.sirma.itt.seip.template.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.template.Template;

public class TemplateRuleUtilsTest {

	@Test
	public void should_ReturnAllTemplatesWithoutARule_WhenNoTemplateWithRuleMatchingTheCriteria() {
		havingTemplate("Case10 Template 1", null);
		havingTemplate("Case10 Template 2", "functional == \"ME\"");
		havingTemplate("Case10 Template 3", "functional == \"ME\"");
		havingTemplate("Case10 Template 4", "functional == \"EL\"");

		forCriteria("functional", "ALL");

		expectTemplates("Case10 Template 1");
	}

	@Test
	public void should_SkipTemplates_when_TheRuleHasFieldsNotProvidedInTheCriteria() {
		havingTemplate("Case10 Template 1", null);
		havingTemplate("Case10 Template 2", "functional2 == \"ME\"");
		havingTemplate("Case10 Template 3", "functional2 == \"ME\"");
		havingTemplate("Case10 Template 4", "functional == \"EL\"");

		forCriteria("functional", "ME");

		expectTemplates("Case10 Template 1");
	}

	@Test
	public void should_ReturnsOnlyTemplatesMatchingTheRule() {
		havingTemplate("Case10 Template 1", null);
		havingTemplate("Case10 Template 2", "functional == \"ME\"");
		havingTemplate("Case10 Template 3", "functional == \"ME\"");
		havingTemplate("Case10 Template 4", "functional == \"EL\"");

		forCriteria("functional", "ME");

		expectTemplates("Case10 Template 2", "Case10 Template 3");
	}

	@Test
	public void should_ReturnsOnlyTemplatesMatchingTheRule_Scenario2() {
		havingTemplate("Case10 Template 1", null);
		havingTemplate("Case10 Template 2", "functional == \"ME\"");
		havingTemplate("Case10 Template 3", "functional == \"ME\"");
		havingTemplate("Case10 Template 4", "functional == \"EL\"");

		forCriteria("functional", "EL");

		expectTemplates("Case10 Template 4");
	}

	@Test
	public void should_HandleSingleValuedListsInTheCriteria() {
		havingTemplate("Case10 Template 1", null);
		havingTemplate("Case10 Template 2", "functional == \"ME\"");
		havingTemplate("Case10 Template 3", "functional == \"ME\"");
		havingTemplate("Case10 Template 4", "functional == \"EL\"");

		forCriteria("functional", (Serializable) Arrays.asList("EL"));

		expectTemplates("Case10 Template 4");
	}

	@Test
	public void should_NotHandleSingleValuedListsInTheCriteria() {
		havingTemplate("Case10 Template 1", null);
		havingTemplate("Case10 Template 2", "functional == \"ME\"");
		havingTemplate("Case10 Template 3", "functional == \"ME\"");
		havingTemplate("Case10 Template 4", "functional == \"EL\"");

		forCriteria("functional", (Serializable) Arrays.asList("EL", "ME"));

		expectTemplates("Case10 Template 4");
	}

	@Test
	public void should_HandleColonsInPropertyNames() {
		havingTemplate("Case10 Template 1", null);
		havingTemplate("Case10 Template 2", "emf:functional == \"OY\"");
		havingTemplate("Case10 Template 3", "functional == \"ME\"");

		forCriteria("emf:functional", "OY");

		expectTemplates("Case10 Template 2");
	}

	@Test
	public void should_ParseAnExpressionWithBooleanField() {
		havingRule("primary == true");

		expectFields(new Object[] { "primary", true });
	}

	@Test
	public void should_ParseAnExpressionWitStringField_PuttingItInAlist() {
		havingRule("department == \"DEV\"");

		expectFields(new Object[] { "department", Arrays.asList("DEV") });
	}

	@Test
	public void should_ParseExpression_when_HavingOrStatementsForOneField() {
		havingRule("(department == \"DEV\" || department == \"QA\")");

		expectFields(new Object[] { "department", Arrays.asList("DEV", "QA") });
	}

	@Test
	public void should_ParseExpression_when_HavingMultipleFields() {
		havingRule("primary == true && (department == \"DEV\" || department == \"BA\")");

		expectFields(new Object[][] {{ "primary", true }, { "department", Arrays.asList("DEV", "BA") }});
	}

	@Test
	public void should_Be_Equal_When_Same_Key_Values_Different_Order() {
		String first = "primary == true && (department == \"DEV\" || department == \"BA\" || department == \"ENG\")";
		String second = "(department == \"ENG\" || department == \"BA\" || department == \"DEV\") && primary == true";

		assertTrue(TemplateRuleUtils.equals(first, second));
	}

	@Test
	public void should_Not_Be_Equal_When_Different_Values() {
		String first = "primary == true && (department == \"HR\" || department == \"BA\" || department == \"ENG\")";
		String second = "(department == \"ENG\" || department == \"BA\" || department == \"DEV\") && primary == true";

		assertFalse(TemplateRuleUtils.equals(first, second));
	}

	@Test
	public void should_Not_Be_Equal_When_Different_Fields() {
		String first = "someBoolean == true && (department == \"DEV\" || department == \"BA\" || department == \"ENG\")";
		String second = "(department == \"ENG\" || department == \"BA\" || department == \"DEV\") && primary == true";

		assertFalse(TemplateRuleUtils.equals(first, second));
	}

	@Test
	public void should_Not_Be_Equal_When_A_Value_Is_Missing_In_Multivalue() {
		String first = "primary == true && (department == \"DEV\" || department == \"BA\" || department == \"ENG\")";
		String second = "(department == \"ENG\" || department == \"DEV\") && primary == true";

		assertFalse(TemplateRuleUtils.equals(first, second));
	}

	@Test
	public void should_Not_Be_Equal_When_A_Conjunction_Operator_Is_Different() {
		String first = "primary == true && (department == \"DEV\" || department == \"BA\" || department == \"ENG\")";
		String second = "(department == \"ENG\" || department == \"BA\" || department == \"DEV\") || primary == true";

		assertFalse(TemplateRuleUtils.equals(first, second));
	}

	private List<Template> templates = new ArrayList<>();

	private void expectTemplates(String... ids) {
		List<Template> result = TemplateRuleUtils.filter(templates, criteria);

		Assert.assertEquals("Result counts is not the same as the expected count", ids.length, result.size());

		for (int i = 0; i < ids.length; i++) {
			assertEquals(ids[i], result.get(i).getId());
		}
	}

	private void havingTemplate(String id, String rule) {
		Template template = new Template();

		template.setId(id);
		template.setRule(rule);
		templates.add(template);
	}

	private Map<String, Serializable> criteria = new HashMap<>();

	private void forCriteria(String name, Serializable value) {
		criteria.put(name, value);
	}

	private String rule;

	private void havingRule(String rule) {
		this.rule = rule;
	}

	private void expectFields(Object[]... fields) {
		Map<String, Serializable> statements = TemplateRuleUtils.parseRule(rule);

		assertEquals("The parser returned different property count", fields.length, statements.size());

		for (Object[] field : fields) {
			String key = (String) field[0];
			Serializable value = (Serializable) field[1];

			assertTrue("The parsed statements don't contain '" + key + "' field", statements.containsKey(key));

			assertEquals(value, statements.get(key));
		}
	}
}
