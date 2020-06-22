import {Component, View, Inject, NgScope} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {InstanceObject} from 'models/instance-object';
import {SearchService} from 'services/rest/search-service';
import 'idoc/widget/object-data-widget/object-data-widget';
import _ from 'lodash';
import models from 'sandbox/idoc/widget/object-data-widget/data/current-object.data.json!';
import template from './bootstrap.html!text';

@Component({
  selector: 'object-data-widget-bootstrap'
})
@View({
  template: template
})
@Inject(PromiseAdapter, SearchService, NgScope)
class ObjectDataWidgetBootstrap {

  constructor(promiseAdapter, searchService, $scope) {
    this.visible = false;
    this.searchDataset = 'single';
    this.modelingMode = true;
    this.objectModel = this.createObjectModel(true);

    $scope.$watch(() => {
      return this.searchDataset;
    }, (newValue)=> {
      searchService.dataset = newValue;
    });

    this.context = {
      mode: 'EDIT',
      getMode: () => {
        return 'edit';
      },
      getCurrentObject: () => {
        if (this.modelingMode) {
          this.objectModel = this.createObjectModel(false);
        } else {
          this.objectModel = this.createObjectModel(true);
        }
        return promiseAdapter.resolve(this.objectModel);
      },
      getSharedObject: () => {
        if (this.modelingMode) {
          this.objectModel = this.createObjectModel(false);
        } else {
          this.objectModel = this.createObjectModel(true);
        }
        return promiseAdapter.resolve(this.objectModel);
      },
      getSharedObjects: () => {
        return promiseAdapter.resolve({
          data: [this.objectModel]
        });
      },
      getSharedObjectsRegistry: () => {
        return {
          onWidgetDelete: () => {
          }
        }
      },
      isModeling: () => {
        return this.modelingMode;
      },
      isEditMode: () => {
        return true;
      },
      isPrintMode: () => {
        return false;
      },
      getCurrentObjectId: () => {

      }
    };
  }

  toggleWidget() {
    this.visible = !this.visible;
  }

  createObjectModel(withData) {
    let model = _.cloneDeep(models);
    return new InstanceObject('emf:123', model, 'content', false);
  }
}
