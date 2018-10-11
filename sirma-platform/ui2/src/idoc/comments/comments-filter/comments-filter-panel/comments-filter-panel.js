import {Component, View, Inject, NgCompile, NgScope, NgElement} from 'app/app';
import 'components/datetimepicker/datetimepicker';
import 'components/select/select';
import {Configurable} from 'components/configurable';
import {Configuration} from 'common/application-config';
import {TranslateService} from 'services/i18n/translate-service';
import {OPEN, RESOLVED} from 'idoc/comments/comment-status';
import _ from 'lodash';
import template from './comments-filter-panel.html!text';
import './comments-filter-panel.css!css';

@Component({
  selector: 'comments-filter-dialog-panel',
  properties: {
    'config': 'config'
  }
})
@View({
  template: template
})
@Inject(NgCompile, NgScope, NgElement, TranslateService, Configuration)
export class CommentsFilterPanel extends Configurable {
  constructor($compile, $scope, $element, translateService, configuration) {
    super({
      filters: {
        // workaround for select2's null values
        author: ''
      }
    });
    this.$scope = $scope;
    this.$compile = $compile;
    this.$element = $element;

    this.configuration = configuration;
    this.translateService = translateService;
  }

  ngOnInit() {
    this.filters = _.cloneDeep(this.config.filters);

    if(this.config.comments) {
      this.config.comments().then((comments)=> {
        this.authorConfig = {
          placeholder: this.translateService.translateInstant('comments.filter.by.autor'),
          cssClass: 'author-field form-control',
          data: this.getAuthors(comments),
          allowClear: true,
          multiple: true
        };
        var compiled = this.$compile('<seip-select config="commentsFilterPanel.authorConfig" ng-model="commentsFilterPanel.filters.author"></seip-select>')(this.$scope);
        this.$element.find('.comment-authors').replaceWith(compiled);
      });
    }

    this.commentStatusConfig = {
      placeholder: this.translateService.translateInstant('comments.filter.by.status'),
      cssClass: 'comment-status-field',
      data: [
        {id: OPEN, text: this.translateService.translateInstant('comments.status.OPEN')},
        {id: RESOLVED, text: this.translateService.translateInstant('comments.status.ON_HOLD')}
      ]
    };

    let dateFormat = this.configuration.get(Configuration.UI_DATE_FORMAT);
    let timeFormat = this.configuration.get(Configuration.UI_TIME_FORMAT);

    this.fromDateConfig = {
      cssClass: 'from-date-field',
      defaultValue: this.filters.fromDate,
      placeholder: 'search.date.from.placeholder',
      dateFormat: dateFormat,
      timeFormat: timeFormat
    };

    this.toDateConfig = {
      cssClass: 'end-date-field',
      defaultValue: this.filters.toDate,
      placeholder: 'search.date.to.placeholder',
      dateFormat: dateFormat,
      timeFormat: timeFormat
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