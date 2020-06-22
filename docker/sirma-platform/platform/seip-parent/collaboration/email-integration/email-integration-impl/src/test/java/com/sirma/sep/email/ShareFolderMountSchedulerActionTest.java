package com.sirma.sep.email;

import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.sep.email.service.ShareFolderAdministrationService;

public class ShareFolderMountSchedulerActionTest {

	private static String TEST_MAIL = "test-mail@domain.com";

	@InjectMocks
	private ShareFolderMountSchedulerAction action;

	@Mock
	private ShareFolderAdministrationService shareFolderAdministrationService;

	@Before
	public void setUp() throws Exception {
		action = mock(ShareFolderMountSchedulerAction.class);
		MockitoAnnotations.initMocks(this);
		doCallRealMethod().when(action).execute(any(SchedulerContext.class));
	}

	@Test
	public void shouldAddUserToSharedFolder() throws Exception {
		when(shareFolderAdministrationService.isShareFolderMounted(TEST_MAIL)).thenReturn(false);
		SchedulerContext context = new SchedulerContext();
		context.put(EMAIL_ADDRESS, TEST_MAIL);
		action.execute(context);
		verify(shareFolderAdministrationService, times(1)).mountShareFolderToUser(TEST_MAIL);
	}

	@Test
	public void shouldNotAddUserIfAlreadyInShareFolder() throws Exception {
		when(shareFolderAdministrationService.isShareFolderMounted(anyString())).thenReturn(true);
		action.execute(new SchedulerContext());
		verify(shareFolderAdministrationService, times(0)).mountShareFolderToUser(TEST_MAIL);
	}
}
