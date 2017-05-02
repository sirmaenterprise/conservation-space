package com.sirma.itt.emf.content.patch.criteria;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.NamespaceRegistryService;

import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;

/**
 * Mocks the migration logic in {@link ContentSearchCriteriaModelPatch}
 *
 * @author Mihail Radkov
 */
public class ContentSearchCriteriaModelPatchTest {

	@Mock
	private DataSource datasource;
	@Mock
	private InstanceContentService instanceContentService;
	@Mock
	private NamespaceRegistryService namespaceRegistryService;
	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@InjectMocks
	private ContentSearchCriteriaModelPatch contentSearchCriteriaModelPatch;

	@Before
	public void setUp() throws SetupException {
		contentSearchCriteriaModelPatch = new ContentSearchCriteriaModelPatch();
		MockitoAnnotations.initMocks(this);
		when(namespaceRegistryService.buildFullUri(any(String.class))).then(a -> a.getArgumentAt(0, String.class));
	}

	@Test
	public void testMigration() throws SQLException, CustomChangeException {
		SearchCriteriaMigrationTestUtils.mockDataSource(datasource);

		InputStream resourceAsStream = getTestFileAsStream("content-for-migration.html");
		ContentInfo contenInfo = new ContentInfoMock("content-name", "1", resourceAsStream, true, Content.PRIMARY_VIEW);

		Collection<ContentInfo> contentInfos = new ArrayList<>();
		contentInfos.add(contenInfo);
		SearchCriteriaMigrationTestUtils.mockInstanceContentService(instanceContentService, Content.PRIMARY_VIEW,
				contentInfos);

		ArgumentCaptor<Content> contentCaptor = ArgumentCaptor.forClass(Content.class);
		when(instanceContentService.updateContent(any(), any(Instance.class), contentCaptor.capture()))
				.thenReturn(null);

		contentSearchCriteriaModelPatch.execute(null);

		verify(instanceContentService).updateContent(any(), any(Instance.class), any(Content.class));

		Content capturedValue = contentCaptor.getValue();
		Assert.assertNotNull(capturedValue);
		Assert.assertEquals(Content.PRIMARY_VIEW, capturedValue.getPurpose());
		Assert.assertFalse(capturedValue.isVersionable());
	}

	@Test
	public void testMigrationWithoutContent() throws SQLException, CustomChangeException {
		SearchCriteriaMigrationTestUtils.mockDataSource(datasource);
		SearchCriteriaMigrationTestUtils.mockInstanceContentService(instanceContentService, Content.PRIMARY_VIEW,
				Collections.emptyList());

		contentSearchCriteriaModelPatch.execute(null);

		verify(instanceContentService, never()).updateContent(any(), any(Instance.class), any(Content.class));
	}

	@Test
	public void testMigrationWithEmptyContent() throws SQLException, CustomChangeException {
		SearchCriteriaMigrationTestUtils.mockDataSource(datasource);

		InputStream resourceAsStream = getTestFileAsStream("content-empty.html");
		ContentInfo contenInfo = new ContentInfoMock("content-name", "1", resourceAsStream, true, Content.PRIMARY_VIEW);

		Collection<ContentInfo> contentInfos = new ArrayList<>();
		contentInfos.add(contenInfo);
		SearchCriteriaMigrationTestUtils.mockInstanceContentService(instanceContentService, Content.PRIMARY_VIEW,
				contentInfos);

		contentSearchCriteriaModelPatch.execute(null);

		verify(instanceContentService, never()).updateContent(any(), any(Instance.class), any(Content.class));
	}

	private InputStream getTestFileAsStream(String name) {
		return ContentSearchCriteriaModelPatchTest.class.getResourceAsStream(name);
	}
}
