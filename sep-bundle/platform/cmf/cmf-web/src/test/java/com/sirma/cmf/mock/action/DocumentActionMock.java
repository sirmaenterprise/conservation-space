package com.sirma.cmf.mock.action;

import javax.enterprise.inject.Alternative;

import com.sirma.cmf.web.document.DocumentAction;

/**
 * DocumentActionMock class.
 *
 * @author yyordanov
 */
@Alternative
public class DocumentActionMock extends DocumentAction {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 33L;
	
	@Override
	public String getLatestDocVersion() {
		return null;
	}
	
}
