import _ from 'lodash';
import {Component, Inject, NgElement, NgScope, NgTimeout} from 'app/app';
import {Select} from 'components/select/select';
import {SearchService} from 'services/rest/search-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchMediator} from 'search/search-mediator';

import {EMF_MODIFIED_ON} from 'instance/instance-properties';
import {ORDER_DESC, ORDER_RELEVANCE} from 'search/order-constants';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

export const OBJECT_SELECT_PROPERTIES = ['id', 'title'];

@Component({
  selector: 'seip-object-select',
  properties: {
    'config': 'config'
  }
})
@Inject(NgElement, NgScope, NgTimeout, SearchService, InstanceRestService, PromiseAdapter)
export class ObjectSelect extends Select {

  constructor($element, $scope, $timeout, searchService, instanceRestService, promiseAdapter) {
    super($element, $scope, $timeout);
    this.searchService = searchService;
    this.instanceRestService = instanceRestService;
    this.promiseAdapter = promiseAdapter;

    var queryBuilder = new QueryBuilder({});
    var searchArguments = {
      properties: OBJECT_SELECT_PROPERTIES,
      orderDirection: ORDER_DESC
    };
    this.mediator = new SearchMediator(this.searchService, queryBuilder, undefined, searchArguments, SearchCriteriaUtils.ADVANCED_MODE);
  }

  createActualConfig() {
    let converter = (response) => {
      let values = response.data.values;
      if (!values) {
        return [];
      }

      return values.map((item) => {
        return {
          id: item.id,
          text: this.getInstanceTitle(item)
        };
      });
    };

    let loader = (params) => {
      this.mediator.abortLastSearch();
      let searchTerm = params && params.data && params.data.q || '';
      let searchTree = this.getSearchTree(searchTerm);

      this.mediator.queryBuilder.init(searchTree);
      this.prepareSearchMediatorArguments(searchTerm);

      return this.mediator.search(true).then((searchResponse) => {
        this.appendPredefinedItems(searchResponse.response);
        return searchResponse.response;
      });
    };

    let mapper = (ids) => {
      return this.promiseAdapter.promise((resolve) => {
        let result = [];
        let promises = [];

        ids.forEach((id) => {
          let item = this.findPredefinedItem(id);
          if (item) {
            result.push({
              id: item.id,
              text: this.getInstanceTitle(item)
            });
          } else {
            promises.push(this.instanceRestService.load(id));
          }
        });

        this.promiseAdapter.all(promises).then((objects) => {
          objects.forEach((object) => {
            result.push({
              id: object.data.id,
              text: this.getInstanceTitle(object.data)
            });
          });
          resolve(result);
        });
      });
    };

    let defaultConfig = {
      multiple: true,
      delay: 250,
      dataLoader: loader,
      dataConverter: converter,
      mapper: mapper
    };

    this.config = _.defaults(this.config, defaultConfig);
    super.createActualConfig();
  }

  findPredefinedItem(id) {
    return _.find(this.config.predefinedItems, (item) => {
      return item.id === id;
    });
  }

  appendPredefinedItems(response) {
    if (!this.config.predefinedItems) {
      return;
    }
    var values = response.data.values || [];
    response.data.values = [...this.config.predefinedItems, ...values];
  }

  getSearchTree(searchTerm) {
    let rules = [];

    // Avoid building empty free text rule
    if (searchTerm && searchTerm.length) {
      rules.push(SearchCriteriaUtils.getDefaultFreeTextRule(searchTerm));
    }

    // When performing a filter return results only amongst availableObjects (array of URIs)
    if (this.config.availableObjects) {
      rules.push(SearchCriteriaUtils.buildRule('instanceId', undefined, AdvancedSearchCriteriaOperators.IN.id, this.config.availableObjects));
    }

    // When performing a filter return results only amongst objects of a given type
    if (this.config.types) {
      rules.push(SearchCriteriaUtils.getDefaultObjectTypeRule(this.config.types));
    }

    return {
      condition: 'AND',
      rules
    };
  }

  /**
   * Gets the title of the provided instances. Handles the case where the title property could be an object instead
   * of a string.
   * @param instance - the provided instance
   * @returns the instance's title
   */
  getInstanceTitle(instance) {
    var titleProperty = instance.properties.title;
    if (titleProperty && titleProperty.text) {
      return titleProperty.text;
    }
    return titleProperty;
  }

  prepareSearchMediatorArguments(searchTerm) {
    // order by relevance when search terms are present, fallback do default otherwise
    let orderBy = searchTerm && searchTerm.length ? ORDER_RELEVANCE : EMF_MODIFIED_ON;
    this.mediator.arguments.orderBy = orderBy;
  }
}