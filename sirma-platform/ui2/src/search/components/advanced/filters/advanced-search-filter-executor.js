import {Inject, Injectable} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {PluginsService} from 'services/plugin/plugins-service';
import _ from 'lodash';

const OPERATOR_FILTER_EXTENSION_POINT = 'advanced-search-operator-filter';
const PROPERTY_FILTER_EXTENSION_POINT = 'advanced-search-property-filter';

/**
 * Service for applying filter conditions upon a provided advanced search configuration.
 *
 * Useful for limiting the advanced search tree form building with custom conditions and logic.
 *
 * To use it, register an Injectable component in the PluginRegistry under the "advanced-search-operator-filter"
 * extension point.
 *
 * @author yasko, Svetlozar Iliev
 */
@Injectable()
@Inject(PromiseAdapter, PluginsService)
export class AdvancedSearchFilterExecutor {

  constructor(promiseAdapter, pluginsService) {
    this.promiseAdapter = promiseAdapter;
    this.pluginsService = pluginsService;
  }

  filterProperties(searchConfig, criteria, properties) {
    return this.filter(searchConfig, criteria, properties, PROPERTY_FILTER_EXTENSION_POINT);
  }

  filterOperators(searchConfig, property, operators) {
    return this.filter(searchConfig, property, operators, OPERATOR_FILTER_EXTENSION_POINT);
  }

  /**
   * Filters a collection or array of values based on a given configuration & condition
   * @param config the filter configuration
   * @param condition the condition based on which to filter
   * @param collection the collection or array of values
   * @param extension the filter extension point
   */
  filter(config, condition, collection, extension) {
    return this.promiseAdapter.promise((resolve) => {
      if (!collection || !collection.length) {
        resolve(collection);
        return;
      }

      this.loadFilters(extension, (modules) => {
        if (!modules || !Object.keys(modules).length) {
          resolve(collection);
          return;
        }

        var filtered = collection;
        Object.keys(modules).forEach((key) => {
          filtered = filtered.filter((operator) => {
            return modules[key].filter(config, condition, operator);
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