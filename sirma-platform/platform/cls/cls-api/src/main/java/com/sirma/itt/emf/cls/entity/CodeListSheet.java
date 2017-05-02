package com.sirma.itt.emf.cls.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * A representation of a sheet object in the context of Codelist implementations, meaning that a sheet contains a list
 * of codelists, instead of the default {@link jxl.Sheet} implementation that contains rolls and cells.
 *
 * @author nvelkov
 */
public class CodeListSheet {

	private List<CodeList> codeLists = new ArrayList<>();

	public List<CodeList> getCodeLists() {
		return codeLists;
	}

	public void setCodeLists(List<CodeList> codeLists) {
		this.codeLists = codeLists;
	}

	/**
	 * Add a codelist to the sheet.
	 *
	 * @param codeList
	 *            the codelist to add
	 */
	public void addCodeList(CodeList codeList) {
		codeLists.add(codeList);
	}
}
