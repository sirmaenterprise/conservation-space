package com.sirma.itt.seip.eai.content.tool.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.util.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.sirma.itt.seip.eai.content.tool.BaseTest;
import com.sirma.itt.seip.eai.content.tool.exception.EAIException;
import com.sirma.itt.seip.eai.content.tool.params.ParametersProvider;
import com.sirma.itt.seip.eai.content.tool.service.io.LocalFileService;

import javafx.concurrent.Worker.State;

/**
 * Tests for {@link SpreadsheetProcessorTask}.
 * 
 * @author bbanchev
 */
public class SpreadsheetProcessorTaskTest extends BaseTest {
	static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().port(11999));

	private static File instanceDirectory;
	private Set<File> workingDirs = new HashSet<>();

	@Before
	public void setUp() throws Exception {
		// create params map
		Map<String, String> paramsNamed = new HashMap<>();
		paramsNamed.put(ParametersProvider.PARAM_API_URL, "http://0.0.0.0:11999");
		paramsNamed.put(ParametersProvider.PARAM_AUTHORIZATION, "header");
		paramsNamed.put(ParametersProvider.PARAM_CONTENT_URI, "emf:uri");
		initParameters(paramsNamed);
		instanceDirectory = LocalFileService.INSTANCE.getInstanceDirectory();
	}

	@Test
	public void testValid() throws Exception {
		// create the tested task
		SpreadsheetProcessorTask task = prepareValidService(new File(storage, "TEST_DOCUMENTS"),
				new File(storage, "internal"));
		copySource("emfuri.xlsx", "emfuri.xlsx");
		task.call();
		assertEquals(State.READY, task.getState());
		UploadbleStatusReport uploadReport = getUploadReport(task);
		assertEquals(0, uploadReport.getFileDuplications().size());
		assertEquals(0, uploadReport.getContentDuplications().size());
		assertEquals(6, uploadReport.getFilesForUpload().size());
		assertEquals(1, uploadReport.getMissingFiles().size());
		assertEquals(3, uploadReport.getNotSetContentFiles().size());
		assertEquals(0, uploadReport.getUnmodifiedFiles().size());
		Set<String> requestUrls = getRequestUrls();
		// upload content request is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/content/"));
		// checksum request
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/content/info"));
		// upload new version of content for given instance with id emf:uri is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/instances/emf:uri/content/"));
		// update request given instance with id emf:uri is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/instance/emf:uri/integration/actions/createOrUpdate"));
	}

	@Test
	public void testValidWithRemoteChecksum() throws Exception {
		// create the tested task
		SpreadsheetProcessorTask task = prepareValidService(new File(storage, "TEST_DOCUMENTS"),
				new File(storage, "internal"));
		wireMockRule.stubFor(post(urlEqualTo("/content/info"))
				.willReturn(aResponse().withHeader("Content-Type", "application/vnd.seip.v2+json").withBody(
						"{\"emf:contentId1\":{\"size\":11111},\"emf:contentId2\":{\"size\":3733}}")));
		copySource("emfuri.xlsx", "emfuri.xlsx");
		task.call();
		assertEquals(State.READY, task.getState());
		UploadbleStatusReport uploadReport = getUploadReport(task);
		assertEquals(0, uploadReport.getFileDuplications().size());
		assertEquals(0, uploadReport.getContentDuplications().size());
		assertEquals(5, uploadReport.getFilesForUpload().size());
		assertEquals(1, uploadReport.getMissingFiles().size());
		assertEquals(3, uploadReport.getNotSetContentFiles().size());
		assertEquals(1, uploadReport.getUnmodifiedFiles().size());

		Set<String> requestUrls = getRequestUrls();
		// upload content request is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/content/"));
		// checksum request
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/content/info"));
		// upload new version of content for given instance with id emf:uri is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/instances/emf:uri/content/"));
		// update request given instance with id emf:uri is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/instance/emf:uri/integration/actions/createOrUpdate"));
	}

	/**
	 * For some currently unknown reason some servers report file sizes with offset of 2 bytes that breaks the general
	 * equality check.
	 */
	@Test
	public void testValidWithRemoteChecksum_diff2() throws Exception {
		// create the tested task
		SpreadsheetProcessorTask task = prepareValidService(new File(storage, "TEST_DOCUMENTS"),
				new File(storage, "internal"));
		wireMockRule.stubFor(post(urlEqualTo("/content/info"))
				.willReturn(aResponse().withHeader("Content-Type", "application/vnd.seip.v2+json").withBody(
						"{\"emf:contentId1\":{\"size\":11111},\"emf:contentId2\":{\"size\":3735}}")));
		copySource("emfuri.xlsx", "emfuri.xlsx");
		task.call();
		assertEquals(State.READY, task.getState());
		UploadbleStatusReport uploadReport = getUploadReport(task);
		assertEquals(0, uploadReport.getFileDuplications().size());
		assertEquals(0, uploadReport.getContentDuplications().size());
		assertEquals(5, uploadReport.getFilesForUpload().size());
		assertEquals(1, uploadReport.getMissingFiles().size());
		assertEquals(3, uploadReport.getNotSetContentFiles().size());
		assertEquals(1, uploadReport.getUnmodifiedFiles().size());

		Set<String> requestUrls = getRequestUrls();
		// upload content request is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/content/"));
		// checksum request
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/content/info"));
		// upload new version of content for given instance with id emf:uri is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/instances/emf:uri/content/"));
		// update request given instance with id emf:uri is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/instance/emf:uri/integration/actions/createOrUpdate"));
	}

	@Test(expected = EAIException.class)
	public void testLocalInvalidMimetype() throws Exception {
		copySource("emfuri.xlsx", "emfuri.pdf");
		SpreadsheetProcessorTask task = prepareValidService(new File(storage, "TEST_DOCUMENTS"),
				new File(storage, "internal"));
		task.call();
	}

	@Test
	public void testDownloadRemote() throws Exception {
		SpreadsheetProcessorTask task = prepareValidService(new File(storage, "TEST_DOCUMENTS"),
				new File(storage, "internal"));
		ByteArrayOutputStream source = copySource("emfuri.xlsx", new ByteArrayOutputStream());
		wireMockRule.stubFor(get(urlEqualTo("/instances/emf:uri/content?download=true")).willReturn(aResponse()
				.withHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
					.withHeader("Content-Disposition", "attachment; filename=\"file.xlsx\";")
					.withBody(source.toByteArray())));
		task.call();
		assertEquals(State.READY, task.getState());

		UploadbleStatusReport uploadReport = getUploadReport(task);
		assertEquals(0, uploadReport.getFileDuplications().size());
		assertEquals(0, uploadReport.getContentDuplications().size());
		assertEquals(6, uploadReport.getFilesForUpload().size());
		assertEquals(1, uploadReport.getMissingFiles().size());
		assertEquals(3, uploadReport.getNotSetContentFiles().size());
		assertEquals(0, uploadReport.getUnmodifiedFiles().size());

		Set<String> requestUrls = getRequestUrls();
		// upload content request is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/content/"));
		// checksum request
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/content/info"));
		// upload new version of content for given instance with id emf:uri is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/instances/emf:uri/content/"));
		// update request given instance with id emf:uri is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/instance/emf:uri/integration/actions/createOrUpdate"));
	}

	@Test(expected = EAIException.class)
	public void testDownloadRemoteFailed() throws Exception {
		SpreadsheetProcessorTask task = prepareValidService(new File(storage, "TEST_DOCUMENTS"),
				new File(storage, "internal"));
		wireMockRule.stubFor(
				get(urlEqualTo("/instances/emf:uri/content?download=true")).willReturn(aResponse().withStatus(500)));
		task.call();
	}

	@Test(expected = EAIException.class)
	public void testDownloadRemoteFailedInvalidMimetype() throws Exception {
		ByteArrayOutputStream byteArrayOutputStream = copySource("emfuri.xlsx", new ByteArrayOutputStream());
		SpreadsheetProcessorTask task = prepareValidService(new File(storage, "TEST_DOCUMENTS"),
				new File(storage, "internal"));
		wireMockRule.stubFor(get(urlEqualTo("/instances/emf:uri/content?download=true")).willReturn(aResponse()
				.withHeader("Content-Type", "application/pdf")
					.withHeader("Content-Disposition", "attachment; filename=\"file.pdf\";")
					.withBody(byteArrayOutputStream.toByteArray())));
		task.call();
	}

	private SpreadsheetProcessorTask prepareValidService(File... storages) throws Exception {
		// create the tested task
		for (File file : storages) {
			workingDirs.add(file);
			ArchiveUtil.unZip(SpreadsheetProcessorTaskTest.class.getResourceAsStream(file.getName() + ".zip"), file);
		}

		wireMockRule.stubFor(post(urlEqualTo("/content/"))
				.willReturn(aResponse().withHeader("Content-Type", "application/vnd.seip.v2+json").withBody(
						"{\"emf:contentId\":\"emf:contentId\",\"name\":\"emfuri.xlsx\",\"mimetype\":\"any\",\"size\":0}")));
		wireMockRule.stubFor(post(urlEqualTo("/instances/emf:uri/content/"))
				.willReturn(aResponse().withHeader("Content-Type", "application/vnd.seip.v2+json").withBody(
						"{\"emf:contentId\":\"emf:contentId\",\"name\":\"emfuri.xlsx\",\"mimetype\":\"any\",\"size\":0}")));
		wireMockRule.stubFor(post(urlEqualTo("/instance/emf:uri/integration/actions/createOrUpdate"))
				.willReturn(aResponse().withHeader("Content-Type", "application/vnd.seip.v2+json").withBody(
						"{\"emf\\:contentId\":\"content\"}")));
		wireMockRule.stubFor(post(urlEqualTo("/content/info"))
				.willReturn(aResponse().withHeader("Content-Type", "application/vnd.seip.v2+json").withBody(
						"{\"emfuri\":{\"size\":1111}}")));

		return new SpreadsheetProcessorTask(workingDirs) {

			@Override
			protected void updateMessage(String message) {
				LOGGER.debug("Update message " + message);
			}

			@Override
			protected void updateProgress(double workDone, double max) {
				LOGGER.debug("Update progress " + workDone + "/" + max);
			}
		};

	}

	@Test
	public void testValidWithDuplicates() throws Exception {
		// create the tested task
		SpreadsheetProcessorTask task = prepareValidService(new File(storage, "TEST_DOCUMENTS"),
				new File(storage, "TEST_DOCUMENTS_2"), new File(storage, "internal"));
		copySource("emfuri.xlsx", "emfuri.xlsx");
		task.call();
		assertEquals(State.READY, task.getState());
		UploadbleStatusReport uploadReport = getUploadReport(task);
		assertEquals(1, uploadReport.getFileDuplications().size());
		assertEquals(0, uploadReport.getContentDuplications().size());
		assertEquals(5, uploadReport.getFilesForUpload().size());
		assertEquals(1, uploadReport.getMissingFiles().size());
		assertEquals(3, uploadReport.getNotSetContentFiles().size());
		assertEquals(0, uploadReport.getUnmodifiedFiles().size());
		Set<String> requestUrls = getRequestUrls();
		// upload content request is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/content/"));
		// checksum request
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/content/info"));
		// upload new version of content for given instance with id emf:uri is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/instances/emf:uri/content/"));
		// update request given instance with id emf:uri is send
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/instance/emf:uri/integration/actions/createOrUpdate"));
	}

	@Test(expected = EAIException.class)
	public void testCallOnInvalidFile() throws Exception {
		SpreadsheetProcessorTask task = prepareValidService();
		copySource("nofilename.xlsx", "nofilename.xlsx");
		task.call();
	}

	@Test
	public void testRemainingTime() {
		long[] times = SpreadsheetProcessorTask.splitToComponentTimes(5 * 3600 + 46 * 60 + 34);
		assertArrayEquals(new long[] { 5, 46, 34 }, times);
	}

	private Set<String> getRequestUrls() {
		Set<String> urls = new HashSet<>();
		for (ServeEvent event : wireMockRule.getAllServeEvents()) {
			urls.add(event.getRequest().getAbsoluteUrl());
		}
		return urls;
	}

	private UploadbleStatusReport getUploadReport(SpreadsheetProcessorTask task) throws Exception {
		Field uploadReport = SpreadsheetProcessorTask.class.getDeclaredField("uploadReport");
		uploadReport.setAccessible(true);
		return (UploadbleStatusReport) uploadReport.get(task);
	}

	private FileOutputStream copySource(String src, String filename) throws IOException, FileNotFoundException {
		try (FileOutputStream out = new FileOutputStream(new File(instanceDirectory, filename))) {
			IOUtils.copy(SpreadsheetProcessorTaskTest.class.getResourceAsStream(src), out);
			return out;
		}
	}

	private ByteArrayOutputStream copySource(String src, ByteArrayOutputStream out) throws IOException {
		IOUtils.copy(SpreadsheetProcessorTaskTest.class.getResourceAsStream(src), out);
		return out;
	}

	@After
	public void tearDown() throws Exception {
		try {
			clearDirectory(instanceDirectory);
		} finally {
			wireMockRule.stop();
			wireMockRule.shutdownServer();
		}
	}

}
