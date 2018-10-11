/**
 *
 */
package com.sirma.itt.emf.label.retrieve;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * @author BBonev
 *
 */
public class UsernameByURIFieldValueRetrieverTest extends EmfTest {

	@InjectMocks
	UsernameByURIFieldValueRetriever retriever;
	@Mock
	private ResourceService resourceService;
	protected TypeConverter typeConverter;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		typeConverter = createTypeConverter();
		super.beforeMethod();
	}

	@Test
	public void test_getValues_all_no_Filtering() {
		when(resourceService.getAllResources(ResourceType.USER, null)).thenReturn(buildUsers(30));
		RetrieveResponse response = retriever.getValues(null, new SearchRequest(), null, null);
		assertNotNull(response);
		assertNotNull(response.getResults());
		assertNotNull(response.getTotal());

		assertEquals(response.getResults().size(), 30);
		assertEquals(response.getTotal(), Long.valueOf(30L));
	}

	@Test
	public void test_getValues_all_offset() {
		when(resourceService.getAllResources(ResourceType.USER, null)).thenReturn(buildUsers(30));
		RetrieveResponse response = retriever.getValues(null, new SearchRequest(), 10, null);
		assertNotNull(response);
		assertNotNull(response.getResults());
		assertNotNull(response.getTotal());

		assertEquals(response.getResults().size(), 20);
		assertEquals(response.getTotal(), Long.valueOf(30L));

		assertEquals(response.getResults().get(0).getFirst(), "emf:user11");
	}

	@Test
	public void test_getValues_all_limit() {
		when(resourceService.getAllResources(ResourceType.USER, null)).thenReturn(buildUsers(30));
		RetrieveResponse response = retriever.getValues(null, new SearchRequest(), null, 20);
		assertNotNull(response);
		assertNotNull(response.getResults());
		assertNotNull(response.getTotal());

		assertEquals(response.getResults().size(), 20);
		assertEquals(response.getTotal(), Long.valueOf(30L));

		assertEquals(response.getResults().get(0).getFirst(), "emf:user1");
	}

	@Test
	public void test_getValues_all_offset_limit() {
		when(resourceService.getAllResources(ResourceType.USER, null)).thenReturn(buildUsers(30));
		RetrieveResponse response = retriever.getValues(null, new SearchRequest(), 5, 20);
		assertNotNull(response);
		assertNotNull(response.getResults());
		assertNotNull(response.getTotal());

		assertEquals(response.getResults().size(), 20);
		assertEquals(response.getTotal(), Long.valueOf(30L));

		assertEquals(response.getResults().get(0).getFirst(), "emf:user6");
		assertEquals(response.getResults().get(response.getResults().size() - 1).getFirst(), "emf:user25");
	}

	@Test
	public void test_getValues_filter_offset_limit() {
		when(resourceService.getAllResources(ResourceType.USER, null)).thenReturn(buildUsers(30));
		RetrieveResponse response = retriever.getValues("user 1", new SearchRequest(), 1, 5);
		assertNotNull(response);
		assertNotNull(response.getResults());
		assertNotNull(response.getTotal());

		assertEquals(response.getResults().size(), 5);
		assertEquals(response.getTotal(), Long.valueOf(11L));

		assertEquals(response.getResults().get(0).getFirst(), "emf:user10");
		assertEquals(response.getResults().get(response.getResults().size() - 1).getFirst(), "emf:user14");
	}

	static List<Resource> buildUsers(int number) {
		List<Resource> list = new ArrayList<>(number	);
		for (int i = 0; i < number; i++) {
			list.add(createUser("emf:user" + (i + 1), "User " + (i + 1)));
		}
		return list;
	}

	static Resource createUser(String id, String displayName) {
		EmfUser user = new EmfUser();
		user.setId(id);
		user.setDisplayName(displayName);
		return user;
	}
}
