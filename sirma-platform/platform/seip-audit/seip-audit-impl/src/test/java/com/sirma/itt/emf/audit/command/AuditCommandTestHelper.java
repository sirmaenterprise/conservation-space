package com.sirma.itt.emf.audit.command;

import org.easymock.EasyMock;

import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.semantic.NamespaceRegistryService;

public class AuditCommandTestHelper {

	/**
	 * Mocks the {@link FieldValueRetrieverService} to return some dummy data and expects some correct data.
	 *
	 * @param service
	 *            - the service to mock
	 * @param expectedId
	 *            - the expected {@link FieldId} that is given to the service
	 * @param expectedValue
	 *            - the expected value that is given to the service
	 * @param label
	 *            - the dummy label
	 */
	public static void mockGetLabel(FieldValueRetrieverService service, String expectedId, String expectedValue,
			String label) {
		EasyMock
				.expect(service.getLabel(EasyMock.eq(expectedId), EasyMock.eq(expectedValue),
						EasyMock.anyObject(SearchRequest.class)))
					.andReturn(label)
					.anyTimes();
		EasyMock.replay(service);
	}

	/**
	 * Mocks the {@link NamespaceRegistryService} to return some dummy data.
	 *
	 * @param service
	 *            - the service to mock
	 * @param fullUri
	 *            - the full uri to be returned
	 */
	public static void mockBuildFullUri(NamespaceRegistryService service, String fullUri) {
		EasyMock.expect(service.buildFullUri(EasyMock.anyString())).andReturn(fullUri).anyTimes();
		EasyMock.replay(service);
	}
}
