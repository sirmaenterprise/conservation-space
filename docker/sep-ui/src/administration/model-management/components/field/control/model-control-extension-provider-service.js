import {Inject, Injectable} from 'app/app';
import {PluginsService} from 'services/plugin/plugins-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

const MODEL_CONTROLS_EXTENSION = 'model-management-field-controls';

/**
 * Service responsible for loading and caching extensions registered for MODEL_CONTROLS_EXTENSION point.
 *
 * @author svelikov
 */
@Injectable()
@Inject(PluginsService, PromiseAdapter)
export class ModelControlExtensionProviderService {

  constructor(pluginsService, promiseAdapter) {
    this.pluginsService = pluginsService;
    this.promiseAdapter = promiseAdapter;

    this.extensions = null;
  }

  /**
   * Loads or returns already loaded extension definitions mapped by their type attribute.
   * @return a promise which resolves with the loaded extension definitions.
   */
  loadModelControlExtensions() {
    if (!this.extensions) {
      return this.pluginsService.loadComponentModules(MODEL_CONTROLS_EXTENSION, 'type')
        .then(extensions => {
          this.extensions = extensions;
          return this.extensions;
        });
    } else {
      return this.promiseAdapter.resolve(this.extensions);
    }
  }

  getExtension(type) {
    return this.extensions[type];
  }
}