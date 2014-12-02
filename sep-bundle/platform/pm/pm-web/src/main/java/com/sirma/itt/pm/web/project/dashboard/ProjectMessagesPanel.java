package com.sirma.itt.pm.web.project.dashboard;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.forum.ForumService;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.util.DateRangeUtil;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * This class manage comments dashlet in project dashboard. To apply comments we update or create
 * topic {@link TopicInstance} and store it in the context.
 * 
 * @author svelikov
 */
@Named
@InstanceType(type = "ProjectDashboard")
@ViewAccessScoped
public class ProjectMessagesPanel extends DashboardPanelActionBase<ProjectInstance> implements
		Serializable, DashboardPanelController {

	private static final long serialVersionUID = -1693155060260907440L;

	private static final String PROJECTDASHBOARD_DASHLET_MESSAGES = "projectdashboard_dashlet_messages";

	/** Check for available topic instance. */
	private boolean isTopicAvailable = false;

	@Inject
	private ForumService forumService;

	/** The list with project comments instances. */
	private List<CommentInstance> projectComments;

	@Override
	public void initData() {
		executeDefaultFilter();
	}

	/**
	 * Method that will load the default filter for generating comments.
	 * 
	 * @param topic
	 *            current topic instance
	 */
	private void loadDefaultComments(TopicInstance topic) {
		forumService.loadComments(topic, DateRangeUtil.getLast7Days());
		projectComments = topic.getComments();
	}

	@Override
	public void executeDefaultFilter() {
		if (!isAjaxRequest() || !isTopicAvailable) {
			isTopicAvailable = true;
			ProjectInstance instance = getDocumentContext().getInstance(ProjectInstance.class);
			TopicInstance topicInstance = forumService.getOrCreateTopicAbout(instance);
			getDocumentContext().setTopicInstance(topicInstance);
			loadDefaultComments(topicInstance);
		}
	}

	@Override
	public Set<String> dashletActionIds() {
		return Collections.emptySet();
	}

	@Override
	public String targetDashletName() {
		return PROJECTDASHBOARD_DASHLET_MESSAGES;
	}

	@Override
	public Instance dashletActionsTarget() {
		return getDocumentContext().getCurrentInstance();
	}

	/**
	 * Getter for retrieving project comments.
	 * 
	 * @return list with project comments
	 */
	public List<CommentInstance> getProjectComments() {
		return projectComments;
	}

	/**
	 * Setter for porject comments.
	 * 
	 * @param projectComments
	 *            list with comments
	 */
	public void setProjectComments(List<CommentInstance> projectComments) {
		this.projectComments = projectComments;
	}

	@Override
	public void updateSearchArguments(SearchArguments<ProjectInstance> searchArguments,
			SearchFilter selectedSearchFilter) {
		// Auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// Auto-generated method stub

	}

}
