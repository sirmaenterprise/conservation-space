import {NgScope, NgElement, Component, View, Inject} from 'app/app';
import {BasicSearchCriteria} from 'search/components/common/basic-search-criteria';
import {QueryBuilder, DynamicTreeWalkListener} from 'search/utils/query-builder';
import {SearchMediator, EVENT_SEARCH, EVENT_BEFORE_SEARCH_CRITERIA_CHANGED} from 'search/search-mediator';
import {SearchService} from 'services/rest/search-service';
import {CURRENT_OBJECT} from 'search/resolvers/contextual-rules-resolver'
import _ from 'lodash';

import template from 'basic-search-criteria-template!text';

@View({
  template: template
})
@Component({
  selector: 'seip-basic-search-criteria-stub'
})
@Inject(NgScope, NgElement, SearchService)
export class BasicSearchCriteriaStub {

  constructor($scope, $element, service) {
    let mediator = new SearchMediator(service, new QueryBuilder(), this.getContext());
    this.config = {
      searchMediator: mediator,
      contextualItems: [{
        id: CURRENT_OBJECT,
        properties: {
          title: 'Current object'
        }
      }]
    };

    let results = $element.find('#results');
    this.registerCriteriaWatcher($scope, mediator.queryBuilder.tree, results);
    mediator.registerListener(EVENT_SEARCH, () => {
      results.val('searched');
    });

    let today = new Date();
    if (today.getDate() === 1) {
      today.setDate(2);
    }

    let yesterday = new Date();
    yesterday.setDate(today.getDate() - 1);
    yesterday.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);

    let initialTree = {
      condition: 'OR',
      rules: [
        {
          condition: 'AND',
          rules: [
            {
              field: 'types',
              type: 'object',
              operation: 'equal',
              value: ['http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document']
            }, {
              condition: 'AND',
              rules: [
                {field: 'freeText', type: 'fts', operation: 'contains', value: 'initial'},
                {
                  field: 'emf:createdBy',
                  type: 'object',
                  operation: 'set_to',
                  renderSeparately: true,
                  value: ['janedoe@doeandco.com']
                },
                {
                  field: 'emf:createdOn',
                  type: 'dateTime',
                  operation: 'between',
                  value: [yesterday.toISOString(), today.toISOString()]
                },
                {field: 'rel:that', type: 'object', operation: 'set_to', value: ['2']}
              ]
            }
          ]
        }
      ]
    };

    mediator = new SearchMediator(service, new QueryBuilder(_.cloneDeep(initialTree)));
    this.registerCriteriaWatcher($scope, mediator.queryBuilder.tree, results);
    this.withInitialCriteria = {
      searchMediator: mediator
    };

    mediator = new SearchMediator(service, new QueryBuilder(_.cloneDeep(initialTree)));
    this.resetCriteria = {
      searchMediator: mediator
    };
  }

  registerCriteriaWatcher($scope, tree, textarea) {
    $scope.$watch(()=> {
      return tree;
    }, (newValue) => {
      var omitted = _.cloneDeep(newValue);
      let removeListener = new DynamicTreeWalkListener().addOnAny((rule) => {
        delete rule.id;
      });
      QueryBuilder.walk(omitted, removeListener);
      textarea.val(JSON.stringify(omitted, null, 3));
    }, true);
  }

  ngOnInit() {
    // Resolving race conditions with the translate service
    this.render = true;
  }

  getContext() {
    return {
      getCurrentObject: () => {
        return new Promise((resolve) => {
          resolve({
            getId: () => {
              return 'resolved-id';
            }
          });
        });
      }
    }
  }
}