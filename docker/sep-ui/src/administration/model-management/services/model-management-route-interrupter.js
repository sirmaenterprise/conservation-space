import {Injectable, Inject} from 'app/app';
import {RouteInterrupter} from 'adapters/router/route-interrupter';
import {ModelManagementStateRegistry} from 'administration/model-management/services/model-management-state-registry';
import {MODEL_MANAGEMENT_EXTENSION_POINT} from 'administration/model-management/model-management';

/**
 * Prevents navigation when there are unsaved changes in the model management page.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(ModelManagementStateRegistry)
export class ModelManagementRouteInterrupter extends RouteInterrupter {

  constructor(modelManagementStateRegistry) {
    super();
    this.modelManagementStateRegistry = modelManagementStateRegistry;
  }

  shouldInterrupt(router) {
    return router.getCurrentState() === MODEL_MANAGEMENT_EXTENSION_POINT && this.modelManagementStateRegistry.hasDirtyState();
  }

}