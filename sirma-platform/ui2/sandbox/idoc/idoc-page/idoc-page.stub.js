import {Component, View, Inject} from 'app/app';
import {Configuration} from 'common/application-config';
import {IdocPage} from 'idoc/idoc-page';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {InstanceRestService} from 'services/rest/instance-service';
import data from 'sandbox/idoc/idoc-page/idoc-page-stub.data.json!';
import template from 'sandbox/idoc/idoc-page/idoc-page.stub.html!text';

@Component({
  selector: 'idoc-page-stub'
})
@View({
  template: template
})
@Inject(StateParamsAdapter, InstanceRestService)
export class IdocPageStub {

  constructor(stateParamsAdapter, instanceRestService) {
    // re-init global state before loading. Required for sandbox restart without refresh
    stateParamsAdapter.init();
    instanceRestService.init();

    sessionStorage.setItem('models', JSON.stringify(data));
  }
}
