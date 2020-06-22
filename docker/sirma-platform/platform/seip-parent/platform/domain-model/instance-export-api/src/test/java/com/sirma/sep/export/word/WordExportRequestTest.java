package com.sirma.sep.export.word;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sirma.sep.export.word.WordExportRequest.WordExportRequestBuilder;

/**
 * Test for {@link WordExportRequest}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class WordExportRequestTest {

	@Test
	public void getName() {
		assertEquals("word", new WordExportRequestBuilder().setInstanceId("instance-id").buildRequest().getName());
	}

	@Test
	public void getTabId() {
		WordExportRequest request = new WordExportRequestBuilder()
				.setInstanceId("instance-id")
					.setTabId("tab-id")
					.setFileName("file-name")
					.buildRequest();
		assertEquals("tab-id", request.getTabId());
	}

}
