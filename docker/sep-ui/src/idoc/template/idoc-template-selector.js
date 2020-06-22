import {View, Component, Inject, NgScope, NgTimeout} from 'app/app';
import {TemplateService} from 'services/rest/template-service';
import {ModelUtils} from 'models/model-utils';
import {TemplateRuleUtils} from 'idoc/template/rules/template-rule-utils';
import {CONTENT_ID} from 'instance/instance-properties';

import 'components/select/instance/instance-select';

import _ from 'lodash';

import template from './idoc-template-selector.html!text';

const DEBOUNCE_INTERVAL = 200;

/**
 * Handles default template assigning and displaying of template selector.
 *
 * Component properties:
 * - selectedTemplate - last selected template
 * - purpose - purpose for which to load templates
 * - instanceObject - the instance to apply template to.
 */
@Component({
  selector: 'seip-idoc-template-selector',
  properties: {
    'selectedTemplate': 'selectedTemplate',
    'instanceObject': 'instanceObject',
    'purpose': 'purpose'
  },
  events: ['onTemplateSelected', 'onTemplateContentLoaded']
})
@View({template})
@Inject(TemplateService, NgScope, NgTimeout)
export class IdocTemplateSelector {

  constructor(templateService, $scope, $timeout) {
    this.templateService = templateService;
    this.$timeout = $timeout;
    // TODO: This debounce is not needed. IMO it was added due to the fact that changing any eligible property value
    // triggers multiple propertyChanged events with the different attributes being changed and thus the multiple
    // #loadTemplate invocations. If we listen only for value propertyChange, we can limit invocations to one.
    this.loadTemplatesWithDebounce = _.debounce(this.loadTemplates, DEBOUNCE_INTERVAL);
    this.propertySubscriptions = [];

    $scope.$watch(() => {
      return this.templateId;
    }, (templateId) => {
      this.setTemplate(templateId);
    });

    $scope.$watch(() => {
      return this.instanceObject.getModels().definitionId;
    }, (newValue, oldValue) => {
      // prevents double loading during initialization https://stackoverflow.com/a/18915585/1119400
      if (newValue !== oldValue) {
        this.loadTemplatesWithDebounce();
      }
    }, true);
  }

  ngOnInit() {
    this.subscribeToInstanceObjectPropertyChange();
    this.loadTemplatesWithDebounce();
  }

  /**
   *
   * Update template filter criteria. It is map with key -> value pair used to filter templates which can used for
   * given instance. For now we extract all editable properties of instance.
   * @returns {{}}
   */
  fetchTemplateFilterCriteria() {
    let rule = {};
    this.getEligibleFields().forEach(viewModelField => {
      let validationField = this.instanceObject.getModels().validationModel[viewModelField.identifier];
      rule[viewModelField.identifier] = validationField.value;
    });
    return rule;
  }

  getEligibleFields() {
    let result = [];
    ModelUtils.walkModelTree(this.instanceObject.getModels().viewModel.fields, {}, (viewModelField) => {
      if (TemplateRuleUtils.isEligible(viewModelField)) {
        result.push(viewModelField);
      }
    });
    return result;
  }

  //The method is called from debounce, so we need to inform angular about the change.
  loadTemplates() {
    this.$timeout(() => {
      this.clearData();
      this.subscribeToInstanceObjectPropertyChange();
      this.objectType = this.instanceObject.getModels().definitionId;
      this.loadAvailableTemplates();
    }, 0);
  }

  subscribeToInstanceObjectPropertyChange() {
    this.getEligibleFields().forEach(viewModelField => {
      let validationField = this.instanceObject.getModels().validationModel[viewModelField.identifier];
      if (validationField) {
        this.propertySubscriptions.push(validationField.subscribe('propertyChanged', (evt) => {
          // trigger loading only when property value get's changed
          if ('value' in evt) {
            this.loadTemplates();
          }
        }));
      }
    });
  }

  loadAvailableTemplates() {
    if (this.objectType) {
      this.templateService.loadTemplates(this.objectType, this.getTemplatePurpose(), this.fetchTemplateFilterCriteria()).then(response => {
        let templates = this.adaptTemplates(response.data);

        if (this.selectTemplateConfig) {
          this.selectTemplateConfig.data = templates;
        } else {
          this.selectTemplateConfig = {
            defaultToFirstValue: true,
            multiple: false,
            data: templates
          };
        }
      });
    }
  }

  getTemplatePurpose() {
    if (!this.purpose) {
      return this.getPurpose();
    }
    return this.purpose;
  }

  // We need to know whether an persisted object is creatable or uploadable in order to dispalay the correct templates. Persisted
  // uploadable objects will always have set value in emf:contentId and creatable won't. At the moment we don't have better
  // mechanism to differentiate between uploadable and creatable persisted objects.
  getPurpose() {
    return this.instanceObject.getPropertyValue(CONTENT_ID) ? 'uploadable' : 'creatable';
  }

  /**
   * @deprecated Since CMF-28503 ENT-NEW15 Suggest the most appropriate primary template on Create new screen
   */
  setSelectorDefaultValue(templates) {
    let selected = _.find(templates, (current) => {
      return current.id === this.selectedTemplate;
    });

    if (this.selectedTemplate && selected) {
      this.selectTemplateConfig.defaultToFirstValue = false;
      this.selectTemplateConfig.defaultValue = this.selectedTemplate;
    } else {
      this.selectTemplateConfig.defaultValue = templates[0].id;
    }
  }

  setTemplate(templateId) {
    if (templateId && templateId.length > 0) {
      let template = _.find(this.templateMaps, template => {
        if (template.id === templateId) {
          return true;
        }
      });
      this.onTemplateSelected({
        event: {template}
      });
      this.templateService.loadContent(templateId).then((response) => {
        let content = response.data;
        this.onTemplateContentLoaded({
          event: {
            content,
            template
          }
        });
      });
    }
  }

  adaptTemplates(templates) {
    this.movePrimaryTemplateAsFirst(templates);

    this.templateMaps = templates.map(t => {
      return {id: t.id, templateInstanceId: t.correspondingInstance, text: t.title};
    });
    return this.templateMaps;
  }

  movePrimaryTemplateAsFirst(templates) {
    var index = _.findIndex(templates, item => item.primary);
    if (index > 0) {
      let item = templates[index];
      templates.splice(index, 1);
      templates.unshift(item);
    }
  }

  clearData() {
    _.forEach(this.propertySubscriptions, function (subscription) {
      subscription.unsubscribe();
    });
    this.propertySubscriptions = [];
  }

  ngOnDestroy() {
    this.clearData();
  }
}
