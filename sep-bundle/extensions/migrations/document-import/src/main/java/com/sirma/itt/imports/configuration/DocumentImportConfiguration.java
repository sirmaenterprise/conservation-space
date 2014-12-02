package com.sirma.itt.imports.configuration;

import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * Configurations for the module document-import.
 * 
 * @author BBonev
 */
@Documentation("Configurations for the module document-import.")
public interface DocumentImportConfiguration extends Configuration {

	/**
	 * The default document definition to be set for the imported documents. Default value is:
	 * commonDocument
	 */
	@Documentation("The default document definition to be set for the imported documents. Default value is: commonDocument")
	String DEFAULT_DOCUMENT_DEFINITION = "imports.document.definition";

	/** The default object definition to be set for incoming objects. Default value is: GEO10001 */
	@Documentation("The default object definition to be set for incoming objects. Default value is: GEO10001")
	String DEFAULT_OBJECT_DEFINITION = "imports.object.definition";

	/** The default object definition to be set for incoming users. Default value is: QVI2PERS */
	@Documentation("The default object definition to be set for incoming users. Default value is: QVI2PERS")
	String DEFAULT_USER_DEFINITION = "imports.user.definition";
}
