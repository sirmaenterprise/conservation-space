package com.sirma.cmf.web.caseinstance.tab;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchFilterConfig;
import com.sirma.itt.emf.search.model.SearchInstance;

/**
 * Test class for {@link CaseSectionsActionBase}
 * 
 * @author cdimitrov
 */
@Test
public class CaseSectionsActionBaseTest extends CMFTest {

	/** The section action base reference. */
	private CaseSectionsActionBase sectionActionBase;

	/** The label provider. */
	private LabelProvider labelProvider;

	/** The section identifier constant. */
	private static final String CASE_SECTION_IDENTIFIER = "section_identifier";

	/** The section counter bundle constant. */
	private static final String CASE_SECTION_COUNTER_BUNDLE = "section.counter.bundle";

	/** The section counter label constant. */
	private static final String CASE_SECTION_COUNTER_LABEL = "label";

	/**
	 * Constructor for initialize test componentns.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CaseSectionsActionBaseTest() {
		sectionActionBase = new CaseSectionsActionBase() {

			@Override
			public String getSectionIdentifier() {
				return CASE_SECTION_IDENTIFIER;
			}

			@Override
			public String getCounterBundle() {
				return CASE_SECTION_COUNTER_BUNDLE;
			}
		};

		SearchService searchService = Mockito.mock(SearchService.class);

		when(
				searchService.getFilterConfiguration(Mockito.anyString(),
						Mockito.eq(SearchInstance.class))).thenReturn(
				new SearchFilterConfig(new ArrayList(), new ArrayList()));

		ReflectionUtils.setField(sectionActionBase, "searchService", searchService);

		labelProvider = mock(LabelProvider.class);

		when(labelProvider.getValue(sectionActionBase.getCounterBundle())).thenReturn(
				CASE_SECTION_COUNTER_LABEL);

		ReflectionUtils.setField(sectionActionBase, "labelProvider", labelProvider);

		sectionActionBase.onOpen();
	}

	/**
	 * Method for testing initial section components.
	 */
	public void baseInitialDataTest() {
		Assert.assertNotNull(sectionActionBase.getSectionHolder());
		Assert.assertEquals(CASE_SECTION_COUNTER_BUNDLE, sectionActionBase.getCounterBundle());
		Assert.assertEquals(CASE_SECTION_COUNTER_LABEL, sectionActionBase.getCounterLabel());
		Assert.assertEquals(CASE_SECTION_IDENTIFIER, sectionActionBase.getSectionIdentifier());
	}

	/**
	 * Method for testing section content.
	 */
	public void getSectionContentByIdTest() {
		SectionInstance section = createSectionInstance(1L);
		String sectionIdentifier = (String) section.getIdentifier();
		SectionContentLoader sectionContentLoader = sectionActionBase.getSectionContentById(sectionIdentifier);
		Assert.assertNotNull(sectionContentLoader);
		Assert.assertEquals(sectionIdentifier, sectionContentLoader.getSectionIdentifier());
	}

}
