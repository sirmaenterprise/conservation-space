package com.sirma.itt.emf.audit.rest;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IExpectationSetters;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.db.AuditService;
import com.sirma.itt.emf.audit.export.AuditExportService;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;
import com.sirma.itt.emf.audit.solr.service.SolrService;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;
import com.sirma.itt.seip.rest.annotations.security.AdminResource;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Tests the logic at {@link AuditRestService} by creating mocks for the injected services in it.
 *
 * @author Mihail Radkov
 */
public class AuditRestServiceTest {

	/** The needle rule. */
	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@ObjectUnderTest(id = "ars")
	private AuditRestService auditRestService;

	@Inject
	private AuditService auditService;

	@Inject
	private SolrService solrService;

	@Inject
	private AuditExportService exportService;

	/**
	 * Tests {@link AuditRestService#getEvents(String, List, int, int, String, String)} with correct parameters and
	 * mock.
	 */
	@Test
	public void testGetEvents() {
		Capture<SolrQuery> serviceMock = setupAuditServiceMock(false);
		Response response = auditRestService.getEvents("zee query", Arrays.asList( "1", "2", "3" ), 1, 2, "", "");

		Assert.assertEquals(200, response.getStatus());
		Assert.assertNotNull(response.getEntity());

		SolrQuery solrQuery = serviceMock.getValue();
		Assert.assertEquals("zee query", solrQuery.getQuery());
		Assert.assertEquals(new Integer(1), solrQuery.getStart());
		Assert.assertEquals(new Integer(2), solrQuery.getRows());
		Assert.assertArrayEquals(new String[] { "1", "2", "3" }, solrQuery.getFilterQueries());
		Assert.assertEquals(0, solrQuery.getSorts().size());
	}

	/**
	 * Tests {@link AuditRestService#getEvents(String, List, int, int, String, String)} with correct parameters and
	 * without any sorting.
	 */
	@Test
	public void testGetEventsWithoutSort() {
		Capture<SolrQuery> serviceMock = setupAuditServiceMock(false);
		auditRestService.getEvents("", Collections.emptyList(), 1, 2, "", "");
		SolrQuery solrQuery = serviceMock.getValue();
		Assert.assertEquals(0, solrQuery.getSorts().size());

		serviceMock = setupAuditServiceMock(false);
		auditRestService.getEvents("", Collections.emptyList(), 1, 2, null, null);
		solrQuery = serviceMock.getValue();
		Assert.assertEquals(0, solrQuery.getSorts().size());

		serviceMock = setupAuditServiceMock(false);
		auditRestService.getEvents("", Collections.emptyList(), 1, 2, null, "asc");
		solrQuery = serviceMock.getValue();
		Assert.assertEquals(0, solrQuery.getSorts().size());

		serviceMock = setupAuditServiceMock(false);
		auditRestService.getEvents("", Collections.emptyList(), 1, 2, "field", null);
		solrQuery = serviceMock.getValue();
		Assert.assertEquals(0, solrQuery.getSorts().size());

		serviceMock = setupAuditServiceMock(false);
		auditRestService.getEvents("", Collections.emptyList(), 1, 2, "field", "");
		solrQuery = serviceMock.getValue();
		Assert.assertEquals(0, solrQuery.getSorts().size());

		serviceMock = setupAuditServiceMock(false);
		auditRestService.getEvents("1", Collections.emptyList(), 1, 2, "field", "blaaa");
		solrQuery = serviceMock.getValue();
		Assert.assertEquals(0, solrQuery.getSorts().size());
		Assert.assertEquals("1", solrQuery.getQuery());

	}

	/**
	 * Tests {@link AuditRestService#getEvents(String, List, int, int, String, String)} with correct parameters but
	 * with sorting.
	 */
	@Test
	public void testGetEventsWithSort() {
		Capture<SolrQuery> serviceMock = setupAuditServiceMock(false);
		auditRestService.getEvents("", Collections.emptyList(), 1, 2, "this is my ASC field", "aSc");
		SolrQuery solrQuery = serviceMock.getValue();
		Assert.assertEquals(1, solrQuery.getSorts().size());
		SortClause sortClause = solrQuery.getSorts().get(0);
		Assert.assertEquals("this is my asc field", sortClause.getItem());
		Assert.assertEquals(ORDER.asc, sortClause.getOrder());

		serviceMock = setupAuditServiceMock(false);
		auditRestService.getEvents("", Collections.emptyList(), 1, 2, "THIS is My DESC field", "dEsC");
		solrQuery = serviceMock.getValue();
		Assert.assertEquals(1, solrQuery.getSorts().size());
		sortClause = solrQuery.getSorts().get(0);
		Assert.assertEquals("this is my desc field", sortClause.getItem());
		Assert.assertEquals(ORDER.desc, sortClause.getOrder());
	}

	/**
	 * Tests {@link AuditRestService#getEvents(String, List, int, int, String, String)} with exception thrown by
	 * {@link AuditService}
	 */
	@Test
	public void testGetEventsWithException() {
		setupAuditServiceMock(true);
		Response response = auditRestService.getEvents("", Collections.emptyList(), 1, 2, "", "");
		Assert.assertEquals(500, response.getStatus());
		Assert.assertEquals("oh no you did not", response.getEntity().toString());
	}

