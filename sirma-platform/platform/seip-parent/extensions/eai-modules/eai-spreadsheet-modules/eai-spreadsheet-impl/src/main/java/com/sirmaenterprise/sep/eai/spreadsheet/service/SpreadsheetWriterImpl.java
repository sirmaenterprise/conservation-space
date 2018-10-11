package com.sirmaenterprise.sep.eai.spreadsheet.service;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;

/***
 * Default implementation of SpreadsheetWriter acting as proxy to specific plugin to process the request. The service
 * relays on extensions of {@link SpreadsheetWriter}
 * 
 * @author bbanchev
 * @author gshevkedov
 */
@Singleton
public class SpreadsheetWriterImpl implements SpreadsheetWriter {

	@Inject
	@ExtensionPoint(SpreadsheetWriter.TARGET_NAME)
	private Iterable<SpreadsheetWriter> writers;

	@Override
	public Content writerEntries(ContentInfo content, List<SpreadsheetEntry> entries) throws EAIException {
		return getWriterByContent(content)
				.orElseThrow(() -> new EAIException("Unsupported content provided for writing!"))
					.writerEntries(content, entries);
	}

	@Override
	public boolean isSupported(ContentInfo content) {
		return getWriterByContent(content).isPresent();
	}

	private Optional<SpreadsheetWriter> getWriterByContent(ContentInfo content) {
		for (SpreadsheetWriter writer : writers) {
			if (writer.isSupported(content)) {
				return Optional.of(writer);
			}
		}
		return Optional.empty();
	}

}
