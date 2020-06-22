package org.openrdf.rio.helpers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import com.sirma.itt.seip.semantic.patch.BackingPatchServiceTest;

/**
 * Used in {@link BackingPatchServiceTest}, because the SPI provider points to 'org.openrdf.rio.helpers' package, but
 * the actual decorator is placed 'org.eclipse.rdf4j.rio.helpers'. This could be removed, when the version of the
 * "rdf4j-rio-api" is updated to 2.4.0 at least.
 *
 * @author A. Kunchev
 */
public class RioFileTypeDetector extends FileTypeDetector {

	public RioFileTypeDetector() {
		super();
	}

	@Override
	public String probeContentType(Path path) throws IOException {
		return Rio.getParserFormatForFileName(path.getFileName().toString())
				.map(RDFFormat::getDefaultMIMEType)
				.orElse(null);
	}
}