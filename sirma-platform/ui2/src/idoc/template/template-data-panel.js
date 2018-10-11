import {Component, View, Inject, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {ModelsService} from 'services/rest/models-service';
import {NamespaceService} from 'services/rest/namespace-service';
import {PropertiesRestService} from 'services/rest/properties-service';
import _ from 'lodash';
import {TEMPLATE_DEFINITION_TYPE, TITLE} from './template-constants';
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
@Inject(NgScope, ModelsService, NamespaceService, PropertiesRestService)
export class TemplateDataPanel extends Configurable {

  constructor($scope, modelsService, nameSpaceService, propertiesRestService) {
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
    this.propertiesRestService = propertiesRestService;
    this.titleValid = false;
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
      this.config.template.purpose = this.config.template.purpose || TemplateDataPanel.CREATABLE;
      this.config.isPurposeDisabled = false;
    }
  }

  onTitleChanged() {
    this.titleValid = true;

    if (this.title) {
      this.propertiesRestService.checkFieldUniqueness(TEMPLATE_DEFINITION_TYPE, undefined, TITLE, this.title).then(response => {
        if (response.data.unique) {
          this.config.template.title = this.title;
        } else {
          this.config.template.title = null;
          this.titleValid = false;
        }
      });
    } else {
      this.config.template.title = null;
      this.titleValid = false;
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