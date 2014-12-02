package com.sirma.itt.objects.web.caseinstance.dashboard;

import static org.mockito.Mockito.mock;

import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.objects.ObjectsTest;

/**
 * Test class for {@link CaseWorkflowPanel}.
 * 
 * @author cdimitrov
 */
@Test
public class CaseWorkflowPanelTest extends ObjectsTest {

	/** The size of supported filters. */
	public int supportedWorkflowFilters = 4;

	/** Reference to the panel that will be tested. */
	private final CaseWorkflowPanel caseWorkflowPanel;

	/** The label provider, for retrieving panel filter labels. */
	private final LabelProvider labelProvider;

	/**
	 * Default test constructor.
	 */
	public CaseWorkflowPanelTest() {

		caseWorkflowPanel = new CaseWorkflowPanel() {

			private static final long serialVersionUID = 1L;

		};
		// mock the label builder
		labelProvider = mock(LabelProvider.class);
		ReflectionUtils.setField(caseWorkflowPanel, "labelProvider", labelProvider);
	}

}
