package com.sirma.itt.cmf.alfresco4.services;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.cmf.alfresco4.remote.AlfrescoUploader;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.alfresco4.services.convert.FieldProcessor;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.adapter.CMFSearchAdapterService;
import com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.collections.ContextualConcurrentMap;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Unit tests for DocumentAlfresco4Service.
 *
 * @author A. Kunchev
 */
public class DocumentAlfresco4ServiceUnitTest {

	private static final String SOME_USER = "someUser";

	@InjectMocks
	private DocumentAlfresco4Service service = new DocumentAlfresco4Service();

	@Mock
	private AlfrescoUploader alfrescoUploader;

	@Mock
	private DMSTypeConverter docConvertor;

	@Mock
	private CMFSearchAdapterService searchAdapter;

	@Mock
	private AdaptersConfiguration adaptersConfiguration;

	@Spy
	private ContextualConcurrentMap<String, String> libraryCache = ContextualConcurrentMap.create();

	/**
	 * Inits mocks.
	 */
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(adaptersConfiguration.getDmsContainerId()).thenReturn(new ConfigurationPropertyMock<>("seip"));
		libraryCache.clear();
		libraryCache.put("seip", "someDmsId");
	}

	// ----------------------------------- uploadContent -------------------------------------

	@Test
	public void testGetLibrary() throws DMSException {
		libraryCache.clear();
		when(searchAdapter.search(any(), any(Class.class))).then(a -> {
			SearchArguments<FileDescriptor> arguments = a.getArgumentAt(0, SearchArguments.class);
			FileDescriptor descriptor = mock(FileDescriptor.class);
			when(descriptor.getId()).thenReturn("someDmsId");
			arguments.setResult(Collections.singletonList(descriptor));
			return arguments;
		});
		String dmsId = service.getLibraryDMSId();
		Assert.assertEquals("someDmsId", dmsId);
	}

	@Test(expected = DMSException.class)
	public void uploadContent_descriptorContainerIdNull_noLibraryId_DMSException() throws DMSException {
		libraryCache.clear();
		when(searchAdapter.search(any(), any(Class.class))).then(a -> {
			SearchArguments<FileDescriptor> arguments = a.getArgumentAt(0, SearchArguments.class);
			arguments.setResult(Collections.emptyList());
			return arguments;
		});
		service.uploadContent(new EmfInstance(), prepareDescriptor(null, null), new HashSet<>());
	}

	@Test(expected = DMSException.class)
	public void uploadContent_nullDescriptorProperties_nullAspectsToInclude_uploadFileThrowsException_DMSException()
			throws DMSException, DMSClientException {
		UploadWrapperDescriptor descriptor = prepareDescriptor("containerId", null);
		when(alfrescoUploader.uploadFile(any())).thenThrow(new DMSClientException(null, 0));
		service.uploadContent(new EmfInstance(), descriptor, null);
	}

	@Test(expected = DMSException.class)
	public void uploadContent_nullDescriptorProperties_nullAspectsToInclude_docConvertorThrowsException_DMSException()
			throws DMSException, DMSClientException, JSONException {
		UploadWrapperDescriptor descriptor = prepareDescriptor("containerId", null);
		stub_AlfrescoUploader_uploadFile("{}");
		when(docConvertor.convertDMStoCMFPropertiesByValue(any(JSONObject.class), any(Instance.class),
				any(FieldProcessor.class))).thenThrow(new JSONException(""));
		service.uploadContent(new EmfInstance(), descriptor, null);
	}

	@Test(expected = DMSException.class)
	public void uploadContent_fileNotUploaded_DMSException() throws DMSClientException, DMSException {
		UploadWrapperDescriptor descriptor = prepareDescriptor("containerId", null);
		stub_AlfrescoUploader_uploadFile(null);
		service.uploadContent(prepareDocumentInstance(), descriptor, null);
	}

	@Test
	public void uploadContent_notNullDescriptorProperties_notNullAspectsToInclude_notNullFileAndPropertiesDescriptor_noCreatedBy()
			throws DMSException, DMSClientException, JSONException {
		uploadContentSuccessfulInternal(prepareDocumentInstance(), new HashMap<>());
	}

	@Test
	public void uploadContent_notNullDescriptorProperties_notNullAspectsToInclude_notNullFileAndPropertiesDescriptor_createdByInDocInstance()
			throws DMSException, DMSClientException, JSONException {
		EmfInstance docInstance = prepareDocumentInstance();
		docInstance.add(DefaultProperties.CREATED_BY, SOME_USER);
		uploadContentSuccessfulInternal(docInstance, new HashMap<>());
	}

	@Test
	public void uploadContent_notNullDescriptorProperties_notNullAspectsToInclude_notNullFileAndPropertiesDescriptor_createdByInPropertiesMap()
			throws DMSException, DMSClientException, JSONException {
		HashMap<String, Serializable> dmsToCMFProperties = new HashMap<>();
		dmsToCMFProperties.put(DefaultProperties.CREATED_BY, SOME_USER);
		uploadContentSuccessfulInternal(prepareDocumentInstance(), dmsToCMFProperties);
	}

	@Test
	public void uploadContent_notNullDescriptorProperties_notNullAspectsToInclude_notNullFileAndPropertiesDescriptor_createdByInPropertiesMapAndDocInstance()
			throws DMSException, DMSClientException, JSONException {
		HashMap<String, Serializable> dmsToCMFProperties = new HashMap<>();
		EmfInstance docInstance = prepareDocumentInstance();
		dmsToCMFProperties.put(DefaultProperties.CREATED_BY, SOME_USER);
		docInstance.add(DefaultProperties.CREATED_BY, SOME_USER);
		uploadContentSuccessfulInternal(docInstance, dmsToCMFProperties);
	}

	private void uploadContentSuccessfulInternal(EmfInstance documentInstance,
			Map<String, Serializable> dmsToCMFProperties) throws DMSClientException, JSONException, DMSException {
		when(docConvertor.convertCMFtoDMSProperties(anyMap(), any(Instance.class), any(FieldProcessor.class)))
				.thenReturn(dmsToCMFProperties);
		when(docConvertor.convertCMFtoDMSProperty(any(), any(), any()))
				.thenReturn(new Pair<String, Serializable>("first", "second"));
		stub_AlfrescoUploader_uploadFile("{\"nodeRef\":\"someNode\"}");
		when(docConvertor.convertDMStoCMFPropertiesByValue(any(), any(), any())).thenReturn(dmsToCMFProperties);
		HashSet<String> aspectsToInclude = new HashSet<>();
		aspectsToInclude.add("something");
		FileAndPropertiesDescriptor result = service.uploadContent(documentInstance,
				prepareDescriptor("containerId", prepareDescriptorMap()), aspectsToInclude);
		assertNotNull(result);
	}

	// -------------------------------- common methods ----------------------------------------------

	private static UploadWrapperDescriptor prepareDescriptor(String containerId, Map<String, Serializable> properties) {
		FileAndPropertiesDescriptor fileAndPropertiesDescriptor = mock(FileAndPropertiesDescriptor.class);
		when(fileAndPropertiesDescriptor.getContainerId()).thenReturn(containerId);
		when(fileAndPropertiesDescriptor.getProperties()).thenReturn(properties);
		return new UploadWrapperDescriptor(fileAndPropertiesDescriptor);
	}

	private void stub_AlfrescoUploader_uploadFile(String toReturn) throws DMSClientException {
		when(alfrescoUploader.uploadFile(any())).thenReturn(toReturn);
	}

	private static EmfInstance prepareDocumentInstance() {
		EmfInstance documentInstance = new EmfInstance();
		documentInstance.getOrCreateProperties().put(DocumentProperties.DOCUMENT_THUMB_MODE,
				ThumbnailGenerationMode.NONE);
		return documentInstance;
	}

	private static Map<String, Serializable> prepareDescriptorMap() {
		Map<String, Serializable> descriptorProps = new HashMap<>();
		descriptorProps.put(DefaultProperties.NAME, "someName");
		return descriptorProps;
	}

}
