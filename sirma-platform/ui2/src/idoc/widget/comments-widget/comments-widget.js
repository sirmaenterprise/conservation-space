import {Widget} from 'idoc/widget/widget';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {View, Inject, NgScope, NgElement, NgTimeout} from 'app/app';
import {CommentsRestService} from 'services/rest/comments-service';
import {SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {CommentsHelper} from 'idoc/comments/comments-helper';
import {DateRangeResolver} from './date-range-resolver';
import {SearchResolverService} from 'services/resolver/search-resolver-service';
import 'idoc/comments/comments';
import {ReloadCommentsEvent} from 'idoc/comments/events/reload-comments-event';
import {IdocCommentsDataProvider} from 'idoc/idoc-comments/idoc-comments-data-provider';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Eventbus} from 'services/eventbus/eventbus';
import {EventEmitter} from 'common/event-emitter';

import _ from 'lodash';
import {AfterCommentExpandedEvent} from 'idoc/comments/events/after-comment-expanded-event';
import commentsWidgetTemplate from './comments-widget.html!text';
import './comments-widget.css!css';

const ERROR_MESSAGE_LABEL = 'comments.widget.error';
@Widget
@View({
  template: commentsWidgetTemplate
})
@Inject(CommentsRestService, NgScope, NgElement, NgTimeout, DateRangeResolver, SearchResolverService, Eventbus, PromiseAdapter)
export class CommentsWidget {

  constructor(commentsService, $scope, $element, $timeout, DateRangeResolver, searchResolver, eventbus, promiseAdapter) {
    this.commentsService = commentsService;
    this.dataProvider = new IdocCommentsDataProvider(this.commentsService);
    this.$scope = $scope;
    this.$element = $element;
    this.timeout = $timeout;
    this.searchResolver = searchResolver;
    this.dateRangeResolver = DateRangeResolver;
    this.eventbus = eventbus;
    this.promiseAdapter = promiseAdapter;

    this.events = [
      this.eventbus.subscribe(ReloadCommentsEvent, this.handleReloadComments.bind(this))
    ];
    this.commentsComponentConfig = {
      dataProvider: this.dataProvider,
      additionalComponent: 'comment-header',
      eventEmitter: new EventEmitter(),
      widgetId: this.control.getId()
    };
    this.errorMessage = ERROR_MESSAGE_LABEL;
    this.config.limit = 0;
    this.comments = [];
    this.allComments = [];
  }

  handleReloadComments(event) {
    if (event[0] === this.control.getId()) {
      this.obtainComments();
    }
  }

  ngAfterViewInit() {
    this.commentsElement = this.$element.find('.comments');
    this.loadingElement = this.$element.find('.comments-loader');
    this.commentsElement.scroll(this.registerInfiniteScroll.bind(this));
    this.registerConfigWatchers();
  }

  obtainComments() {
    this.createCommentsLoaderConfig().then((config) => {
      this.commentsLoaderConfig = config;
      return this.isVersion();
    }).then((isVersion) => {
      this.commentsLoaderConfig.isHistoricalVersion = isVersion;
      this.commentsService.loadRecentComments(this.commentsLoaderConfig).then((response) => {
        if (!(this.context.isModeling() && (this.config.selectCurrentObject || this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY ))) {
          //When page is open in print mode we have to wait all replies to be loaded
          //but they are loaded asynchronously so we take count of comments and register listener for
          //AfterCommentExpandedEvent.
          if (this.context.isPrintMode()) {
            this.commentsCount = response.data.annotations.length;
            if (this.commentsCount === 0) {
              this.publishWidgetReadyEvent();
            } else {
              this.subscribeToAvatarLoadedEvent();
              this.events.push(this.eventbus.subscribe(AfterCommentExpandedEvent, this.handleAfterCommentExpandedEvent.bind(this)));
            }
          }
          this.extractRecentComments(response.data);
          this.publishWidgetReadyEvent();
        } else {
          this.publishWidgetReadyEvent();
          delete this.errorMessage;
        }
        this.loadingActive = false;
        this.loadingElement.css({display: 'none'});
      });

      this.commentsService.loadCommentsCount(this.commentsLoaderConfig).then((response) => {
        this.count = response.data.count;
      });
    });
  }

