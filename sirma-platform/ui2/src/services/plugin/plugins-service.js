import {$injector} from 'angular';
import {Injectable, Inject} from 'app/app';
import {Logger} from 'services/logging/logger';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {filterBy} from 'common/object-utils';
import _ from 'lodash';

@Injectable()
@Inject(Logger, '$injector', PromiseAdapter)
export class PluginsService {

  constructor(logger, $injector, promiseAdapter) {
    this.logger = logger;
    this.$injector = $injector;
    this.promiseAdapter = promiseAdapter;
  }

  /**
   * Loads components/directives modules for given extension point. Once all the component modules are loaded, their
   * plugin definitions are returned mapped by concrete property from their definition.
   *
   * @param extensionPoint
   * @param mapBy The key to be used for their mapping in the result map. This key should be an existing property from
   *          plugin definitions for that extension point.
   * @returns {Promise.<T>|Promise} A map that contains plugin definitions for loaded modules mapped by provided key.
   */
  loadComponentModules(extensionPoint, mapBy, filterFunc) {
    var promises = [];
    let definitions = this.getPluginDefinitions(extensionPoint, mapBy);
    definitions = filterBy(definitions, filterFunc);

    Object.keys(definitions).forEach((definition) => {
      promises.push(this.executeImport(definitions[definition].module));
    });
    return this.promiseAdapter.all(promises).then(() => {
      return definitions;
    });
  }

  /**
   * Extracts from the PluginRegistry plugin definitions registered for given extension point. The plugin definitions
   * are mapped by name or other their property that can be provided with the mapBy argument.
   *
   * @param extensionPointName
   *          The extension point for which plugin definitions should be loaded.
   * @param mapBy
   *          The property name of the plugin definitin by which the definitoions to be mapped in the returned map.
   * @returns {{}} loaded plugin definition objects mapped by provided key
   */
  getPluginDefinitions(extensionPointName, mapBy) {
    let actualMapBy = mapBy || 'name';
    let pluginDefinitions = this.getDefinitions(extensionPointName) || [];
    let loadedPluginDefinitions = {};
    pluginDefinitions.forEach((pluginDefinition) => {
      loadedPluginDefinitions[pluginDefinition[actualMapBy]] = pluginDefinition;
    });
    return loadedPluginDefinitions;
  }

  getDefinitions(extensionPointName) {
    return PluginRegistry.get(extensionPointName);
  }

  /**
   * Loads a service module registered for given extension point and resolves with the module instance. The instance is
   * also stored under some key in provided map that can be passed from the caller. Such a map can be used as a cache
   * for already loaded modules instances.
   *
   * @param pluginDefinition
   *          The module plugin definition.
   * @param loadedModules
   *          Already loaded modules instances. The new loaded module will be added here as well.
   * @param mapBy
   *          The property from plugin definition that should be used to map or check loaded plugin modules or 'id' will
   *          be used as default.
   * @param registeredPluginDefintions
   *          This is an optional argument that should contain registered plugin defintions as returned by
   *          getPluginDefinitions for the extension point where the requested plugin module is registered.
   *          Sometimes the pluginDefitnion may be different from that is used for registering in the registry and
   *          may not have the module property. Then the actual defintion is resolved trough this argument.
   * @returns {Promise} Resolves with the loaded module instance.
   */
  loadPluginModule(pluginDefinition, loadedModules, mapBy, registeredPluginDefintions) {
    return this.promiseAdapter.promise((resolve) => {
      let moduleName = pluginDefinition[mapBy] || pluginDefinition['id'];
      // if the module is already loaded and stored, just return it
      let loadedModule = loadedModules[moduleName];
      if (loadedModule) {
        resolve(loadedModule);
      }
      // otherwise load the module by its module path
      let module = pluginDefinition.module;
      if (!module) {
        module = registeredPluginDefintions[moduleName].module;
      }
      this.executeImport(module).then(() => {
        // create an instance and store it
        // get the loaded module name from the promise
        let loadedModuleName = _.capitalize(moduleName);
        loadedModule = this.$injector.get(loadedModuleName);
        loadedModules[moduleName] = loadedModule;
        resolve(loadedModule);
      });
    });
  }

  /**
   * Loads all plugin service modules registered with given extension point.
   *
   * @param extensionPointName
   *          The extension point for which to load all plugin modules.
   * @param mapBy
   *          The key under which the plugins will be mapped and returned.
   * @param ordered
   *         If provided and is true, then all loaded plugin references will be returned as an array where their order
   *         will be as defined in their plugin definitions. Otherwise they will be returned in a map by their names as
   *         a key.
   * @returns {Promise|Promise.<>} Resolves with a map or array that contains all loaded modules references.
   */
  loadPluginServiceModules(extensionPointName, mapBy, ordered) {
    let promises = [];
    let definitions = this.getPluginDefinitions(extensionPointName, mapBy);
    Object.keys(definitions).forEach((key) => {
      promises.push(this.executeImport(definitions[key].module));
    });
    return this.promiseAdapter.all(promises).then(() => {
      return definitions;
    }).then(() => {
      let result;
      let loadedModules = {};
      Object.keys(definitions).forEach((key) => {
        loadedModules[key] = this.$injector.get(_.capitalize(key));
      });
      if (ordered) {
        let orderedPlugins = [];
        PluginRegistry.get(extensionPointName).forEach((definition) => {
          orderedPlugins.push(loadedModules[definition.name]);
        });
        result = orderedPlugins;
      } else {
        result = loadedModules;
      }
      return result;
    }).catch(console.log.bind(console));
  }

  importModules(modules) {
    var promises = [];
    for (let i = 0; i < modules.length; i++) {
      promises.push(this.executeImport(modules[i]));
    }
    return this.promiseAdapter.all(promises);
  }

  // istanbul ignore next
  executeImport(module) {
    return System.import(module).then(function (module) {
      return module;
    }).catch(function (error) {
      // systemjs rejects the promise when a module cannot be loaded instead of throwing an error
      throw new Error(error);
    });
  }
}