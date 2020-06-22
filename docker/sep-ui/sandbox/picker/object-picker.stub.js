import {Component, View, Inject, NgScope} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {PickerService, SEARCH_EXTENSION} from 'services/picker/picker-service';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {InstanceObject} from 'models/instance-object';
import 'components/extensions-panel/extensions-panel';
import _ from 'lodash';

import template from 'object-picker-template!text';

@Component({
  selector: 'seip-object-picker-stub'
})
@View({
  template: template
})
@Inject(NgScope, PickerService, PromiseAdapter)
export class ObjectPickerStub {

  constructor($scope, pickerService, promiseAdapter) {
    this.$scope = $scope;
    this.pickerService = pickerService;

    this.embeddedPickerConfig = {extensions: {}};
    this.embeddedPickerConfig.extensions[SEARCH_EXTENSION] = {
      useRootContext: false,
      results: {
        config: {
          selection: MULTIPLE_SELECTION
        }
      }
    };
    this.pickerService.assignDefaultConfigurations(this.embeddedPickerConfig);

    this.dialogConfig = {
      exclusions: ['seip-object-picker-test-extension']
    };
    this.pickerService.assignDefaultConfigurations(this.dialogConfig);

    this.singleExtensionPickerConfig = {
      inclusions: ['seip-object-picker-test-extension']
    };
    this.pickerService.assignDefaultConfigurations(this.singleExtensionPickerConfig);

    this.context = this.getCurrentContext([{'id': '1', readAllowed: true}]);
    this.pickerContext = this.getCurrentContext([
      {'id': '1', readAllowed: true},
      {'id': '2', readAllowed: true},
      {'id': '3', readAllowed: true}
    ]);
  }

  openPickerDialog() {
    var promise = this.pickerService.open(this.dialogConfig, this.pickerContext).then((selectedItems) => {
      this.dialogSelection = JSON.stringify(selectedItems);
    }).catch(() => {
      // Handling promise rejection to avoid errors
    });
  }

  openSingleExtensionPickerDialog() {
    this.pickerService.open(this.singleExtensionPickerConfig, this.context);
  }

  getCurrentContext(config) {
    return {
      getCurrentObject: () => {
        let instance = new InstanceObject(config[0].id);
        instance.setContextPath(config);
        return Promise.resolve(instance);
      },
      getCurrentObjectId: function () {
      }
    };
  }
}