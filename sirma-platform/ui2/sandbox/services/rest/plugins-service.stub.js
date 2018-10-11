import {Injectable, Inject} from 'app/app';
import {IdocEditModeRouteInterrupter} from 'idoc/idoc-navigation/idoc-edit-mode-route-interrupter';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {LocalStorageService} from 'services/storage/local-storage-service';

@Injectable()
@Inject(PromiseAdapter, LocalStorageService)
export class PluginsService {
  constructor(promise, localStorageService) {
    this.promise = promise;
    this.localStorageService = localStorageService;
  }

  loadPluginServiceModules(options) {
    return this.promise.resolve({"idocEditModeRouteInterrupter" : new IdocEditModeRouteInterrupter(this.localStorageService)});
  }

}
