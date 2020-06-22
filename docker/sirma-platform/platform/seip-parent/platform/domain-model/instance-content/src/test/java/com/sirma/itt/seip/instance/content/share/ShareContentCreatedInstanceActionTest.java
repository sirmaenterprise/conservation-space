package com.sirma.itt.seip.instance.content.share;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntry;
import com.sirma.itt.seip.tasks.SchedulerRetryException;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.export.ExportFailedException;
import com.sirma.sep.export.ExportRequest;
import com.sirma.sep.export.ExportService;
import com.sirma.sep.export.ExportURIBuilder;
import com.sirma.sep.export.pdf.PDFExportRequest;
import com.sirma.sep.export.word.WordExportRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.File;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ShareContentCreatedInstanceAction}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 13/09/2017
 */
public class ShareContentCreatedInstanceActionTest {

	@InjectMocks
	private ShareContentCreatedInstanceAction cut;

	@Mock
	private ExportURIBuilder uriBuilder;
	@Mock
	private ExportService exportService;
	@Mock
	private InstanceContentService instanceContentService;
	@Mock
	private SchedulerService schedulerService;
	@Mock
	private ContentInfo contentInfo;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(exportService.export(any(ExportRequest.class))).thenReturn(mock(File.class));

		when(contentInfo.exists()).thenReturn(true);
		when(instanceContentService.saveContent(any(), any(Content.class))).thenReturn(contentInfo);
	}

	@Test
	public void test_execute_exportToWord() throws Exception {
		SchedulerContext context = BaseShareInstanceContentAction.createContext("emf:id", "title", "token", "contentId",
																				"word");
		cut.execute(context);
		verify(instanceContentService).saveContent(any(), any(Content.class));
		verify(exportService).export(any(WordExportRequest.class));
	}

	@Test
	public void test_execute_withTitleNormalization() throws Exception{
		ArgumentCaptor<WordExportRequest> captor = ArgumentCaptor.forClass(WordExportRequest.class);
		SchedulerContext context = BaseShareInstanceContentAction.createContext("emf:id","Test_Title/Usi   ng://Inva.lid, char_acters..", "toke", "contentId", "word");
		cut.execute(context);
		verify(exportService).export(captor.capture());
		WordExportRequest request = captor.getValue();
		assertEquals(request.getFileName(),"Test_Title-Usi-ng-Inva-lid-char_acters-");
	}
	
	@Test(expected = SchedulerRetryException.class)
	public void test_execute_exportToWord_exportFails() throws Exception {
		SchedulerContext context = BaseShareInstanceContentAction.createContext("emf:id", "title", "token", "contentId",
																				"word");
		SchedulerEntry entry = new SchedulerEntry();
		entry.setConfiguration(new DefaultSchedulerConfiguration());
		context.put(SchedulerContext.SCHEDULER_ENTRY, entry);
		doThrow(new ExportFailedException()).when(exportService).export(any());
		cut.execute(context);
		verify(transactionSupport).invokeAfterTransactionCompletion(any(Executable.class));
	}

	@Test
	public void test_execute_exportToPDF() throws Exception {
		SchedulerContext context = BaseShareInstanceContentAction.createContext("emf:id", "title", "token", "contentId",
																				"pdf");
		when(uriBuilder.generateURI(any(), any())).thenReturn(new URI("http://ses.com/"));
		cut.execute(context);
		verify(instanceContentService).saveContent(any(), any(Content.class));
		verify(exportService).export(any(WordExportRequest.class));
		verify(uriBuilder).generateURI(any(), any());
	}

	@Test
	public void test_execute_exportUnsupportedFormat() throws Exception {
		SchedulerContext context = BaseShareInstanceContentAction.createContext("emf:id", "title", "token", "contentId",
																				"something");
		verify(exportService, times(0)).export(any(WordExportRequest.class));
		verify(exportService, times(0)).export(any(PDFExportRequest.class));
		cut.execute(context);
	}
}