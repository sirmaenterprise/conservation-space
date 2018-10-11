package com.sirma.itt.seip.eai.content.tool.service;

import static com.sirma.itt.seip.eai.content.tool.model.EAIContentConstants.PRIMARY_CONTENT_ID;
import static com.sirma.itt.seip.eai.content.tool.params.ParametersProvider.PARAM_API_URL;
import static com.sirma.itt.seip.eai.content.tool.params.ParametersProvider.PARAM_AUTHORIZATION;
import static com.sirma.itt.seip.eai.content.tool.params.ParametersProvider.PARAM_CONTENT_URI;
import static com.sirma.itt.seip.eai.content.tool.service.io.LocalFileService.deleteFile;
import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.SocketPermission;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.eai.content.tool.exception.EAIException;
import com.sirma.itt.seip.eai.content.tool.model.ContentInfo;
import com.sirma.itt.seip.eai.content.tool.model.EAIContentConstants;
import com.sirma.itt.seip.eai.content.tool.model.ErrorBuilderProvider;
import com.sirma.itt.seip.eai.content.tool.model.SpreadsheetEntryId;
import com.sirma.itt.seip.eai.content.tool.model.SpreadsheetSheet;
import com.sirma.itt.seip.eai.content.tool.params.ParametersProvider;
import com.sirma.itt.seip.eai.content.tool.service.io.LocalFileService;
import com.sirma.itt.seip.eai.content.tool.service.net.FormRequestSender;
import com.sirma.itt.seip.eai.content.tool.service.net.GetRequestSender;
import com.sirma.itt.seip.eai.content.tool.service.net.PayloadRequestSender;
import com.sirma.itt.seip.eai.content.tool.service.net.RequestSender;
import com.sirma.itt.seip.eai.content.tool.service.net.URIBuilder;
import com.sirma.itt.seip.eai.content.tool.service.reader.EAISpreadsheetParser;
import com.sirma.itt.seip.eai.content.tool.service.reader.EAISpreadsheetParserFactory;
import com.sirma.itt.seip.eai.content.tool.service.writer.EAISpreadsheetWriterFactory;

import javafx.concurrent.Task;

/**
 * The SpreadsheetProcessorTask is the main service task for uploading and spreadsheet content processing. The task
 * collects the needed for upload files and intersect them with the provided sources. All valid files are uploaded and
 * content id is set in the spreadsheet.
 *
 * @author bbanchev
 */
