package com.sirma.sep.content.idoc.extract;

import java.util.Optional;

import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Extractor capable to resolve the contents of Idoc widgets
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/09/2018
 */
public interface WidgetContentExtractor {
	/**
	 * Extract the widgets content for the given instance. The instance will be used in the widgets as
	 * {@code currentInstance}
	 *
	 * @param currentInstanceId the id of the instance to be used as current
	 * @param descriptor the descriptor that can be used to retrieve the content
	 * @return the extracted content if any
	 */
	Optional<String> extractWidgetsContent(String currentInstanceId, FileDescriptor descriptor);
}
