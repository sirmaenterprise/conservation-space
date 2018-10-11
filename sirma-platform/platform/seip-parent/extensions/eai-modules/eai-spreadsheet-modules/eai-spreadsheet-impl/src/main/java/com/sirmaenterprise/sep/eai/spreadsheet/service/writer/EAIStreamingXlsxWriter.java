package com.sirmaenterprise.sep.eai.spreadsheet.service.writer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.service.SpreadsheetWriter;

/**
 * Writes data in XLSX format by lazy reading the input file and generating output file and integrating the user data
 * on the fly. The writer is intended for use for big files as the default Apache POI API uses a lot of memory and may
 * cause {@link OutOfMemoryError}
 *
 * <p>This writer have some limitations and does not produce identical files in terms for formatting.
 * Known issues are:<ul>
 *     <li>column filters are not transferred in the new file</li>
 *     <li>original cell sizes are not transferred. <ul>
 *         <li>The columns are auto sized to fit their data</li>
 *         <li>All rows are with fixed height of 15 points</li>
 *     </ul></li>
 *     <li>all input blank lines are omitted</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/01/2018
 */
@Extension(target = SpreadsheetWriter.TARGET_NAME, order = 1)
public class EAIStreamingXlsxWriter implements SpreadsheetWriter {
	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "eai.spreadsheet.config.enableXlsxStreamingProcessing",
			type = Boolean.class, defaultValue = "true", sensitive = true,
			label = "Enables/disables an optimization for big xlsx file processing. The writer is optimized for big files. "
					+ "A reason to disable the writer if the clients whats their original files to be updated and not generated")
	private ConfigurationProperty<Boolean> isWriterEnabled;

	@Override
	public boolean isSupported(ContentInfo content) {
		return EAISpreadsheetConstants.XLSX_MIMETYPE.equals(content.getMimeType()) && isWriterEnabled.get();
	}

	@Override
	public Content writerEntries(ContentInfo content, List<SpreadsheetEntry> entries) throws EAIException {
		File inputFile = tempFileProvider.createTempFile("tempXlsxInputFile", ".xlsx");
		try {
			content.writeTo(inputFile);
		} catch (IOException e) {
			throw new EAIException("Could not copy input file", e);
		}
		File outputFile = tempFileProvider.createTempFile("tempXlsxOutputFile", ".xlsx");

		StreamingXlsxWriter streamingXlsxWriter = new StreamingXlsxWriter();
		try {
			streamingXlsxWriter.write(inputFile, outputFile, entries);
			return Content.createFrom(content).setContent(outputFile);
		} catch (Exception e) {
			throw new EAIException("Updating xlsx file failed", e);
		} finally {
			tempFileProvider.deleteFile(inputFile);
		}
	}
}
