import {View, Component, Inject, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {TemplateService} from 'services/rest/template-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {AfterEditActionExecutedEvent} from 'idoc/actions/events/after-edit-action-executed-event';
import _ from 'lodash';

import template from './select-idoc-template.html!text';
/**
 * Handles default template assigning and displaying of template selector.
 */
@Component({
  selector: 'seip-select-idoc-template',
  properties: {
    'config': 'config',
    'callback': 'callback'
  }
})
@View({
  template: template
})
@Inject(TemplateService, Eventbus, NgScope)
export class SelectIdocTemplate extends Configurable {

  constructor(templateService, eventbus, $scope) {
    super({});
    this.templateService = templateService;
    this.events = [eventbus.subscribe(AfterEditActionExecutedEvent, () => this.handleEntityLoad())];

    this.handleEntityLoad();

    $scope.$watch(() => {
      return this.templateId;
    }, (templateId) => {
      this.setTemplate(templateId);
    });
  }

  handleEntityLoad() {
    var object = this.config.currentObject;
    if (!object || object.isPersisted() && object.content) {
      return;
    }

    this.loadAvailableTemplates(object);
  }

  loadAvailableTemplates(object) {
    var definitionId = object.models.definitionId;
    this.templateService.loadTemplates(definitionId).then(response => {
      var templates = this.adaptTemplates(response.data);
      if (object.isPersisted()) {
        this.setTemplate(templates[0].id);
      } else {
        this.createSelectConfig(templates);
      }
    });
  }

  setTemplate(templateId) {
    if (templateId && templateId.length > 0) {
      this.templateService.loadContent(templateId).then((response) => {
        let content = response.data;
        let template = _.find(this.templateMaps, template => {
          if (template.id === templateId) {
            return true;
          }
        });
        this.callback(content, template);
      });
    }
  }

  createSelectConfig(templates) {
    this.selectTemplateConfig = {
      defaultToFirstValue: true,
      multiple: false,
      data: templates
    };
  }

  adaptTemplates(templates) {
    // move primary to be first
    var index = _.findIndex(templates, item => (item.properties && item.properties.primary));
    if (index > 0) {
      let item = templates[index];
      templates.splice(index, 1);
      templates.unshift(item);
    }
    this.templateMaps = templates.map(t => {
      return {id: t.id, templateInstanceId: t.templateInstanceId, text: t.properties.title};
    });
    return this.templateMaps;
  }

  isSelectDisplayed() {
    var object = this.config.currentObject;
    var context = this.config.idocContext;

    var viableTarget = object && !object.isPersisted();
    return context.isEditMode() && viableTarget && !!this.selectTemplateConfig;
  }

  ngOnDestroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
  }
}