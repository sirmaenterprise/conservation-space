import {Component, View, Inject} from 'app/app';
import $ from 'jquery';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {TransitionAction} from 'idoc/actions/transition-action';
import {ActionExecutor} from 'services/actions/action-executor';
import {InstanceRestService} from 'services/rest/instance-service';
import {ActionExecutedEvent} from 'services/actions/events';
import {Eventbus} from 'services/eventbus/eventbus';
import template from './transition-action-bootstrap.html!text';

const APPROVE_OPERATION = 'approve';
const REJECT_OPERATION = 'reject';
const RESTART_OPERATION = 'restart';

@Component({
  selector: 'transition-action-bootstrap',
  properties: {
    'config': 'config'
  }
})
@View({
  template
})
@Inject(ActionExecutor, TransitionAction, IdocContextFactory, InstanceRestService, Eventbus)
export class TransitionActionBootstrap {

  constructor(actionExecutor, transitionAction, idocContextFactory, instanceRestService, eventbus) {
    this.actionExecutor = actionExecutor;
    this.transitionAction = transitionAction;
    this.eventbus = eventbus;
    instanceRestService.loadInstanceObject('8', APPROVE_OPERATION).then((instanceObject) => {
      this.actionContextInstance1 = {
        placeholder: 'idoc.actions.menu',
        idocContext: idocContextFactory.getCurrentContext('8', APPROVE_OPERATION),
        currentObject: instanceObject
      };
    });
    instanceRestService.loadInstanceObject('7', REJECT_OPERATION).then((instanceObject) => {
      this.actionContextInstance2 = {
        placeholder: 'idoc.actions.menu',
        idocContext: idocContextFactory.getCurrentContext('7', REJECT_OPERATION),
        currentObject: instanceObject
      };
    });
    instanceRestService.loadInstanceObject('9', RESTART_OPERATION).then((instanceObject) => {
      this.actionContextInstance3 = {
        placeholder: 'idoc.actions.menu',
        idocContext: idocContextFactory.getCurrentContext('9', RESTART_OPERATION),
        currentObject: instanceObject
      };
    });

    this.eventbus.subscribe(ActionExecutedEvent, (evt) => {
      let actionName = evt.action.name;
      $(`<div id="actionName">${actionName}</div>`).appendTo('.container');
    });
  }

  approve() {
    this.transitionAction.name = APPROVE_OPERATION;
    this.transitionAction.label = APPROVE_OPERATION;
    this.actionExecutor.execute(this.transitionAction, this.actionContextInstance1);
  }

  reject() {
    this.transitionAction.name = REJECT_OPERATION;
    this.transitionAction.label = REJECT_OPERATION;
    this.actionExecutor.execute(this.transitionAction, this.actionContextInstance2);
  }

  restart() {
    this.transitionAction.name = RESTART_OPERATION;
    this.transitionAction.label = RESTART_OPERATION;
    this.actionExecutor.execute(this.transitionAction, this.actionContextInstance3);
  }
}