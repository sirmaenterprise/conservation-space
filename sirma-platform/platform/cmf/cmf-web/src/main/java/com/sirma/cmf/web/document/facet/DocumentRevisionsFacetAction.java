package com.sirma.cmf.web.document.facet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.Action;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * This class will manage document revisions. After we open document landing page will be retrieved all revisions for
 * the current document and will displayed in the facet zone. <br />
 * <b>The logic is temporary <i>hard-coded</i>, because there no back-end functionality.</b>
 *
 * @author cdimitrov
 */
@Named
@ViewAccessScoped
public class DocumentRevisionsFacetAction extends Action implements Serializable {
	private static final long serialVersionUID = 7820379790600628440L;

	/** Holds all document revisions. */
	private List<Instance> tempInstances;

	/**
	 * Retrieve document revisions on initializing.
	 */
	@PostConstruct
	public void initRevisions() {
		tempInstances = new ArrayList<Instance>();
		Instance instance = getDocumentContext().getCurrentInstance();
		tempInstances.add(instance);
		tempInstances.add(instance);
	}

	/**
	 * Getter document revisions.
	 *
	 * @return list with document revisions
	 */
	public List<Instance> getTempInstances() {
		return tempInstances;
	}

}
