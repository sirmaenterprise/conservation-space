import {Injectable, Inject} from 'app/app';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {ActionHandler} from 'services/actions/action-handler';
import {CreatePanelService} from 'services/create/create-panel-service';
import {ModelsService} from 'services/rest/models-service';
import {NamespaceService} from 'services/rest/namespace-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(CreatePanelService, ModelsService, NamespaceService, WindowAdapter, PromiseAdapter)
export class CreateInstanceAction extends ActionHandler {

  constructor(createPanelService, modelsService, namespaceService, windowAdapter, promiseAdapter) {
    super();
    this.createPanelService = createPanelService;
    this.namespaceService = namespaceService;
    this.modelsService = modelsService;
    this.windowAdapter = windowAdapter;
    this.promiseAdapter = promiseAdapter;
  }

  /**
   * Executes action with predefined type and sub types. Types and subtypes will be configured into definition.
   * Type will be described as array with type ids. Its name is predefinedTypes. Sub types will be describe as
   * array with subtypes ids. Its name is predefinedSubTypes. Execute method will checks this arrays and will create
   * configuration described how to be open create instance dialog. There are few possible situations.
   * 1. If predefinedTypes is not set then create instance dialog will be open with all type definitions and
   *  sub types will be ignored.
   * 2. If predefinedTypes is set with one type and without syb types then create instance dialog will be open with
   *  populated type. Dropdown with types will be disabled.
   * 3. If predefinedTypes is set with one type and one sub type then create instance dialog will be open with
   *  populated type and subtype. Dropdowns with types and sybtypes will be disabled.
   * 4. If predefinedTypes is set with one type and more than one sub type then create instance dialog will be open
   *  with populated type and list with subtypes listed into predefinedSubTypes. Dropdown with types will be disabled.
   * 5. If predefinedTypes is set with more than one type then create instance dialog will be open with list types
   *  listed into predefinedTypes, sub types will be ignored.
   * Context selector can be configured to be enabled or disabled by configuration of action (default is disabled).
   * @param action
   * @param context
   */
  execute(action, context) {
    return this.promiseAdapter.promise((resolve, reject) => {
      let actionConfig = action.configuration;
      actionConfig.predefinedTypes = actionConfig.predefinedTypes ? this.removeEmptyElements(actionConfig.predefinedTypes) : [];
      if (actionConfig.predefinedTypes.length === 1) {
        actionConfig.predefinedSubTypes = actionConfig.predefinedSubTypes ? this.removeEmptyElements(actionConfig.predefinedSubTypes) : [];

        this.modelsService.getModels('create', null, null, null, actionConfig.predefinedTypes, actionConfig.predefinedSubTypes).then((data) => {
          //Check if there is result with combination predefinedTypes and predefinedSubTypes if there is no this means
          //we don't have sub type of predefinedTypes then predefinedSubTypes is set to empty array (this will trigger loading
          // of all available sub types. See CMF-23540.
          if (data.models.length === 0) {
            actionConfig.predefinedSubTypes = [];
          }
          this.openDialogWithPredefinedType(actionConfig, context, resolve, reject);
        });
      } else {
        actionConfig.predefinedSubTypes = [];
        this.createPanelService.openCreateInstanceDialog(this.initDialogOptionsFromActionConfig(actionConfig, undefined, context, undefined, resolve, reject));
      }
    });
  }

  /**
   * Open dialog wih configuration explained 2, 3 and 4 of method execute(action, context) documentation. Method also
   * check if object described with type is creatable and uploadable and populate exclusions if they are not.
   * @param actionConfig
   * @param context
   */
  openDialogWithPredefinedType(actionConfig, context, resolve, reject) {
    this.modelsService.getClassInfo(actionConfig.predefinedTypes).then((response) => {
      let exclusions = [];
      let typeInfo = response.data;
      if (!typeInfo.creatable) {
        exclusions.push('instance-create-panel');
      }
      if (!typeInfo.uploadable) {
        exclusions.push('file-upload-panel');
      }
      this.namespaceService.toFullURI(actionConfig.predefinedTypes).then((fullUrisMap) => {
        let initialInstanceType = fullUrisMap.data[actionConfig.predefinedTypes[0]];
        this.createPanelService.openCreateInstanceDialog(this.initDialogOptionsFromActionConfig(actionConfig, initialInstanceType, context, exclusions, resolve, reject));
      });
    });
  }

  /**
   * Iterates over sourceArray and skip elements which are undefined, null or empty.
   * This can be happen if in definition someone make mistake. For example configuration of action can be:
   *{
   * "predefinedTypes" : ["emf:Document", " "],
   * "predefinedSubTypes" :["AD210001"]
   *}
   * @param sourceArray source array.
   * @returns {Array} copy of sourceArray without undefined, null or empty elements.
   */
  removeEmptyElements(sourceArray) {
    return sourceArray.filter(function (element) {
      return element && element.trim().length > 0;
    });
  }

  /**
   * Create dialog options from action configuration used to open create instance dialog.
   * @param actionConfig
   * @param initialInstanceType if set dropdown of type will be populate with this value. Important: fullUri have to be
   * used.
   * @param context
   * @param exclusions arrays with panels to be excluded. For example: ["instance-create-panel"]
   * @returns {{parentId, returnUrl: string, operation: string, predefinedTypes: *, predefinedSubTypes: *, exclusions: *, instanceType: *, instanceCreatePanelContextSelectorMode, fileUploadPanelContextSelectorMode}}
   */
  initDialogOptionsFromActionConfig(actionConfig, initialInstanceType, context, exclusions, resolve, reject) {
    return {
      parentId: context.currentObject.id,
      returnUrl: this.windowAdapter.location.href,
      operation: 'create',
      predefinedTypes: actionConfig.predefinedTypes,
      predefinedSubTypes: actionConfig.predefinedSubTypes,
      forceCreate: actionConfig.forceCreate,
      openInNewTab: actionConfig.openInNewTab,
      exclusions,
      instanceType: initialInstanceType,
      contextSelectorDisabled: CreatePanelService.resolveContextSelectorDisabled(actionConfig, true),
      onClosed: (result) => {
        if (result.instanceCreated) {
          resolve();
        } else {
          reject();
        }
      }
    };
  }
}
