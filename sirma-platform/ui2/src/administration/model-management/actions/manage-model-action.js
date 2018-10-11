import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {Router} from 'adapters/router/router';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {NamespaceService} from 'services/rest/namespace-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {
  MODEL_MANAGEMENT_QUERY_PARAMETER,
  MODEL_MANAGEMENT_EXTENSION_POINT
} from 'administration/model-management/model-management';

@Injectable()
@Inject(Router, StateParamsAdapter, NamespaceService, PromiseAdapter)
export class ManageModelAction extends ActionHandler {

  constructor(router, stateParamsAdapter, namespaceService, promiseAdapter) {
    super();
    this.router = router;
    this.stateParamsAdapter = stateParamsAdapter;
    this.namespaceService = namespaceService;
    this.promiseAdapter = promiseAdapter;
  }

  execute(action, actionContext) {
    let modelId = actionContext.currentObject.getId();
    if (this.namespaceService.isFullUri(modelId)) {
      this.navigate(modelId);
      return this.promiseAdapter.resolve();
    } else {
      return this.namespaceService.convertToFullURI([modelId]).then(converted => {
        this.navigate(converted[0]);
      });
    }
  }

  navigate(modelId) {
    this.stateParamsAdapter.setStateParam(MODEL_MANAGEMENT_QUERY_PARAMETER, modelId);
    this.router.navigate(MODEL_MANAGEMENT_EXTENSION_POINT, this.stateParamsAdapter.getStateParams());
  }

}