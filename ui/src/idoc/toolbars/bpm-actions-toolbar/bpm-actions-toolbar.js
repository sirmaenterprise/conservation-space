import {View, Component, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {Configuration} from 'common/application-config';
import {BpmService} from 'services/rest/bpm-service';
import {ActionsService} from 'services/rest/actions-service';
import {ActionsMenu} from 'idoc/actions-menu/actions-menu';
import {ActionExecutor} from 'services/actions/action-executor';
import {StatusCodes} from 'services/rest/status-codes';
import {Eventbus} from 'services/eventbus/eventbus';
import {ActionExecutedEvent} from 'services/actions/events';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import template from './bpm-actions-toolbar.html!text';
import './bpm-actions-toolbar.css!';

const BPM_ACTION_IDENTIFIER = 'bpm';
const BPM_ACTIONS_TOOLBAR_SELECTOR = 'bpm-actions-toolbar';

@Component({
  selector: BPM_ACTIONS_TOOLBAR_SELECTOR,
  properties: {
    config: 'config'
  }
})
@View({
  template: template
})
@Inject(BpmService, ActionsService, Configuration, ActionExecutor, Eventbus)
export class BpmActionsToolbar extends Configurable {

  constructor(bpmService, actionsService, configService, actionExecutor, eventBus) {
    super({});
    this.bpmService = bpmService;
    this.actionsService = actionsService;
    this.configService = configService;
    this.actionExecutor = actionExecutor;
    this.eventBus = eventBus;
    this.isBpmTask = false;
    this.actionsButtons = [];
    this.events = [];
    if (this.config.idocContext.isPreviewMode() && this.isToolbarEnabled()) {
      this.loadWorkflow();
      this.loadActions();
    }
  }

  /**
   * Subscribe listener to ActionExecutedEvent where verify the if the current action bpm and if it is then reloads the toolbar action buttons.
   *
   * @returns {[*]} array of subscribed events
   */
  registerActionExecutedListener() {
    return [
      this.eventBus.subscribe(ActionExecutedEvent, this.onActionExecutedEvent.bind(this)),
      this.eventBus.subscribe(InstanceRefreshEvent, this.onActionExecutedEvent.bind(this))
    ];
  }

  /**
   * Reload actions after ActionExecutedEvent is fired and if action operation starts with 'bpm'.
   */
  onActionExecutedEvent() {
      this.loadActions();
  }

  loadActions() {
    let id = this.config.idocContext.id;
    let config = ActionsMenu.getActionsLoaderConfig(this.config.currentObject, BPM_ACTIONS_TOOLBAR_SELECTOR);
    this.handlers = ActionsMenu.collectImplementedHandlers(BPM_ACTIONS_TOOLBAR_SELECTOR);
    this.actionsService.getActions(id, config).then((response) => {
      this.filterActions(response);
    });
  }

  /**
   * Filters and constructs BPM actions.
   *
   * @param response available actions response
   */
  filterActions(response) {
    this.actionsButtons = response.data.filter((action) => {
      return action.serverOperation && action.serverOperation.indexOf(BPM_ACTION_IDENTIFIER) === 0 && this.handlers[action.serverOperation + 'Action'];
    }).map((action) => {
      return {
        name: action.serverOperation + 'Action',
        action: action.userOperation,
        label: action.label,
        tooltip: action.tooltip,
        disabled: action.disabled,
        confirmationMessage: action.confirmationMessage,
        extensionPoint: 'actions',
        configuration: action.configuration,
        serverOperation: action.serverOperation
      };
    });
  }

  loadWorkflow() {
    this.bpmService.getInfo(this.config.currentObject.id).then((result) => {
      if (result.status === StatusCodes.SUCCESS && result.data.process) {
        this.workflowInstance = {
          id: result.data.process.id,
          isActive: result.data.active,
          headers: result.data.process.headers,
          config: {
            isMenuAllowed: false
          }
        };
        this.isBpmTask = this.workflowInstance.id !== this.config.currentObject.id;
        if (this.isBpmTask) {
          this.events = this.registerActionExecutedListener();
        }
      }
    });
  }

  isToolbarEnabled() {
    return this.configService.get('processes.camunda.engine.name') !== undefined;
  }

  /**
   * Verify that idoc is in Preview Mode and current idoc object is also an activity task.
   *
   * @returns {*|boolean}
   */
  showSection() {
    return this.config.idocContext.isPreviewMode() && this.isBpmTask;
  }

  /**
   * Execute action on user click.
   *
   * @param action selected action to execute
   */
  executeAction(action) {
    if (!action.disabled) {
      this.actionExecutor.execute(action, this.config);
    }
  }

  /**
   * Destroy events on leave.
   */
  ngOnDestroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
  }
}