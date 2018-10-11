import {View, Component, Inject, NgScope, NgElement, NgCompile} from 'app/app';
import {PluginsService} from 'services/plugin/plugins-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

import './model-attribute-view.css!css';
import template from './model-attribute-view.html!text';

const DEFAULT_EXTENSION_TYPE = 'string';
const MODEL_ATTRIBUTE_EXTENSION = 'model-management-attribute';

/**
 * Component responsible for visualizing a given model attribute. When
 * an attribute is provided though the component property internally
 * the type of the attribute is used to determine the proper view it
 * requires to be rendered. The proved attribute property should be
 * of type {@link ModelAttribute} or any types extending off of it.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-attribute-view',
  properties: {
    'attribute': 'attribute',
  }
})
@View({
  template
})
@Inject(PluginsService, PromiseAdapter, NgScope, NgElement, NgCompile)
export class ModelAttributeView {

  constructor(pluginsService, promiseAdapter, $scope, $element, $compile) {
    this.$scope = $scope;
    this.$element = $element;
    this.$compile = $compile;

    this.promiseAdapter = promiseAdapter;
    this.pluginsService = pluginsService;
  }

  ngOnInit() {
    this.getAttributeExtension(this.attribute).then(extension => this.compileAttributeExtension(extension));
  }

  getAttributeExtension(attribute) {
    let type = attribute.getType();

    if (!this.modules) {
      return this.pluginsService.loadComponentModules(MODEL_ATTRIBUTE_EXTENSION, 'type')
        .then(modules => (this.modules = modules) && this.getModuleByType(type));
    } else {
      this.promiseAdapter.resolve(this.getModuleByType(type));
    }
  }

  compileAttributeExtension(extension) {
    if (this.extensionScope) {
      this.extensionScope.$destroy();
    }
    this.extensionScope = this.$scope.$new();

    let html = `<${extension.component}`;
    html += ' attribute="modelAttributeView.attribute"';
    html += `></${extension.component}>`;

    let compiledExtension = this.$compile(html)(this.extensionScope)[0];
    let extensionElement = this.$element.find('.attribute-extension');
    extensionElement.empty();
    extensionElement.append(compiledExtension);
  }

  getModuleByType(type) {
    return this.modules[type] || this.modules[DEFAULT_EXTENSION_TYPE];
  }
}