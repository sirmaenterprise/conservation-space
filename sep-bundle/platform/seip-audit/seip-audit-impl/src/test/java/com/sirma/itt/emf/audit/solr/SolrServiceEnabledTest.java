package com.sirma.itt.emf.audit.solr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IExpectationSetters;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.emf.audit.solr.query.SolrQueryParams;
import com.sirma.itt.emf.audit.solr.query.SolrResult;
import com.sirma.itt.emf.audit.solr.service.SolrService;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;
import com.sirma.itt.emf.audit.solr.service.SolrServiceImpl;

import de.akquinet.jbosscc.needle.annotation.InjectInto;
import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Tests the logic behind {@link SolrServiceImpl}.
 * 
 * @author Mihail Radkov
 */
public class SolrServiceEnabledTest {

	/** The solr service. */
	@ObjectUnderTest(id = "solr", implementation = SolrServiceImpl.class)
	private SolrService solrService;

	@InjectInto(targetComponentId = "solr")
	private SolrServer server = EasyMock.createNiceMock(HttpSolrServer.class);

	/** Enables {@link SolrServiceImpl}. */
	@InjectInto(targetComponentId = "solr")
	private Boolean enabled = true;

	/** The needle rule. */
	@Rule
	public NeedleRule needleRule = new NeedleRule();

	/**
	 * Try to get ids from the solr server with empty {@link SolrQueryParams}. Shouldn't be allowed.
	 * 
	 * @throws SolrServiceException
	 *             the expected exception
	 */
	@Test(expected = SolrServiceException.class)
	public void testWithEmptyQuery() throws SolrServiceException {
		solrService.getIDsFromQuery(new SolrQueryParams());
	}

	/**
	 * Tests the building of the solr query with correct parameters.
	 */
	@Test
	public void testWithCorrectQuery() {
		Capture<SolrParams> capture = mockSolrServer();
		SolrQueryParams queryParams = new SolrQueryParams();
		queryParams.setQuery("test query");
		queryParams.setStart(1);
		queryParams.setRows(9001);

		try {
			solrService.getIDsFromQuery(queryParams);
		} catch (SolrServiceException e) {
			e.printStackTrace();
			Assert.fail();
		}

		SolrParams capturedValue = capture.getValue();

		Assert.assertEquals("test query", capturedValue.get("q"));
		Assert.assertEquals(1, (int) capturedValue.getInt("start"));
		Assert.assertEquals(9001, (int) capturedValue.getInt("rows"));
		Assert.assertNull(capturedValue.get("sort"));
	}

	/**
	 * Tests the building of the solr query with zero rows.
	 */
	@Test
	public void testWithZeroRows() {
		Capture<SolrParams> capture = mockSolrServer();
		SolrQueryParams queryParams = new SolrQueryParams();
		queryParams.setQuery("test query");
		queryParams.setRows(0);

		try {
			solrService.getIDsFromQuery(queryParams);
		} catch (SolrServiceException e) {
			e.printStackTrace();
			Assert.fail();
		}

		SolrParams capturedValue = capture.getValue();
		Assert.assertEquals(0, (int) capturedValue.getInt("start"));
		Assert.assertNull(capturedValue.getInt("rows"));
	}

	/**
	 * Tests the building of the solr query with sorting in ascending order.
	 */
	@Test
	public void testWithAscOrder() {
		Capture<SolrParams> capture = mockSolrServer();
		SolrQueryParams queryParams = new SolrQueryParams();
		queryParams.setQuery("test query");
		queryParams.setSortField("sort field");
		queryParams.setSortOrder("asc");

		try {
			solrService.getIDsFromQuery(queryParams);
		} catch (SolrServiceException e) {
			e.printStackTrace();
			Assert.fail();
		}

		SolrParams capturedValue = capture.getValue();
		Assert.assertEquals("sort field asc", capturedValue.get("sort"));
	}

	/**
	 * Tests the building of the solr query with sorting in descending order.
	 */
	@Test
	public void testWithDescOrder() {
		Capture<SolrParams> capture = mockSolrServer();
		SolrQueryParams queryParams = new SolrQueryParams();
		queryParams.setQuery("test query");
		queryParams.setSortField("sort field");
		queryParams.setSortOrder("desc");

		try {
			solrService.getIDsFromQuery(queryParams);
		} catch (SolrServiceException e) {
			e.printStackTrace();
			Assert.fail();
		}

		SolrParams capturedValue = capture.getValue();
		Assert.assertEquals("sort field desc", capturedValue.get("sort"));
	}

