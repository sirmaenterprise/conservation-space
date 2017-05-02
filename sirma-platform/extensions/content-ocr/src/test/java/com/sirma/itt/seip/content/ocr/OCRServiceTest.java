package com.sirma.itt.seip.content.ocr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.content.event.ContentUpdatedEvent;
import com.sirma.itt.seip.content.ocr.status.OCRStatus;
import com.sirma.itt.seip.content.ocr.tesseract.TesseractOCR;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * The Class OCRServiceTest.
 * 
 * @author Hristo Lungov
 */
public class OCRServiceTest {

	/** The Constant EMF_TEST_ID. */
	private static final String EMF_TEST_ID = "emf:testId";
	private static final String EMF_TEST_USER_ID = "emf:testUserId";

	/** The ocr service. */
	@InjectMocks
	private OCRService ocrService;

	/** The tesseract ocr. */
	@Mock
	private TesseractOCR tesseractOCR;

	/** The ocr plugins list. */
	private List<OCREngine> ocrPluginsList = new LinkedList<>();

	/** The ocr plugins. */
	@Spy
	private Plugins<OCREngine> ocrPlugins = new Plugins<>("", ocrPluginsList);

	/** The search service. */
	@Mock
	private SearchService searchService;

	/** The instance service. */
	@Mock
	private InstanceService instanceService;

