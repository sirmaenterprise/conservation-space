package com.sirma.itt.emf.audit.processor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.db.AuditDao;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Test for {@link RecentActivitiesImporter}
 *
 * @author BBonev
 */
public class RecentActivitiesImporterTest {

	@InjectMocks
	private RecentActivitiesImporter importer;

	@Spy
	private ConfigurationProperty<Integer> requestBatchSize = new ConfigurationPropertyMock<>();
	@Mock
	private AuditDao auditDao;
	@Mock
	private RecentActivitiesSolrImporter activitiesSolrImporter;

	@Before
	@SuppressWarnings("unchecked")
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(auditDao.getActivitiesAfter(any(), any())).thenReturn(Arrays.asList(createActivity(), createActivity()),
				Arrays.asList(createActivity()), Collections.emptyList());
	}

	private static AuditActivity createActivity() {
		AuditActivity activity = new AuditActivity();
		activity.setEventDate(new Date());
		return activity;
	}

	@Test
	public void shouldImportUntilThereIsDataToImport() throws Exception {
		importer.triggerImport();

		verify(activitiesSolrImporter, times(2)).importActivities(anyCollection());
	}

	@Test
	public void shouldImportWithInitialData() throws Exception {
		when(activitiesSolrImporter.getLastKnownActivityDate()).thenReturn(new Date());

		importer.triggerImport();

		verify(auditDao, times(3)).getActivitiesAfter(any(Date.class), any());
		verify(activitiesSolrImporter, times(2)).importActivities(anyCollection());
	}

	@Test
	public void shouldStopImportOnImportError() throws Exception {
		doThrow(RollbackedException.class).when(activitiesSolrImporter).importActivities(anyCollection());

		importer.triggerImport();

		verify(activitiesSolrImporter).importActivities(anyCollection());
	}
}
