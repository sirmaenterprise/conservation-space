import {View, Component, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {PickerService, SEARCH_EXTENSION} from 'services/picker/picker-service';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {TranslateService} from 'services/i18n/translate-service';
import {HeadersService} from 'instance-header/headers-service';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {EventEmitter} from 'common/event-emitter';

import 'font-awesome/css/font-awesome.css!';
import './context-selector.css!';
import contextSelectorTemplate from './context-selector.html!text';

// Constants used for interaction with context selector.
export const CONTEXT_CHANGED_EVENT = 'ContextChangedEvent';
export const CLEAR_CONTEXT_COMMAND = 'ClearContextCommand';
export const ADD_CONTEXT_ERROR_MESSAGE_COMMAND = 'AddContextErrorMessageCommand';
export const REMOVE_CONTEXT_ERROR_MESSAGE_COMMAND = 'RemoveContextErrorMessageCommand';
export const CLEAR_CONTEXT_ERROR_MESSAGES_COMMAND = 'ClearContextErrorMessagesCommand';

// Constants used for disabled/enabled buttons of context selector view
export const SELECTION_MODE_BOTH = 'BOTH';
export const SELECTION_MODE_IN_CONTEXT = 'IN_CONTEXT';
export const SELECTION_MODE_WITHOUT_CONTEXT = 'WITHOUT_CONTEXT';

@Component({
  selector: 'seip-context-selector',
  properties: {
    'config': 'config'
  }
})
@View({
  template: contextSelectorTemplate
})

/**
 * Component used for selection of context. Interaction with component is based on {@link EventEmitter}.
 * Configuration of component:
 *  1. contextSelectorDisabled - if true all buttons of context selector will be disabled.
 *  2. contextSelectorSelectionMode - there are three options {@link SELECTION_MODE_BOTH}, {@link SELECTION_MODE_IN_CONTEXT} and {@link SELECTION_MODE_WITHOUT_CONTEXT}.
 *    2.1 {@link SELECTION_MODE_BOTH} all buttons will be enabled (meaning of this is context can be selected or not).
 *    2.2 {@link SELECTION_MODE_IN_CONTEXT} clear context button will be disabled (meaning of this is context can not be empty).
 *    2.3 {@link SELECTION_MODE_WITHOUT_CONTEXT} select context button will be disabled (meaning of this is context can not be set).
 *  3. eventEmitter - used for interaction with context selector. It can be passed as configuration, if not new one will be created.
 *  4. parentId - used for initialization of component. If passed instance will be loaded and its header will be displayed.
 * Interaction with component.
 * Events:
 *  1. Context is changed:
 *    When new context is chosen, the contextSelector will publish event {@link CONTEXT_CHANGED_EVENT} with parameter id of the new context.
 * Commands:
 *  1. Clear context:
 *     the context selector is listening for {@link CLEAR_CONTEXT_COMMAND}. When the event occurred context will be cleared
 *     and event {@link CONTEXT_CHANGED_EVENT} will be published without id for context.
 *  2. Add error message:
 *    the context selector is listening for command {@link ADD_CONTEXT_ERROR_MESSAGE_COMMAND} with parameter label key of error message.
 *    When the command executed, the context selector will add the new error message to existing (if any).
 *  3. Remove error message:
 *    the context selector is listening for command {@link REMOVE_CONTEXT_ERROR_MESSAGE_COMMAND} with parameter label key of error message.
 *    When the command executed, the context selector will remove the error message from existing.
 *  4. Clear error messages:
 *    the context selector is listening for command {@link CLEAR_CONTEXT_ERROR_MESSAGES_COMMAND}. When the command executed all error messages
 *    will be removed.
 *
 *
 */
@Inject(PickerService, TranslateService, HeadersService, IdocContextFactory)
export class ContextSelector extends Configurable {

  constructor(pickerService, translateService, headersService, idocContextFactory) {
    super({
      contextSelectorDisabled: false,
      contextSelectorSelectionMode: SELECTION_MODE_BOTH,
      eventEmitter: new EventEmitter()
    });
    this.pickerService = pickerService;
    this.translateService = translateService;
    this.headersService = headersService;
    this.idocContextFactory = idocContextFactory;
    this.noContextHeader = this.translateService.translateInstant(ContextSelector.NO_CONTEXT);
  }

  ngOnInit() {
    this.registerEventHandlers();
    this.errorMessages = new Set();
    this.pickerConfig = ContextSelector.getPickerConfig();
    this.pickerService.assignDefaultConfigurations(this.pickerConfig);

    let parentId = this.config.parentId;
    if (parentId) {
      this.pickerService.setSelectedItems(this.pickerConfig, [{id: parentId}]);
      this.loadContext(parentId).then(() => {
        this.publishContextChangedEvent(parentId);
      }).catch(() => {
        this.clearContextAndNotify();
      });
    } else {
      this.clearContextAndNotify();
    }
  }

  static getPickerConfig() {
    return {
      extensions: {
        [SEARCH_EXTENSION]: {
          arguments: {
            filterByWritePermissions: true
          }
        }
      }
    };
  }

  /**
   * Opens dialog for selection of context. After dialog closed and if new selection is different than old one, then event
   * {@link CONTEXT_CHANGED_EVENT} will be fired.
   */
  selectContext() {
    this.config.contextSelectorDisabled = true;
    this.pickerService.configureAndOpen(this.pickerConfig, this.idocContextFactory.getCurrentContext()).then((selectedItems) => {
      let newContextId = ContextSelector.getFirstSelectedId(selectedItems);
      let oldContextId = this.config.parentId;
      if (oldContextId !== newContextId) {
        this.loadContext(newContextId).then(() => {
          this.publishContextChangedEvent(newContextId);
          this.config.contextSelectorDisabled = false;
        }).catch(() => {
          this.clearContextAndNotify();
          this.config.contextSelectorDisabled = false;
        });
      }
    }).catch(() => {
      this.config.contextSelectorDisabled = false;
    });
  }

  loadContext(id) {
    return this.headersService.loadHeaders([id], HEADER_BREADCRUMB).then((headers) => {
      this.header = headers[id][HEADER_BREADCRUMB];
      this.config.parentId = id;
    });
  }

  /**
   * Clear context.
   */
  clearContext() {
    this.config.parentId = null;
    this.pickerService.clearSelectedItems(this.pickerConfig);
    this.header = this.noContextHeader;
  }

  /**
   * Clear context and publish event {@link CONTEXT_CHANGED_EVENT}.
   */
  clearContextAndNotify() {
    this.clearContext();
    this.publishContextChangedEvent();
  }

  /**
   * Publish {@link CONTEXT_CHANGED_EVENT} with new content id.
   */
  publishContextChangedEvent(contextId) {
    this.errorMessages.clear();
    this.config.eventEmitter.publish(CONTEXT_CHANGED_EVENT, contextId);
  }

  isSelectionModeInContext() {
    return this.config.contextSelectorSelectionMode === SELECTION_MODE_IN_CONTEXT;
  }

  isSelectionModeWithoutContext() {
    return this.config.contextSelectorSelectionMode === SELECTION_MODE_WITHOUT_CONTEXT;
  }

  getErrorMessages() {
    return [...this.errorMessages];
  }

  /**
   * Return id of selected context or undefined if selectedItems are empty.
   */
  static getFirstSelectedId(selectedItems = []) {
    if (selectedItems.length > 0) {
      return selectedItems[0].id;
    }
  }

  registerEventHandlers() {
    this.addErrorMessageHandler = this.registerAddErrorMessageHandler();
    this.clearErrorMessageHandler = this.registerClearErrorMessageHandler();
    this.removeErrorMessageHandler = this.registerRemoveErrorMessageHandler();
    this.clearContextHandler = this.registerClearContextHandler();
  }

  registerAddErrorMessageHandler() {
    return this.config.eventEmitter.subscribe(ADD_CONTEXT_ERROR_MESSAGE_COMMAND, (errorMessage) => {
      this.errorMessages.add(errorMessage);
    });
  }

  registerClearErrorMessageHandler() {
    return this.config.eventEmitter.subscribe(CLEAR_CONTEXT_ERROR_MESSAGES_COMMAND, () => {
      this.errorMessages.clear();
    });
  }

  registerRemoveErrorMessageHandler() {
    return this.config.eventEmitter.subscribe(REMOVE_CONTEXT_ERROR_MESSAGE_COMMAND, (errorMessage) => {
      this.errorMessages.delete(errorMessage);
    });
  }

  registerClearContextHandler() {
    return this.config.eventEmitter.subscribe(CLEAR_CONTEXT_COMMAND, () => {
      this.clearContextAndNotify();
    });
  }

  ngOnDestroy() {
    this.addErrorMessageHandler.unsubscribe();
    this.clearErrorMessageHandler.unsubscribe();
    this.removeErrorMessageHandler.unsubscribe();
    this.clearContextHandler.unsubscribe();
  }
}

ContextSelector.NO_CONTEXT = 'context.selector.no.context';