	/**
	 * Setups a mock for {@link AuditService#getActivitiesBySolrQuery(SolrQuery)} that will return an empty
	 * {@link ServiceResult} or will throw {@link SolrServiceException} depending on the boolean flag.
	 *
	 * @param throwException
	 *            - the flag telling to throw an exception or not
	 * @return a capture for {@link SolrQuery}
	 */
	private Capture<SolrQuery> setupAuditServiceMock(boolean throwException) {
		EasyMock.reset(auditService);
		Capture<SolrQuery> capture = new Capture<>();
		IExpectationSetters<ServiceResult> expect = null;
		try {
			expect = EasyMock.expect(auditService.getActivitiesBySolrQuery(EasyMock.capture(capture)));
			if (throwException) {
				expect.andThrow(new SolrServiceException("oh no you did not"));
			} else {
				expect.andReturn(new ServiceResult());
			}
		} catch (SolrServiceException e) {
			Assert.fail(e.getMessage());
		}
		EasyMock.replay(auditService);
		return capture;
	}

	/**
	 * Tests {@link AuditRestService#export(String, List, String, String, String, String)} without any specified
	 * file format.
	 */
	@Test
	public void testExportWithoutFormat() {
		Response response = auditRestService.export("", Collections.emptyList(), "", "", "", "");
		Assert.assertEquals(400, response.getStatus());
	}

	/**
	 * Tests {@link AuditRestService#export(String, List, String, String, String, String)} with 'CSV' for file
	 * format.
	 */
	@Test
	public void testExportAsCSV() {
		Capture<SolrQuery> auditServiceMock = setupAuditServiceMock(false);
		setupExportServiceMock("csv", null);

		Response response = auditRestService.export("", Collections.emptyList(), "", "", "csV", "[]");

		Assert.assertEquals(new Integer(Integer.MAX_VALUE), auditServiceMock.getValue().getRows());
		Assert.assertEquals(200, response.getStatus());
		Assert.assertNotNull(response.getMetadata().get("Content-Disposition"));
	}

	/**
	 * Tests {@link AuditRestService#export(String, List, String, String, String, String)} with 'PDF' for file
	 * format.
	 */
	@Test
	public void testExportAsPdf() {
		Capture<SolrQuery> auditServiceMock = setupAuditServiceMock(false);
		setupExportServiceMock("pdf", null);

		Response response = auditRestService.export("", Collections.emptyList(), "", "", "pDf", "[]");

		Assert.assertEquals(new Integer(Integer.MAX_VALUE), auditServiceMock.getValue().getRows());
		Assert.assertEquals(200, response.getStatus());
		Assert.assertNotNull(response.getMetadata().get("Content-Disposition"));
	}

	/**
	 * Tests {@link AuditRestService#export(String, List, String, String, String, String)} with exceptions thrown by
	 * {@link AuditService} and {@link AuditExportService}.
	 */
	@Test
	public void testExportWithException() {
		setupAuditServiceMock(false);
		setupExportServiceMock("pdf", new IOException());
		Response response = auditRestService.export("", Collections.emptyList(), "", "", "pdf", "[]");
		Assert.assertEquals(500, response.getStatus());

		setupAuditServiceMock(true);
		setupExportServiceMock("pdf", null);
		response = auditRestService.export("", Collections.emptyList(), "", "", "pdf", "[]");
		Assert.assertEquals(500, response.getStatus());

		setupAuditServiceMock(false);
		setupExportServiceMock("pdf", new JSONException(""));
		response = auditRestService.export("", Collections.emptyList(), "", "", "pdf", "[]");
		Assert.assertEquals(500, response.getStatus());
	}

	/**
	 * Tests that the class is annotated with {@link AdminResource}.
	 */
	@Test
	public void testMarkedAsAdminResource() {
		AdminResource annotation = AuditRestService.class.getAnnotation(AdminResource.class);
		assertNotNull(annotation);
	}

	/**
	 * Setups a mock for {@link AuditExportService#exportAsCsv(List, String, JSONArray)} or
	 * {@link AuditExportService#exportAsPdf(List, String, JSONArray)} depending on the provided parameter. It will
	 * return an empty {@link File} or will throw the provided exception if not null.
	 *
	 * @param type
	 *            - the provided file format
	 * @param ex
	 *            - the exception to throw if not null
	 */
	@SuppressWarnings("unchecked")
	private void setupExportServiceMock(String type, Exception ex) {
		EasyMock.reset(exportService);
		IExpectationSetters<File> expect = null;
		try {
			if ("csv".equals(type)) {
				expect = EasyMock.expect(exportService.exportAsCsv((List<AuditActivity>) EasyMock.anyObject(),
						EasyMock.anyString(), EasyMock.anyObject(JSONArray.class)));
			} else if ("pdf".equals(type)) {
				expect = EasyMock.expect(exportService.exportAsPdf((List<AuditActivity>) EasyMock.anyObject(),
						EasyMock.anyString(), EasyMock.anyObject(JSONArray.class)));
			}
		} catch (IOException | JSONException e) {
			Assert.fail(e.getMessage());
		}

		if (expect == null) {
			Assert.fail("Invalid type: " + type);
			return;
		}
		if (ex != null) {
			expect.andThrow(ex);
		} else {
			expect.andReturn(new File("test"));
		}
		EasyMock.replay(exportService);
	}

	/**
	 * Tests {@link AuditRestService#dataImport()}. Totally useless test I guess...
	 */
	@Test
	public void testDataImport() {
		EasyMock.expect(solrService.dataImport(EasyMock.anyBoolean())).andReturn(true);
		auditRestService.dataImport();
	}

}
