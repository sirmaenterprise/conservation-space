package com.sirma.itt.seip.domain.definition;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests Condition methods.
 * 
 * @author Boyan Tonchev
 *
 */
@SuppressWarnings("static-method")
public class ConditionTest {

	/**
	 * Tests method byType.
	 *
	 * @param conditionType type to be tested.
	 * @param expectedResult expected result.
	 * @param errorMessage error message to be shown if test fail.
	 */
	@Test(dataProvider = "byTypeDP")
	public void byTypeTest(String conditionType, boolean expectedResult, String errorMessage) {
		Condition condition = Mockito.mock(Condition.class);
		Mockito.when(condition.getRenderAs()).thenReturn("render as");
		Assert.assertEquals(Condition.byType(conditionType).test(condition), expectedResult, errorMessage);
	}
	
	@DataProvider
	public Object[][] byTypeDP() {
		return new Object[][] {
			{null, Boolean.FALSE, "Null condition type scenario."},
			{"not match", Boolean.FALSE, "Not match scenario."},
			{"render as", Boolean.TRUE, "Same case scenario."},
			{"RENDER as", Boolean.TRUE, "Tests case-sensitive scenario"}			
		};
	}
}
