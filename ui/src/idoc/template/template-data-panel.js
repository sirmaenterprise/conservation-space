import {Component, View, Inject, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {ModelsService} from 'services/rest/models-service';
import {NamespaceService} from 'services/rest/namespace-service';
import _ from 'lodash';
import 'components/select/object/object-type-select';
import './template-data-panel.css!';
import template from './template-data-panel.html!text';

@Component({
  selector: 'seip-template-data-panel',
  properties: {
    'config': 'config'
  }
})
@View({
  template: template
})
@Inject(NgScope, ModelsService, NamespaceService)
export class TemplateDataPanel extends Configurable {

  constructor($scope, modelsService, nameSpaceService) {
    super({
      template: {
        title: '',
        primary: false
      },
      isPurposeDisabled: false
    });
    this.$scope = $scope;
    this.modelsService = modelsService;
    this.nameSpaceService = nameSpaceService;
  }

  ngOnInit() {
    this.createObjectTypeConfig();
  }

  createObjectTypeConfig() {
    this.objectTypeConfig = {
      multiple: false,
      preferDefinitionType: true,
      publishCallback: (types) => {
        this.config.types = types;
        //the items have to be initialized before start watching for the type change
        this.$scope.$watch(() => {
          return this.config.type;
        }, () => {
          this.handleTypeChange();
        });
      },
      disableTypesWithoutDefinition: true
    };

    if (this.config.typeFilter) {
      this.objectTypeConfig.classFilter = this.config.typeFilter;
      this.objectTypeConfig.defaultToFirstValue = true;
    }
  }

  handleTypeChange() {
    let selectedType = _.find(this.config.types, (type) => {
      return type.id === this.config.type;
    });
    //if no selected type, disable the form and return.
    if (!selectedType) {
      this.config.isPurposeDisabled = true;
      return;
    }
    //if an URI is present, use it for getting the class info
    let selectedId = selectedType.uri || selectedType.id;
    //if only definition id is present, use the Full URI of the parent class
    if (!this.nameSpaceService.isFullUri(selectedId)) {
      selectedId = selectedType.parent;
    }
    this.modelsService.getClassInfo(selectedId).then(response => {
      let classInfo = response.data;
      this.updateTemplatePurpose(classInfo);
    });
  }

  updateTemplatePurpose(classInfo) {
    if (TemplateDataPanel.isCreatable(classInfo)) {
      this.config.template.purpose = TemplateDataPanel.CREATABLE;
      this.config.isPurposeDisabled = true;
    } else if (TemplateDataPanel.isUploadable(classInfo)) {
      this.config.template.purpose = TemplateDataPanel.UPLOADABLE;
      this.config.isPurposeDisabled = true;
    } else {
      this.config.template.purpose = TemplateDataPanel.CREATABLE;
      this.config.isPurposeDisabled = false;
    }
  }

  static isCreatable(classInfo) {
    return classInfo.creatable && !classInfo.uploadable || !classInfo.creatable && !classInfo.uploadable;
  }

  static isUploadable(classInfo) {
    return !classInfo.creatable && classInfo.uploadable;
  }
}

TemplateDataPanel.UPLOADABLE = 'uploadable';
TemplateDataPanel.CREATABLE = 'creatable';