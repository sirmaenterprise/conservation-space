import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {PickerService, SEARCH_EXTENSION, BASKET_EXTENSION} from 'services/picker/picker-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ActionsService} from 'services/rest/actions-service';
import {NotificationService} from 'services/notification/notification-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import {InstanceRestService, EDIT_OPERATION_NAME} from 'services/rest/instance-service';
import {Configuration} from 'common/application-config';
import {MULTIPLE_SELECTION, SINGLE_SELECTION} from 'search/search-selection-modes';
import {IDOC_PAGE_ACTIONS_PLACEHOLDER} from 'idoc/idoc-constants';
import {InstanceObjectWrapper} from 'models/instance-object-wrapper';

const LIMIT = -1;

/**
 * Executes "addRelation" action.
 * The definitions support action configuration for creating relationships between two objects.
 * Example of that configuration:
 * {
 *   "predefinedTypes" : [],
 *   "relation" : []
 * }
 *
 * predefinedTypes - predefined list of objects (based on the defined range of the action). They will filled into search.
 * Examples of usage: ["emf:User"] or ["emg:Document", "emf:User"];
 * relation - arrays with links which will be removed/added. Examples of usage: ["emf:Watchers", "emf:Attachment"]
 *
 * After execution of action relations will be updated between objects. According of picker selection new relation will
 * be added, others will be moved.
 */
@Injectable()
@Inject(PickerService, ActionsService, NotificationService, Eventbus, PromiseAdapter, InstanceRestService, Configuration)
export class AddRelationAction extends ActionHandler {
  constructor(pickerService, actionsService, notificationService, eventbus, promiseAdapter, instanceRestService, configuration) {
    super();
    this.pickerService = pickerService;
    this.actionsService = actionsService;
    this.notificationService = notificationService;
    this.eventbus = eventbus;
    this.promiseAdapter = promiseAdapter;
    this.instanceRestService = instanceRestService;
    this.configuration = configuration;
  }

  execute(action, context) {
    let pickerConfig;
    let oldSelectedItems;
    let instanceObject;
    return this.loadInstanceObjectProperties(context).then((loadedInstanceObject) => {
      instanceObject = loadedInstanceObject;
      pickerConfig = this.buildPickerConfig(action, instanceObject);
      return pickerConfig;
    }).then(() => {
      return this.getSelectedItems(action.configuration.relation, instanceObject);
    }).then((selectedItems) => {
      oldSelectedItems = selectedItems;
      this.pickerService.setSelectedItems(pickerConfig, oldSelectedItems);
      return this.pickerService.configureAndOpen(pickerConfig, new InstanceObjectWrapper(this.promiseAdapter, instanceObject));
    }).then((newSelectedItems) => {
      let toBeAdded = this.getToBeAdded(oldSelectedItems, newSelectedItems);
      let toBeRemoved = this.getToBeRemoved(oldSelectedItems, newSelectedItems);
      if (toBeAdded.length > 0 || toBeRemoved.length > 0) {
        let data = AddRelationAction.buildRelationRequestData(instanceObject.id, action, toBeAdded, toBeRemoved);
        return this.updateRelations(data, action.label);
      } else {
        // Handle case when "Ok" button of picker service dialog is clicked without any changes.
        // Rejection of promise will cause 'ActionExecutor' to not publish 'ActionExecutedEvent',
        // which will prevent triggering of all listeners when operation is done. For example reload of widgets.
        return this.promiseAdapter.reject();
      }
    });
  }

  loadInstanceObjectProperties(actionContext) {
    if (actionContext.placeholder === IDOC_PAGE_ACTIONS_PLACEHOLDER) {
      return actionContext.idocContext.getCurrentObject();
    } else {
      return this.instanceRestService.loadInstanceObject(actionContext.currentObject.getId(), EDIT_OPERATION_NAME);
    }
  }

  updateRelations(data, header) {
    return this.actionsService.updateRelations(data).then((response) => {
      this.notificationService.success(header);
      // we already have notification, so send only the object
      this.eventbus.publish(new InstanceRefreshEvent({response}));
    });
  }

  buildPickerConfig(action, instanceObject) {
    let pickerLabel = action.label;
    let predefinedTypes = action.configuration.predefinedTypes;
    let relations = action.configuration.relation;
    let selectionType = AddRelationAction.getSelectionType(relations, instanceObject, action.configuration);
    let pickerRestrictions = action.configuration.restrictions;
    let pickerConfig = AddRelationAction.initPickerConfig(pickerLabel);
    AddRelationAction.setupSearchExtension(pickerConfig, predefinedTypes, selectionType, instanceObject.getId(), pickerRestrictions);
    AddRelationAction.setupBasketExtension(pickerConfig, relations, instanceObject);
    return pickerConfig;
  }

