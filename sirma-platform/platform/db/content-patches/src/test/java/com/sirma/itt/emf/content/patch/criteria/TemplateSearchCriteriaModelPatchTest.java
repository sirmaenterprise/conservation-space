/**
 * 
 */
package com.sirma.itt.emf.content.patch.criteria;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.template.TemplateInstance;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.semantic.NamespaceRegistryService;

import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;

/**
 * Tests the criteria model migration logic in {@link TemplateSearchCriteriaModelPatch}.
 * 
 * @author Mihail Radkov
 */
public class TemplateSearchCriteriaModelPatchTest {

	@Mock
	private DataSource datasource;
	@Mock
	private TemplateService documentTemplateService;
	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@InjectMocks
	private TemplateSearchCriteriaModelPatch templateCriteriaModelPatch;

	@Before
	public void setUp() throws SetupException {
		templateCriteriaModelPatch = new TemplateSearchCriteriaModelPatch();
		MockitoAnnotations.initMocks(this);
		when(namespaceRegistryService.buildFullUri(any(String.class))).then(a -> a.getArgumentAt(0, String.class));
	}

	@Test
	public void testMigration() throws SQLException, IOException, CustomChangeException {
		SearchCriteriaMigrationTestUtils.mockDataSource(datasource);

		String content = getTestFile("content-for-migration.html");

		TemplateInstance template = new TemplateInstance();
		template.add(DefaultProperties.CONTENT, content);
		template.setPublicTemplate(true);

		mockDocumentTemplateService("1", template);

		templateCriteriaModelPatch.execute(null);

		// Should have loaded the content
		Mockito.verify(documentTemplateService, Mockito.times(1)).loadContent(Matchers.eq(template));
		// Should have saved it
		Mockito.verify(documentTemplateService, Mockito.times(1)).activate(Matchers.eq(template));
	}

	@Test
	public void testMigrationForEmptyContent() throws SQLException, IOException, CustomChangeException {
		SearchCriteriaMigrationTestUtils.mockDataSource(datasource);

		String content = getTestFile("content-empty.html");

		TemplateInstance template = new TemplateInstance();
		template.add(DefaultProperties.CONTENT, content);
		template.setPublicTemplate(true);

		mockDocumentTemplateService("1", template);

		templateCriteriaModelPatch.execute(null);

		// Should have loaded the content
		Mockito.verify(documentTemplateService, Mockito.times(1)).loadContent(Matchers.eq(template));
		// Should have NOT saved it
		Mockito.verify(documentTemplateService, Mockito.times(0)).activate(Matchers.eq(template));
	}

	private void mockDocumentTemplateService(String id, TemplateInstance template) {
		Mockito.when(documentTemplateService.getTemplate(Matchers.eq(id))).thenReturn(template);
	}

	private String getTestFile(String name) throws IOException {
		InputStream resourceAsStream = TemplateSearchCriteriaModelPatchTest.class.getResourceAsStream(name);
		return IOUtils.toString(resourceAsStream);
	}
}
