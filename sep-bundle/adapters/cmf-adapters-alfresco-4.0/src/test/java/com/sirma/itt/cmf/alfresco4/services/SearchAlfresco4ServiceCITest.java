package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.services.convert.ConverterConstants;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.CommonProperties;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.adapter.CMFSearchAdapterService;
import com.sirma.itt.cmf.test.BaseAlfrescoTest;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.adapter.DMSTenantAdapterService;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.time.ISO8601DateFormat;

/**
 * @author bbanchev
 */
public class SearchAlfresco4ServiceCITest extends BaseAlfrescoTest {
	private CMFSearchAdapterService searchAdapter;

	/** The Constant CASE_ASPECTS. */
	private final static HashSet<String> CASE_ASPECTS = new HashSet<String>(1);

	/** The Constant DOCUMENT_ASPECTS. */
	private final static HashSet<String> DOCUMENT_ASPECTS = new HashSet<String>(2);

	private DMSTenantAdapterService tenantAdapter;

	private Set<String> emfContainers;

	static {
		CASE_ASPECTS.add(CaseProperties.TYPE_CASE_INSTANCE);

		DOCUMENT_ASPECTS.add(DocumentProperties.TYPE_DOCUMENT_ATTACHMENT);
		DOCUMENT_ASPECTS.add(DocumentProperties.TYPE_DOCUMENT_STRUCTURED);
	}

	@BeforeClass
	@Override
	protected void setUp() {
		super.setUp();
		searchAdapter = mockupProvider.mockupSearchAdapter();
		tenantAdapter = mockupProvider.mockupTenantAdapter();
		try {
			emfContainers = tenantAdapter.getEmfContainers();
			assertTrue(emfContainers != null && emfContainers.size() > 0,
					"EMF should be enabled and initialized!");
		} catch (DMSException e) {
			fail(e);
		}
	}

	private Map<String, JSONObject> idToPropsCache = new HashMap<>();

	/**
	 * Test method for
	 * {@link com.sirma.itt.cmf.alfresco4.services.SearchAlfresco4Service#search(com.sirma.itt.emf.search.model.SearchArguments, java.lang.Class)}
	 * .
	 */
	@Test
	public void testSearch() throws Exception {

		SearchArguments<FileDescriptor> args = new SearchArguments<FileDescriptor>();
		args.setContext(emfContainers.iterator().next());
		args.setQuery(new Query(CommonProperties.KEY_SEARCHED_ASPECT, CASE_ASPECTS));
		args.setMaxSize(25);
		args.setTotalItems(0);
		SearchArguments<FileDescriptor> search = searchAdapter.search(args, CaseInstance.class);
		List<FileDescriptor> result = search.getResult();

		assertTrue(result.size() >= 0 && result.size() <= 25, "Result should max of 25");
		checkSorting(result, CASE_ASPECTS, "cm:modified", new Comparator<Date>() {

			@Override
			public int compare(Date o1, Date o2) {
				return o2.compareTo(o1);
			}
		});

		args.setSorter(new Sorter(CaseProperties.MODIFIED_ON, Sorter.SORT_ASCENDING));
		args.setTotalItems(0);
		search = searchAdapter.search(args, CaseInstance.class);
		result = search.getResult();

		assertTrue(result.size() >= 0 && result.size() <= 25, "Result should max of 25");
		checkSorting(result, CASE_ASPECTS, "cm:modified", new Comparator<Date>() {

			@Override
			public int compare(Date o1, Date o2) {
				return o1.compareTo(o2);
			}
		});
		args.setQuery(new Query(CommonProperties.KEY_SEARCHED_ASPECT, DOCUMENT_ASPECTS));

		args.setSorter(new Sorter(DocumentProperties.MODIFIED_ON, Sorter.SORT_ASCENDING));
		search = searchAdapter.search(args, DocumentInstance.class);
		result = search.getResult();

		assertTrue(result.size() >= 0 && result.size() <= 25, "Result should max of 25");
		checkSorting(result, DOCUMENT_ASPECTS, "cm:modified", new Comparator<Date>() {

			@Override
			public int compare(Date o1, Date o2) {
				return o1.compareTo((Date) o2);
			}
		});
	}

	/**
	 * Helper method to check sorting after search.s
	 *
	 * @param result
	 *            the result list
	 * @param aspectId
	 *            the aspects to check in the result
	 * @param propId
	 *            for sorting
	 * @param comparator
	 *            the comparator for sorting
	 * @throws Exception
	 *             on any error
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void checkSorting(List<FileDescriptor> result, Set<String> aspectId, String propId,
			Comparator comparator) throws Exception {
		LinkedList<Comparable> extracted = new LinkedList<>();
		DMSTypeConverter mockupDMSTypeConverter = mockupProvider
				.mockupDMSTypeConverter(ConverterConstants.CASE.toLowerCase());
		for (FileDescriptor dmsFileDescriptor : result) {
			String id = dmsFileDescriptor.getId();
			assertNotNull(id, "NodeRef should be set");
			if (!idToPropsCache.containsKey(id)) {
				HttpMethod getInfo = httpClient.createMethod(new GetMethod(), (String) null, true);

				String request = httpClient.request("/api/metadata?nodeRef=" + id
						+ "&shortQNames=true", getInfo);
				JSONObject jsonObject = new JSONObject(request);

				idToPropsCache.put(id, jsonObject);

			}

			JSONObject props = idToPropsCache.get(id).getJSONObject(
					AlfrescoCommunicationConstants.KEY_PROPERTIES);
			JSONArray aspects = idToPropsCache.get(id).getJSONArray("aspects");
			boolean contained = false;
			for (int i = 0; i < aspects.length(); i++) {
				Pair<String, Serializable> convertDMSToCMFProperty = mockupDMSTypeConverter
						.convertDMSToCMFProperty(aspects.getString(i), "",
								DMSTypeConverter.PROPERTIES_MAPPING);
				if (convertDMSToCMFProperty != null
						&& aspectId.contains(convertDMSToCMFProperty.getFirst())) {
					contained = true;
					break;
				}

			}
			assertTrue(contained, "Searched by aspect should return the same aspect");
			String date = props.getString(propId);
			Date parsed = ISO8601DateFormat.parse(date);
			extracted.add(parsed);
		}

		LinkedList<Comparable> sortable = new LinkedList<>(extracted);
		Collections.sort(sortable, comparator);
		assertEquals(extracted, sortable, "Sorting is not correct");
	}
}
