package com.sirma.itt.seip.adapters.iiif;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.content.ContentStoreManagementService;

/**
 * Test for {@link IiifImageContentStoreCleanup}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 03/01/2018
 */
public class IiifImageContentStoreCleanupTest {

	@InjectMocks
	private IiifImageContentStoreCleanup storeCleanup;

	@Mock
	private ContentStoreManagementService contentManagementService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void cleanImageContentStoreOnTenantDelete() throws Exception {
		storeCleanup.cleanImageContentStoreOnTenantDelete();

		verify(contentManagementService).emptyContentStore(IiifImageContentStore.STORE_NAME);
	}

}
