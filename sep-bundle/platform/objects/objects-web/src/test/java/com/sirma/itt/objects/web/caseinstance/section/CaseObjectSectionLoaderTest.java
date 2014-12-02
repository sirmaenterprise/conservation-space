package com.sirma.itt.objects.web.caseinstance.section;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchInstance;
import com.sirma.itt.objects.ObjectsTest;

/**
 * Test class for {@see CaseDocumentSectionLoader}
 * 
 * @author cdimitrov
 */
@Test
public class CaseObjectSectionLoaderTest extends ObjectsTest {

	/** Case document section loader. */
	private CaseObjectSectionLoader objectSectionLoader;

	/** The label provider. */
	private LabelProvider labelProvider;

	/** The counter label. */
	private static final String OBJECT_COUNTER = "objects";

	/**
	 * Initialize test data.
	 */
	public CaseObjectSectionLoaderTest() {
		objectSectionLoader = new CaseObjectSectionLoader();

		SectionInstance firstSection = createSectionInstance(Long.valueOf(1L));
		SectionInstance secondSection = createSectionInstance(Long.valueOf(2L));

		List<SectionInstance> testSections = new ArrayList<SectionInstance>();

		testSections.add(firstSection);
		testSections.add(secondSection);

		SectionObjectsAction sectionActions = Mockito.mock(SectionObjectsAction.class);
		Mockito.when(sectionActions.getCaseObjectSections()).thenReturn(testSections);

		ReflectionUtils.setField(objectSectionLoader, "sectionObjectAction", sectionActions);

		labelProvider = mock(LabelProvider.class);
		when(labelProvider.getValue(objectSectionLoader.getCounterBundle())).thenReturn(
				OBJECT_COUNTER);
		ReflectionUtils.setField(objectSectionLoader, "labelProvider", labelProvider);

		SearchService searchService = Mockito.mock(SearchService.class);
		when(
				searchService.getFilterConfiguration(Mockito.anyString(),
						Mockito.eq(SearchInstance.class))).thenReturn(null);
		ReflectionUtils.setField(objectSectionLoader, "searchService", searchService);
	}

	/**
	 * Initialize object section loader data.
	 */
	@Before
	public void initializeTestData() {
		objectSectionLoader.initData();
	}

	/**
	 * Test method for supported sections.
	 */
	public void supportedSectionsTest() {
		Assert.assertEquals(objectSectionLoader.getSectionList().size(), 2);
	}

	/**
	 * Test method for unsupported section content.
	 */
	public void sectionContentTest() {
		List<SectionInstance> sections = objectSectionLoader.getSectionList();
		Assert.assertNotNull(sections);
		List<Instance> sectionContent = sections.get(0).getContent();
		Assert.assertEquals(sectionContent, new ArrayList<>());
	}

}
