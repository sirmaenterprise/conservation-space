import {Injectable, Inject} from 'app/app';
import {PluginsService} from 'services/plugin/plugins-service';
import _ from 'lodash';

const PROCESSOR_PLUGIN_NAME = 'model-action-processors';

/**
 * Abstract action factory which takes care of processing a given action by distributing
 * the execution to the proper concrete action processor. Action processing should be
 * strictly handled and executed from the abstract processor and not done manually.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(PluginsService)
export class ModelActionProcessor {

  constructor(pluginsService) {
    this.pluginsService = pluginsService;
    this.loadActionProcessors();
  }

  loadActionProcessors() {
    return this.pluginsService.loadPluginServiceModules(PROCESSOR_PLUGIN_NAME).then(modules => {
      let definitions = this.pluginsService.getPluginDefinitions(PROCESSOR_PLUGIN_NAME);
      // make mapping between the action types and the processor modules corresponding to them
      Object.keys(definitions).forEach(key => this[definitions[key].action] = modules[key]);
    });
  }

  execute(actions) {
    return this.flatMap(actions, action => this.invoke(this, action, 'execute'));
  }

  restore(actions) {
    return this.flatMap(actions, action => this.invoke(this, action, 'restore'));
  }

  changeset(actions) {
    return this.flatMap(actions, action => this.invoke(this, action, 'changeset'));
  }

  on(actions, executors) {
    return this.flatMap(actions, action => this.invoke(executors, action, null, false));
  }

  invoke(context, action, method, enforce = true) {
    let processor = this.getProcessor(context, action, enforce);
    let callback = !method ? processor : processor[method];
    return _.isFunction(callback) ? callback.call(processor, action) : null;
  }

  flatMap(actions, callback) {
    return _.flatten(_.map(actions, callback));
  }

  getProcessor(context, action, enforce) {
    let type = action.getType();
    let processor = context[type];

    if (!processor && enforce) {
      throw new TypeError(`Processor for ${type} does not exist !`);
    }
    return processor;
  }
}