import {View, Component, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {Configuration} from 'common/application-config';
import {BpmService} from 'services/rest/bpm-service';
import {ActionsService} from 'services/rest/actions-service';
import {ActionExecutor} from 'services/actions/action-executor';
import {StatusCodes} from 'services/rest/status-codes';
import {Eventbus} from 'services/eventbus/eventbus';
import {ActionExecutedEvent} from 'services/actions/events';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import {ActionsHelper} from 'idoc/actions/actions-helper';
import {BPM_ACTIVITY_ID} from 'instance/instance-properties';
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
    this.loadHeaderActions();
  }

  /**
   * Subscribe listener to events to reload the toolbar action buttons.
   */
  registerActionExecutedListener() {
    this.events.push(this.eventBus.subscribe(ActionExecutedEvent, this.loadActions.bind(this)));
    this.events.push(this.eventBus.subscribe(InstanceRefreshEvent, this.loadActions.bind(this)));
  }

  /**
   * Loads actions toolbar if in context fo BPMN document and preview mode.
   */
  loadHeaderActions() {
    if (this.isToolbarEnabled() && this.config.idocContext.isPreviewMode()) {
      this.registerActionExecutedListener();
      this.loadWorkflow().then(() => {
        this.loadActions();
      });
    }
  }

  loadActions() {
    if (this.showSection()) {
      let id = this.config.idocContext.id;
      let config = ActionsHelper.getActionsLoaderConfig(this.config.currentObject, BPM_ACTIONS_TOOLBAR_SELECTOR);
      this.handlers = ActionsHelper.collectImplementedHandlers(BPM_ACTIONS_TOOLBAR_SELECTOR);
      this.actionsService.getFlatActions(id, config).then((response) => {
        this.filterActions(response);
      });
    }
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
        serverOperation: action.serverOperation,
        forceRefresh: this.handlers[action.serverOperation + 'Action'].forceRefresh
      };
    });
  }

  loadWorkflow() {
    return this.bpmService.getInfo(this.config.currentObject.id).then((result) => {
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
      }
    });
  }

  isToolbarEnabled() {
    return !!this.config.currentObject.getPropertyValue(BPM_ACTIVITY_ID)
      && this.configService.get(Configuration.BPM_ENGINE_NAME) !== undefined;
  }

  /**
   * Verify that idoc is an activity task.
   *
   * @returns {*|boolean}
   */
  showSection() {
    return this.isBpmTask;
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