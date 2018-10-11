package com.sirma.itt.seip.eai.service.script;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;

/**
 * Test {@link EAIScriptService}
 *
 * @author gshevkedov
 */
public class EAIScriptServiceTest {

	@Test
	public void testGeBindings() {
		EAIScriptService scriptProvider = new EAIScriptService();
		assertNotNull(scriptProvider.getBindings());
		assertNotNull(scriptProvider.getBindings().keySet().contains("eaiScriptService"));
	}

	@Test
	public void testGetScripts() {
		EAIScriptService scriptProvider = new EAIScriptService();
		assertNotNull(scriptProvider.getScripts());
	}

	@Test
	public void testSearchAndImport() {
		EAIScriptService scriptProvider = new EAIScriptService();
		EAIScriptService spy = Mockito.spy(scriptProvider);
		SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);
		Mockito.doCallRealMethod().when(spy).searchAndImport(searchArgs);
		int pageSize = 25;
		Mockito.when(searchArgs.getPageSize()).thenReturn(pageSize);
		int totalItems = 65;
		Mockito.when(searchArgs.getTotalItems()).thenReturn(totalItems);
		List<Instance> searchResult = new ArrayList<Instance>();
		Mockito.when(searchArgs.getResult()).thenReturn(searchResult);

		spy.searchAndImport(searchArgs);
		Mockito.verify(spy, Mockito.times(3)).search(searchArgs);
	}
}