public class SpreadsheetProcessorTask extends Task<Void> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final ExecutorService TASK_EXECUTOR = Executors.newFixedThreadPool(5);
	private static final String DOCUMENT_TYPE = "emf:Document";
	private static final String IMAGE_TYPE = "emf:Image";

	private Set<File> userSelection;
	private URI apiURL;
	private String instanceId;
	private File localSpreadsheetFile;
	private File uploadResultStorage;
	private File instanceStorageDir;
	private List<File> cachedEntities = new LinkedList<>();
	private UploadbleStatusReport uploadReport = new UploadbleStatusReport();

	private enum SpreadsheetSupport {
		XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
				".xlsx"), XLS("application/vnd.ms-excel", ".xls"), CSV("text/csv", ".csv");
		private final String mimetype;
		private final String extension;

		SpreadsheetSupport(String mimetype, String extension) {
			this.mimetype = mimetype;
			this.extension = extension;
		}

		public String getMimetype() {
			return mimetype;
		}

		public String getExtension() {
			return extension;
		}

		static Optional<SpreadsheetSupport> detectByMimetype(String mimetype) {
			if (mimetype == null) {
				return Optional.empty();
			}
			for (SpreadsheetSupport type : values()) {
				if (mimetype.startsWith(type.mimetype)) {
					return Optional.of(type);
				}
			}
			return Optional.empty();
		}

		static Optional<SpreadsheetSupport> detectByExtension(File file) {
			if (file == null) {
				return Optional.empty();
			}
			for (SpreadsheetSupport type : values()) {
				if (file.getName().endsWith(type.extension)) {
					return Optional.of(type);
				}
			}
			return Optional.empty();
		}

	}

	/**
	 * Creates the javafx task and initialize all needed properties.
	 *
	 * @param selected
	 *            the selected directories and files to search under for uploadable files
	 */
	public SpreadsheetProcessorTask(Set<File> selected) {
		this.userSelection = selected == null ? new HashSet<>(0) : selected;
		this.apiURL = URI.create(ParametersProvider.get(PARAM_API_URL));
		this.instanceId = ParametersProvider.get(PARAM_CONTENT_URI);
		initializeStorage();
		if (System.getSecurityManager() != null) {
			System.getSecurityManager().checkPermission(new SocketPermission(apiURL.getHost() + ":" + apiURL.getPort(),
					"accept, connect, listen, resolve"));
		}
	}

	private void initializeStorage() {
		this.instanceStorageDir = LocalFileService.INSTANCE.getInstanceDirectory();
		this.uploadResultStorage = LocalFileService.createDirectory(instanceStorageDir, ".cache");
	}

	@Override
	protected Void call() throws Exception {
		try {
			updateMessage("Starting spreadsheet processing on '" + instanceId + "'");
			updateProgress(INDETERMINATE_PROGRESS, INDETERMINATE_PROGRESS);
			SpreadsheetSheet spreadsheet = loadSpreadsheet();
			processSpreadsheet(spreadsheet);
			completeSpreadsheetProcessing(spreadsheet);
			return null;
		} catch (IOException e) {
			LOGGER.error("Request failed with details: " + e.getMessage(), e);
			throw e;
		} catch (EAIException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			LOGGER.error("Spreadsheet processing has failed with details: '" + e.getMessage()
					+ "'! Check log for more details!", e);
			throw e;
		}
	}

	private void completeSpreadsheetProcessing(SpreadsheetSheet spreadsheet) throws EAIException, IOException {
		ErrorBuilderProvider errorAppender = uploadReport.getErrorAppender();
		boolean hasIncompleteImports = uploadReport.hasIncompleteImports();
		boolean hasErrors = errorAppender.hasErrors();
		boolean hasUpdates = !uploadReport.getProcessed().isEmpty() || hasIncompleteImports;
		// if there was anything to upload
		boolean hasToUpload = !uploadReport.getFilesForUpload().isEmpty();
		logOperationStatus(hasErrors, hasUpdates, hasToUpload, hasIncompleteImports);
		if (hasUpdates) {
			storeAndUploadSpreadsheet(spreadsheet);
		}
		if (!hasErrors) {
			clearStorage();
		} else {
			throw new EAIException("Operation completed with error/s during processing:\n" + errorAppender.toString());
		}
	}

	private static void logOperationStatus(boolean hasErrors, boolean hasUpdates, boolean hasToUpload,
			boolean hasIncompleteImports) {
		if (!hasToUpload && !hasErrors) {
			LOGGER.info("Folder/file selection does not affect spreadsheet. Skipping update!");
		}
		if (hasUpdates) {
			if (hasIncompleteImports) {
				LOGGER.info("Spreadsheet had pending local changes. Attempting spreadsheet upload...");
			} else if (hasErrors) {
				LOGGER.error("Partial content import is executed. Attempting spreadsheet upload...");
			} else {
				LOGGER.info(
						"Successfully stored results after spreadsheet processing without problems. Attempting spreadsheet upload...");
			}
		}
	}

	private void storeAndUploadSpreadsheet(SpreadsheetSheet spreadsheet) throws IOException, EAIException {
		updateMessage("Saving results...");
		EAISpreadsheetWriterFactory.getWriter(spreadsheet).writerEntries(spreadsheet, localSpreadsheetFile);
		URI spreadsheetContentUpdateURI = new URIBuilder(apiURL)
				.append("/instances/")
					.append(instanceId)
					.append("/content/")
					.build();
		// first we upload new content for existing instance and get the contentId
		String contentId = uploadFile(localSpreadsheetFile, DOCUMENT_TYPE, spreadsheetContentUpdateURI);
		clearInternalCache();
		// we update instance contentId with the new one
		updateMessage("Uploading spreadsheet to the remote server...");
		updateDataSource(localSpreadsheetFile, contentId);
		LOGGER.info("Successfully uploaded spreadsheet to the remote server!");
	}

	private void clearInternalCache() {
		for (File file : cachedEntities) {
			LocalFileService.deleteFile(file);
		}
	}

	private void clearStorage() {
		// now it is time to delete the temporary storage and the
		deleteFile(localSpreadsheetFile);
		deleteFile(uploadResultStorage);
		deleteFile(instanceStorageDir);
	}

	private SpreadsheetSheet loadSpreadsheet() throws IOException, EAIException {
		ContentInfo content = loadContent();
		updateMessage("Parsing spreadsheet...");
		EAISpreadsheetParser parser = EAISpreadsheetParserFactory.getParser(content);
		return parser.parseEntries(content, null);
	}

	private void updateDataSource(File updatedFile, String contentId) throws IOException {
		// execute integration service to provide runAs
		URI updateUrl = new URIBuilder(apiURL)
				.append("/instance/")
					.append(ParametersProvider.get(PARAM_CONTENT_URI))
					.append("/integration/actions/createOrUpdate")
					.build();
		JsonObject payloadJson = Json
				.createObjectBuilder()
					.add("userOperation", "uploadNewVersion")
					.add("targetInstance",
							Json.createObjectBuilder().add("properties",
									Json
											.createObjectBuilder()
												.add(PRIMARY_CONTENT_ID, contentId)
												.add("name", updatedFile.getName())
												.add("size", updatedFile.length())))
					.build();
		Map<String, String> headers = new HashMap<>(3, 1);
		headers.put(EAIContentConstants.HEADER_AUTHORIZATION, ParametersProvider.get(PARAM_AUTHORIZATION));
		headers.put(EAIContentConstants.HEADER_ACCEPT, EAIContentConstants.MIMETYPE_APPLICATION_VND_SEIP_V2_JSON);
		headers.put(EAIContentConstants.HEADER_CONTENT_TYPE, EAIContentConstants.MIMETYPE_APPLICATION_VND_SEIP_V2_JSON);
		PayloadRequestSender networkService = new PayloadRequestSender(updateUrl, EAIContentConstants.METHOD_POST);
		networkService.init(headers).appendRequestPayload(payloadJson.toString());
		networkService.send();
	}

	@SuppressWarnings("resource")
	private ContentInfo loadContent() throws IOException, EAIException {
		updateMessage("Loading spreadsheet with id '" + instanceId + "'");
		File[] files = instanceStorageDir.listFiles(File::isFile);
		if (files != null && files.length > 0) {
			this.localSpreadsheetFile = files[0];
		}
		if (localSpreadsheetFile != null && localSpreadsheetFile.canRead() && localSpreadsheetFile.length() > 0) {
			String mimetype = SpreadsheetSupport
					.detectByExtension(localSpreadsheetFile)
						.orElseThrow(() -> new EAIException(
								"Could not detect proper type of local sheet instance with id: " + instanceId))
						.getMimetype();
			return new ContentInfo(mimetype, localSpreadsheetFile.toURI(),
					new BufferedInputStream(new FileInputStream(localSpreadsheetFile)));
		}
		updateMessage("Downloading spreadsheet with id '" + instanceId + "' from server...");
		ContentInfo content = downloadContent();
		String extension = SpreadsheetSupport
				.detectByMimetype(content.getMimetype())
					.orElseThrow(() -> new EAIException(
							"Could not detect proper type of remote sheet instance with id: " + instanceId))
					.getExtension();
		String detectedName = Objects.toString(content.getName(), "");
		boolean isFullName = detectedName.endsWith(extension);
		localSpreadsheetFile = LocalFileService.createFile(instanceStorageDir,
				isFullName ? detectedName : instanceStorageDir.getName(), isFullName ? "" : extension);
		return content;
	}

	private ContentInfo downloadContent() throws EAIException {
		try {
			RequestSender downloadRequest = new GetRequestSender(buildDownloadURI(instanceId))
					.init(Collections.singletonMap(EAIContentConstants.HEADER_AUTHORIZATION,
							ParametersProvider.get(PARAM_AUTHORIZATION)));
			ContentInfo contentInfo = downloadRequest.send();
			// extract the filename if available
			List<String> dispositions = downloadRequest.getResponseHeaders().get("Content-Disposition");
			if (dispositions != null && dispositions.size() == 1) {
				String fileName = dispositions.get(0).replaceFirst("(?i)^.*filename=\"([^\"]+)\".*$", "$1");
				contentInfo.setName(fileName);
			}
			return contentInfo;
		} catch (Exception e) {
			throw new EAIException("Unable to load spreadsheet from remote server!", e);
		}
	}

	private URI buildDownloadURI(String uri) {
		return new URIBuilder(apiURL).append("/instances/").append(uri).append("/content?download=true").build();
	}

	private void processSpreadsheet(SpreadsheetSheet spreadsheetSheets) throws IOException {
		LOGGER.debug("Starting scan of directories: {}", userSelection);
		updateMessage("Detecting uploadable files...");
		generateStatusReport(spreadsheetSheets);
		updateMessage("Uploaded 0/" + uploadReport.getFilesForUpload().size() + " files");
		final AtomicLong uploadedBytes = new AtomicLong(0);
		final AtomicInteger uploadedCount = new AtomicInteger(0);
		final long startTime = System.currentTimeMillis();
		List<Future<ContentEntry>> futures = uploadReport
				.getFilesForUpload()
				.entrySet()
				.stream()
				.map(entry -> doProcessSpreadsheetEntryCallable(entry, uploadedBytes, uploadedCount, startTime))
				.map(Executors::privilegedCallable)
				// now split in parallel on user machine and limit to TASK_EXECUTOR pool size
				.map(SpreadsheetProcessorTask::submitProcessEntryCallable)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		// after submitting all tasks for upload, wait here for all to complete before continue
		uploadReport.setProcessed(waitForAll(futures));
	}

	private List<ContentEntry> waitForAll(Collection<Future<ContentEntry>> futures) {
		LinkedList<Future<?>> copy = new LinkedList<>(futures);
		while (!copy.isEmpty()) {
			sleepForAWhile();
			copy.removeIf(Future::isDone);
		}
		return futures.stream().map(future -> {
			try {
				return future.get();
			} catch (InterruptedException e) {
				LOGGER.error("Task canceled during task processing!", e);
				return null;
			} catch (ExecutionException e) {
				LOGGER.error("Failure during task processing!", e);
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	private static void sleepForAWhile() {
		try {
			// we will wait some time before first next check
			Thread.sleep(10);
		} catch (InterruptedException e) {
			LOGGER.trace("", e);
			// not interested
		}
	}

	private Callable<ContentEntry> doProcessSpreadsheetEntryCallable(Entry<ContentEntry, File> entry,
			final AtomicLong uploadedBytes, AtomicInteger numberOfProcessed, long startTime) {
		return () -> {
			ContentEntry contentEntry = processContentEntryUpload(entry.getKey());
			// we update the progress after the file upload so it will have correct upload rate
			updateProgress(uploadedBytes.addAndGet(entry.getValue().length()), uploadReport.getTotalUploadSize());
			long rate = calculateRate(uploadedBytes, startTime);
			long remainingKBytes = (uploadReport.getTotalUploadSize() - uploadedBytes.get()) / 1000;
			long remainingSeconds = remainingKBytes / rate;
			long[] timeComponents = splitToComponentTimes(remainingSeconds);
			updateMessage(String.format("Uploaded %d/%d files (%d KB/s) remaining time %2d:%2d:%2d", numberOfProcessed.incrementAndGet(),
					uploadReport.getFilesForUpload().size(), rate, timeComponents[0], timeComponents[1], timeComponents[2]));
			return contentEntry;
		};
	}

	static long[] splitToComponentTimes(long seconds) {
		long hours = seconds / 3600;
		long remainder = seconds - hours * 3600;
		long mins = remainder / 60;
		remainder = remainder - mins * 60;
		long secs = remainder;
		return new long[] {hours , mins , secs};
	}

	private long calculateRate(AtomicLong jobSize, long startTime) {
		// use doubles for calculations not to have devision by zero if the time is less than a second
		return (long) ((jobSize.get() / 1000.0) / ((System.currentTimeMillis() - startTime) / 1000.0));
	}

	private static Future<ContentEntry> submitProcessEntryCallable(Callable<ContentEntry> future) {
		try {
			// returning the value here will mean to wait for the task to finish during scheduling
			// and we will loose the parallle processing. this is way here we return futures and then wait for their results
			return TASK_EXECUTOR.submit(future);
		} catch (Exception e) {
			LOGGER.error("Failure during task processing!", e);
			return null;
		}
	}

	private static void setContentIdForEntry(ContentEntry uploadContentEntry, String contentId) {
		if (contentId != null && uploadContentEntry.getSource() != null) {
			uploadContentEntry.getSource().getProperties().put(PRIMARY_CONTENT_ID, contentId);
		}
	}

	private void generateStatusReport(SpreadsheetSheet spreadsheetSheets) throws IOException {
		List<ContentEntry> contentEntries = spreadsheetSheets.getEntries().stream().map(ContentEntry::new).collect(
				Collectors.toCollection(ArrayList::new));
		List<ContentEntry> requestedContents = contentEntries.stream().filter(ContentEntry::hasContent).collect(
				Collectors.toCollection(ArrayList::new));
		// remove content entries with set content
		contentEntries.removeAll(requestedContents);
		uploadReport.getNotSetContentFiles().addAll(contentEntries);
		detectIncompleteImports(requestedContents);

		Map<ContentEntry, File> uploadableSet = collectDirectoryContent(requestedContents);
		Map<ContentEntry, Long> localChecksums = getLocalContentFingerpint(uploadableSet);
		Map<ContentEntry, Long> remoteCheckSums = getRemoteContentFingerpint(localChecksums);

		for (ContentEntry uploadContentEntry : requestedContents) {
			// already processed duplication
			if (uploadReport.getFileDuplications().get(uploadContentEntry) != null) {
				continue;
			}
			if (!uploadableSet.containsKey(uploadContentEntry)) {
				// use the original requested name
				uploadReport.getMissingFiles().put(uploadContentEntry, uploadContentEntry.getContentName());
			} else {
				File requestedFile = uploadContentEntry.getContentSource();
				if (!uploadContentEntry.isExisting() || remoteCheckSums.get(uploadContentEntry) == null
						|| hasChanges(remoteCheckSums.get(uploadContentEntry), localChecksums.get(uploadContentEntry))) {
					LOGGER.debug("Checksum: local {}, remote {}. File will be uploaded: {}", localChecksums.get(uploadContentEntry),
							remoteCheckSums.get(uploadContentEntry), uploadContentEntry.getContentName());
					uploadReport.getFilesForUpload().put(uploadContentEntry, requestedFile);
				} else if (uploadContentEntry.isExisting()) {
					LOGGER.debug("File not modified and will not be uploaded: {}", uploadContentEntry.getContentName());
					uploadReport.getUnmodifiedFiles().put(uploadContentEntry, requestedFile);
				}
			}
		}
		logUploadReport();
	}

	private static boolean hasChanges(Long remote, Long local) {
		if (remote == null || local == null) {
			return true;
		}
		// For some currently unknown reason some servers report file sizes with offset of 2 bytes
		long diff = remote - local;
		return diff != 2 && diff != 0;
	}

	private Map<ContentEntry, File> collectDirectoryContent(List<ContentEntry> source) {
		Map<ContentEntry, File> processableContent = new HashMap<>();
		for (File file : userSelection) {
			if (file.isFile()) {
				processFile(source, processableContent, file, null);
			} else if (file.isDirectory()) {
				iterateDirectoryContent(source, file, processableContent, null);
			}
		}
		return processableContent;
	}

	private void iterateDirectoryContent(List<ContentEntry> primaryContentFiles, File parent,
			Map<ContentEntry, File> processableContent, File iterationPath) {
		File[] children;
		if (parent == null || (children = parent.listFiles()) == null) {
			LOGGER.debug("Skipped folder '{}' with no children!", parent);
			return;
		}
		for (File file : children) {
			if (file.isFile()) {
				processFile(primaryContentFiles, processableContent, file, iterationPath);
			} else if (file.isDirectory()) {
				iterateDirectoryContent(primaryContentFiles, file, processableContent,
						new File(iterationPath, file.getName()));
			}
		}
	}

	private void processFile(List<ContentEntry> requested, Map<ContentEntry, File> processableContent, File file,
			File iterationPath) {

		File relativeFile = new File(iterationPath, file.getName());
		// any file to 1+ more contents
		List<ContentEntry> requestedValues = requested
				.stream()
					.filter(r -> relativeFile.equals(r.getContentName()) || file.equals(r.getContentName()))
					.collect(Collectors.toList());
		if (requestedValues.size() > 1) {
			uploadReport.getContentDuplications().put(relativeFile, requestedValues);
			return;
		}
		for (ContentEntry entry : requestedValues) {
			if (processableContent.containsKey(entry)) {
				// add both files and remove content for further processing
				uploadReport.addFileDuplication(entry, processableContent.get(entry));
				uploadReport.addFileDuplication(entry, file);
				processableContent.remove(entry);
				continue;
			} else if (uploadReport.getFileDuplications().get(entry) != null) {
				uploadReport.addFileDuplication(entry, file);
			}
			// convert to full path files
			entry.setPrimaryContent(file);
			processableContent.put(entry, file);
		}
	}

	private void detectIncompleteImports(List<ContentEntry> source) {
		boolean hasPartialImports = false;
		for (ContentEntry entry : source) {
			File contentIdFile = getSpreadsheetEntryCacheStorage(entry);
			if (!entry.isExisting() && contentIdFile.canRead()) {
				hasPartialImports = true;
				// read contentId if not already stored
				try (DataInputStream in = new DataInputStream(new FileInputStream(contentIdFile))) {
					setContentIdForEntry(entry, in.readUTF());
					cachedEntities.add(contentIdFile);
				} catch (@SuppressWarnings("unused") Exception e) {// NOSONAR
					// skip
				}
			}
		}
		// indicate error so not to delete the temporary location after task restart
		uploadReport.setIncompleteImports(hasPartialImports);
	}

	private static Map<ContentEntry, Long> getLocalContentFingerpint(Map<ContentEntry, File> uploadableSet) {
		Map<ContentEntry, Long> checksums = new HashMap<>(uploadableSet.size(), 1);
		for (Entry<ContentEntry, File> entry : uploadableSet.entrySet()) {
			// only files with content id
			if (entry.getKey().isExisting()) {
				// content id to size
				checksums.put(entry.getKey(), entry.getValue().length());
			}
		}
		return checksums;
	}

	private Map<ContentEntry, Long> getRemoteContentFingerpint(Map<ContentEntry, Long> localChecksums)
			throws IOException {
		URI contentInfoURI = new URIBuilder(apiURL).append("/content/info").build();
		JsonArrayBuilder ids = Json.createArrayBuilder();
		Map<String, ContentEntry> contentIdToContentEntry = localChecksums
				.keySet()
					.stream()
					.filter(ContentEntry::isExisting)
					.collect(Collectors.toMap(ContentEntry::getContentId, Function.identity()));
		contentIdToContentEntry.keySet().forEach(ids::add);
		if (contentIdToContentEntry.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, String> headers = new HashMap<>(3, 1);
		headers.put(EAIContentConstants.HEADER_AUTHORIZATION, ParametersProvider.get(PARAM_AUTHORIZATION));
		headers.put(EAIContentConstants.HEADER_ACCEPT, EAIContentConstants.MIMETYPE_APPLICATION_VND_SEIP_V2_JSON);
		headers.put(EAIContentConstants.HEADER_CONTENT_TYPE, EAIContentConstants.MIMETYPE_APPLICATION_VND_SEIP_V2_JSON);
		PayloadRequestSender networkService = new PayloadRequestSender(contentInfoURI, EAIContentConstants.METHOD_POST);
		networkService.init(headers).appendRequestPayload(ids.build().toString());
		ContentInfo send = networkService.send();
		Map<ContentEntry, Long> remoteChecksums = new HashMap<>(contentIdToContentEntry.size(), 1);
		try (JsonReader reader = Json.createReader(send.getInputStream())) {
			JsonObject response = reader.readObject();
			Set<String> keySet = response.keySet();
			for (String contentId : keySet) {
				JsonNumber sizeValue = response.getJsonObject(contentId).getJsonNumber("size");
				if (sizeValue == JsonValue.NULL || contentIdToContentEntry.get(contentId) == null) {
					continue;
				}
				remoteChecksums.put(contentIdToContentEntry.get(contentId), sizeValue.longValue());
			}
		}
		return remoteChecksums;
	}

	private void logUploadReport() {
		StringBuilder builder = new StringBuilder();
		builder.append("Uploaded report:\n");
		outputContentEntryCollection(builder, uploadReport.getNotSetContentFiles(),
				e -> "There is no content to be uploaded.");
		outputContentEntryCollection(builder, uploadReport.getMissingFiles().keySet(),
				e -> "There is no file with name " + uploadReport.getMissingFiles().get(e)
						+ " in the upload selection");
		outputContentEntryCollection(builder, uploadReport.getFilesForUpload().keySet(),
				e -> e.isExisting() ? ("The file " + uploadReport.getFilesForUpload().get(e) + " will be updated")
						: ("A new file " + e.getContentSource() + " will be uploaded"));
		outputContentEntryCollection(builder, uploadReport.getFileDuplications().keySet(),
				e -> "Folder/File name is duplicated. Please, remove the duplications - "
						+ uploadReport.getFileDuplications().get(e) + "!");
		uploadReport.getContentDuplications().forEach((key, value) -> outputContentEntryCollection(builder,
				value, k -> "Detected multiple requests for file: " + key));
		if (builder.length() == 18) {
			builder.append("\t\tNo changes");
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(builder.toString());
		}
	}

	private static void outputContentEntryCollection(StringBuilder builder, Collection<ContentEntry> list,
			Function<ContentEntry, String> message) {
		list
				.stream()
					.sorted(contentEntrySorter())
					.forEach(entry -> builder
							.append("\t\t")
								.append(entry.getSourceId())
								.append(":   \t")
								.append(message.apply(entry))
								.append("\n"));
	}

	private static Comparator<? super ContentEntry> contentEntrySorter() {
		return (o1, o2) -> {
			SpreadsheetEntryId o1Id = o1.getSourceId();
			SpreadsheetEntryId o2Id = o2.getSourceId();

			int sheetCompare = Integer.compare(Integer.parseInt(o1Id.getSheetId()),
					Integer.parseInt(o2Id.getSheetId()));
			if (sheetCompare != 0) {
				return sheetCompare;
			}
			return Integer.compare(Integer.parseInt(o1Id.getExternalId()), Integer.parseInt(o2Id.getExternalId()));
		};
	}

	private ContentEntry processContentEntryUpload(ContentEntry entry) throws IOException {
		File contentSource = entry.getContentSource();
		LOGGER.debug("Uploading file '{}' to remote server...", contentSource);
		try {
			URI contentUploadURI = new URIBuilder(apiURL).append("/content/").build();
			String contentId = uploadFile(contentSource, getType(entry), contentUploadURI);
			if (contentId == null) {
				// indicate problem to user
				uploadReport.getErrorAppender().append("Invalid server response for file '", contentSource, "'\n");
				return null;
			}
			LOGGER.debug("Upload generated contentId {} for file '{}'", contentId, contentSource);
			LOGGER.info(entry.isExisting() ? ("The file {} has been updated") : ("A new file {} has been uploaded"),
					contentSource);
			setContentIdForEntry(entry, contentId);
			cacheContentInfo(entry, contentId);
		} catch (Exception ex) {// NOSONAR
			uploadReport.getErrorAppender().append("Failure during upload of file '",
					entry.getContentSource().getName(), "' with details: ", ex.getMessage(), "\n");
			return null;
		}
		return entry;
	}

	private String getType(ContentEntry entry) {
		Map<String, Object> properties = entry.getSource().getProperties();
		Object rdfType = properties.get("rdf:type");
		if (rdfType != null) {
			// we have the semantic type here, we are done
			return rdfType.toString();
		}
		// no semantic type, check for definition id
		Object type = properties.get("emf:type");
		if (type != null && type.toString().toLowerCase().contains("image")) {
			// currently there is no service that can resolve the semantic class
			// by emf:type from the definition
			// this implementation is sufficient for now
			return IMAGE_TYPE;
		}
		return DOCUMENT_TYPE;
	}

	private void cacheContentInfo(ContentEntry entry, String contentId) {
		File file = getSpreadsheetEntryCacheStorage(entry);
		try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
			out.writeUTF(contentId);
			cachedEntities.add(file);
		} catch (@SuppressWarnings("unused") Exception e) { // NOSONAR
		}
	}

	private File getSpreadsheetEntryCacheStorage(ContentEntry entry) {
		return new File(uploadResultStorage,
				entry.getSourceId().getSheetId() + "_" + entry.getSourceId().getExternalId());
	}

	private static String uploadFile(File file, String type, URI requestURI) throws IOException {
		try (FormRequestSender multipart = new FormRequestSender(requestURI,
				Collections.singletonMap(EAIContentConstants.HEADER_AUTHORIZATION,
				ParametersProvider.get(PARAM_AUTHORIZATION)))) {
			multipart.addFilePart(file.getName(), file);
			multipart.addPart("metadata", Json.createObjectBuilder().add("rdf:type", type).build().toString());
			ContentInfo response = multipart.send();
			try (JsonReader reader = Json.createReader(response.getInputStream())) {
				return reader.readObject().getString(PRIMARY_CONTENT_ID, null);
			}
		}
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		completeOperation(
				"Operation completed successfully! You may now close the Import tool and continue with file validation.");
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		completeOperation("Operation is cancelled!");
	}

	@Override
	protected void failed() {
		super.failed();
		completeOperation(
				"Operation has failed! Please select again button \"Upload files\" or close the Import tool and contact your system administrator.");
	}

	private void completeOperation(String message) {
		updateProgress(0, 0);
		updateMessage(message);
	}

}
