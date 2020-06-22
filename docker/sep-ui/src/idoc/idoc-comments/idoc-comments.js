import {View, Component, Inject, NgTimeout} from 'app/app';
import {CommentsRestService} from 'services/rest/comments-service';
import {ReloadCommentsEvent} from 'idoc/comments/events/reload-comments-event';
import {Eventbus} from 'services/eventbus/eventbus';
import {IdocCommentsDataProvider} from 'idoc/idoc-comments/idoc-comments-data-provider';
import {CommentsHelper} from 'idoc/comments/comments-helper';
import 'idoc/idoc-comments/idoc-comments-toolbar';
import 'idoc/comments/comments';
import {CommentsFilteredEvent} from 'idoc/comments/comments-filter/comments-filtered-event';
import {CommentsFilterService} from 'idoc/comments/comments-filter/comments-filter-service';
import {EMPTY_FILTERS} from 'idoc/comments/comments-filter/comments-filter-const';
import _ from 'lodash';
import commentTemplate from 'idoc/idoc-comments/idoc-comments.html!text';
import 'idoc/idoc-comments/idoc-comments.css!';

@Component({
  selector: 'seip-idoc-comments',
  properties: {
    'context': 'context',
    'tab': 'tab'
  }
})
@View({
  template: commentTemplate
})
@Inject(CommentsRestService, Eventbus, CommentsFilterService, NgTimeout)
export class IdocComments {

  constructor(commentsRestService, eventbus, commentsFilterService, $timeout) {
    this.displayComments = false;
    this.context.getCurrentObject().then(currentObject => {
      if (currentObject.isPersisted()) {
        $timeout(() => {
          this.displayComments = true;
          this.init(commentsRestService, eventbus, commentsFilterService);
        }, 0);
      }
    });
  }

  init(commentsRestService, eventbus, commentsFilterService) {
    this.dataProvider = new IdocCommentsDataProvider(commentsRestService);
    this.eventbus = eventbus;
    this.commentsFilterService = commentsFilterService;
    this.loadComments();
    this.events = [
      this.eventbus.subscribe(ReloadCommentsEvent, (event) => {
        // The event is not widget specific so trigger only if it is for the tab
        if (event[0] === this.tab.id) {
          this.loadComments(true);
        }
      }),
      this.eventbus.subscribe(CommentsFilteredEvent, this.handleCommentsFilteredEvent.bind(this))
    ];

    let filters = _.cloneDeep(EMPTY_FILTERS);

    this.filtersConfig = {
      filters,
      comments: () => {
        // since loadComments returns both comments and their replies due to bug
        // TODO: update this when bug is fixed : CMF-24076
        return this.dataProvider.loadComments(this.context.getCurrentObjectId(), this.tab.id).then((response) => {
          return this.allComments = CommentsHelper.convertAndSort(response.data);
        });
      },
      tabId: this.tab.id
    };

    this.commentsComponentConfig = {
      dataProvider: this.dataProvider,
      tabId: this.tab.id,
      toolbarComponent: 'seip-idoc-comments-toolbar',
      filtersConfig: this.filtersConfig
    };
  }

  handleCommentsFilteredEvent(event) {
    if (event[0] === this.tab.id) {
      this.comments = this.commentsFilterService.filter(this.allComments, this.filtersConfig.filters);
    }
  }

  loadComments(firstExpanded) {
    this.dataProvider.loadComments(this.context.getCurrentObjectId(), this.tab.id).then((response) => {
      if (response.data) {
        this.comments = CommentsHelper.convertAndSort(response.data);
        if (this.comments && this.comments.length) {
          this.comments = this.commentsFilterService.filter(this.comments, this.filtersConfig.filters);
        }
        if (this.comments.length) {
          this.comments[0].expanded = firstExpanded;
        }
      }
    });
  }

  ngOnDestroy() {
    if (this.events) {
      for (let event of this.events) {
        event.unsubscribe();
      }
    }
  }
}
