package com.sirma.cmf.web.entity.bookmark;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.instance.model.InstanceReference;

/**
 * Test class for BookmarkUtil.
 * 
 * @author svelikov
 */
@Test
public class BookmarkUtilTest extends CMFTest {

	/** The util. */
	private final BookmarkUtil util;

	/** The type converter. */
	private TypeConverter typeConverter;

	/**
	 * Instantiates a new bookmark util test.
	 */
	public BookmarkUtilTest() {
		util = new BookmarkUtil() {
			@Override
			public String getRequestContextPath() {
				return "/emf/entity/open.jsf?";
			}
		};

		typeConverter = Mockito.mock(TypeConverter.class);

		ReflectionUtils.setField(util, "typeConverter", typeConverter);
	}

	/**
	 * Builds the link test.
	 */
	public void buildLinkTest() {
		// if no instance is provided, we expect an empty string as result
		String link = util.buildLink(null);
		assertTrue(link.isEmpty());

		//
		String expected = "/emf/entity/open.jsf?type=caseinstance&instanceId=1";
		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		InstanceReference instanceReference = caseInstance.toReference();
		Mockito.when(typeConverter.convert(InstanceReference.class, caseInstance)).thenReturn(
				instanceReference);
		link = util.buildLink(caseInstance);
		assertEquals(link, expected);
	}

	/**
	 * Builds the link with tab test.
	 */
	public void buildLinkWithTabTest() {
		// if no instance is provided, we expect an empty string as result
		String link = util.buildLink(null, null);
		assertTrue(link.isEmpty());

		//
		String expected = "/emf/entity/open.jsf?type=caseinstance&instanceId=1";
		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		InstanceReference instanceReference = caseInstance.toReference();
		Mockito.when(typeConverter.convert(InstanceReference.class, caseInstance)).thenReturn(
				instanceReference);
		link = util.buildLink(caseInstance, null);
		assertEquals(link, expected);

		// if tab name is provided we expect it to be added as link parameter
		expected = "/emf/entity/open.jsf?type=caseinstance&instanceId=1&tab=details";
		link = util.buildLink(caseInstance, "details");
		assertEquals(link, expected);
	}

	/**
	 * Adds the target blank test.
	 */
	public void addTargetBlankTest() {
		// if null is passed instead of header definition, we expect null to be returned
		String updatedHeader = util.addTargetBlank(null);
		assertNull(updatedHeader);

		// if header is an empty string, we expect an empty string to be returned
		String header = "";
		updatedHeader = util.addTargetBlank(header);
		assertEquals(updatedHeader, header);

		// if header already contains 'target' attribute, we expect the header to not be modified
		header = "<a target=\"_blank\" class=\"COMPLETED\" href=\"/emf/entity/open.jsf?type=taskinstance&instanceId=emf:434ed0c3-25ed-4944-9ad6-70d4bc8eb9cf&currentPage=task-details\"><b>5-6 Task for Execution (Completed)</b></a><br />изпълнител: <a href=\"javascript:void(0)\"><b>John</b></a>, създадена на: <b>21.02.2014, 13:59</b>";
		updatedHeader = util.addTargetBlank(header);
		assertEquals(updatedHeader, header);

		// if header doesn't contain 'target' attribute, we expect to be added one
		header = "<a class=\"COMPLETED\" href=\"/emf/entity/open.jsf?type=taskinstance&instanceId=emf:434ed0c3-25ed-4944-9ad6-70d4bc8eb9cf&currentPage=task-details\"><b>5-6 Task for Execution (Completed)</b></a><br />изпълнител: <a href=\"javascript:void(0)\"><b>John</b></a>, създадена на: <b>21.02.2014, 13:59</b>";
		updatedHeader = util.addTargetBlank(header);
		assertTrue(updatedHeader.contains("target='_blank'"));

		// if header doesn't contain 'target' attribute we expect a marker css class to be added
		// that prevents the blockUI to be activated
		assertTrue(updatedHeader.contains("dontBlockUI"));
	}
}
