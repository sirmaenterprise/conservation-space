package com.sirma.sep.export.services;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.annotations.AnnotationService;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.annotations.model.AnnotationProperties;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.OA;
import com.sirma.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Tests for HtmlTableAnnotationService.
 *
 * @author Boyan Tonchev
 */
@SuppressWarnings("static-method")
public class HtmlTableAnnotationServiceTest {

	private static final String COMPACT_HEADER = "compact header";
	private static final String UI2_URL = "https://ses.sirmaplatform.com/#/idoc/emf:a24e9abf-d3e6-4603-96c1-2d98bcda84cc";
	private static final String TARGET_ID = "emf:targetId";
	private static final String TARGET_COMPACT_HEADER = "<span><img src=\"some src\"/></span><span><a href=\"some href\">" + COMPACT_HEADER + "</a></span>";

	private static final String STATUS_OPEN = "open";
	private static final String STATUS_OPEN_LABEL = "Open";

	private static final String STATUS_CLOSED = "closed";
	private static final String STATUS_CLOSED_LABEL = "Closed";

	private SimpleDateFormat df = new SimpleDateFormat("dd.mm.yyyy");

	@Mock
	private Instance targetInstance;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private AnnotationService annotationService;

	@Mock
	private DateConverter dateConverter;

	@Mock
	private InstanceService instanceService;

	@Mock
	private InstanceLoadDecorator instanceLoadDecorator;

	@Mock
	private CodelistService codelistService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private UserPreferences userPreferences;

	@Mock
	private TransactionSupport transactionSupport;

	@Mock
	private LabelProvider labelProvider;

	@InjectMocks
	private HtmlTableAnnotationService htmlTableAnnotationService;

	/**
	 * Runs Before method init.
	 */
	@BeforeMethod
	public void beforeClass() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(systemConfiguration.getUi2Url()).thenReturn(new ConfigurationPropertyMock<>(UI2_URL));
		Mockito.when(userPreferences.getLanguage()).thenReturn("en");

		Mockito.when(targetInstance.getString(DefaultProperties.HEADER_COMPACT)).thenReturn(TARGET_COMPACT_HEADER);
		Mockito.when(targetInstance.getId()).thenReturn(TARGET_ID);
		Mockito.when(instanceService.loadByDbId(Arrays.asList(TARGET_ID))).thenReturn(Arrays.asList(targetInstance));

		CodeValue codeValueOne = Mockito.mock(CodeValue.class);
		Mockito.when(codeValueOne.getDescription(Matchers.any())).thenReturn(STATUS_OPEN_LABEL);
		Mockito.when(codelistService.getCodeValue(4, STATUS_OPEN)).thenReturn(codeValueOne);

		CodeValue codeValueTwo = Mockito.mock(CodeValue.class);
		Mockito.when(codeValueTwo.getDescription(Matchers.any())).thenReturn(STATUS_CLOSED_LABEL);
		Mockito.when(codelistService.getCodeValue(4, STATUS_CLOSED)).thenReturn(codeValueTwo);

		DefinitionModel instanceDefinition = Mockito.mock(DefinitionModel.class);
		PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
		Mockito.when(propertyDefinition.getCodelist()).thenReturn(4);
		Mockito.when(definitionService.getInstanceDefinition(Matchers.any(Annotation.class))).thenReturn(instanceDefinition);
		Mockito.when(instanceDefinition.getField(DefaultProperties.EMF_STATUS)).thenReturn(Optional.of(propertyDefinition));

