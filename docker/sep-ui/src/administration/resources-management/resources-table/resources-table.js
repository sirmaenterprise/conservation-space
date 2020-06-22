import {View, Component, Inject, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {DialogService} from 'components/dialog/dialog-service';
import {PropertiesSelectorHelper} from 'idoc/widget/properties-selector/properties-selector-helper';
import {ConfigurationRestService} from 'services/rest/configurations-service';
import {Configuration} from 'common/application-config';
import {ModelUtils} from 'models/model-utils';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import {HEADER_COMPACT} from 'instance-header/header-constants';
import {MODE_PREVIEW} from 'idoc/idoc-constants';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {InstanceModelProperty} from 'models/instance-model';
import 'components/instance-selector/instance-selector';
import _ from 'lodash';
import 'idoc/widget/properties-selector/properties-selector';

import './resources-table.css!css';
import template from './resources-table.html!text';

/**
 * Component for resource (user/group) management.
 *
 * It consists of table with resources. The table can be configured which properties of the resources to be shown,
 * for that the properties selector helper service is used along with the dialog service to select them in modal
 * dialog. The configuration for the selected resource properties are stored in a tenant configuration.
 *
 * Expected configurations:
 * config = {
 *    propertiesConfigKey: tenant configuration key for the selected properties,
 *    models: models of the resources,
 *    definitionId: definition id of the resources,
 *    editCallback: callback function which to be invoked when resource is edited
 * }
 *
 * resources = [array of user instances, each instance must have context property with config for actions]
 */
@Component({
  selector: 'seip-resources-table',
  properties: {
    'config': 'config',
    'resources': 'resources'
  }
})
@View({
  template
})
@Inject(DialogService, PropertiesSelectorHelper, ConfigurationRestService, Configuration, Eventbus, NgScope)
export class ResourcesTable extends Configurable {

  constructor(dialogService, propertiesSelectorHelper, configurationService, configuration, eventbus, $scope) {
    super({});

    this.dialogService = dialogService;
    this.propertiesSelectorHelper = propertiesSelectorHelper;
    this.configurationService = configurationService;
    this.configuration = configuration;
    this.eventbus = eventbus;
    this.$scope = $scope;
  }

  ngOnInit() {
    this.initializeSubscriptions();

    this.initializeSelectedProperties(this.configuration.getJson(this.config.propertiesConfigKey).columns);

    this.initResourcesWatcher();

    this.initializeViewModel();

    this.createPropertiesSelectorConfig();
  }

  initializeSubscriptions() {
    if (_.isFunction(this.config.actionExecutedCallback)) {
      this.instanceRefreshEventSubscription = this.eventbus.subscribe(InstanceRefreshEvent, () => this.config.actionExecutedCallback());
    }
  }

  initializeSelectedProperties(selectedProperties) {
    this.selectedProperties = PropertiesSelectorHelper.transformSelectedProperies(selectedProperties);
  }

  initResourcesWatcher() {
    this.$scope.$watch(() => {
      return this.resources;
    }, () => {
      if (this.resources) {
        this.initObjectPropertiesData();
      }
    });
  }

  initObjectPropertiesData() {
    this.relations = {};
    this.resources.forEach((resource) => {
      this.relations[resource.id] = {};
      Object.keys(resource.properties).forEach((propertyName) => {
        if (ModelUtils.isObjectProperty(resource.properties[propertyName]) && (Object.keys(this.selectedProperties).indexOf(propertyName) !== -1)) {
          this.relations[resource.id][propertyName] = {};
          this.relations[resource.id][propertyName].model = new InstanceModelProperty({value: resource.properties[propertyName]});
          this.relations[resource.id][propertyName].config = {
            propertyName,
            mode: MODE_PREVIEW,
            objectId: resource.id,
            // selection is a configuration needed to object picker and is set to 'multiple' just for the sake of
            // completeness, but is actually not used as the related objects can't be edited in this view but just
            // previewed
            selection: MULTIPLE_SELECTION,
            instanceHeaderType: HEADER_COMPACT
          };
        }
      });
    });
  }

  initializeViewModel() {
    this.flatViewModel = ModelUtils.flatViewModel(this.config.models.viewModel);
  }

  createPropertiesSelectorConfig() {
    this.propertiesSelectorHelper.extractDefinitionsByIdentifiers([this.config.definitionId]).then((definitions) => {
      this.propertiesSelectorConfig = {
        config: {
          definitions,
          selectedProperties: {
            [this.config.definitionId]: _.cloneDeep(this.selectedProperties)
          }
        }
      };

      this.createPropertiesDialogConfig();
    });
  }

  createPropertiesDialogConfig() {
    this.propertiesDialogConfig = {
      showHeader: true,
      header: 'resources.table.properties.dialog.header',
      buttons: [{
        id: DialogService.OK,
        label: 'dialog.button.save',
        cls: 'btn-primary',
        onButtonClick: (button, dialogScope, dialogConfig) => {
          this.initializeSelectedProperties(_.cloneDeep(this.propertiesSelectorConfig.config.selectedProperties[this.config.definitionId]));

          this.initObjectPropertiesData();

          this.updateConfiguration();

          dialogConfig.dismiss();
        }
      }, {
        id: DialogService.CANCEL,
        label: 'dialog.button.cancel',
        dismiss: true
      }]
    };
  }

  updateConfiguration() {
    let value = {
      columns: Object.keys(this.selectedProperties)
    };

    let configuration = {
      key: this.config.propertiesConfigKey,
      value: JSON.stringify(value)
    };
    this.configurationService.updateConfigurations([configuration]);
  }

  editResource(resource) {
    if (_.isFunction(this.config.editCallback)) {
      this.config.editCallback(resource);
    }
  }

  configureTable() {
    this.dialogService.create('seip-properties-selector', this.propertiesSelectorConfig, this.propertiesDialogConfig);
  }

  getPropertyLabel(propertyId) {
    let property = this.flatViewModel.get(propertyId);
    if (property) {
      return property.label;
    }
  }

  getPropertyType(propertyId) {
    let property = this.flatViewModel.get(propertyId);
    if (property) {
      return ModelUtils.defineControlType(property);
    }
  }

  ngOnDestroy() {
    if (this.instanceRefreshEventSubscription) {
      this.instanceRefreshEventSubscription.unsubscribe();
    }
  }

}