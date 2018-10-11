import {View, Component, Inject, NgTimeout, NgScope, NgElement} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {CommentInstance} from 'idoc/comments/comment-instance';
import {AnnotationListUpdatedEvent, WindowRemovedEvent, WindowUpdatedEvent, SlotActivatedEvent} from 'idoc/widget/image-widget/mirador-integration/mirador-events';
import {CommentsHelper} from 'idoc/comments/comments-helper';
import 'idoc/comments/comments-filter/comments-filter';
import 'idoc/comments/comments';
import {ReloadCommentsEvent} from 'idoc/comments/events/reload-comments-event';
import {ReloadRepliesEvent} from 'idoc/comments/events/reload-replies-event';
import {EMPTY_FILTERS} from 'idoc/comments/comments-filter/comments-filter-const';
import _ from 'lodash';
import imageCommentsSectionTemplate from './image-comments-section.html!text';

const SVG_SELECTOR = 'oa:SvgSelector';

@Component({
  selector: 'image-comments-section',
  properties: {
    'context': 'context',
    'control': 'control',
    'config': 'config'
  }
})
@View({
  template: imageCommentsSectionTemplate
})
@Inject(NgScope, NgElement, Eventbus, NgTimeout)
export class ImageCommentsSection {

  constructor($scope, element, eventbus, $timeout) {
    this.events = [];
    this.element = element;
    this.eventbus = eventbus;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.widgetId = this.control.getId();
    this.dataProvider = this.config.dataProvider;
    this.slotComments = {};
    this.shouldExpandComment = false;
    this.subscribeToEvents();
  }

  subscribeToEvents() {

    this.events.push(this.eventbus.subscribe(AnnotationListUpdatedEvent, (event) => {
      if (event[0] === this.widgetId) {
        this.loadComments(event[1].annotationsList, event[1].windowId, true);
      }
    }));

    //Expands first comment only if it is newly created or edited.
    this.events.push(this.eventbus.subscribe(ReloadCommentsEvent, (event) => {
      if (event[0] === this.widgetId) {
        this.shouldExpandComment = true;
      }
    }));

    this.events.push(this.eventbus.subscribe(ReloadRepliesEvent, (event) => {
      if (event[0] === this.widgetId) {
        this.shouldExpandComment = true;
      }
    }));

    this.events.push(this.eventbus.subscribe(WindowRemovedEvent, (event) => {
      if (event[0] === this.widgetId) {
        this.removeSlotComments(event[1]);
      }
    }));

    this.events.push(this.eventbus.subscribe(SlotActivatedEvent, (event) => {
      if (event[0] === this.widgetId) {
        this.loadComments(null);
      }
    }));

    this.events.push(this.eventbus.subscribe(WindowUpdatedEvent, (event) => {
      if (event[0] === this.widgetId) {
        this.loadComments(null);
      }
    }));

  }

  removeSlotComments(slotId) {
    delete this.slotComments[slotId];
  }

  loadComments(annotationsList, slotId, firstExpanded) {
    //Necessary for triggering a digest cycle after changing the comments
    this.$timeout(() => {

      if (annotationsList) {
        let comments = annotationsList.filter(function (comment) {
          let commentInstance = new CommentInstance(comment);
          return commentInstance.getSelectorType() === SVG_SELECTOR || !commentInstance.getSelectorType();
        }).map((comment) => {
          return new CommentInstance(comment);
        });
        comments = CommentsHelper.sortByDate(comments);
        if (comments.length && this.shouldExpandComment) {
          comments[0].expanded = firstExpanded;
          this.shouldExpandComment = false;
        }
        this.slotComments[this.dataProvider.eventsAdapter.getImageIdBySlot(slotId)] = comments;
      }

      if (this.dataProvider.eventsAdapter.isLastSelectedSlotImageView()) {
        this.comments = this.slotComments[this.dataProvider.eventsAdapter.getCurrentImageId()];
      }
      else {
        this.comments = [];
      }
    });
  }

  isVisible() {
    let visible = true;

    if (!this.dataProvider.eventsAdapter.isLastSelectedSlotImageView()) {
      visible = false;
    }

    if (_.isEqual(EMPTY_FILTERS, this.config.filterConfig.filters) && !(this.comments && this.comments.length)) {
      visible = false;
    }

    return visible;
  }

  ngOnDestroy() {
    this.element.remove();
    for (let event of this.events) {
      event.unsubscribe();
    }
  }
}