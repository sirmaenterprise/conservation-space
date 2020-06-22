import {Injectable, Inject} from 'app/app';
import {PluginsService} from 'services/plugin/plugins-service';
import _ from 'lodash';

const FACTORY_PLUGIN_NAME = 'model-action-factories';

/**
 * Abstract action factory which takes care of creating a given action by distributing
 * the execution to the proper concrete action factory. Action creation should be
 * strictly handled and executed from the abstract factory and not done manually.
 *
 * Each concrete action factory has two distinct methods which are used by the
 * abstract action factory. The create method is strictly obligatory while the
 * evaluate method is optional and it is used when the action state needs to
 * be finalized before the action is returned to the caller or creator.
 *
 * Evaluation step is the step where the action state is initialized or computed
 * based on some prerequisites - transformation of data, additional initializations
 * etc. Such that could not be executed during the creation of the action instance
 * due to incomplete initialization or lack of data.
 *
 * - create - method called to create the base instance of a given action
 * - evaluate - method called to initialize additional or final action state
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(PluginsService)
export class ModelActionFactory {

  constructor(pluginsService) {
    this.pluginsService = pluginsService;
    this.loadActionFactories();
  }

  loadActionFactories() {
    return this.pluginsService.loadPluginServiceModules(FACTORY_PLUGIN_NAME).then(modules => {
      let definitions = this.pluginsService.getPluginDefinitions(FACTORY_PLUGIN_NAME);
      // make mapping between the action types and the factory modules corresponding to them
      Object.keys(definitions).forEach(key => this[definitions[key].action] = modules[key]);
    });
  }

  create(type, model, context, ...args) {
    let factory = this.getFactory(type, this, true);
    let action = this.createAction(factory, ...args);
    this.initializeAction(model, context, action);
    this.evaluateAction(factory, action);
    return action;
  }

  createAction(factory, ...args) {
    return _.isFunction(factory.create) ? factory.create(...args) : null;
  }

  evaluateAction(factory, action) {
    return _.isFunction(factory.evaluate) ? factory.evaluate(action) : null;
  }

  initializeAction(model, context, action) {
    model && action.setModel(model);
    context && action.setContext(context);
    return action;
  }

  getFactory(type, context, enforce) {
    let factory = context[type];

    if (!factory && enforce) {
      throw new TypeError(`Factory for ${type} does not exist !`);
    }
    return factory;
  }
}