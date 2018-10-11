import {View, Inject, Component, NgScope} from 'app/app';
import {SearchCriteriaUtils, ANY_OBJECT} from 'search/utils/search-criteria-utils';
import {CreatePanelService, INSTANCE_CREATE_PANEL, FILE_UPLOAD_PANEL} from 'services/create/create-panel-service';
import {ModelsService} from 'services/rest/models-service';
import {NamespaceService} from 'services/rest/namespace-service';
import {RefreshWidgetsCommand} from 'idoc/actions/events/refresh-widgets-command';
import {Eventbus} from 'services/eventbus/eventbus';
import {SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';

import './datatable-header-create-item.css!';
import template from './datatable-header-create-item.html!text';

@Component({
  selector: 'seip-datatable-header-create-item',
  properties: {
    context: 'context',
    control: 'control',
    config: 'config'
  }
})
@View({
  template
})
@Inject(CreatePanelService, ModelsService, NamespaceService, NgScope, Eventbus)
export class DatatableHeaderCreateItem {
  constructor(createPanelService, modelsService, namespaceService, $scope, eventbus) {
    this.createPanelService = createPanelService;
    this.modelsService = modelsService;
    this.namespaceService = namespaceService;
    this.$scope = $scope;
    this.eventbus = eventbus;
  }

  ngOnInit() {
    this.assignConfigChangeWatchers();
  }

  openCreateDialog(){
    let params = {
      // Overriding with predefined data
      parentId: this.context.getId(),
      operation: 'create',
      predefinedTypes: this.filters.classFilter,
      predefinedSubTypes: this.filters.definitionFilter,
      exclusions: this.exclusions,
      // Overriding the dialog look & behaviour
      header: 'instance.create.dialog.header',
      helpTarget: 'idoc.action.create',
      createButtonLabel: 'instance.create.panel.create',
      showTemplateSelector: true,
      forceCreate: true,
      onClosed: (result) => {
        if (result.instanceCreated) {
          this.eventbus.publish(new RefreshWidgetsCommand());
        }
      }
    };
    this.createPanelService.openCreateInstanceDialog(params);
  }

  assignConfigChangeWatchers() {
    this.$scope.$watchCollection(() => {
      let watchConditions = [this.config.selectObjectMode, this.config.displayCreateAction];
      if (this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY && this.config.displayCreateAction) {
        watchConditions.push(this.config.criteria);
      }
      return watchConditions;
    }, () => {
      this.onConfigChange();
    });
  }

  onConfigChange() {
    this.createActionAvailable = false;
    if (this.config.displayCreateAction) {
      let criteriaTypes = SearchCriteriaUtils.getTypesFromCriteria(this.config.criteria);
      if (this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY && criteriaTypes.length > 0 && criteriaTypes.indexOf(ANY_OBJECT) === -1) {
        this.filters = this.extractClassAndDefinitionFilters(criteriaTypes);
        this.modelsService.getModels([ModelsService.PURPOSE_CREATE, ModelsService.PURPOSE_UPLOAD], this.context.getCurrentObjectId(), undefined, undefined, this.filters.classFilter, this.filters.definitionFilter).then((result) => {
          this.createActionAvailable = result.models.length > 0 && !result.errorMessage;
          this.determineExclusions(result.models);
        });
      }
    }
  }

  determineExclusions(models) {
    let hasPurposeCreate = false;
    let hasPurposeUpload = false;
    models.forEach(model => {
      if (model.creatable) {
        hasPurposeCreate = true;
      }
      if (model.uploadable) {
        hasPurposeUpload = true;
      }
    });
    this.exclusions = [];
    if(!hasPurposeCreate){
      this.exclusions.push(INSTANCE_CREATE_PANEL);
    }
    if(!hasPurposeUpload){
      this.exclusions.push(FILE_UPLOAD_PANEL);
    }
  }

  extractClassAndDefinitionFilters(criteriaTypes) {
    let classFilter = [];
    let definitionFilter = [];
    criteriaTypes.forEach((criteriaType) => {
      if(this.namespaceService.isUri(criteriaType)) {
        classFilter.push(criteriaType);
      } else {
        definitionFilter.push(criteriaType);
      }
    });
    return {classFilter, definitionFilter};
  }
}
