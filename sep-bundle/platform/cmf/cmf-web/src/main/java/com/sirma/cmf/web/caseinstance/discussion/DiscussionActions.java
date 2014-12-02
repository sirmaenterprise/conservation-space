package com.sirma.cmf.web.caseinstance.discussion;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.EntityAction;
import com.sirma.itt.emf.forum.ForumService;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.emf.util.DateRangeUtil;

/**
 * This class holds actions for managing discussions. Currently all discussions are linked with
 * opened case.
 * 
 * @author cdimitrov
 */
@Named
@ViewAccessScoped
public class DiscussionActions extends EntityAction implements Serializable {

	private static final long serialVersionUID = 1L;

	/** The forum service. */
	@Inject
	private ForumService forumService;

	/** Comment */
	private String comment;

	/**
	 * Creating new comment for specific topic. The topic will be created or retrieved for current
	 * case.
	 * 
	 * @param comment
	 *            comment for case
	 */
	public void addNewComment(String comment) {

		TopicInstance topic = getDocumentContext().getTopicInstance();

		CommentInstance commentInstance = forumService.createComment(comment);

		forumService.postComment(topic, commentInstance);

		setComment(null);
	}

	/**
	 * Get all comments for current case topic by given date-range
	 * 
	 * @param historyKey
	 *            date-range identifier
	 */
	public void retrieveCommentHistory(int historyKey) {

		DateRange dateRange = null;

		switch (historyKey) {
			case 1:
				dateRange = DateRangeUtil.getToday();
				break;

			case 2:
				dateRange = DateRangeUtil.getThisWeek();
				break;

			case 3:
				dateRange = DateRangeUtil.getThisMonth();
				break;

			case 4:
				dateRange = DateRangeUtil.getAll();
				break;
			case 5:
				dateRange = DateRangeUtil.getLast7Days();
				break;
		}

		TopicInstance topicInstance = getDocumentContext().getTopicInstance();

		forumService.loadComments(topicInstance, dateRange);

	}

	/**
	 * Get method for case comment
	 * 
	 * @return comment for case
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Set method for case comment
	 * 
	 * @param comment
	 *            comment for case
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

}