	/**
	 * Tests the building of the solr query without specifying the sort field.
	 */
	@Test
	public void testWithoutSortField() {
		Capture<SolrParams> capture = mockSolrServer();
		SolrQueryParams queryParams = new SolrQueryParams();
		queryParams.setQuery("test query");
		queryParams.setSortOrder("somewhere");

		try {
			solrService.getIDsFromQuery(queryParams);
		} catch (SolrServiceException e) {
			e.printStackTrace();
			Assert.fail();
		}

		SolrParams capturedValue = capture.getValue();
		Assert.assertNull(capturedValue.get("sort"));
	}

	/**
	 * Tests the building of the solr query without specifying the sord direction.
	 */
	@Test
	public void testWithoutSortOrder() {
		Capture<SolrParams> capture = mockSolrServer();
		SolrQueryParams queryParams = new SolrQueryParams();
		queryParams.setQuery("test query");
		queryParams.setSortField("sort field");

		try {
			solrService.getIDsFromQuery(queryParams);
		} catch (SolrServiceException e) {
			e.printStackTrace();
			Assert.fail();
		}

		SolrParams capturedValue = capture.getValue();
		Assert.assertNull(capturedValue.get("sort"));
	}

	/**
	 * Tests the building of the solr response.
	 */
	@Test
	public void testResponse() {
		mockSolrServer();
		SolrQueryParams queryParams = new SolrQueryParams();
		queryParams.setQuery("test query");

		SolrResult solrResult = null;
		try {
			solrResult = solrService.getIDsFromQuery(queryParams);
		} catch (SolrServiceException e) {
			e.printStackTrace();
			Assert.fail();
		}

		Assert.assertEquals(5, solrResult.getTotal());
		Assert.assertEquals(Arrays.asList(1L, 2L, 3L, 4L, 5L), solrResult.getIds());
	}

	/**
	 * Tests the behavior of the service when an error occurs. TODO: WTF
	 * 
	 * @throws Exception
	 *             - the expected exception
	 */
	@Test(expected = SolrServiceException.class)
	public void testWithError() throws Exception {
		EasyMock.expect(server.query(EasyMock.anyObject(SolrParams.class))).andThrow(
				new SolrServerException("FAILURE"));
		EasyMock.replay(server);

		SolrQueryParams queryParams = new SolrQueryParams();
		queryParams.setQuery("test query");

		solrService.getIDsFromQuery(queryParams);
	}