	/** The instance content service. */
	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private LockService lockService;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	/**
	 * Before method.
	 */
	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		ocrPluginsList.clear();
		ocrPluginsList.add(tesseractOCR);
	}

	/**
	 * On content update correct test.
	 */
	@Test
	public void onContentUpdateCorrectTest() {
		EmfInstance dummyInstance = new EmfInstance();
		dummyInstance.setId(EMF_TEST_ID);
		ObjectInstance loadedInstance = Mockito.spy(ObjectInstance.class);
		Mockito.doReturn(null).when(loadedInstance).toReference();
		Mockito.when(lockService.lockStatus(Matchers.any(InstanceReference.class))).thenReturn(new LockInfo());
		loadedInstance.add(DefaultProperties.PRIMARY_CONTENT_ID, "primaryContentId");
		Mockito.when(instanceService.loadByDbId(EMF_TEST_ID)).thenReturn(loadedInstance);
		ContentUpdatedEvent event = new ContentUpdatedEvent(dummyInstance, null, null);
		ocrService.onContentUpdate(event);
		Mockito.verify(instanceService).save(loadedInstance, null);
		Assert.assertNotNull(loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
		Assert.assertEquals(OCRStatus.NOT_STARTED.toString(), loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
	}

	/**
	 * On content update locked instance test.
	 */
	@Test
	public void onContentUpdateLockedTest() {
		EmfInstance dummyInstance = new EmfInstance();
		dummyInstance.setId(EMF_TEST_ID);
		ObjectInstance loadedInstance = Mockito.spy(ObjectInstance.class);
		Mockito.doReturn(null).when(loadedInstance).toReference();
		Mockito.when(lockService.lockStatus(Matchers.any(InstanceReference.class))).thenReturn(new LockInfo(null, EMF_TEST_USER_ID, null, null, null));
		loadedInstance.add(DefaultProperties.PRIMARY_CONTENT_ID, "primaryContentId");
		Mockito.when(instanceService.loadByDbId(EMF_TEST_ID)).thenReturn(loadedInstance);
		ContentUpdatedEvent event = new ContentUpdatedEvent(dummyInstance, null, null);
		ocrService.onContentUpdate(event);
		Mockito.verify(instanceService, Mockito.never()).save(loadedInstance, null);
		Assert.assertNull(loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
	}

	/**
	 * Test for missing Primary Content.
	 */
	@Test
	public void onContentUpdateMissingPrimaryContentTest() {
		EmfInstance dummyInstance = new EmfInstance();
		dummyInstance.setId(EMF_TEST_ID);
		ObjectInstance loadedInstance = Mockito.spy(ObjectInstance.class);
		Mockito.when(instanceService.loadByDbId(EMF_TEST_ID)).thenReturn(loadedInstance);
		ContentUpdatedEvent event = new ContentUpdatedEvent(dummyInstance, null, null);
		ocrService.onContentUpdate(event);
		Mockito.verify(instanceService, Mockito.never()).save(loadedInstance, null);
		Assert.assertNull(loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
	}

	/**
	 * On content update incorrect test.
	 */
	@Test
	public void onContentUpdateInCorrectTest() {
		EmfInstance instance = new EmfInstance();
		instance.setId(EMF_TEST_ID);
		ContentUpdatedEvent event = new ContentUpdatedEvent("Test", null, null);
		ocrService.onContentUpdate(event);
		Mockito.verifyZeroInteractions(instanceService);
	}

	/**
	 * On content update null instance test.
	 */
	@Test
	public void onContentUpdate_Null_Instance_Test() {
		EmfInstance dummyInstance = new EmfInstance();
		dummyInstance.setId(EMF_TEST_ID);
		Mockito.when(instanceService.loadByDbId(EMF_TEST_ID)).thenReturn(null);
		ContentUpdatedEvent event = new ContentUpdatedEvent(dummyInstance, null, null);
		ocrService.onContentUpdate(event);
		Mockito.verify(instanceService, Mockito.never()).save(Matchers.any(Instance.class), Matchers.any(Operation.class));
	}

	/**
	 * Do ocr correct test.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	@SuppressWarnings("boxing")
	public void doOcrCorrectTest() throws IOException {
		EmfInstance dummyInstance = new EmfInstance();
		dummyInstance.setId(EMF_TEST_ID);
		List<Instance> instances = new ArrayList<>(1);
		instances.add(dummyInstance);
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.getMimeType()).thenReturn(TesseractOCR.APPLICATION_PDF);
		Mockito.when(contentInfo.getContentId()).thenReturn(EMF_TEST_ID);
		Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		ObjectInstance loadedInstance = Mockito.spy(ObjectInstance.class);
		Mockito.doReturn(null).when(loadedInstance).toReference();
		Mockito.when(lockService.lockStatus(Matchers.any(InstanceReference.class))).thenReturn(new LockInfo());
		Mockito.when(instanceService.loadByDbId(EMF_TEST_ID)).thenReturn(loadedInstance);
		Mockito.when(instanceContentService.getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);
		Mockito.when(tesseractOCR.isApplicable(TesseractOCR.APPLICATION_PDF)).thenReturn(Boolean.TRUE);
		Mockito.when(tesseractOCR.doOcr(TesseractOCR.APPLICATION_PDF, contentInfo)).thenReturn(EMF_TEST_ID);
		ocrService.doOcr(instances);
		Mockito.verify(instanceService).loadByDbId(EMF_TEST_ID);
		Mockito.verify(instanceContentService).getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT);
		Mockito.verify(instanceService).save(loadedInstance, null);
		Assert.assertNotNull(loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
		Assert.assertEquals(OCRStatus.COMPLETED.toString(), loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
		Assert.assertEquals(EMF_TEST_ID, loadedInstance.getProperties().get(OCRService.OCR_CONTENT_PROP));
	}

	/**
	 * Check locked instance test.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void doOcrLockedInstanceTest() throws IOException {
		EmfInstance dummyInstance = new EmfInstance();
		dummyInstance.setId(EMF_TEST_ID);
		List<Instance> instances = new ArrayList<>(1);
		instances.add(dummyInstance);
		ObjectInstance loadedInstance = Mockito.spy(ObjectInstance.class);
		Mockito.doReturn(null).when(loadedInstance).toReference();
		Mockito.when(lockService.lockStatus(Matchers.any(InstanceReference.class))).thenReturn(new LockInfo(null, EMF_TEST_USER_ID, null, null, null));
		Mockito.when(instanceService.loadByDbId(EMF_TEST_ID)).thenReturn(loadedInstance);
		ocrService.doOcr(instances);
		Mockito.verify(instanceService).loadByDbId(EMF_TEST_ID);
		Mockito.verify(instanceContentService, Mockito.never()).getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT);
		Mockito.verify(instanceService, Mockito.never()).save(loadedInstance, null);
		Assert.assertNull(loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
	}

	/**
	 * Check locked instance after OCR test.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	@SuppressWarnings("boxing")
	public void doOcrLockedInstanceAfterOCRTest() throws IOException {
		EmfInstance dummyInstance = new EmfInstance();
		dummyInstance.setId(EMF_TEST_ID);
		List<Instance> instances = new ArrayList<>(1);
		instances.add(dummyInstance);
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.getMimeType()).thenReturn(TesseractOCR.APPLICATION_PDF);
		Mockito.when(contentInfo.getContentId()).thenReturn(EMF_TEST_ID);
		Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		ObjectInstance loadedInstance = Mockito.spy(ObjectInstance.class);
		Mockito.doReturn(null).when(loadedInstance).toReference();
		Mockito.when(lockService.lockStatus(Matchers.any(InstanceReference.class))).thenReturn(new LockInfo()).thenReturn(new LockInfo(null, EMF_TEST_USER_ID, null, null, null));
		Mockito.when(instanceService.loadByDbId(EMF_TEST_ID)).thenReturn(loadedInstance);
		Mockito.when(instanceContentService.getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);
		Mockito.when(tesseractOCR.isApplicable(TesseractOCR.APPLICATION_PDF)).thenReturn(Boolean.TRUE);
		Mockito.when(tesseractOCR.doOcr(TesseractOCR.APPLICATION_PDF, contentInfo)).thenReturn(EMF_TEST_ID);
		ocrService.doOcr(instances);
		Mockito.verify(instanceService).loadByDbId(EMF_TEST_ID);
		Mockito.verify(instanceContentService).getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT);
		Mockito.verify(instanceService, Mockito.never()).save(loadedInstance, null);
		Assert.assertNotNull(loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
		Assert.assertEquals(OCRStatus.COMPLETED.toString(), loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
		Assert.assertEquals(EMF_TEST_ID, loadedInstance.getProperties().get(OCRService.OCR_CONTENT_PROP));
	}

	/**
	 * Do ocr_ null_ content_ test.
	 */
	@Test
	public void doOcr_Null_Content_Test() {
		EmfInstance dummyInstance = new EmfInstance();
		dummyInstance.setId(EMF_TEST_ID);
		List<Instance> instances = new ArrayList<>(1);
		instances.add(dummyInstance);
		ObjectInstance loadedInstance = Mockito.spy(ObjectInstance.class);
		Mockito.doReturn(null).when(loadedInstance).toReference();
		Mockito.when(lockService.lockStatus(Matchers.any(InstanceReference.class))).thenReturn(new LockInfo());
		Mockito.when(instanceService.loadByDbId(EMF_TEST_ID)).thenReturn(loadedInstance);
		Mockito.when(instanceContentService.getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT)).thenReturn(null);
		ocrService.doOcr(instances);
		Mockito.verify(instanceService).loadByDbId(EMF_TEST_ID);
		Mockito.verify(instanceContentService).getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT);
		Mockito.verify(instanceService).save(Matchers.any(Instance.class), Matchers.any(Operation.class));
		Assert.assertEquals(OCRStatus.EXCLUDED.toString(), loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
	}

	/**
	 * Do OCR not existing_ content_ test.
	 */
	@Test
	@SuppressWarnings("boxing")
	public void doOcr_NotExisting_Content_Test() {
		EmfInstance dummyInstance = new EmfInstance();
		dummyInstance.setId(EMF_TEST_ID);
		List<Instance> instances = new ArrayList<>(1);
		instances.add(dummyInstance);
		ObjectInstance loadedInstance = Mockito.spy(ObjectInstance.class);
		Mockito.doReturn(null).when(loadedInstance).toReference();
		Mockito.when(lockService.lockStatus(Matchers.any(InstanceReference.class))).thenReturn(new LockInfo());
		Mockito.when(instanceService.loadByDbId(EMF_TEST_ID)).thenReturn(loadedInstance);
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.exists()).thenReturn(Boolean.FALSE);
		Mockito.when(instanceContentService.getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);
		ocrService.doOcr(instances);
		Mockito.verify(instanceService).loadByDbId(EMF_TEST_ID);
		Mockito.verify(instanceContentService).getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT);
		Mockito.verify(instanceService).save(Matchers.any(Instance.class), Matchers.any(Operation.class));
		Assert.assertEquals(OCRStatus.EXCLUDED.toString(), loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
	}

	/**
	 * Do ocr_ not present_ content_ test.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	@SuppressWarnings("boxing")
	public void doOcr_NotPresent_Content_Test() throws IOException {
		EmfInstance dummyInstance = new EmfInstance();
		dummyInstance.setId(EMF_TEST_ID);
		List<Instance> instances = new ArrayList<>(1);
		instances.add(dummyInstance);
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.getMimeType()).thenReturn(TesseractOCR.APPLICATION_PDF);
		Mockito.when(contentInfo.getContentId()).thenReturn(EMF_TEST_ID);
		Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		ObjectInstance loadedInstance = Mockito.spy(ObjectInstance.class);
		Mockito.doReturn(null).when(loadedInstance).toReference();
		Mockito.when(lockService.lockStatus(Matchers.any(InstanceReference.class))).thenReturn(new LockInfo());
		Mockito.when(instanceService.loadByDbId(EMF_TEST_ID)).thenReturn(loadedInstance);
		Mockito.when(instanceContentService.getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);
		Mockito.when(tesseractOCR.isApplicable(TesseractOCR.APPLICATION_PDF)).thenReturn(Boolean.TRUE);
		Mockito.when(tesseractOCR.doOcr(TesseractOCR.APPLICATION_PDF, contentInfo)).thenReturn(null);
		ocrService.doOcr(instances);
		Mockito.verify(instanceService).loadByDbId(EMF_TEST_ID);
		Mockito.verify(instanceContentService).getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT);
		Mockito.verify(instanceService).save(loadedInstance, null);
		Assert.assertNotNull(loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
		Assert.assertEquals(OCRStatus.EXCLUDED.toString(), loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
	}

	/**
	 * Do OCR Exception when missing Content test.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	@SuppressWarnings("boxing")
	public void doOcr_Exception_Content_Test() throws IOException {
		EmfInstance dummyInstance = new EmfInstance();
		dummyInstance.setId(EMF_TEST_ID);
		List<Instance> instances = new ArrayList<>(1);
		instances.add(dummyInstance);
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.getMimeType()).thenReturn(TesseractOCR.APPLICATION_PDF);
		Mockito.when(contentInfo.getContentId()).thenReturn(EMF_TEST_ID);
		Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		ObjectInstance loadedInstance = Mockito.spy(ObjectInstance.class);
		Mockito.doReturn(null).when(loadedInstance).toReference();
		Mockito.when(lockService.lockStatus(Matchers.any(InstanceReference.class))).thenReturn(new LockInfo());
		Mockito.when(instanceService.loadByDbId(EMF_TEST_ID)).thenReturn(loadedInstance);
		Mockito.when(instanceContentService.getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);
		Mockito.when(tesseractOCR.isApplicable(TesseractOCR.APPLICATION_PDF)).thenReturn(Boolean.TRUE);
		Mockito.when(tesseractOCR.doOcr(TesseractOCR.APPLICATION_PDF, contentInfo)).thenThrow(new IOException());
		ocrService.doOcr(instances);
		Mockito.verify(instanceService).loadByDbId(EMF_TEST_ID);
		Mockito.verify(instanceContentService).getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT);
		Mockito.verify(instanceService).save(loadedInstance, null);
		Assert.assertNotNull(loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
		Assert.assertEquals(OCRStatus.FAILED.toString(), loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
	}

	/**
	 * Check OCR if empty result test.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void ocrProcess_Empty_Result_Test() {
		SearchArguments<Instance> searchArguments = Mockito.mock(SearchArguments.class);
		Mockito.when(searchService.getFilter(Matchers.any(), Matchers.anyObject(), Matchers.anyObject())).thenReturn(searchArguments);
		Mockito.when(searchArguments.getResult()).thenReturn(CollectionUtils.emptyList());
		ocrService.ocrProcess();
		Mockito.verifyZeroInteractions(instanceService);
	}

	/**
	 * Check OCR Process with correct search result.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	@SuppressWarnings({ "unchecked", "boxing" })
	public void ocrProcess_Result_Test() throws IOException {
		SearchArguments<Instance> searchArguments = Mockito.mock(SearchArguments.class);
		Mockito.when(searchService.getFilter(Matchers.any(), Matchers.anyObject(), Matchers.anyObject())).thenReturn(searchArguments);
		EmfInstance dummyInstance = new EmfInstance();
		dummyInstance.setId(EMF_TEST_ID);
		List<Instance> instances = new ArrayList<>(1);
		instances.add(dummyInstance);
		Mockito.when(searchArguments.getResult()).thenReturn(instances);
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.getMimeType()).thenReturn(TesseractOCR.APPLICATION_PDF);
		Mockito.when(contentInfo.getContentId()).thenReturn(EMF_TEST_ID);
		Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		ObjectInstance loadedInstance = Mockito.spy(ObjectInstance.class);
		Mockito.doReturn(null).when(loadedInstance).toReference();
		Mockito.when(lockService.lockStatus(Matchers.any(InstanceReference.class))).thenReturn(new LockInfo());
		Mockito.when(instanceService.loadByDbId(EMF_TEST_ID)).thenReturn(loadedInstance);
		Mockito.when(instanceContentService.getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);
		Mockito.when(tesseractOCR.isApplicable(TesseractOCR.APPLICATION_PDF)).thenReturn(Boolean.TRUE);
		Mockito.when(tesseractOCR.doOcr(TesseractOCR.APPLICATION_PDF, contentInfo)).thenReturn(EMF_TEST_ID);
		ocrService.doOcr(instances);
		Mockito.verify(instanceService).loadByDbId(EMF_TEST_ID);
		Mockito.verify(instanceContentService).getContent(EMF_TEST_ID, Content.PRIMARY_CONTENT);
		Mockito.verify(instanceService).save(loadedInstance, null);
		Assert.assertNotNull(loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
		Assert.assertEquals(OCRStatus.COMPLETED.toString(), loadedInstance.getProperties().get(OCRService.OCR_STATUS_PROP));
		Assert.assertEquals(EMF_TEST_ID, loadedInstance.getProperties().get(OCRService.OCR_CONTENT_PROP));
	}

}
