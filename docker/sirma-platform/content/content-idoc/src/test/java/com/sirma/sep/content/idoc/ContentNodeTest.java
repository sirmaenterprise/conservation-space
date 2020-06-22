package com.sirma.sep.content.idoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.SectionNode;
import com.sirma.sep.content.idoc.Widget;

/**
 * Tests for the base node functionality.
 *
 * @author Adrian Mitev
 */
public class ContentNodeTest {

	@Test
	public void should_TurnANodeIntoHtml() {
		Idoc document = Idoc.parse("<sections><section data-id=\"test\"></section></section>");

		String sectionHtml = document.getSections().getSectionByIndex(0).asHtml();

		assertEquals("<section data-id=\"test\"></section>", sectionHtml);
	}

	@Test
	public void should_GenerateNewNodeId() {
		Idoc document = Idoc.parse("<sections><section data-id=\"test\"></section></section>");

		SectionNode section = document.getSections().getSectionByIndex(0);

		assertEquals("test", section.getId());

		section.generateNewId();

		assertNotEquals("test", section.getId());
	}

	@Test
	public void should_GetAndSetElementId() {
		Idoc document = Idoc.parse(
				"<sections><section data-id=\"test\"><div id=\"widget1\" widget=\"info-box\" /></div></section></section>");

		SectionNode section = document.getSections().getSectionByIndex(0);

		Widget widget = section.widgets().findFirst().get();

		assertEquals("widget1", widget.getId());

		widget.setId("widget2");

		assertEquals("widget2", widget.getId());
	}

	@Test
	public void should_CheckIfAnElementIsAWidget() {
		Idoc document = Idoc
				.parse("<sections><section data-id=\"test\"><div widget=\"info-box\" /></div></section></section>");

		SectionNode section = document.getSections().getSectionByIndex(0);

		assertFalse(section.isWidget());

		Optional<Widget> firstWidget = section.widgets().findFirst();

		assertTrue(firstWidget.get().isWidget());
	}

	@Test
	public void should_ReportThatElementIsNotAnImage() {
		Idoc document = Idoc.parse("<sections><section data-id=\"test\"></section></section>");

		SectionNode section = document.getSections().getSectionByIndex(0);

		assertFalse(section.isImage());
	}

	@Test
	public void should_CheckIfAnElementIsATextNode() {
		Idoc document = Idoc.parse("<sections><section data-id=\"test\">test</section></section>");

		SectionNode section = document.getSections().getSectionByIndex(0);

		ContentNode child = section.children().findFirst().get();

		assertTrue(child.isTextNode());
	}

}
