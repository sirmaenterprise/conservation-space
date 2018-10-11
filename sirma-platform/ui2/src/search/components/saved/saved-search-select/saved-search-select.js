import {Component, View, Inject, NgElement, NgScope, NgTimeout} from 'app/app';
import {Configurable} from 'components/configurable';
import {SavedSearchesLoader} from 'search/components/saved/saved-searches-loader';
import {SearchService} from 'services/rest/search-service';
import _ from 'lodash';

import './saved-search-select.css!css';
import template from './saved-search-select.html!text';

export const SAVED_SEARCH_URI = 'emf:SavedSearch';
export const OPEN_SAVED_SEARCH_EVENT = 'open-saved-search';

/**
 * Wrapper component with a select for searching and loading saved searches.
 *
 * Constructs a new search mediator that triggers search only for instances with type emf:SavedSearch.
 *
 * It requires a provided search mediator in the configuration property of the component to notify other components
 * that a search has been loaded.
 *
 * @author Tsvetomir Dimitrov
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-saved-search-select',
  properties: {
    'config': 'config'
  }
})
@View({
  template: template
})
@Inject(NgElement, NgScope, NgTimeout, SearchService)
export class SavedSearchSelect extends Configurable {

  constructor($element, $scope, $timeout, searchService) {
    super({});
    this.$scope = $scope;
    this.$element = $element;
    this.$timeout = $timeout;
    this.searchService = searchService;
  }

  ngOnInit() {
    this.config.selectConfig = _.defaults(this.config, this.getDefaultConfig());
    this.savedSearchesLoader = new SavedSearchesLoader(this.searchService);
    this.applyClosingListener();
  }

  getDefaultConfig() {
    let loader = (params) => this.savedSearchLoader(params);
    let converter = (response) => this.savedSearchConverter(response);
    let selectEvent = (event) => this.openSavedSearch(event);
    return {
      delay: 500,
      multiple: false,
      dataLoader: loader,
      dataConverter: converter,
      listeners: {
        'select2:select': selectEvent
      }
    };
  }

  savedSearchLoader(params) {
    let terms = params && params.data && params.data.q || '';
    return this.savedSearchesLoader.filterSavedSearches(terms);
  }

  savedSearchConverter(response) {
    return (response.values || []).map((savedSearch) => SavedSearchesLoader.convertSavedSearch(savedSearch));
  }

  openSavedSearch(event) {
    // Transform the event payload ?
    this.config.searchMediator.trigger(OPEN_SAVED_SEARCH_EVENT, event.params.data);
    // Clearing the select's model in case the same search is selected again - it needs to trigger ng-change
    this.selectedSearch = undefined;
  }

  applyClosingListener() {
    this.$element.on('select2:closing', ()=> {
      this.config.visible = false;
      this.$scope.$digest();
    });
  }

  triggerSavedSearchSelect() {
    this.config.visible = true;
    this.$timeout(()=> {
      this.select.select2('open');
    });
  }

  get select() {
    return this.$element.find('select');
  }
}