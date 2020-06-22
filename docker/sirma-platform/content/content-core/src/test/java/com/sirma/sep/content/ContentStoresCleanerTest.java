package com.sirma.sep.content;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * Test for {@link ContentStoresCleaner}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentStoresCleanerTest {

	@InjectMocks
	private ContentStoresCleaner cleaner;

	@Mock
	private ContentStore contentStore1;

	@Mock
	private ContentStore contentStore2;

	@Spy
	private InstanceProxyMock<ContentStore> stores = new InstanceProxyMock<>();

	@Mock
	private ContentStoreManagementService contentStoreManagementService;

	@Test
	public void clearStoresOnExecution() {
		when(contentStore1.isCleanSupportedOnTenantDelete()).thenReturn(true);
		when(contentStore2.isCleanSupportedOnTenantDelete()).thenReturn(false);

		stores.add(contentStore1);
		stores.add(contentStore2);

		cleaner.cleanStoresOnTenantDelete();

		verify(contentStoreManagementService).emptyContentStore(any());
		verify(contentStore1).getName();
		verify(contentStore2).isCleanSupportedOnTenantDelete();
	}
}