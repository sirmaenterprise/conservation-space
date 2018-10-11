import {Component, View, Inject, NgScope} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {SELECT_OBJECT_MANUALLY} from 'idoc/widget/object-selector/object-selector';
import {SearchService} from 'services/rest/search-service';
import _ from 'lodash';

import data from 'sandbox/idoc/widget/content-viewer/services/search-service.data.json!';
import template from 'content-viewer-bootstrap-template!text';

@Component({
  selector: 'content-viewer-bootstrap'
})
@View({
  template: template
})
@Inject(NgScope, SearchService, PromiseAdapter)
class ContentViewerBootstrap {

  constructor($scope, searchService, promiseAdapter) {
    this.searchService = searchService;
    this.isModeling = false;
    $scope.$watch(() => {
      return this.searchDataset;
    }, (newValue)=> {
      this.searchService.dataset = newValue;
    });

    this.searchDataset = 'empty';

    this.context = {
      getMode: () => {
        return 'edit';
      },
      isModeling: () => {
        return this.isModeling;
      },
      getCurrentObject: () => {
        return promiseAdapter.resolve({
          getId: () => {
            return 'pdf';
          },
          isPersisted: () => {
            return true;
          }
        });
      },
      getSharedObject: (id) => {
        let objects = data[this.searchService.dataset].data.values;
        let sharedObject =_.find(objects, (object) => {
          return object.id === id;
        });

        return promiseAdapter.resolve({
          id: id,
          getId: () => id,
          getPropertyValue: (propertyName) => {
            return sharedObject.properties[propertyName];
          }
        });
      },
      getSharedObjects: () => {
        return promiseAdapter.resolve([]);
      }, isPrintMode: () => {
        return false;
      }, isEditMode: () => {
        return false;
      }
    };
  }

  insertWidget() {
    this.showWidget = !this.showWidget;
  }
}