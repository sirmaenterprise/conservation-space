import {Injectable, Inject} from 'app/app';
import {RouteInterrupter} from 'adapters/router/route-interrupter';
import {ADMINISTRATION_STATE} from 'administration/admin-configuration';
import {AdminToolRegistry} from 'administration/admin-tool-registry';

/**
 * Prevents navigation when there are unsaved changes in the administration page.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(AdminToolRegistry)
export class AdminToolRouteInterrupter extends RouteInterrupter {

  constructor(adminToolRegistry) {
    super();
    this.adminToolRegistry = adminToolRegistry;
  }

  shouldInterrupt(router) {
    return router.getCurrentState() === ADMINISTRATION_STATE && this.adminToolRegistry.hasUnsavedState();
  }

}