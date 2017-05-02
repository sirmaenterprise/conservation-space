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
import {Eventbus} from 'services/eventbus/eventbus';
import _ from 'lodash';
import commentsWidgetTemplate from './comments-widget.html!text';
import './comments-widget.css!css';

const ERROR_MESSAGE_LABEL = 'comments.widget.error';
@Widget
@View({
  template: commentsWidgetTemplate
})
@Inject(CommentsRestService, NgScope, NgElement, NgTimeout, DateRangeResolver, SearchResolverService, Eventbus)
export class CommentsWidget {

  constructor(commentsService, $scope, $element, $timeout, DateRangeResolver, searchResolver, eventbus) {
    this.commentsService = commentsService;
    this.dataProvider = new IdocCommentsDataProvider(this.commentsService);
    this.$scope = $scope;
    this.$element = $element;
    this.timeout = $timeout;
    this.searchResolver = searchResolver;
    this.dateRangeResolver = DateRangeResolver;
    this.eventbus = eventbus;

    this.events = [
      this.eventbus.subscribe(ReloadCommentsEvent, this.handleReloadComments.bind(this))
    ];
    this.commentsComponentConfig = {
      dataProvider: this.dataProvider,
      additionalComponent: 'comment-header',
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
      this.commentsService.loadRecentComments(config).then((response) => {
        this.publishWidgetReadyEvent();
        if (!(this.context.isModeling() && (this.config.selectCurrentObject || this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY ))) {
          this.extractRecentComments(response.data);
        } else {
          delete this.errorMessage;
        }
        this.loadingActive = false;
        this.loadingElement.css({display: 'none'});
      });

      this.commentsService.loadCommentsCount(config).then((response) => {
        this.count = response.data.count;
      });
    });
  }

  extractRecentComments(response) {
    this.comments = CommentsHelper.convertToCommentInstances(response.annotations);
    this.comments.map((comment) => {
      comment.setHeader(response.instanceHeaders[comment.getTarget()]);
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
    if (this.config.selectCurrentObject && this.config.selectedObjects.indexOf(this.context.getCurrentObjectId()) < 0) {
      loadCommentsConfig.manuallySelectedObjects.push(this.context.getCurrentObjectId());
    }
    if (this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
      return this.searchResolver.resolve(_.cloneDeep(this.config.criteria), this.context).then((response) => {
        //the resolver returns array with two equal objects
        loadCommentsConfig.criteria = response[0];
        return Promise.resolve(loadCommentsConfig);
      });
    } else {
      loadCommentsConfig.manuallySelectedObjects.push.apply(loadCommentsConfig.manuallySelectedObjects, this.config.selectedObjects);
      return Promise.resolve(loadCommentsConfig);
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