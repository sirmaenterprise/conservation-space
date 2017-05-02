package com.sirma.itt.cmf.constants;

import com.sirma.itt.seip.instance.relation.LinkConstants;

/**
 * CMF specific link constants
 *
 * @author BBonev
 */
public class LinkConstantsCmf extends LinkConstants {

	/** Link identifier used in CMF cases relations between the case and his sections. */
	public static final String CASE_TO_SECTION = "emf:case_to_section";

	/** Link identifier used in CMF section relations between the section and direct children. */
	public static final String SECTION_TO_CHILD = "emf:section_to_child";

	/** The logged work. */
	public static final String LOGGED_WORK = "emf:workDone";
}