  subscribeToAvatarLoadedEvent() {
    let commentsAvatarsLoaded = 0;
    let propertyLoadedHandler = this.commentsComponentConfig.eventEmitter.subscribe('loaded', () => {
      commentsAvatarsLoaded += 1;
      if (this.commentExpanded && commentsAvatarsLoaded === this.comments.length) {
        propertyLoadedHandler.unsubscribe();
        this.publishWidgetReadyEvent();
      }
    });
  }

  /**
   * Handler for AfterCommentExpandedEvent. This event is thrown when comment is expanded see comment.js.
   * Update counter of comments which are not expanded and check if all comments are ready throw publishWidgetReadyEvent.
   */
  handleAfterCommentExpandedEvent() {
    this.commentExpanded = true;
    if (!--this.commentsCount && this.avatarLoaded) {
      this.publishWidgetReadyEvent();
    }
  }

  extractRecentComments(response) {
    this.comments = CommentsHelper.convertToCommentInstances(response.annotations);
    this.comments.map((comment) => {
      comment.setHeader(response.instanceHeaders[comment.getTarget()]);
    });
  }

  /**
   * @returns {*} Promise which resolves with true or false depending if current object is version or not
   */
  isVersion() {
    return this.context.getCurrentObject().then((currentObject) => {
      return currentObject.isVersion();
    });
  }

  createCommentsLoaderConfig() {
    let loadCommentsConfig = {
      criteria: {},
      manuallySelectedObjects: [],
      filters: {
        userIds: this.config.selectedUsers || [],
        //set 0 if widget is in print mode to load all comments.
        limit: this.context.isPrintMode() ? 0 : this.config.limit,
        //offset is set to 1 because any time the backend must return from the first comment
        offset: 1,
        dateRange: this.dateRangeResolver.resolveRule(_.cloneDeep(this.config.filterCriteria))
      }
    };

    if (this.config.selectCurrentObject) {
      let currentInstanceIndex = this.config.selectedObjects.indexOf(this.context.getCurrentObjectId());

      if (currentInstanceIndex > -1) {
        this.config.selectedObjects.splice(currentInstanceIndex, 1);
      }

      loadCommentsConfig.manuallySelectedObjects.push(this.context.getCurrentObjectId());
    }

    if (this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
      return this.searchResolver.resolve(_.cloneDeep(this.config.criteria), this.context).then((response) => {
        //the resolver returns array with two equal objects
        loadCommentsConfig.criteria = response[0];
        return this.promiseAdapter.resolve(loadCommentsConfig);
      });
    } else {
      loadCommentsConfig.manuallySelectedObjects.push.apply(loadCommentsConfig.manuallySelectedObjects, this.config.selectedObjects);
      return this.promiseAdapter.resolve(loadCommentsConfig);
    }
  }

  registerInfiniteScroll() {
    //Disable scrolling while loading comments.
    if (this.loadingActive) {
      return;
    }
    //Check if the scroll is to the bottom of the element.
    if (this.commentsElement.scrollTop() + this.commentsElement.innerHeight() >= this.commentsElement[0].scrollHeight) {
      this.loadingActive = true;
      //Show the loading element while loading the comments.
      this.loadingElement.css({display: 'block'});
      this.timeout(() => {
        this.config.limit = parseInt(this.config.limit) + parseInt(this.config.size);
        this.obtainComments();
      });
    }
  }

  registerConfigWatchers() {
    this.$scope.$watchCollection(() => {
      return [this.config.selectedObjects, this.config.selectObjectMode, this.config.size, this.config.selectCurrentObject, this.config.filterCriteria];
    }, () => {
      if (this.config.size && parseInt(this.config.size) !== this.config.limit) {
        this.config.limit = parseInt(this.config.size);
      }
      if (this.config.selectedObjects && this.config.selectedObjects.length || this.config.selectCurrentObject) {
        this.obtainComments();
      } else {
        this.publishWidgetReadyEvent();
      }
    });
  }

  publishWidgetReadyEvent() {
    if (!this.loaded) {
      this.eventbus.publish(new WidgetReadyEvent({
        widgetId: this.control.getId()
      }));
      this.loaded = true;
    }
  }

  ngOnDestroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
  }
}