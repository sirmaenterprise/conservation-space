package com.sirma.itt.objects.services.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Tests for the contextual help logic.
 *
 * @author nvelkov
 */
public class HelpServiceTest {

	@Mock
	private SearchService searchService;

	@InjectMocks
	private HelpServiceImpl helpService;

	/**
	 * Init the annotations.
	 */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the help instance retrieval with one instance with 2 targets.
	 */
	@Test
	public void testGetHelpInstances() {
		Instance helpInstance = createInstance();
		mockSearchService(helpInstance);
		Map<String, String> helpMapping = helpService.getHelpIdToTargetMapping();
		Assert.assertEquals(2, helpMapping.size());
		Assert.assertEquals("id", helpMapping.get("case"));
	}

	/**
	 * Test the help instance retrieval with no help instances.
	 */
	@Test
	public void testGetHelpInstancesNoHelpInstances() {
		mockSearchService(null);
		Map<String, String> helpMapping = helpService.getHelpIdToTargetMapping();
		Assert.assertEquals(helpMapping.size(), 0);
	}

	/**
	 * Test that the search service is called with the correct parameters.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testAllHelpInstancesRetrieval() {
		Instance helpInstance = createInstance();
		mockSearchService(helpInstance);

		ArgumentCaptor<SearchArguments<Instance>> argumentCaptor = ArgumentCaptor.forClass(SearchArguments.class);
		helpService.getHelpIdToTargetMapping();

		Mockito.verify(searchService).searchAndLoad(Matchers.any(), argumentCaptor.capture());
		Assert.assertEquals(argumentCaptor.getValue().getArguments().get(DefaultProperties.SEMANTIC_TYPE),
				EMF.HELP.toString());
		Assert.assertEquals(argumentCaptor.getValue().getPageSize(), 0);
		Assert.assertEquals(argumentCaptor.getValue().getMaxSize(), -1);
	}

	private static Instance createInstance() {
		Instance helpInstance = new EmfInstance();
		helpInstance.add("helpTarget", (Serializable) Arrays.asList("case", "project"));
		helpInstance.setId("id");
		return helpInstance;
	}

	@SuppressWarnings("unchecked")
	private void mockSearchService(Instance helpInstance) {
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				SearchArguments<Instance> args = (SearchArguments<Instance>) invocation.getArguments()[1];
				if (helpInstance != null) {
					args.setResult(Arrays.asList(helpInstance));
				} else {
					args.setResult(new ArrayList<>());
				}
				return null;
			}
		}).when(searchService).searchAndLoad(Matchers.any(), Matchers.any(SearchArguments.class));
	}
}
