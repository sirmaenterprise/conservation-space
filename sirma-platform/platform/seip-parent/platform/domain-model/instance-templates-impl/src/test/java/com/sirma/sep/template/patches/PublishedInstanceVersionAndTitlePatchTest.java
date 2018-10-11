package com.sirma.sep.template.patches;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.instance.version.VersionsResponse;
import com.sirma.itt.seip.template.db.TemplateEntity;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;

import liquibase.exception.CustomChangeException;

public class PublishedInstanceVersionAndTitlePatchTest {

	@InjectMocks
	private PublishedInstanceVersionAndTitlePatch patch;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Spy
	private TransactionSupportFake transactionSupport;

	@Mock
	private DbDao dbDao;

	@Captor
	private ArgumentCaptor<List<Pair<String, Object>>> captor;

	@Before
	@SuppressWarnings("unchecked")
	public void init() {
		patch = new PublishedInstanceVersionAndTitlePatch();
		MockitoAnnotations.initMocks(this);

		VersionsResponse reponse = new VersionsResponse();
		reponse.setResults(versions);

		when(instanceVersionService.getInstanceVersions(TEMPLATE_INSTANCE_ID, 0, -1)).thenReturn(reponse);

		when(dbDao.fetch(anyString(), anyList())).thenReturn(Arrays.asList(TEMPLATE_INSTANCE_ID));
	}

	@Test
	public void should_SetMissingPublishedTemplateVersion() {
		withVersion("1.0", "DRAFT", "Main template");
		withVersion("1.1", "ACTIVE", "Main template");
		withVersion("1.2", "DRAFT", "Main template");

		verifyPublishedVersion("1.1", "Main template");
	}

	private void verifyPublishedVersion(String expectedVersion, String expectedTitle) {
		try {
			patch.execute(null);

			verify(dbDao).executeUpdate(eq(TemplateEntity.QUERY_UPDATE_PUBLISHED_INSTANCE_VERSION_AND_TITLE_KEY),
					captor.capture());

			List<Pair<String, Object>> queryParams = captor.getValue();
			assertEquals(new Pair<String, Object>("templateInstanceId", TEMPLATE_INSTANCE_ID), queryParams.get(0));
			assertEquals(new Pair<String, Object>("instanceVersion", expectedVersion), queryParams.get(1));
			assertEquals(new Pair<String, Object>("title", expectedTitle), queryParams.get(2));
		} catch (CustomChangeException e) {
			fail(e.getMessage());
		}
	}

	private void withVersion(String version, String state, String title) {
		Instance instance = new EmfInstance();
		instance.add(DefaultProperties.VERSION, version);
		instance.add(DefaultProperties.STATUS, state);
		instance.add(DefaultProperties.TITLE, title);

		versions.add(instance);
	}

	private List<Instance> versions = new ArrayList<>();

	private final String TEMPLATE_INSTANCE_ID = "template_instance";
}
