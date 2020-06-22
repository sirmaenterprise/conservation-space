package com.sirmaenterprise.sep.eai.spreadsheet.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.sep.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;

/**
 * {@link SpreadsheetParserImpl} implementation is proxy to execute {@link SpreadsheetParser} methods using on of the
 * registered plugins
 * 
 * @author bbanchev
 * @author gshevkedov
 */
@Singleton
public class SpreadsheetParserImpl implements SpreadsheetParser {

	@Inject
	@ExtensionPoint(SpreadsheetParser.TARGET_NAME)
	private Iterable<SpreadsheetParser> parsers;

	@Override
	public SpreadsheetSheet parseEntries(ContentInfo content, Map<String, Collection<String>> requestedEntries)
			throws EAIException {
		return getParserByContent(content)
				.orElseThrow(() -> new EAIException("Unsupported content provided for parsing!"))
					.parseEntries(content, requestedEntries);
	}

	@Override
	public boolean isSupported(ContentInfo content) {
		return getParserByContent(content).isPresent();
	}

	private Optional<SpreadsheetParser> getParserByContent(ContentInfo content) {
		for (SpreadsheetParser parser : parsers) {
			if (parser.isSupported(content)) {
				return Optional.of(parser);
			}
		}
		return Optional.empty();
	}

}
