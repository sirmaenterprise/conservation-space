package com.sirma.cmf.web.userdashboard.panel;

import static org.mockito.Mockito.mock;

import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Test method for {@link ProjectCasePanel}
 * 
 * @author cdimitrov
 */
@Test
public class MyCasesPanelTest extends CMFTest {

	/** The case panel reference. */
	private final MyCasesPanel myCasesPanel;

	/** The label provider that will be used for retrieving filter lables. */
	private final LabelProvider labelProvider;

	/** The default supported case filters. */
	private final int supportedCaseFilters = 4;

	/** The default supported date filters. */
	private final int supportedDateFilters = 4;

	/** The case instance that will be used in the tests. */
	private final CaseInstance caseInstance;

	/**
	 * Default constructor for the test class.
	 */
	public MyCasesPanelTest() {

		myCasesPanel = new MyCasesPanel() {

			private static final long serialVersionUID = 1L;

		};

		// mock the label builder
		labelProvider = mock(LabelProvider.class);

		ReflectionUtils.setField(myCasesPanel, "labelProvider", labelProvider);

		caseInstance = createCaseInstance((Long.valueOf(1)));

	}

}
