import {Inject, Injectable} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {PluginsService} from 'services/plugin/plugins-service';

const EXTENSION_POINT = 'advanced-search-operator-filter';

/**
 * Service for applying filter conditions upon a provided advanced search configuration.
 *
 * Useful for limiting the advanced search tree form building with custom conditions and logic.
 *
 * To use it, register an Injectable component in the PluginRegistry under the "advanced-search-operator-filter"
 * extension point.
 *
 * @author yasko
 */
@Injectable()
@Inject(PromiseAdapter, PluginsService)
export class AdvancedSearchFilterExecutor {

  constructor(promiseAdapter, pluginsService) {
    this.promiseAdapter = promiseAdapter;
    this.pluginsService = pluginsService;
  }

  filterOperators(searchConfig, property, operators) {
    return this.promiseAdapter.promise((resolve) => {
      if (!operators || !operators.length) {
        resolve(operators);
        return;
      }

      this.loadFilters(EXTENSION_POINT, (modules) => {
        if (!modules || !Object.keys(modules).length) {
          resolve(operators);
          return;
        }

        var filtered = operators;
        Object.keys(modules).forEach((key) => {
          filtered = filtered.filter((operator) => {
            return modules[key].filter(searchConfig, property, operator);
          });
        });

        resolve(filtered);
      });
    });
  }

  loadFilters(extensionPoint, callback) {
    this.pluginsService.loadPluginServiceModules(extensionPoint, 'component').then(callback);
  }
}