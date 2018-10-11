package com.sirma.itt.emf.audit.export;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.emf.audit.TestUtils;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.SearchServiceImpl;

import de.akquinet.jbosscc.needle.annotation.InjectInto;
import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Testing the export functionality of the audit activities.
 *
 * @author Nikolay Velkov
 * @author Vilizar Tsonev
 */
public class AuditExportServiceImplTest {

	/** The activities. */
	private static List<AuditActivity> activities;

	@Rule
	public NeedleRule needleRule = new NeedleRule();
	/** The export service. */
	@ObjectUnderTest(id = "aes", implementation = AuditExportServiceImpl.class)
	private AuditExportService exportService;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MM yyyy HH:mm:ss");

	@InjectInto(targetComponentId = "aes")
	private final String path = "src/test/resources";

	@InjectInto(targetComponentId = "aes")
	private SearchService searchService = EasyMock.createMock(SearchServiceImpl.class);

	@Inject
	private DateConverter dateConverter;

	private String lastCreatedFile;

	/**
	 * Creates a list of {@link AuditActivity} objects to be used in the export tests.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
		activities = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			AuditActivity activity = new AuditActivity();
			String iStr = Integer.toString(i);
			activity.setActionID(iStr);
			if (i < 5) {
				activity.setContext("unique:Project Project title (Project)");
			} else {
				activity.setContext("unique:Case Case title (Case)");
			}
			activity.setDateReceived(new Date());
			activity.setEventDate(new Date());
			activity.setObjectID(iStr);
			activity.setObjectPreviousState(iStr);
			activity.setObjectState(iStr);
			activity.setObjectSubType(iStr);
			activity.setObjectSystemID(iStr);
			activity.setObjectTitle(iStr);
			activity.setObjectType(iStr);
			activity.setObjectURL(iStr);
			activity.setUserName(iStr);

			activities.add(activity);
		}
	}

	@Before
	public void beforeMethod() {
		EasyMock.expect(dateConverter.getSystemFullFormat()).andReturn(dateFormatter).anyTimes();
		EasyMock.replay(dateConverter);
	}

	/**
	 * Tests if the csv file is properly generated.
	 *
	 * @throws IOException
	 *             if a problem occurs while exporting
	 * @throws JSONException
	 */
	@Test
	public void testExportAsCsv() throws IOException, JSONException {
		List<Instance> mockedInstances = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			mockedInstances.add(mockProjectInstance(i));
		}
		for (int i = 5; i < 10; i++) {
			mockedInstances.add(mockCaseInstance(i));
		}
		mockSearchService(mockedInstances);
		JSONArray columns = new JSONArray("[{id:'eventDate',name:'Event Date'}, {id:'context', name:'Context'}]");
		File csvFile = exportService.exportAsCsv(activities, "", columns);
		lastCreatedFile = csvFile.getName();
		try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
			// skip the first three lines to go to the actual data
			reader.readLine();
			reader.readLine();
			reader.readLine();
			String line;
			int index = 0;
			while ((line = reader.readLine()) != null) {
				if (index < 4) {
					assertEquals(line, "\"" + dateFormatter.format(activities.get(index).getEventDate())
							+ "\";\"unique:Project Project title (Project)\"");

				} else {
					assertEquals(line, "\"" + dateFormatter.format(activities.get(index).getEventDate())
							+ "\";\"unique:Case Case title (Case)\"");
				}
				index++;
			}
		} finally {
			csvFile.delete();
		}
	}

	/**
	 * Test export as pdf.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws JSONException
	 */
	@Test
	public void testExportAsPdf() throws IOException, JSONException {
		JSONArray columns = new JSONArray("[{id:'action',name:'Action'},{id:'eventDate',name:'Event Date'}]");
		File file = exportService.exportAsPdf(activities, "", columns);
		lastCreatedFile = file.getName();
		try {
			Assert.assertTrue(file.exists() && !file.isDirectory());
		} finally {
			assertTrue(file.delete());
		}
	}

	/**
	 * Mock the context map, used for retrieving the contexts of all objects.
	 *
	 * @param mockedInstances
	 *            the mocked instances, that are going to be returned when searching.
	 */
	private void mockSearchService(List<Instance> mockedInstances) {
		final List<Instance> results = new ArrayList<>();
		results.addAll(mockedInstances);
		final Capture<SearchArguments<Instance>> searchArgs = new Capture<>();
		final Capture<Class<?>> instanceClass = new Capture<>();
		searchService.searchAndLoad(EasyMock.capture(instanceClass), EasyMock.capture(searchArgs));

		EasyMock.expectLastCall().andAnswer(() -> {
			searchArgs.getValue().setResult(results);
			return null;
		});
		EasyMock.replay(searchService);
	}

	/**
	 * Mock a project instance.
	 *
	 * @param id
	 *            the id of the instance
	 * @return the project instance
	 */
	private static Instance mockProjectInstance(int id) {
		Instance instance = new EmfInstance();
		instance.setId(id);
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.UNIQUE_IDENTIFIER, "unique:Project");
		properties.put(DefaultProperties.TITLE, "Project title");
		instance.setProperties(properties);
		return instance;
	}

	/**
	 * Mock a case instance.
	 *
	 * @param id
	 *            the id of the instance
	 * @return the case instance
	 */
	private static Instance mockCaseInstance(int id) {
		Instance instance = new EmfInstance();
		instance.setId(id);
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.UNIQUE_IDENTIFIER, "unique:Case");
		properties.put(DefaultProperties.TITLE, "Case title");
		instance.setProperties(properties);
		return instance;
	}

	/**
	 * Delete the lastly created file.
	 */
	@After
	public void deleteFiles() {
		TestUtils.deleteFile(path + "/" + lastCreatedFile);
	}
}
