package com.sirma.itt.cmf.constants;

import com.sirma.itt.emf.link.LinkConstants;

/**
 * CMF specific link constants
 * 
 * @author BBonev
 */
public interface LinkConstantsCmf extends LinkConstants {

	/** Link identifier used in CMF cases relations between the case and his sections. */
	String CASE_TO_SECTION = "emf:case_to_section";

	/** Link identifier used in CMF section relations between the section and direct children. */
	String SECTION_TO_CHILD = "emf:section_to_child";

	/** The logged work. */
	String LOGGED_WORK = "emf:workDone";
}
