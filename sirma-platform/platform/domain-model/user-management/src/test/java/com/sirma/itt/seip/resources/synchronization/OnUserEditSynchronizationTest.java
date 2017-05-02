package com.sirma.itt.seip.resources.synchronization;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.LANGUAGE;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.event.InstancePersistedEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Test for {@link OnUserEditSynchronization}
 *
 * @author BBonev
 */
public class OnUserEditSynchronizationTest {

	private static final String EDIT_DETAILS = "editDetails";

	@InjectMocks
	private OnUserEditSynchronization synchronization;

	@Spy
	private ConfigurationProperty<Set<String>> propertiesToSync = new ConfigurationPropertyMock<>(
			Collections.singleton(LANGUAGE));
	@Mock
	private ResourceService resourceService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onInvalidInstance() throws Exception {
		synchronization.onUserChange(new InstancePersistedEvent<>(new EmfInstance(), null, EDIT_DETAILS));

		verify(resourceService, never()).findResource(any());
		verify(resourceService, never()).save(any(), any());
	}

	@Test
	public void onModifiedUser() throws Exception {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:user");
		instance.add(LANGUAGE, "en");
		ClassInstance type = new ClassInstance();
		type.setCategory("user");
		instance.setType(type);

		EmfUser user = new EmfUser();
		user.setId("emf:user");
		when(resourceService.findResource(any())).thenReturn(user);

		synchronization.onUserChange(new InstancePersistedEvent<>(instance, null, EDIT_DETAILS));

		verify(resourceService).save(user, new Operation(EDIT_DETAILS));
	}

	@Test
	public void onModifiedUser_noChanges() throws Exception {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:user");
		ClassInstance type = new ClassInstance();
		type.setCategory("user");
		instance.setType(type);

		EmfUser user = new EmfUser();
		user.setId("emf:user");
		when(resourceService.findResource(any())).thenReturn(user);

		synchronization.onUserChange(new InstancePersistedEvent<>(instance, null, EDIT_DETAILS));

		verify(resourceService, never()).save(user, new Operation(EDIT_DETAILS));
	}

	@Test
	public void onModifiedResource() throws Exception {
		EmfUser instance = new EmfUser();
		instance.setId("emf:user");
		ClassInstance type = new ClassInstance();
		type.setCategory("user");
		instance.setType(type);

		synchronization.onUserChange(new InstancePersistedEvent<>(instance, null, EDIT_DETAILS));

		verify(resourceService, never()).findResource(any());
		verify(resourceService, never()).save(any(), any());
	}

	@Test
	public void onModifiedNonResource() throws Exception {
		EmfUser instance = new EmfUser();
		instance.setId("emf:user");
		ClassInstance type = new ClassInstance();
		type.setCategory("case");
		instance.setType(type);

		synchronization.onUserChange(new InstancePersistedEvent<>(instance, null, EDIT_DETAILS));

		verify(resourceService, never()).findResource(any());
		verify(resourceService, never()).save(any(), any());
	}
}