		Mockito.when(labelProvider.getLabel(HtmlTableAnnotationService.KEY_LABEL_TOPIC)).thenReturn("Topic");
		Mockito.when(labelProvider.getLabel(HtmlTableAnnotationService.KEY_LABEL_REPLY)).thenReturn("Reply");
		Mockito.when(labelProvider.getLabel(HtmlTableAnnotationService.KEY_LABEL_OBJECT)).thenReturn("Object");
		Mockito.when(labelProvider.getLabel(HtmlTableAnnotationService.KEY_LABEL_TYPE)).thenReturn("Type");
		Mockito.when(labelProvider.getLabel(HtmlTableAnnotationService.KEY_LABEL_CREATOR)).thenReturn("Creator");
		Mockito.when(labelProvider.getLabel(HtmlTableAnnotationService.KEY_LABEL_CREATED_ON)).thenReturn("Date");
		Mockito.when(labelProvider.getLabel(HtmlTableAnnotationService.KEY_LABEL_TEXT)).thenReturn("Text");
		Mockito.when(labelProvider.getLabel(HtmlTableAnnotationService.KEY_LABEL_STATUS)).thenReturn("Status");
	}

	@Test
	public void reateAnnotationWithReplays() {
		HtmlTableAnnotationService spyedHtmlTableAnnotationService = Mockito.spy(htmlTableAnnotationService);

		String title = "tile createAnnotationTableEmptyAnnotation";
		HtmlTableBuilder tableBuilder = new HtmlTableBuilder(title);

		Annotation annotationWithReplay = Mockito.mock(Annotation.class);
		String annotationWithReplayId = "annotationWithReplay-Id";
		Mockito.when(annotationWithReplay.getId()).thenReturn(annotationWithReplayId);
		Mockito.when(annotationWithReplay.getString(AnnotationProperties.HAS_TARGET)).thenReturn(TARGET_ID);
		Mockito.when(annotationWithReplay.getTargetId()).thenReturn(TARGET_ID);
		String commentAnnotation = "<p>coment one</p>";
		Mockito.when(annotationWithReplay.getComment()).thenReturn(commentAnnotation);
		Mockito.when(annotationWithReplay.getCurrentStatus()).thenReturn(STATUS_OPEN);

		Mockito.when(annotationService.loadAnnotation(annotationWithReplayId)).thenReturn(Optional.of(annotationWithReplay));

		Mockito.when(instanceService.loadByDbId(Arrays.asList(TARGET_ID), false)).thenReturn(Arrays.asList(targetInstance));

		Annotation replay = Mockito.mock(Annotation.class);
		Mockito.when(replay.isReply()).thenReturn(true);
		String commentReplay = "<p>coment from Replay</p>";
		Mockito.when(replay.getComment()).thenReturn(commentReplay);
		Mockito.when(replay.getCurrentStatus()).thenReturn(STATUS_CLOSED);
		Mockito.when(replay.getTargetId()).thenReturn(TARGET_ID);

		Mockito.when(annotationWithReplay.getReplies()).thenReturn(Arrays.asList(replay));

		spyedHtmlTableAnnotationService.createAnnotationTable(tableBuilder, Arrays.asList(annotationWithReplay));

		Element table = tableBuilder.build();

		Assert.assertEquals(table.text(), "tile createAnnotationTableEmptyAnnotation compact header Topic coment one Open Reply coment from Replay Closed");

		Assert.assertEquals(table.select("tr:eq(0) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "5");

		Assert.assertEquals(table.select("tr:eq(1) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(1) > td:eq(0)").text(), COMPACT_HEADER);

		Assert.assertEquals(table.select("tr:eq(1) > td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(1) > td:eq(1)").text(), "Topic");

		Assert.assertEquals(table.select("tr:eq(1) > td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(1) > td:eq(2)").text(), "");

		Assert.assertEquals(table.select("tr:eq(1) > td:eq(3)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(1) > td:eq(3)").text(), "coment one");

		Assert.assertEquals(table.select("tr:eq(1) > td:eq(4)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(1) > td:eq(4)").text(), "Open");

		Assert.assertEquals(table.select("tr:eq(2) > td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(2) > td:eq(1)").text(), "Reply");

		Assert.assertEquals(table.select("tr:eq(2) > td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(2) > td:eq(2)").text(), "");

		Assert.assertEquals(table.select("tr:eq(2) > td:eq(3)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(2) > td:eq(3)").text(), "coment from Replay");

		Assert.assertEquals(table.select("tr:eq(2) > td:eq(4)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(2) > td:eq(4)").text(), "Closed");
	}

	@Test
	public void createAnnotationTableEmptyAnnotation() {
		String title = "tile createAnnotationTableEmptyAnnotation";
		HtmlTableBuilder tableBuilder = new HtmlTableBuilder(title);

		htmlTableAnnotationService.createAnnotationTable(tableBuilder, Collections.emptyList());

		Element build = tableBuilder.build();
		Assert.assertEquals(build.text(), "tile createAnnotationTableEmptyAnnotation");
		Assert.assertEquals(build.select("td").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "0");
	}

	@Test
	public void createAnnotationTableTest() throws ParseException {
		String titleOfTable = "Title of table";
		String creatorOne = "creator one";
		String creatorOneCompactHeader = "creator - one - compact - header";
		String headerOne = "<span><img src=\"some src\"/></span><span><a href=\"some href\">" + creatorOneCompactHeader + "</a></span>";
		Date dateOne = Calendar.getInstance().getTime();
		String oneCreatedOn = df.format(dateOne);

		Mockito.when(dateConverter.formatDateTime(dateOne)).thenReturn(oneCreatedOn);
		String commentOne = "text one";
		String annotationIdOne = "annotation-id-one";

		Annotation annotationOne = createAnnotation(annotationIdOne, null, creatorOne, headerOne, dateOne, "<p>" + commentOne + "</P>", STATUS_OPEN);

		String creatorTwo = "creator two";
		String creatorTwoCompactHeader = "creator - two - compact - header";
		String headerTwo = "<span><img src=\"some src\"/></span><span><a href=\"some href\">" + creatorTwoCompactHeader + "</a></span>";
		Date dateTwo = Calendar.getInstance().getTime();
		String twoCreatedOn = df.format(dateTwo);

		Mockito.when(dateConverter.formatDateTime(dateTwo)).thenReturn(twoCreatedOn);
		String commentTwo = "text two";
		String annotationIdTwo = "annotation-id-two";
		Annotation annotationTwo = createAnnotation(annotationIdTwo, annotationIdOne, creatorTwo, headerTwo, dateTwo, "<p>" + commentTwo + "</p>", STATUS_CLOSED);
		Mockito.when(annotationService.loadAnnotation(annotationIdOne)).thenReturn(Optional.of(annotationOne));
		Mockito.when(annotationService.loadAnnotation(annotationIdTwo)).thenReturn(Optional.of(annotationTwo));

		HtmlTableBuilder createAnnotationTable = htmlTableAnnotationService.createAnnotationTable(titleOfTable, Arrays.asList(annotationOne, annotationTwo));

		Element table = createAnnotationTable.build();
		Assert.assertEquals(table.select("tr:eq(0) > td:eq(0) p").text(), "Title of table");

		Assert.assertEquals(table.select("tr:eq(1) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(1) > td:eq(0) p").text(), "Object");
		Assert.assertNotNull(table.select("tr:eq(1) > td:eq(0)").select(JsoupUtil.TAG_STRONG));

		Assert.assertEquals(table.select("tr:eq(1) > td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(1) > td:eq(1) p").text(), "Type");
		Assert.assertNotNull(table.select("tr:eq(1) > td:eq(1) p").select(JsoupUtil.TAG_STRONG));

		Assert.assertEquals(table.select("tr:eq(1) > td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(1) > td:eq(2) p").text(), "Creator");
		Assert.assertNotNull(table.select("tr:eq(1) > td:eq(2) p").select(JsoupUtil.TAG_STRONG));

		Assert.assertEquals(table.select("tr:eq(1) > td:eq(3)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(1) > td:eq(3) p").text(), "Date");
		Assert.assertNotNull(table.select("tr:eq(1) > td:eq(3) p").select(JsoupUtil.TAG_STRONG));

		Assert.assertEquals(table.select("tr:eq(1) > td:eq(4)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(1) > td:eq(4) p").text(), "Text");
		Assert.assertNotNull(table.select("tr:eq(1) > td:eq(4) p").select(JsoupUtil.TAG_STRONG));

		Assert.assertEquals(table.select("tr:eq(1) > td:eq(5)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(1) > td:eq(5) p").text(), "Status");
		Assert.assertNotNull(table.select("tr:eq(1) > td:eq(5) p").select(JsoupUtil.TAG_STRONG));

		Assert.assertEquals(table.select("tr:eq(2) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(2) > td:eq(0) table tr:eq(0) td:eq(1) a").text(), COMPACT_HEADER);

		Assert.assertEquals(table.select("tr:eq(2) > td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(2) > td:eq(1) p").text(), "Topic");
		Assert.assertNotNull(table.select("tr:eq(2) > td:eq(1)").select(JsoupUtil.TAG_P));

		Assert.assertEquals(table.select("tr:eq(2) > td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(2) > td:eq(2) table tr:eq(0) td:eq(1) a").text(), creatorOneCompactHeader);

		Assert.assertEquals(table.select("tr:eq(2) > td:eq(3)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(2) > td:eq(3) p").text(), oneCreatedOn);
		Assert.assertNotNull(table.select("tr:eq(2) > td:eq(3)").select(JsoupUtil.TAG_P));

		Assert.assertEquals(table.select("tr:eq(2) > td:eq(4)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(2) > td:eq(4) p").text(), commentOne);
		Assert.assertNotNull(table.select("tr:eq(2) > td:eq(4)").select(JsoupUtil.TAG_P));

		Assert.assertEquals(table.select("tr:eq(2) > td:eq(5)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(2) > td:eq(5) p").text(), STATUS_OPEN_LABEL);
		Assert.assertNotNull(table.select("tr:eq(2) > td:eq(5)").select(JsoupUtil.TAG_P));

		Assert.assertEquals(table.select("tr:eq(3) > td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(3) > td:eq(0) table tr:eq(0) td:eq(1) a").text(), COMPACT_HEADER);

		Assert.assertEquals(table.select("tr:eq(3) > td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(3) > td:eq(1) p").text(), "Reply");
		Assert.assertNotNull(table.select("tr:eq(3) > td:eq(1)").select(JsoupUtil.TAG_P));

		Assert.assertEquals(table.select("tr:eq(3) > td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(3) > td:eq(2) table tr:eq(0) td:eq(1) a").text(), creatorTwoCompactHeader);

		Assert.assertEquals(table.select("tr:eq(3) > td:eq(3)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(3) > td:eq(3) p").text(), twoCreatedOn);
		Assert.assertNotNull(table.select("tr:eq(3) > td:eq(3)").select(JsoupUtil.TAG_P));

		Assert.assertEquals(table.select("tr:eq(3) > td:eq(4)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(3) > td:eq(4) p").text(), commentTwo);
		Assert.assertNotNull(table.select("tr:eq(3) > td:eq(4)").select(JsoupUtil.TAG_P));

		Assert.assertEquals(table.select("tr:eq(3) > td:eq(5)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(table.select("tr:eq(3) > td:eq(5) p").text(), STATUS_CLOSED_LABEL);
		Assert.assertNotNull(table.select("tr:eq(3) > td:eq(5)").select(JsoupUtil.TAG_P));
	}

	@Test
	public void createTableHeadingsTest() {
		String tableTitle = "Title of table";
		HtmlTableBuilder htmlTableBuilder = new HtmlTableBuilder(tableTitle);
		htmlTableAnnotationService.createTableHeadings(htmlTableBuilder);
		Element build = htmlTableBuilder.build();
		Assert.assertEquals(build.text(), "Title of table Object Type Creator Date Text Status");
	}

	private Annotation createAnnotation(String id, String replayToId, String createdByTitle, String createdByCompactHeader, Date createdOn, String comment, String status) throws ParseException {
		Annotation result = new Annotation();
		result.setId(id);
		Map<String, Serializable> annotationProperties = new HashMap<>();
		result.setProperties(annotationProperties);
		if (StringUtils.isNotBlank(replayToId)) {
			annotationProperties.put(AnnotationProperties.REPLY_PROPERTY, replayToId);
		}
		annotationProperties.put(HtmlTableAnnotationService.PROPERTY_EMF_CREATED_ON, createdOn);
		annotationProperties.put(AnnotationProperties.HAS_TARGET, TARGET_ID);
		annotationProperties.put(OA.PREFIX + ":" + OA.HAS_BODY.getLocalName(), comment);
		annotationProperties.put(EMF.PREFIX + ":" + EMF.STATUS.getLocalName(), status);
		result.setContent(comment);

		EmfUser creator = new EmfUser();
		Map<String, Serializable> userProperties = new HashMap<>();
		creator.setProperties(userProperties);
		userProperties.put(DefaultProperties.TITLE, createdByTitle);
		userProperties.put(DefaultProperties.HEADER_COMPACT, createdByCompactHeader);

		annotationProperties.put("emf:createdBy", creator);
		return result;
	}
}
