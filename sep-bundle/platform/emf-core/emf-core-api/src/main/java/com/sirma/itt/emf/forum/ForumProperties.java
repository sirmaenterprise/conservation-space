package com.sirma.itt.emf.forum;

import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Known properties for forum objects: topic and comments.
 * 
 * @author BBonev
 */
public interface ForumProperties extends DefaultProperties {

	/** The topic today comment count. */
	String TODAY_COMMENT_COUNT = "topicTodayCommentCount";

	/** The topic is expanded. */
	String IS_TOPIC_EXPANDED = "isTopicExpanded";

	/**
	 * The topic affinity. Optional field that can be filled when a topic is created for concrete
	 * instance.
	 */
	String TOPIC_ABOUT = "about";

	/** The image annotation. */
	String IMAGE_ANNOTATION = "imageAnnotation";

	/** The ia view box. */
	String IA_VIEW_BOX = "viewBox";

	/** The ia zoom level. */
	String IA_ZOOM_LEVEL = "zoomLevel";

	/** The ia svg value. */
	String IA_SVG_VALUE = "svgValue";

	/** The reply to. */
	String REPLY_TO = "replyTo";

	/** The topic about section. */
	String TOPIC_ABOUT_SECTION = "aboutSection";

	/** The tags. */
	String TAGS = "tags";

	String CREATED_BY_LABEL = "createdByLabel";

	String CHILDREN = "children";
}
