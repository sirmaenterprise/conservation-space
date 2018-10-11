import {Inject, Injectable} from 'app/app';
import {ExtensionsDialogService} from 'services/extensions-dialog/extensions-dialog-service';
import {PickerRestrictionsService} from 'services/picker/picker-restrictions-service';
import {NO_SELECTION, SINGLE_SELECTION} from 'search/search-selection-modes';

import _ from 'lodash';

export const PICKER_EXTENSION_POINT = 'picker';
export const PICKER_DEFAULT_HEADER = 'picker.dialog.header';
export const PICKER_DEFAULT_HELP_TARGET = 'picker';
export const PICKER_DIALOG_CLASS = 'picker-modal';
export const PICKER_PANEL_CLASS = 'picker';

export const SEARCH_EXTENSION = 'seip-object-picker-search';
export const BASKET_EXTENSION = 'seip-object-picker-basket';
export const RECENT_EXTENSION = 'seip-object-picker-recent';
export const CREATE_EXTENSION = 'seip-object-picker-create';
export const UPLOAD_EXTENSION = 'seip-object-picker-upload';

/**
 * Stateless service for configuring extensions configuration and opening an object picker with it. Each extension is
 * related to specific component (Search, Basket etc.) and thus the service is tightly coupled with it. To include
 * another component in the picker means to ensure its correct configuration in this service
 *
 * Because of the nature of this service to work with specific components, it accepts minimal initial configurations
 * and assigns default ones to relieve components of having to specify every property.
 *
 * To visualize a component in the picker, it must be registered in PluginRegistry under the "picker" extension name.
 *
 * This service internally uses the {@link ExtensionsDialogService} and {@link ExtensionsPanel} so the provided
 * picker configuration must be in the following format:
 * {
 *    extensionPoint: '', // The name under which the extensions are registered in the plugin registry. Default is
 *                        // 'picker'.Anything other than this is discouraged.
 *    header: '',         // The dialog header. Has a default value so it's not mandatory
 *    helpTarget: '',     // The help target visualized in the dialog header. Has a default value.
 *    extensions: {       // Map of extension name - extension configurations
 *      'some-extension' : {...} // The configuration object which is given to this component
 *    },
 *    tabs: {}            // Map for providing specific picker tabs configurations per extension
 * }
 *
 * The extensions configurations are not necessarily designed to be for a dialog, they could also be used directly for
 * embedding in a panel.
 *
 * One of the most important properties for the picker is the <code>selectedItems</code> array which is shared among
 * the picker extensions so it is very important to never ever change the reference or it will not work correctly. This
 * is also valid for the rest of the extension configurations.
 *
 * The service provides {@link PickerService#getSelectedItems} and {@link PickerService#setSelectedItems} to avoid to
 * directly access them through the picker configuration.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(ExtensionsDialogService, PickerRestrictionsService)
export class PickerService {

  constructor(extensionsDialogService, pickerRestrictionsService) {
    this.extensionsDialogService = extensionsDialogService;
    this.pickerRestrictionsService = pickerRestrictionsService;
  }

  /**
   * Opens the picker with the provided extensions configuration. Response structure is the map of the extensions
   * configurations if not externally overridden in the dialogConfiguration object.
   *
   * @param pickerConfiguration - the configuration for the registered picker extensions
   * @param context - iDoc context provided to the extensions
   * @param dialogConfiguration - reference or overriding the configurations for the dialog in which the picker is
   *   displayed
   * @returns {Promise} resolved with the selected items when the picker is closed with OK.
   */
  open(pickerConfiguration, context, dialogConfiguration) {
    return this.extensionsDialogService.openDialog(pickerConfiguration, context, dialogConfiguration).then((extensionsConfigurations) => {
      return this.getSelectedItems(extensionsConfigurations);
    });
  }

  /**
   * Configures the provided extensions configuration with default values and then opens the picker with them. Response
   * structure is the map of the extensions configurations if not externally overridden in the
   * <code>dialogConfiguration</code> object.
   *
   * @param pickerConfiguration - the configuration for the registered picker extensions
   * @param context - iDoc context provided to the extensions
   * @param dialogConfiguration - reference or overriding the configurations for the dialog in which the picker is
   *   displayed
   * @returns {Promise} resolved when the picker is closed with OK
   */
  configureAndOpen(pickerConfiguration, context, dialogConfiguration) {
    pickerConfiguration = pickerConfiguration || {};
    dialogConfiguration = dialogConfiguration || {};
    this.assignDefaultConfigurations(pickerConfiguration);
    this.assignDefaultDialogConfigurations(dialogConfiguration);
    return this.open(pickerConfiguration, context, dialogConfiguration);
  }

  /**
   * Assigns default values for the provided extensions configuration object to avoid specifying it in every component
   * that uses the picker.
   */
  assignDefaultConfigurations(pickerConfiguration) {
    pickerConfiguration = _.defaultsDeep(pickerConfiguration, {
      extensionPoint: PICKER_EXTENSION_POINT,
      header: PICKER_DEFAULT_HEADER,
      helpTarget: PICKER_DEFAULT_HELP_TARGET,
      modalCls: PICKER_DIALOG_CLASS,
      panelCls: PICKER_PANEL_CLASS,
      extensions: {},
      tabs: {}
    });

    this.assignDefaultSearchConfigurations(pickerConfiguration.extensions);
    this.assignDefaultCreateConfigurations(pickerConfiguration.extensions);
    this.assignDefaultUploadConfigurations(pickerConfiguration.extensions);
    this.assignDefaultBasketConfigurations(pickerConfiguration.extensions, pickerConfiguration.tabs);
    this.assignDefaultRecentObjectsConfigurations(pickerConfiguration.extensions);
    this.assignDefaultPickerDialogConfigurations(pickerConfiguration);
  }

  /**
   * Assigns default picker configurations for the {@link SEARCH_EXTENSION} extension. Those configurations will be
   * used later in the other extensions to preserve references.
   */
  assignDefaultSearchConfigurations(extensions) {
    extensions[SEARCH_EXTENSION] = _.defaultsDeep(extensions[SEARCH_EXTENSION] || {}, {
      criteria: {},
      // Picker should always trigger search
      triggerSearch: true,
      useRootContext: true,
      predefinedTypes: [],
      results: {
        config: {
          // Instance header redirect confirm dialog should be showed
          linkRedirectDialog: true,
          // Usually the picker is used only with single selection
          selection: SINGLE_SELECTION,
          // Good defaults to avoid specifying them everywhere
          selectedItems: [],
          exclusions: []
        }
      }
    });

    this.assignRestrictions(extensions[SEARCH_EXTENSION]);
    this.assignDefaultSelectionHandler(extensions[SEARCH_EXTENSION]);
  }

  assignRestrictions(searchConfig) {
    if (searchConfig && searchConfig.restrictions) {
      this.pickerRestrictionsService.assignRestrictionCriteria(searchConfig.criteria, searchConfig.restrictions);
    }
  }

  assignDefaultPickerDialogConfigurations(pickerConfig) {
    let searchConfig = pickerConfig.extensions[SEARCH_EXTENSION];

    if (searchConfig && searchConfig.restrictions) {
      this.pickerRestrictionsService.assignRestrictionMessage(pickerConfig, searchConfig.restrictions);
    }
  }

  /**
   * Assigns the default selection handler which is invoked every time when a selection is changed among the picker
   * extensions. The behaviour depends on the current selection mode (single or multiple).
   */
  assignDefaultSelectionHandler(searchConfig) {
    let selectedItems = searchConfig.results.config.selectedItems;
    let singleSelection = searchConfig.results.config.selection === SINGLE_SELECTION;

    _.defaultsDeep(searchConfig, {
      results: {
        config: {
          selectionHandler: (instance) => {
            this.handleSelection(selectedItems, singleSelection, instance);
          }
        }
      }
    });
  }

  assignDefaultDialogConfigurations(dialogConfig) {
    dialogConfig = _.defaultsDeep(dialogConfig, {
      buttons: [{
        label: 'dialog.button.select',
      }, {
        label: 'dialog.button.cancel',
      }]
    });
    return dialogConfig;
  }

  /**
   * Assigns default configuration for the create extension point to the provided extensions configuration map.
   */
  assignDefaultCreateConfigurations(extensions) {
    let searchConfig = extensions[SEARCH_EXTENSION];
    let useContext = searchConfig.useContext;
    let predefinedTypes = searchConfig.predefinedTypes;
    let selectedItems = searchConfig.results.config.selectedItems;
    let defaultHandler = searchConfig.results.config.selectionHandler;

    let selectionHandler = (instance) => {
      if (searchConfig.restrictions) {
        this.pickerRestrictionsService.handleSelection(instance, searchConfig.restrictions, defaultHandler);
      } else {
        defaultHandler(instance);
      }
    };

    extensions[CREATE_EXTENSION] = _.defaultsDeep(extensions[CREATE_EXTENSION] || {}, {
      selectedItems,
      predefinedTypes,
      selectionHandler,
      useContext
    });
  }

  /**
   * Assigns default configuration for the upload extension point to the provided extensions configuration map.
   * In this specific case the default upload configuration is the same as the default create configuration.
   */
  assignDefaultUploadConfigurations(extensions) {
    extensions[UPLOAD_EXTENSION] = _.defaultsDeep(extensions[UPLOAD_EXTENSION] || {}, extensions[CREATE_EXTENSION]);
  }

  /**
   * Assigns default configuration for the basket extension point to the provided extensions configuration map.
   * It also assigns additional configurations for the extension tab - the postfix function which renders the current
   * selection count.
   */
  assignDefaultBasketConfigurations(extensions, tabs) {
    let searchConfig = extensions[SEARCH_EXTENSION];
    let linkRedirectDialog = searchConfig.results.config.linkRedirectDialog;
    let selectableItems = searchConfig.results.config.selection !== NO_SELECTION;
    let selectedItems = searchConfig.results.config.selectedItems;
    let singleSelection = searchConfig.results.config.selection === SINGLE_SELECTION;
    let selectionHandler = searchConfig.results.config.selectionHandler;

    extensions[BASKET_EXTENSION] = _.defaultsDeep(extensions[BASKET_EXTENSION] || {}, {
      selectableItems,
      singleSelection,
      selectedItems,
      selectionHandler,
      linkRedirectDialog
    });

    tabs[BASKET_EXTENSION] = _.defaultsDeep(tabs[BASKET_EXTENSION] || {}, {
      postfix: () => {
        // Means to display the current count of selected items in the basket tab
        return `<span class="badge">${selectedItems.length}</span>`;
      }
    });
  }

  /**
   * Assigns default configuration for the recently used objects extension point to the provided extensions
   * configuration map.
   */
  assignDefaultRecentObjectsConfigurations(extensions) {
    let searchConfig = extensions[SEARCH_EXTENSION];

    // Recent and basket components are using instance-list internally so their configurations are almost the same thus
    // we need just a shallow copy of the basket one with some additional configurations for the recently used.
    let recentConfigDefaults = _.clone(extensions[BASKET_EXTENSION]);
    extensions[RECENT_EXTENSION] = _.defaultsDeep(extensions[RECENT_EXTENSION] || {}, recentConfigDefaults);
    extensions[RECENT_EXTENSION].criteria = searchConfig.criteria;
    extensions[RECENT_EXTENSION].predefinedTypes = searchConfig.predefinedTypes;
    extensions[RECENT_EXTENSION].exclusions = searchConfig.results.config.exclusions;
    extensions[RECENT_EXTENSION].filterByWritePermissions = searchConfig.arguments && searchConfig.arguments.filterByWritePermissions || false;

    if(searchConfig.restrictions) {
      //provide a restriction filter only when restrictions are available
      extensions[RECENT_EXTENSION].restrictionFilter = (identifiers) => {
        return this.pickerRestrictionsService.filterByRestrictions(identifiers, searchConfig.restrictions);
      };
    }
  }

  /**
   * Based on the given selection mode (single or multiple) it handles the selection of the provided instance in the
   * selected items array.
   * 1) single selection - it assigns the provided instance as the first element in <code>selectedItems</code>
   * 2) multiple selection - the instance is added in <code>selectedItems</code> if it's not present or is removed
   *                         if present
   */
  handleSelection(selectedItems, singleSelection, instance) {
    if (singleSelection) {
      selectedItems[0] = instance;
    } else {
      let index = _.findIndex(selectedItems, (current) => current.id === instance.id);
      if (index === -1) {
        selectedItems.push(instance);
      } else {
        selectedItems.splice(index, 1);
      }
    }
  }

  /**
   * Clears the <code>selectedItems</code> in the provided picker configuration.
   */
  clearSelectedItems(pickerConfig) {
    pickerConfig.extensions[SEARCH_EXTENSION].results.config.selectedItems.splice(0);
  }

  /**
   * Retrieves the selected items from the provided picker configuration. This method supports two structures for the
   * provided configuration for ease of use:
   * 1) the one in which extension configurations are nested in the map <code>extensions: {}</code>. This is the one
   *    prepared from {@link #assignDefaultConfigurations}
   * 2) the second where the picker configuration is the extension configurations map itself.
   */
  getSelectedItems(pickerConfig) {
    if (pickerConfig.extensions) {
      return pickerConfig.extensions[SEARCH_EXTENSION].results.config.selectedItems;
    }
    return pickerConfig[SEARCH_EXTENSION].results.config.selectedItems;
  }

  /**
   * Replaces the <code>selectedItems</code> in the given picker configuration with those in the provided array. This
   * operation mutates the original array to preserve the reference.
   */
  setSelectedItems(pickerConfig, selectedItems) {
    let currentSelection = this.getSelectedItems(pickerConfig);
    if (currentSelection && selectedItems) {
      currentSelection.splice(0);
      currentSelection.push(...selectedItems);
    }
  }

}