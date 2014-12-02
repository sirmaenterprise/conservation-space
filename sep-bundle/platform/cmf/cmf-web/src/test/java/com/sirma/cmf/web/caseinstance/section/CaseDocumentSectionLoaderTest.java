package com.sirma.cmf.web.caseinstance.section;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.mock.service.SearchServiceMock;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Test class for {@see CaseDocumentSectionLoader}
 * 
 * @author cdimitrov
 */
@Test
public class CaseDocumentSectionLoaderTest extends CMFTest {

	/** Case document section loader. */
	private CaseDocumentSectionLoader documentSectionLoader;

	/** The label provider. */
	private LabelProvider labelProvider;

	/** The counter label. */
	private static final String DOCUMENT_COUNTER = "documents";

	/**
	 * Initialize test data.
	 */
	public CaseDocumentSectionLoaderTest() {
		documentSectionLoader = new CaseDocumentSectionLoader();

		SectionInstance firstSection = createSectionInstance(Long.valueOf(1L));
		SectionInstance secondSection = createSectionInstance(Long.valueOf(2L));

		List<SectionInstance> testSections = new ArrayList<SectionInstance>();

		testSections.add(firstSection);
		testSections.add(secondSection);

		SectionDocumentsAction sectionActions = Mockito.mock(SectionDocumentsAction.class);
		Mockito.when(sectionActions.getCaseDocumentSections()).thenReturn(testSections);

		ReflectionUtils.setField(documentSectionLoader, "section", sectionActions);

		labelProvider = mock(LabelProvider.class);
		when(labelProvider.getValue(documentSectionLoader.getCounterBundle())).thenReturn(
				DOCUMENT_COUNTER);
		ReflectionUtils.setField(documentSectionLoader, "labelProvider", labelProvider);

		SearchServiceMock searchService = new SearchServiceMock();
		ReflectionUtils.setField(documentSectionLoader, "searchService", searchService);
	}

	/**
	 * Initialize document section loader data.
	 */
	@Before
	public void initializeTestData() {
		documentSectionLoader.initData();
	}

	/**
	 * Test method for supported sections.
	 */
	public void supportedSectionsTest() {
		Assert.assertEquals(documentSectionLoader.getSectionList().size(), 2);
	}

	/**
	 * Test method for unsupported section content.
	 */
	public void sectionContentTest() {
		List<SectionInstance> sections = documentSectionLoader.getSectionList();
		Assert.assertNotNull(sections);
		List<Instance> sectionContent = sections.get(0).getContent();
		Assert.assertEquals(sectionContent, new ArrayList<>());
	}

}
