import {Component, View, Inject, NgScope} from 'app/app';
import {PollingUtils} from 'common/polling-utils';

import template from './polling-utils-bootstrap.html!text';

@Component({
  selector: 'polling-utils-bootstrap'
})
@View({
  template: template
})
@Inject(PollingUtils, NgScope)
export class PollingUtilsBootstrap {

  constructor(pollingUtils, $scope) {
    this.pollingUtils = pollingUtils;
    this.$scope = $scope;
    this.counter = 0;
  }

  startPolling() {
    this.pollingTask = this.pollingUtils.pollInfinite('counterTask', () => {
      if (this.counter === 5) {
        this.pollingTask.stop();
        return;
      }
      this.$scope.$evalAsync(() => {
        this.counter += 1;
      });
    }, 100, true);
  }

  stopPolling() {
    this.pollingTask.stop();
  }

  showPopup () {
    alert(this.counter);
  }

}