	/**
	 * Mocks the solr server.
	 * 
	 * @return a {@link Capture} of {@link SolrServer#query(SolrParams)}
	 */
	private Capture<SolrParams> mockSolrServer() {
		Capture<SolrParams> capture = new Capture<>();
		try {
			EasyMock.expect(server.query(EasyMock.capture(capture))).andAnswer(getResponse(5))
					.once();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		EasyMock.replay(server);
		return capture;
	}

	/**
	 * Creates an answer for the mocks with sample test data.
	 * 
	 * @param count
	 *            - the count of the generated test data
	 * @return the answer
	 */
	private IAnswer<QueryResponse> getResponse(final int count) {
		IAnswer<QueryResponse> answer = new IAnswer<QueryResponse>() {

			@Override
			public QueryResponse answer() throws Throwable {
				QueryResponse response = new QueryResponse();
				SolrDocumentList list = new SolrDocumentList();
				for (int i = 1; i < count + 1; i++) {
					SolrDocument document = new SolrDocument();
					document.setField("id", i);
					list.add(document);
				}
				list.setNumFound(count);
				NamedList<Object> namedList = new NamedList<>();
				namedList.add("response", list);
				response.setResponse(namedList);
				return response;
			}
		};
		return answer;
	}

	/**
	 * Tests the logic behind {@link SolrService#deleteById(List)} when providing correct parameter.
	 * 
	 * @throws Exception
	 *             - if some error occurs
	 */
	@Test
	public void testDelete() throws Exception {
		EasyMock.expect(server.deleteById(EasyMock.<List<String>> anyObject()))
				.andAnswer(emptyAnswer()).atLeastOnce();
		EasyMock.expect(server.commit()).andAnswer(emptyAnswer()).atLeastOnce();
		EasyMock.replay(server);

		solrService.deleteById(Arrays.asList("1"));
	}

	/**
	 * Tests the logic behind {@link SolrService#deleteById(List)} when providing an empty
	 * {@link List} and null.
	 * 
	 * @throws Exception
	 *             - if some error occurs
	 */
	@Test
	public void testDeleteWithEmptyList() throws Exception {
		EasyMock.expect(server.deleteById(EasyMock.<List<String>> anyObject()))
				.andThrow(new AssertionFailedError()).anyTimes();
		EasyMock.expect(server.commit()).andThrow(new AssertionFailedError()).anyTimes();
		EasyMock.replay(server);

		solrService.deleteById(new ArrayList<String>());
		solrService.deleteById(null);
	}

	/**
	 * Tests the logic behind {@link SolrService#deleteById(List)} when an exception is handled.
	 * 
	 * @throws Exception
	 *             the expected exception TODO: WTF
	 */
	@Test(expected = SolrServiceException.class)
	public void testDeleteWithError() throws Exception {
		EasyMock.expect(server.deleteById(EasyMock.<List<String>> anyObject())).andThrow(
				new SolrServerException("oh no"));
		EasyMock.replay(server);

		solrService.deleteById(Arrays.asList("1"));
	}

	/**
	 * Builds an empty answer of {@link UpdateResponse}.
	 * 
	 * @return the answer
	 */
	private IAnswer<UpdateResponse> emptyAnswer() {
		IAnswer<UpdateResponse> answer = new IAnswer<UpdateResponse>() {
			@Override
			public UpdateResponse answer() throws Throwable {
				return null;
			}
		};
		return answer;
	}

	/**
	 * Tests the logic behind {@link SolrService#dataImport(boolean)} when there the data import is
	 * clean.
	 */
	@Test
	public void testCleanDataImport() {
		Capture<SolrParams> capture = mockDataImport(false);
		Assert.assertTrue(solrService.dataImport(true));

		SolrParams lastCapture = capture.getValue();
		Assert.assertEquals("/dataimport", lastCapture.get("qt"));
		Assert.assertEquals("full-import", lastCapture.get("command"));
		Assert.assertEquals("true", lastCapture.get("clean"));
		Assert.assertEquals("true", lastCapture.get("commit"));
	}

	/**
	 * Tests the logic behind {@link SolrService#dataImport(boolean)} when there is no running data
	 * import.
	 */
	@Test
	public void testDataImport() {
		Capture<SolrParams> capture = mockDataImport(false);
		Assert.assertTrue(solrService.dataImport(false));

		SolrParams lastCapture = capture.getValue();

		Assert.assertEquals("*:*", lastCapture.get("q"));
		Assert.assertEquals("id desc", lastCapture.get("sort"));
		Assert.assertEquals("0", lastCapture.get("start"));
		Assert.assertEquals("1", lastCapture.get("rows"));
	}

	/**
	 * Tests the logic behind {@link SolrService#dataImport(boolean)} when there is running data
	 * import.
	 */
	@Test
	public void testRunningDataImport() {
		Capture<SolrParams> capture = mockDataImport(true);
		Assert.assertFalse(solrService.dataImport(true));
		Assert.assertEquals("status", capture.getValue().get("command"));
	}

	/**
	 * Tests the logic behind {@link SolrService#dataImport(boolean)} when an exception is handled.
	 * 
	 * @throws SolrServerException
	 *             - if a problem occurs
	 */
	@Test
	public void testDataImportWithError() throws SolrServerException {
		EasyMock.expect(server.query(EasyMock.anyObject(ModifiableSolrParams.class))).andThrow(
				new SolrServerException("panic"));
		EasyMock.replay(server);
		Assert.assertFalse(solrService.dataImport(true));
	}

	/**
	 * Mocks {@link SolrServer#query(ModifiableSolrParams)} based on the provided parameter.
	 * 
	 * @param running
	 *            - if there is a running data import or not
	 * @return a {@link Capture} of {@link SolrServer#query(ModifiableSolrParams)}
	 */
	private Capture<SolrParams> mockDataImport(boolean running) {
		Capture<SolrParams> capture = new Capture<>();
		try {
			IExpectationSetters<QueryResponse> expect = EasyMock.expect(server.query(EasyMock
					.capture(capture)));
			if (running) {
				expect.andAnswer(dataImportResponse("busy")).once();
			} else {
				expect.andAnswer(dataImportResponse("not busy")).once();
				EasyMock.expect(server.query(EasyMock.capture(capture))).andAnswer(getResponse(1))
						.once();
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		EasyMock.replay(server);
		return capture;
	}

	/**
	 * Builds an answer based on the provided status for the data import mocks.
	 * 
	 * @param status
	 *            the provided status
	 * @return the answer
	 */
	private IAnswer<QueryResponse> dataImportResponse(final String status) {
		IAnswer<QueryResponse> answer = new IAnswer<QueryResponse>() {

			@Override
			public QueryResponse answer() throws Throwable {
				QueryResponse response = new QueryResponse();
				NamedList<Object> namedList = new NamedList<>();
				namedList.add("status", status);
				response.setResponse(namedList);
				return response;
			}
		};
		return answer;
	}
}
