package com.sirma.sep.emf.hash;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.LinkedList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.emf.hash.EmfHashCalculatorExtension;
import com.sirma.itt.emf.hash.HashCalculatorImpl;
import com.sirma.itt.seip.definition.util.hash.HashCalculatorExtension;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.resources.ResourceHashCalculatorExtension;
import com.sirma.sep.definition.hash.GenericDefinitionHashCalculator;

/**
 * The Class HashCalculatorImplTest.
 *
 * @author BBonev
 */
public class HashCalculatorImplTest extends BaseScriptDefinitionTest {

	@Spy
	private Iterable<HashCalculatorExtension> extensions = new LinkedList<>();

	@InjectMocks
	private HashCalculatorImpl calculator;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
		List<HashCalculatorExtension> list = (List<HashCalculatorExtension>) extensions;
		list.clear();
		list.add(new EmfHashCalculatorExtension());
		list.add(new GenericDefinitionHashCalculator());
		list.add(new ResourceHashCalculatorExtension());

		calculator.initializeMapping();
	}

	/**
	 * Tests if the calculator compares (calculate hash code) correctly of 2 definitions
	 *
	 * @param base
	 *            the base definition path
	 * @param compareTo
	 *            the compare to definition path
	 * @param expected
	 *            the expected
	 */
	@Test(dataProvider = "provideDefinitionsToCompare")
	public void testEqualsByHash(String base, String compareTo, boolean expected) {
		List<DefinitionModel> definitions = loadDefinitions(getClass(), base, compareTo);
		assertNotNull(definitions);
		assertEquals(definitions.size(), 2);
		assertEquals(calculator.equalsByHash(definitions.get(0), definitions.get(1)), expected);
	}

	/**
	 * Provide definitions to compare.
	 *
	 * @return the object[][]
	 */
	@DataProvider(name = "provideDefinitionsToCompare")
	public Object[][] provideDefinitionsToCompare() {
		return new Object[][] { { "generic_field_base.xml", "generic_field_base.xml", true },
				{ "generic_field_base.xml", "generic_field_change_codelist.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_field_codelist.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_field_displayType.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_field_name.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_field_order.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_field_type.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_field_uri.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_field_value.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_id.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_new_param.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_param_name.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_param_value.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_paramId.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_ui_param_id.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_ui_param_name.xml", false },
				{ "generic_field_base.xml", "generic_field_change_control_ui_param_value.xml", false },
				{ "generic_field_base.xml", "generic_field_change_displayType.xml", false },
				{ "generic_field_base.xml", "generic_field_change_filters.xml", false },
				{ "generic_field_base.xml", "generic_field_change_label.xml", false },
				{ "generic_field_base.xml", "generic_field_change_mandatory.xml", false },
				{ "generic_field_base.xml", "generic_field_change_multiValue.xml", false },
				{ "generic_field_base.xml", "generic_field_change_name.xml", false },
				{ "generic_field_base.xml", "generic_field_change_order.xml", false },
				{ "generic_field_base.xml", "generic_field_change_previewEmpty.xml", false },
				{ "generic_field_base.xml", "generic_field_change_rnc.xml", false },
				{ "generic_field_base.xml", "generic_field_change_tooltip.xml", false },
				{ "generic_field_base.xml", "generic_field_change_type.xml", false },
				{ "generic_field_base.xml", "generic_field_change_uri.xml", false },
				{ "generic_field_base.xml", "generic_field_change_value.xml", false },
				{ "generic_field_base.xml", "generic_field_change_dmsType.xml", false },
				{ "generic_region_base.xml", "generic_region_base.xml", true },
				{ "generic_region_base.xml", "generic_region_change_condition_id.xml", false },
				{ "generic_region_base.xml", "generic_region_change_condition_renderAs.xml", false },
				{ "generic_region_base.xml", "generic_region_change_condition_value.xml", false },
				{ "generic_region_base.xml", "generic_region_change_control_field_value.xml", false },
				{ "generic_region_base.xml", "generic_region_change_control_id.xml", false },
				{ "generic_region_base.xml", "generic_region_change_control_param_id.xml", false },
				{ "generic_region_base.xml", "generic_region_change_control_param_name.xml", false },
				{ "generic_region_base.xml", "generic_region_change_control_param_value.xml", false },
				{ "generic_region_base.xml", "generic_region_change_control_ui_param_id.xml", false },
				{ "generic_region_base.xml", "generic_region_change_control_ui_param_name.xml", false },
				{ "generic_region_base.xml", "generic_region_change_control_ui_param_value.xml", false },
				{ "generic_region_base.xml", "generic_region_change_displayType.xml", false },
				{ "generic_region_base.xml", "generic_region_change_field_codelist.xml", false },
				{ "generic_region_base.xml", "generic_region_change_field_displayType.xml", false },
				{ "generic_region_base.xml", "generic_region_change_field_dmsType.xml", false },
				{ "generic_region_base.xml", "generic_region_change_field_name.xml", false },
				{ "generic_region_base.xml", "generic_region_change_field_order.xml", false },
				{ "generic_region_base.xml", "generic_region_change_field_type.xml", false },
				{ "generic_region_base.xml", "generic_region_change_field_uri.xml", false },
				{ "generic_region_base.xml", "generic_region_change_field_value.xml", false },
				{ "generic_region_base.xml", "generic_region_change_id.xml", false },
				{ "generic_region_base.xml", "generic_region_change_label.xml", false },
				{ "generic_region_base.xml", "generic_region_change_order.xml", false },
				{ "generic_region_base.xml", "generic_region_change_tooltip.xml", false },
				{ "generic_stateTransition_base.xml", "generic_stateTransition_base.xml", true },
				{ "generic_stateTransition_base.xml", "generic_stateTransition_change_condition_id.xml", false },
				{ "generic_stateTransition_base.xml", "generic_stateTransition_change_condition_new.xml", false },
				{ "generic_stateTransition_base.xml", "generic_stateTransition_change_condition_remove.xml", false },
				{ "generic_stateTransition_base.xml", "generic_stateTransition_change_condition_renderAs.xml", false },
				{ "generic_stateTransition_base.xml", "generic_stateTransition_change_condition_value.xml", false },
				{ "generic_stateTransition_base.xml", "generic_stateTransition_change_from.xml", false },
				{ "generic_stateTransition_base.xml", "generic_stateTransition_change_to.xml", false },
				{ "generic_stateTransition_base.xml", "generic_stateTransition_change_transition.xml", false },
				{ "generic_stateTransition_base.xml", "generic_stateTransition_new_transition.xml", false },
				{ "generic_stateTransition_base.xml", "generic_stateTransition_remove_transition.xml", false },
				{ "generic_allowed_base.xml", "generic_allowed_base.xml", true },
				{ "generic_allowed_base.xml", "generic_allowed_change_control_name.xml", false },
				{ "generic_allowed_base.xml", "generic_allowed_change_control_value.xml", false },
				{ "generic_allowed_base.xml", "generic_allowed_change_filter_property.xml", false },
				{ "generic_allowed_base.xml", "generic_allowed_change_filter_value.xml", false },
				{ "generic_allowed_base.xml", "generic_allowed_change_fulter_mode.xml", false },
				{ "generic_allowed_base.xml", "generic_allowed_change_id.xml", false },
				{ "generic_allowed_base.xml", "generic_allowed_change_new_child.xml", false },
				{ "generic_allowed_base.xml", "generic_allowed_change_new_filter.xml", false },
				{ "generic_allowed_base.xml", "generic_allowed_change_remove_child.xml", false },
				{ "generic_allowed_base.xml", "generic_allowed_change_remove_config.xml", false },
				{ "generic_allowed_base.xml", "generic_allowed_change_type.xml", false },
				{ "generic_transition_base.xml", "generic_transition_base.xml", true },
				{ "generic_transition_base.xml", "generic_transition_change_condition_id.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_condition_renderAs.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_condition_value.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_confirm.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_defautTransition.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_disableReason.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_displayType.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_eventId.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_field_control_id.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_field_control_param_id.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_field_control_param_name.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_field_control_param_value.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_field_name.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_field_value.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_id.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_immediate.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_label.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_nextPrimary.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_nextSecondary.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_no_purpose.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_order.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_purpose.xml", false },
				{ "generic_transition_base.xml", "generic_transition_change_tooltip.xml", false } };
	}

}
