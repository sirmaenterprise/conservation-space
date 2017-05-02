import _ from 'lodash';
import {Component, Inject, NgElement, NgScope, NgTimeout} from 'app/app';
import {Select} from 'components/select/select';
import {SearchService} from 'services/rest/search-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchMediator} from 'search/search-mediator';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

const OBJECT_SELECT_PROPERTIES = ['id', 'title'];

@Component({
  selector: 'seip-object-select',
  properties: {
    'config': 'config'
  }
})
@Inject(NgElement, NgScope, NgTimeout, SearchService, InstanceRestService)
export class ObjectSelect extends Select {

  constructor($element, $scope, $timeout, searchService, instanceRestService) {
    super($element, $scope, $timeout);
    this.searchService = searchService;
    this.instanceRestService = instanceRestService;

    var queryBuilder = new QueryBuilder({});
    var searchArguments = {
      properties: OBJECT_SELECT_PROPERTIES
    };
    // TODO: Remove the search mode when the basic search is routed to the new search query building
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
      let searchTerm = params && params.data && params.data.q || '';

      var searchTree = this.getSearchTree(searchTerm);
      this.mediator.queryBuilder.init(searchTree);

      return this.mediator.search().then((searchResponse) => {
        this.appendPredefinedItems(searchResponse.response);
        return searchResponse.response;
      });
    };

    let mapper = (ids) => {
      return new Promise((resolve) => {
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

        Promise.all(promises).then((objects) => {
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
    return {
      rules: [{
        // TODO: Configure it ?
        field: 'dcterms:title',
        type: 'string',
        operator: AdvancedSearchCriteriaOperators.CONTAINS.id,
        value: searchTerm || ''
      }]
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

}