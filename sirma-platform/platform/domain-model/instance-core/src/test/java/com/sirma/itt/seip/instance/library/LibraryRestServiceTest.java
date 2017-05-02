package com.sirma.itt.seip.instance.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.ObjectInstance;

/**
 * Test for {@link LibraryRestService}
 *
 * @author BBonev
 */
public class LibraryRestServiceTest {
	@InjectMocks
	private LibraryRestService libraryRestService;

	@Mock
	private LibraryProvider libraryProvider;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		ObjectInstance instance1 = new ObjectInstance();
		instance1.setId("emf:Case");
		instance1.add("searchable", Boolean.TRUE);
		instance1.preventModifications();
		ObjectInstance instance2 = new ObjectInstance();
		instance2.setId("emf:Audio");
		instance2.add("searchable", Boolean.TRUE);
		ObjectInstance instance3 = new ObjectInstance();
		instance3.setId("emf:Document");
		instance3.add("searchable", Boolean.TRUE);

		when(libraryProvider.getLibraries(ActionTypeConstants.VIEW_DETAILS))
				.thenReturn(Arrays.asList(instance1, instance2, instance3));
	}

	@Test
	public void testGetLibraryResult() throws Exception {

		SearchArguments<Instance> arguments = libraryRestService.getLibraries();
		assertNotNull(arguments);
		assertEquals(3, arguments.getTotalItems());

		List<Instance> list = arguments.getResult();
		assertNotNull(list);
		assertEquals(3, list.size());
	}
}
