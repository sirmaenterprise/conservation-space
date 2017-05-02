import {View, Component, Inject, NgElement} from 'app/app';
import 'services/i18n/translate-service';
import 'idoc/comments/comments-filter/comments-filter';
import {CommentsFilteredEvent} from 'idoc/comments/comments-filter/comments-filtered-event';
import {Eventbus} from 'services/eventbus/eventbus';
import {EMPTY_FILTERS} from 'idoc/comments/comments-filter/comments-filter-const';
import 'components/ui-preference/ui-preference';
import _ from 'lodash';
import commentsToolbarTemplate from 'idoc/idoc-comments/idoc-comments-toolbar.html!text';

@Component({
  selector: 'seip-idoc-comments-toolbar',
  properties: {
    'commentsComponent': 'comments-component',
    'context': 'context'
  }
})
@View({
  template: commentsToolbarTemplate
})
@Inject(Eventbus, NgElement)
export class IdocCommentsToolbar {

  constructor(eventbus, element) {
    this.filterConfig = this.commentsComponent.config.filtersConfig;
    this.event = eventbus.subscribe(CommentsFilteredEvent, this.applyFilter.bind(this));
    this.element = element;

    this.uiPreferenceConfig = {
      sourceElements: {
        top: '.idoc-wrapper .fixed-container'
      },
      copyParentWidth: '.idoc-comments-wrapper'
    };
  }

  applyFilter(event) {
    if (event[0] === this.commentsComponent.tabId) {
      if (!_.isEqual(EMPTY_FILTERS, this.commentsComponent.config.filtersConfig.filters)) {
        this.element.find('.btn-filter-comment').addClass('filtered');
      } else {
        this.element.find('.btn-filter-comment').removeClass('filtered');
      }
    }
  }

  ngOnDestroy() {
    this.element.remove();
    this.event.unsubscribe();
  }
}