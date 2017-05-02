import {Injectable, Inject} from 'app/app';
import {RouteInterrupter} from 'adapters/router/route-interrupter';
import {MODE_EDIT, IDOC_STATE} from 'idoc/idoc-constants';
import {LocalStorageService} from 'services/storage/local-storage-service';

@Injectable()
@Inject(LocalStorageService)
export class IdocEditModeRouteInterrupter extends RouteInterrupter {

  constructor(localStorageService) {
    super();
    this.localStorageService = localStorageService;
  }

  shouldInterrupt(router) {
    var timeout = this.localStorageService.get(LocalStorageService.SESSION_TIMEOUT) === 'true';
    return router.getCurrentState() === IDOC_STATE && router.$state.params.mode === MODE_EDIT && !timeout;
  }

}