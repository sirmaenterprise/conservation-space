import {Component, Inject, View} from 'app/app';
import {Configurable} from 'components/configurable';
import {QueryBuilder} from 'search/utils/query-builder';
import {ModelsService} from 'services/rest/models-service';
import {
  ANY_OBJECT,
  ANY_RELATION,
  CRITERIA_FTS_RULE_FIELD,
  CRITERIA_TYPES_RULE_FIELD
} from 'search/utils/search-criteria-utils';
import {InstanceRestService} from 'services/rest/instance-service';
import {EVENT_CLEAR, EVENT_SEARCH, SearchMediator} from 'search/search-mediator';
import 'instance-header/static-instance-header/static-instance-header';
import 'components/select/select';
import _ from 'lodash';

import 'font-awesome/css/font-awesome.css!';
import template from './results-toolbar.html!text';
import './results-toolbar.css!';

/**
 * Search results component which serves to show the current number of search results, the selected
 * search type, the selected context & the content of the FTS field. The component builds a final
 * message given all, some or none of the fore mentioned criteria from the criteria tree.
 * If any of them are not specified then they are skipped. Component accepts a very basic configuration
 * containing only the search mediator and a configuration which can selectively disable or enable
 * different parts of the message which is displayed. Example configuration:
 * {
 *    searchMediator: searchMediator
 *    message: {
 *      renderResults: true,
        renderType: true,
        renderContext: true,
        renderFts: true
 *    }
 * }
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-results-toolbar',
  properties: {
    'config': 'config'
  }
})
@View({template: template})
@Inject(InstanceRestService, ModelsService)
export class ResultsToolbar extends Configurable {

  constructor(instanceRestService, modelsService) {
    super({
      message: {
        renderResults: true,
        renderType: true,
        renderContext: true,
        renderFts: true
      }
    });
    this.modelsService = modelsService;
    this.instanceRestService = instanceRestService;
  }

  ngOnInit() {
    // assign defaults
    this.resultData = {};
    this.isSearchPerformed = false;
    // initialize the component
    this.createModelTypesPromise();
    this.subscribeToSearchEvent();
    this.subscribeToClearEvent();
  }

  createModelTypesPromise() {
    this.modelPromise = this.modelsService.getModels(ModelsService.PURPOSE_SEARCH);
  }

  loadModelTypes(response) {
    this.modelTypes = {};
    _.forEach(response.models, (type) => {
      this.modelTypes[type.id] = type;
    });
  }

  subscribeToClearEvent() {
    this.config.searchMediator.registerListener(EVENT_CLEAR, () => {
      // clear result data
      this.resultData = {};
      // clear search flag
      this.isSearchPerformed = false;
    });
  }

  subscribeToSearchEvent() {
    let mediator = this.config.searchMediator;

    mediator.registerListener(EVENT_SEARCH, (eventData) => {
      if (eventData && !SearchMediator.isSearchQueryEmpty(eventData.query)) {
        let query = eventData.query;
        let response = eventData.response;
        let argumentsMap = eventData.arguments;

        // clear result data
        this.resultData = {};

        if (this.config.message.renderType) {
          // If the models are not yet loaded
          if (this.modelPromise) {
            this.modelPromise.then((response) => {
              this.loadModelTypes(response);
              this.extractObjectTypes(query);
              // delete the promise when done
              delete this.modelPromise;
            });
          } else {
            this.extractObjectTypes(query);
          }
        }

        if (this.config.message.renderContext) {
          // extract all relations & convert context breadcrumb headers
          this.resultData.relations = this.extractFieldFromTree(query, ANY_RELATION);
          this.convertContextHeaders(); // convert breadcrumb header for each relation
        }

        if (this.config.message.renderFts) {
          // extract free text search field value from tree
          this.resultData.fts = this.extractFieldFromTree(query, CRITERIA_FTS_RULE_FIELD);
        }

        // configure lower & upper bounds for found results
        this.configureResultsAndBoundaries(response.data.resultSize, argumentsMap);
        this.isSearchPerformed = true;
      }
    });
  }

  extractObjectTypes(query) {
    // extract current type from tree & type id from the model types map
    let types = this.extractFieldFromTree(query, CRITERIA_TYPES_RULE_FIELD);
    this.resultData.types = this.extractModelTypesAsPlainText(types);
  }

  configureResultsAndBoundaries(resultSize, argumentsMap) {
    // ensure that all of the arguments are always valid
    this.resultData.resultSize = Math.max(0, resultSize);
    this.resultData.pageSize = argumentsMap.pageSize || 0;
    this.resultData.pageNumber = argumentsMap.pageNumber || 1;

    this.resultData.lower = this.calculateLowerBound();
    this.resultData.upper = this.calculateUpperBound();
  }

  calculateLowerBound() {
    return 1 + (this.resultData.pageSize * this.resultData.pageNumber) - this.resultData.pageSize;
  }

  calculateUpperBound() {
    let upper = this.resultData.pageSize * this.resultData.pageNumber;
    return Math.min(upper, this.resultData.resultSize);
  }

  extractFieldFromTree(query, field) {
    let rule = QueryBuilder.getFirstRule(query.tree, field);
    // check if rule has a value associated with it
    if (QueryBuilder.ruleHasValues(rule)) {
      return rule.value;
    }
  }

  extractModelTypesAsPlainText(types = []) {
    //filter all valid types and map them to array by their label
    let labels = types.filter((type) => this.isTypeValid(type)).map((type) => {
      return this.modelTypes[type].label;
    });
    // convert model types to string
    if (labels && labels.length) {
      return labels.join(', ');
    }
  }

  convertContextHeaders() {
    let relations = this.resultData.relations;
    // filter out any object from the provided relations
    relations = _.filter(relations, r => r !== ANY_OBJECT);

    if (relations && relations.length) {
      // fetch context based on the extracted relation
      this.instanceRestService.loadBatch(relations).then((response) => {
        //map all breadcrumb_headers for fetched instances
        this.resultData.context = response.data.map((relation) => {
          return relation.headers.breadcrumb_header;
        }).join(',&nbsp;');
      });
    } else {
      // delete relations nothing to show
      delete this.resultData.relations;
    }
  }

  isTypeValid(type) {
    return type && type !== ANY_OBJECT && this.modelTypes[type];
  }

  shouldRenderContext() {
    return !!this.resultData.relations && this.config.message.renderContext;
  }

  shouldRenderType() {
    return !!this.resultData.types && this.config.message.renderType;
  }

  shouldRenderResults() {
    return !!this.resultData.resultSize && this.config.message.renderResults;
  }

  shouldRenderFts() {
    return !!this.resultData.fts && this.resultData.fts.length && this.config.message.renderFts;
  }
}