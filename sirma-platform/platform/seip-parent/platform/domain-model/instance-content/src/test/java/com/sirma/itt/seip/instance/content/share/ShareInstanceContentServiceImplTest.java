package com.sirma.itt.seip.instance.content.share;

import com.beust.jcommander.internal.Sets;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.sep.content.ContentConfigurations;
import com.sirma.sep.export.ExportURIBuilder;
import org.apache.commons.lang.NotImplementedException;
import org.hibernate.cfg.Settings;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ShareInstanceContentServiceImpl}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 13/09/2017
 */
public class ShareInstanceContentServiceImplTest {

	@InjectMocks
	private ShareInstanceContentServiceImpl cut;

	@Mock
	private DomainInstanceService domainInstanceService;
	@Mock
	private DatabaseIdManager idManager;
	@Mock
	private SystemConfiguration systemConfiguration;
	@Mock
	private ContentConfigurations contentConfigurations;
	@Mock
	private ExportURIBuilder uriBuilder;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private SchedulerService schedulerService;
	@Mock
	private Instance instance;

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		// Mock an instance.
		when(instance.getString(any(String.class), any(String.class))).thenReturn("title");
		when(instance.getId()).thenReturn("emf:id");
		when(domainInstanceService.loadInstance("emf:id")).thenReturn(instance);

		when(idManager.generateId()).thenReturn("emf:new-id");
		// mock shareCode config
		ConfigurationProperty shareCodeKey = mock(ConfigurationProperty.class);
		when(shareCodeKey.getOrFail()).thenReturn("secretKey");
		when(contentConfigurations.getShareCodeSecretKey()).thenReturn(shareCodeKey);

		// mock security
		User mockUser = mock(User.class);
		when(mockUser.getIdentityId()).thenReturn("identityId");
		when(securityContext.getEffectiveAuthentication()).thenReturn(mockUser);

		// mock system access url config
		ConfigurationProperty systemUrlMock = mock(ConfigurationProperty.class);
		when(systemUrlMock.get()).thenReturn(new URI("http://ses.com"));
		when(systemConfiguration.getSystemAccessUrl()).thenReturn(systemUrlMock);
	}

	@Test
	public void test_getSharedContentURI_forUploaded() throws Exception {
		when(instance.isUploaded()).thenReturn(true);
		String shareUrl = cut.getSharedContentURI("emf:id", "pdf");
		verify(schedulerService, times(0)).buildConfiguration(any(ShareInstanceContentEvent.class));
		assertTrue(shareUrl.contains("http://ses.com/emf/share/content/emf:new-id?shareCode="));
		verify(schedulerService).schedule(Mockito.eq(ShareContentUploadedInstancesAction.ACTION_NAME), any(), any());
	}

	@Test
	public void test_getSharedContentURI_forCreated() throws Exception {
		when(instance.isUploaded()).thenReturn(false);
		String shareUrl = cut.getSharedContentURI("emf:id", "pdf");
		verify(schedulerService, times(0)).buildConfiguration(any(ShareInstanceContentEvent.class));
		assertTrue(shareUrl.contains("http://ses.com/emf/share/content/emf:new-id?shareCode="));
		verify(schedulerService).schedule(Mockito.eq(ShareContentCreatedInstanceAction.ACTION_NAME), any(), any());
	}

	@Test(expected = NotImplementedException.class)
	public void test_getSharedContentURIs() throws Exception {
		cut.getSharedContentURIs(Collections.singleton("emf:id"), "pdf");
	}

	@Test
	public void test_createContentShareTask_uploaded() throws Exception {
		when(schedulerService.buildConfiguration(any(ShareInstanceContentEvent.class)))
				.thenReturn(new DefaultSchedulerConfiguration());
		when(instance.isUploaded()).thenReturn(true);
		String shareUrl = cut.createContentShareTask("emf:id", "pdf");
		verify(schedulerService).buildConfiguration(any(ShareInstanceContentEvent.class));
		verify(schedulerService).schedule(Mockito.eq(ShareContentUploadedInstancesAction.ACTION_NAME), any(), any());
		assertTrue(shareUrl.contains("http://ses.com/emf/share/content/emf:new-id?shareCode="));
	}

	@Test
	public void test_createContentShareTask_created() throws Exception {
		when(schedulerService.buildConfiguration(any(ShareInstanceContentEvent.class)))
				.thenReturn(new DefaultSchedulerConfiguration());
		when(instance.isUploaded()).thenReturn(false);
		String shareUrl = cut.createContentShareTask("emf:id", "pdf");
		verify(schedulerService).buildConfiguration(any(ShareInstanceContentEvent.class));
		verify(schedulerService).schedule(Mockito.eq(ShareContentCreatedInstanceAction.ACTION_NAME), any(), any());
		assertTrue(shareUrl.contains("http://ses.com/emf/share/content/emf:new-id?shareCode="));
	}

	@Test(expected = EmfRuntimeException.class)
	public void test_createContentShareTask_shareCodeGenerationFail() throws Exception {
		when(schedulerService.buildConfiguration(any(ShareInstanceContentEvent.class)))
				.thenReturn(new DefaultSchedulerConfiguration());
		when(instance.isUploaded()).thenReturn(false);

		User mockUser = mock(User.class);
		when(mockUser.getIdentityId()).thenReturn("");
		when(securityContext.getEffectiveAuthentication()).thenReturn(mockUser);

		String shareUrl = cut.createContentShareTask("emf:id", "pdf");
		verify(schedulerService).buildConfiguration(any(ShareInstanceContentEvent.class));
		verify(schedulerService).schedule(Mockito.eq(ShareContentCreatedInstanceAction.ACTION_NAME), any(), any());
		assertTrue(shareUrl.contains("http://ses.com/emf/share/content/emf:new-id?shareCode="));
	}

	@Test
	public void test_triggerContentShareTask() throws Exception {
		cut.triggerContentShareTask("emf:id");
		verify(schedulerService).onEvent(any());
	}

	@Test
	public void test_createContentShareTasks() throws Exception {
		Instance anotherInstance = mock(Instance.class);
		when(anotherInstance.isUploaded()).thenReturn(true);
		when(anotherInstance.getString(any(String.class), any(String.class))).thenReturn("title2");
		when(anotherInstance.getId()).thenReturn("emf:id2");
		when(domainInstanceService.loadInstance("emf:id2")).thenReturn(anotherInstance);
		when(domainInstanceService.loadInstance("emf:id45")).thenReturn(anotherInstance);
		when(domainInstanceService.loadInstance("emf:id54323")).thenReturn(anotherInstance);

		when(schedulerService.buildConfiguration(any(ShareInstanceContentEvent.class)))
				.thenReturn(new DefaultSchedulerConfiguration());
		String[] instanceIds = { "emf:id", "emf:id2","emf:id45","emf:id54323" };
		Map<String, String> urls = cut.createContentShareTasks(Arrays.asList(instanceIds), "pdf");
		verify(schedulerService, times(4)).buildConfiguration(any(ShareInstanceContentEvent.class));
		assertEquals(4, urls.size());
		String[] keys =  urls.keySet().toArray(new String[3]);
		for (int i = 0; i < 3; i++) {
			assertEquals(keys[i], instanceIds[i]);
		}
		urls.values()
				.forEach(value -> assertTrue(value.contains("http://ses.com/emf/share/content/emf:new-id?shareCode=")));
	}

	@Test
	public void test_triggerContentShareTasks() throws Exception {
		cut.triggerContentShareTasks(Arrays.asList("emf:id", "emf:id2"));
		verify(schedulerService, times(2)).onEvent(any());
	}
}