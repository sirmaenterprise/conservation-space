import {Component, View, Inject, NgScope} from 'app/app';
import {SearchMediator} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder'
import {SearchService} from 'services/rest/search-service';
import {InstanceObject} from 'models/instance-object';
import {AdvancedSearchComponents} from 'search/components/advanced/advanced-search-components';
import  'search/components/advanced/advanced-search';
import _ from 'lodash';


import searchCriteria from 'advanced-search-criteria.data.json!';
import template from 'advanced-search-template!text';

@Component({
  selector: 'seip-advanced-search-stub'
})
@View({
  template: template
})
@Inject(NgScope, SearchService)
export class AdvancedSearchStub {

  constructor($scope, searchService) {
    this.searchService = searchService;

    this.setEmptyCriteria();
    this.setContext();

    $scope.$watch(()=> {
      return this.config.searchMediator.queryBuilder.tree;
    }, (criteria)=> {
      this.results = JSON.stringify(criteria, null, 3);
    }, true);
  }

  setPredefinedCriteria() {
    this.setCriteria(_.cloneDeep(searchCriteria.predefined));
  }

  setPredefinedCriteria2() {
    this.setCriteria(_.cloneDeep(searchCriteria.predefined2));
  }

  setEmptyCriteria() {
    this.setCriteria(_.cloneDeep(searchCriteria.empty));
  }

  toggleLock() {
    if (!this.config.locked || this.config.locked.length < 1) {
      this.config.locked = AdvancedSearchComponents.getAllComponents();
    } else {
      this.config.locked = [];
    }
    this.setCriteria(_.cloneDeep(this.criteria));
  }

  setCriteria(criteria) {
    this.criteria = criteria;
    if (!this.config) {
      this.config = {
        searchMediator: this.getMediator()
      };
    } else {
      this.config.searchMediator.queryBuilder.init(this.criteria);
    }
  }

  setContext() {
    this.context = this.getCurrentContext();
  }

  getMediator() {
    return new SearchMediator(this.searchService, new QueryBuilder(this.criteria));
  }

  toggleDisabled() {
    this.config.disabled = !this.config.disabled;
  }

  getCurrentContext() {
    return {
      getCurrentObject: () => {
        let contextPath = [{'id': 'test_id'}];
        let instance = new InstanceObject(contextPath[0].id);
        instance.setContextPath(contextPath);
        return Promise.resolve(instance);
      }
    };
  }
}
