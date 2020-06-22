package com.sirma.itt.seip.resources.patches;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

import liquibase.database.Database;

/**
 * Tests for {@link ChangeAdminGroup}.
 *
 * @author smustafov
 */
public class ChangeAdminGroupTest {

	@Mock
	private ResourceService resourceService;
	@Mock
	private SenderService senderService;
	@Mock
	private TransactionSupport transactionSupport;
	@Mock
	private SecurityConfiguration securityConfiguration;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private ConfigurationManagement configurationManagement;

	@Mock
	private Database database;

	@InjectMocks
	private ChangeAdminGroup changeAdmingroup;

	@Before
	public void init() {
		initMocks(this);

		doAnswer(invocation -> {
			Executable executable = invocation.getArgumentAt(0, Executable.class);
			executable.execute();
			return null;
		}).when(transactionSupport).invokeInNewTx(any(Executable.class));

		EmfGroup adminGroup = new EmfGroup(ResourceService.SYSTEM_ADMIN_GROUP_ID,
				ResourceService.SYSTEM_ADMIN_GROUP_ID);
		adminGroup.setId(EMF.PREFIX + ":" + ResourceService.SYSTEM_ADMIN_GROUP_ID);
		when(resourceService.getSystemAdminGroup()).thenReturn(adminGroup);

		EmfUser adminUser = new EmfUser("admin");
		adminUser.setId("emf:admin");
		ConfigurationProperty<com.sirma.itt.seip.security.User> adminUserConfig = new ConfigurationPropertyMock<>(
				adminUser);
		when(securityConfiguration.getAdminUser()).thenReturn(adminUserConfig);
	}

	@Test
	public void shouldDoNothing_When_CurrentAdminGroupIsAlreadyTheDefaultOne() throws Exception {
		when(securityConfiguration.getAdminGroup())
				.thenReturn(new ConfigurationPropertyMock<>(ResourceService.SYSTEM_ADMIN_GROUP_ID));
		when(resourceService.getContainedResources(any(Serializable.class)))
				.thenReturn(asList(buildUser("emf:admin1")));

		MessageSender messageSender = mock(MessageSender.class);
		when(senderService.createSender(anyString(), any(SendOptions.class))).thenReturn(messageSender);

		changeAdmingroup.execute(database);

		verify(messageSender, never()).send(any());
	}

	@Test
	public void shouldChangeAdminGroup_When_CurrentOneIsDifferent() throws Exception {
		when(securityConfiguration.getAdminGroup()).thenReturn(new ConfigurationPropertyMock<>("Alf_Admins"));
		when(resourceService.getContainedResources(any(Serializable.class)))
				.thenReturn(asList(buildUser("emf:admin"), buildUser("emf:admin1"), buildUser("emf:admin2")));

		MessageSender messageSender = mock(MessageSender.class);
		when(senderService.createSender(anyString(), any(SendOptions.class))).thenReturn(messageSender);

		changeAdmingroup.execute(database);

		ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);

		verify(messageSender).send(argCaptor.capture());
		JSON.readObject(argCaptor.getValue(), json -> {
			assertEquals(EMF.PREFIX + ":" + ResourceService.SYSTEM_ADMIN_GROUP_ID, json.getString("group"));
			assertEquals(2, json.getJsonArray("add").size());
			return null;
		});

		verify(configurationManagement).updateConfiguration(any(Configuration.class));
	}

	@Test
	public void shouldDeleteIdpAdminGroup() throws Exception {
		when(securityConfiguration.getAdminGroup()).thenReturn(new ConfigurationPropertyMock<>("Alf_Admins"));
		when(resourceService.findResource("emf:GROUP_admin")).thenReturn(new EmfGroup());

		MessageSender messageSender = mock(MessageSender.class);
		when(senderService.createSender(anyString(), any(SendOptions.class))).thenReturn(messageSender);

		changeAdmingroup.execute(database);

		verify(resourceService).delete(any(Instance.class), any(Operation.class), eq(true));
	}

	private static User buildUser(String id) {
		EmfUser user = new EmfUser();
		user.setId(id);
		return user;
	}

}