  /**
   * Return intersection of property values which have uri specified in relations from instanceObject.
   * For example:
   * if relations = ['emf:hasWatchers', 'emf:hasAttachments', 'emf:someRelation']
   * and current object has properties with those uri
   *  for emf:hasWatchers value is [1, 2, 3, 6]
   *  for emf:hasAttachments value is [2, 6]
   *  for emf:someRelation value is [55, 2, 3, 6]
   *
   *  result will be [2, 6]
   */
  getSelectedItems(relations = [], instanceObject) {
    let loaders = [];

    relations.forEach((relation) => {
      let currentViewModelField = instanceObject.getViewModelFieldByUri(relation);
      let currentPropertyName = currentViewModelField.identifier;
      // Load the rest of the selected relations if need
      loaders.push(this.loadRelations(instanceObject.id, currentPropertyName));
    });

    return this.promiseAdapter.all(loaders).then((response) => {
      let selectedItems = [];
      if (response.length > 0) {
        selectedItems = response[0];
      }

      for (let index = 1; index < response.length; index++) {
        selectedItems = this.getIntersection(selectedItems, response[index]);
      }

      return AddRelationAction.convertIdsToObjects(selectedItems);
    });
  }

  /**
   * Resolves with a promise holding a list with instance ids of all selected objects for given relation.
   * @param objectId
   * @param propertyName
   * @return {*}
   */
  loadRelations(objectId, propertyName) {
    // TODO: don't call backend if relations are less than  the limited amount
    return this.instanceRestService.getInstanceProperty(objectId, propertyName, 0, LIMIT)
      .then(({data}) => {
        return data;
      });
  }

  /**
   * Object properties (aka relations) has only array with instance ids as a value but the picker expects array with
   * objects whereas every object contains attribute id. So a conversion must be made.
   *
   * @param ids Is an array with instance ids.
   * @return An array in format [ { id: 'instanceid1' } , { id: 'instanceid2' } ]
   */
  static convertIdsToObjects(ids) {
    return ids.map((id) => {
      return {id};
    });
  }

  static initPickerConfig(label) {
    return {
      header: label,
      extensions: {},
      tabs: {}
    };
  }

  static setupSearchExtension(pickerConfig, predefinedTypes = [], selection, instanceObjectId, restrictions) {
    pickerConfig.extensions[SEARCH_EXTENSION] = {
      predefinedTypes,
      restrictions,
      results: {
        config: {
          selection,
          exclusions: [instanceObjectId],
          selectedItems: []
        }
      }
    };
  }

  /**
   * Configured basket extension.
   * If relations contains only one relation it's label will be used as label of  basket extension otherwise configuration
   * will not modified and when dialog is opened default "Basket" will be used.
   */
  static setupBasketExtension(pickerConfig, relations = [], instanceObject) {
    if (relations.length === 1) {
      let propertyUri = relations[0];
      pickerConfig.tabs[BASKET_EXTENSION] = {
        label: instanceObject.getViewModelFieldByUri(propertyUri).label
      };
    }
  }

  /**
   * Return type of selection 'single' or 'multiple'.
   * If relations contains only one relation selection will be get from field witch have uri same as relation otherwise
   * selection will be taken from action configuration. If configuration have not defined selection multiple will be
   * return.
   *
   * @returns Return type of selection 'single' or 'multiple'.
   */
  static getSelectionType(relations = [], instanceObject, actionConfiguration) {
    if (relations.length === 1) {
      let propertyUri = relations[0];
      return instanceObject.getViewModelFieldByUri(propertyUri).multivalue ? MULTIPLE_SELECTION : SINGLE_SELECTION;
    }
    // For now we can configure relations to be something like ['emf:hasAttachment', 'emf:hasWatchers']
    // but if emf:hasAttachment is single value and emf:hasWatchers is multi value we can't determine which one is right
    // so configuration actionConfiguration.selection will be set only in this case.
    // Validation of this case have to be added in back end, which have not allow configuration w
    // ith multiple relations, if any of them is set on single field. When validation is done
    // actionConfiguration.selection can be removed.
    return actionConfiguration.selection || MULTIPLE_SELECTION;
  }

  static buildRelationRequestData(instanceObjectId, transition, toBeAdded = [], toBeRemoved = []) {
    let addRelations = [];
    let removeRelations = [];
    let idsToBeAdded = toBeAdded.map(item => item.id);
    let idsToBeRemoved = toBeRemoved.map(item => item.id);
    for (let relation of transition.configuration.relation) {
      addRelations.push({
        linkId: relation,
        ids: idsToBeAdded
      });
      removeRelations.push({
        linkId: relation,
        ids: idsToBeRemoved
      });
    }
    return {
      id: instanceObjectId,
      userOperation: transition.action,
      add: addRelations,
      remove: removeRelations
    };
  }

  getToBeAdded(oldSelectedItems, newSelectedItems) {
    return this.getMissingInSource(oldSelectedItems, newSelectedItems);
  }

  getToBeRemoved(oldSelectedItems, newSelectedItems) {
    return this.getMissingInSource(newSelectedItems, oldSelectedItems);
  }

  /**
   * Return arrays with values from array which source array not contains
   */
  getMissingInSource(source = [], array = []) {
    if (source.length === 0) {
      return array;
    }
    return array.filter(function (valueFromArray) {
      return source.every(function (valueFromSource) {
        return valueFromArray.id !== valueFromSource.id;
      });
    });
  }

  /**
   * Return array with values that are contained into firstArray and secondArray.
   */
  getIntersection(firstArray = [], secondArray = []) {
    return firstArray.filter(function (valueFromFirstArray) {
      return secondArray.some(function (valueFromSecondArray) {
        return valueFromFirstArray.id === valueFromSecondArray.id;
      });
    });
  }
}