package com.sirma.cmf.web.configuration;

import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * Configuration properties for CMF web modele
 * 
 * @author BBonev
 */
@Documentation("Configuration properties for CMF web modele")
public interface CmfWebConfigurationProperties extends Configuration {

	/** The tree pager page size. */
	@Documentation("Number of elements (children) to be loaded when expanding tree node without showing a button to load more. <b>Default value: 500</b>")
	String TREE_PAGER_PAGESIZE = "tree.pager.pagesize";
}
