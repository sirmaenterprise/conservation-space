import {Component, View, Inject, NgCompile, NgScope, NgElement} from 'app/app';
import 'components/datetimepicker/datetimepicker';
import 'components/select/select';
import './comments-filter-panel.css!css';
import {TranslateService} from 'services/i18n/translate-service';
import {OPEN, RESOLVED} from 'idoc/comments/comment-status';
import _ from 'lodash';
import template from './comments-filter-panel.html!text';

@Component({
  selector: 'comments-filter-dialog-panel',
  properties: {
    'config': 'config'
  }
})
@View({
  template: template
})
@Inject(NgCompile, NgScope, NgElement, TranslateService)
export class CommentsFilterPanel {
  constructor($compile, $scope, $element, translateService) {
    this.filters = {};
    if (this.config.filters) {
      this.filters = _.cloneDeep(this.config.filters);
    }
    //workaround for select2's null values
    if (!this.filters.author) {
      this.filters.author = '';
    }

    this.$compile = $compile;

    this.config.comments().then((comments)=> {
      this.authorConfig = {
        placeholder: translateService.translateInstant('comments.filter.by.autor'),
        cssClass: 'author-field form-control',
        data: this.getAuthors(comments),
        allowClear: true,
        multiple: true
      };
      var compiled = this.$compile('<seip-select config="commentsFilterPanel.authorConfig" ng-model="commentsFilterPanel.filters.author"></seip-select>')($scope);
      $element.find('.comment-authors').replaceWith(compiled);
    });

    this.commentStatusConfig = {
      placeholder: translateService.translateInstant('comments.filter.by.status'),
      cssClass: 'comment-status-field',
      data: [{id: OPEN, text: translateService.translateInstant('comments.status.OPEN')}, {id: RESOLVED, text: translateService.translateInstant('comments.status.ON_HOLD')}]
    };

    this.fromDateConfig = {
      placeholder: 'search.createdfrom.placeholder',
      cssClass: 'from-date-field',
      defaultValue: this.filters.fromDate
    };

    this.toDateConfig = {
      placeholder: 'search.createdto.placeholder',
      cssClass: 'end-date-field',
      defaultValue: this.filters.toDate
    };
  }

  clearFilters() {
    for (let filter in this.filters) {
      this.filters[filter] = '';
    }
  }

  getAuthors(comments) {
    let addedAuthors = new Map();
    let authors = [];
    if (comments && comments.length) {
      this.getCommentsAuthors(comments, authors, addedAuthors);
      for (let comment of comments) {
        if (comment.getReplies() && comment.getReplies().length) {
          this.getCommentsAuthors(comment.getReplies(), authors, addedAuthors);
        }
      }
    }
    return authors;
  }

  getCommentsAuthors(comments, authors, addedAuthors) {
    for (let comment of comments) {
      if (!addedAuthors.get(comment.getAuthorId())) {
        authors.push({id: comment.getAuthorId(), text: comment.getAuthorLabel()});
        addedAuthors.set(comment.getAuthorId(), true);
      }
    }
  }

  saveToConfig() {
    this.config.filters = _.cloneDeep(this.filters);
  }
}