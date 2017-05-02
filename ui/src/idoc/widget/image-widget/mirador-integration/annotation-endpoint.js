import {CommentParser} from 'idoc/comments/comment-parser';
import {CommentInstance} from 'idoc/comments/comment-instance';
import {EMPTY_FILTERS} from 'idoc/comments/comments-filter/comments-filter-const';
import {OPEN} from 'idoc/comments/comment-status';
import _ from 'lodash';


export class AnnotationEndpoint {
  constructor(commentRestService, widget) {
    this.commentRestService = commentRestService;
    this.widget = widget;
  }

  search(miradorEndpointContext, options) {
    let loaded = miradorEndpointContext.dfd;
    miradorEndpointContext.annotationsList = [];
    //checks if there is saved filter and if present filters the comments,otherwise just loads them
    if (!_.isEqual(EMPTY_FILTERS, this.widget.filterConfig.filters)) {
      //obtains and filters the comments
      this.commentRestService.loadAllComments(options.uri).then((response) => {
        if (response.data.length) {
          let comments = response.data.map((comment) => {
            return new CommentInstance(comment);
          });
          miradorEndpointContext.annotationsList = this.widget.commentsFilterService.filter(comments, this.widget.filterConfig.filters).map((comment) => {
            return comment.comment;
          });
        } else {
          miradorEndpointContext.annotationsList = [];
        }
        this.updateEndpointContext(miradorEndpointContext, loaded);
      });
    } else {
      //if there isn't saved filter just loads the comments
      this.commentRestService.loadComments(options.uri).then((response) => {
        miradorEndpointContext.annotationsList = response.data;
        this.updateEndpointContext(miradorEndpointContext, loaded);
      });
    }
  }

  updateEndpointContext(miradorEndpointContext, loaded) {
    miradorEndpointContext.annotationsList.forEach(function (annotation) {
      annotation.endpoint = miradorEndpointContext;
    });
    loaded.resolve(true);
  }

  update(miradorEndpointContext, oaAnnotaton, successCallback, errorCallback) {
    delete oaAnnotaton.endpoint;
    this.commentRestService.updateComment(CommentParser.getId(oaAnnotaton), JSON.stringify(oaAnnotaton)).then((response) => {
      response.data.endpoint = miradorEndpointContext;
      successCallback(response.data);
    }).catch(errorCallback);
    oaAnnotaton.endpoint = miradorEndpointContext;
  }

  create(miradorEndpointContext, oaAnnotation, successCallback, errorCallback) {
    this.commentRestService.createComment(oaAnnotation).then(function (response) {
      response.data.endpoint = miradorEndpointContext;
      successCallback(response.data);
    }).catch(errorCallback);
  }

  deleteAnnotation(miradorEndpointContext, annotationID, successCallback, errorCallback) {
    this.commentRestService.deleteComment(annotationID).then(successCallback).catch(errorCallback);
  }

  /**
   * Shows action in mirador popup if returns true
   * @param miradorEndpointContext
   * @param action
   * @param annotation
   * @returns {boolean}
   */
  userAuthorize(miradorEnpointContext, action, annotation) {
    // In locked mode there should be no actions when you hover shape
    if (this.widget.context.isPreviewMode() && this.widget.config.lockWidget) {
      return false;
    }
    if (CommentParser.getStatus(annotation) !== OPEN) {
      return false;
    }
    return true;
  }

}