import {Component, Inject, View, NgTimeout} from 'app/app';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelActionExecutionTypes} from 'administration/model-management/model/model-action-execution';
import {TREE_NODE_SELECTED} from 'administration/model-management/components/action-group-tree/model-actions-tree';
import _ from 'lodash';
import template from './model-action-view.html!text';

@Component({
  selector: 'seip-model-action-view',
  properties: {
    'emitter': 'emitter',
    'model': 'model',
    'config': 'config'
  },
  events: ['onAttributeChange']
})
@View({
  template
})
@Inject(NgTimeout)
export class ModelActionView {

  constructor($timeout) {
    this.$timeout = $timeout;
  }

  ngOnInit() {
    this.scriptExecutions = [];
    this.relationExecutions = [];
    this.eventHandlers = [];
    this.expandedExecutions = [];
    this.setSourceareaConfig();
    this.initNodeSelected();
  }

  initNodeSelected() {
    // synchronize Angular with the DOM tree manipulations when TREE_NODE_SELECTED is triggered
    this.$timeout(() => {
      this.eventHandlers.push(this.emitter.subscribe(TREE_NODE_SELECTED, () => this.selectionChanged()));
      this.scriptExecutions = this.getActionScriptExecutions();
      this.relationExecutions = this.getActionRelationExecutions();
    }, 0);
  }

  setSourceareaConfig() {
    this.sourceareaConfig = {
      viewportMargin: Infinity,
      mode: 'javascript',
      lineNumbers: true,
      readOnly: false
    };
  }

  getModelTitle() {
    return this.model && this.model.getDescription().getValue();
  }

  onActionAttributeChanged(attribute) {
    return this.onAttributeChange && this.onAttributeChange({attribute});
  }

  // Action script executors are usually js scripts, executed before or after an action.
  getActionScriptExecutions() {
    return this.model && this.model.getActionExecutions()
        .filter(execution => this.resolveExecutionType(execution, ModelActionExecutionTypes.MODEL_ACTION_EXECUTION_TYPE_EXECUTE_SCRIPT));
  }

  // Action relation executors are usually json structure, that describes a relation state after an action execution.
  getActionRelationExecutions() {
    return this.model && this.model.getActionExecutions()
        .filter(execution => this.resolveExecutionType(execution, ModelActionExecutionTypes.MODEL_ACTION_EXECUTION_TYPE_CREATE_RELATION));
  }

  resolveExecutionType(execution, type) {
    return execution.getAttribute(ModelAttribute.TYPE_ATTRIBUTE).getValue().getValue() === type;
  }

  // Requirements and preconditions for relation executor visualization:
  // 1. There may be only one relation creating executor or none
  // 2. The relation creating executor should be displayed among the other attributes
  // 3. The relation creating executor should be displayed after the 3-th attribute(Purpose)
  // 4. The relation creating executor should not be displayed among other executors
  getModelAttributes() {
    if (this.model) {
      let modelAttributes = this.model.getAttributes().slice(0);
      if (this.relationExecutions && this.relationExecutions.length >= 1) {
        let position = 1 + _.findIndex(modelAttributes, function (attr) {
          return attr.getId() === ModelAttribute.PURPOSE_ATTRIBUTE;
        });
        modelAttributes.splice(position, 0, this.relationExecutions[0].getAttribute(ModelAttribute.VALUE_ATTRIBUTE));
        return modelAttributes;
      }
      return modelAttributes;
    }
  }

  hasActionExecutions() {
    return this.getExecutions().length > 0;
  }

  getExecutions() {
    return this.model && this.model.getActionExecutions instanceof Function && this.model.getActionExecutions() || [];
  }

  toggleView(actionExecution) {
    let execution = actionExecution.getId();
    let found = this.expandedExecutions.indexOf(execution);
    if (found >= 0) {
      this.expandedExecutions.splice(found, 1);
    } else {
      this.expandedExecutions.push(execution);
    }
  }

  isExecutionExpanded(actionExecution) {
    return this.expandedExecutions.indexOf(actionExecution.getId()) > -1;
  }

  selectionChanged() {
    this.unSubscribeAllHandlers();
    this.initNodeSelected();
  }

  unSubscribeAllHandlers() {
    this.eventHandlers.forEach(handler => handler && handler.unsubscribe());
    this.eventHandlers = [];
  }

  ngOnDestroy() {
    this.unSubscribeAllHandlers();
  }

}