import {View, Component, Inject, NgScope, NgCompile, NgElement} from 'app/app';
import {ModelControlExtensionProviderService} from 'administration/model-management/components/field/control/model-control-extension-provider-service';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

import 'components/collapsible/collapsible-panel';
import 'components/hint/label-hint';
import 'filters/to-trusted-html';

import './model-field-control-view.css!css';
import template from './model-field-control-view.html!text';

/**
 * Component responsible for visualizing a given model field control.
 * The type of the view is determined from the extension definition which
 * is obtained from the service which loads and cache the extensions. The
 * provided control property should be of type ModelAttribute.
 *
 * - control: current ModelControl which to be bound to this view
 * - context: the control's context - the owning ModelField
 *
 * @author svelikov
 */
@Component({
  transclude: true,
  selector: 'model-field-control-view',
  properties: {
    'control': 'control',
    'context': 'context'
  },
  events: ['onAttributeChange']
})
@View({
  template
})
@Inject(NgScope, NgCompile, NgElement, TranslateService, PromiseAdapter, ModelControlExtensionProviderService)
export class ModelFieldControlView {

  constructor($scope, $compile, $element, translateService, promiseAdapter, modelControlExtensionProviderService) {
    this.$scope = $scope;
    this.$compile = $compile;
    this.$element = $element;
    this.translateService = translateService;
    this.promiseAdapter = promiseAdapter;
    this.modelControlExtensionProviderService = modelControlExtensionProviderService;
  }

  ngOnInit() {
    this.extension = this.modelControlExtensionProviderService.getExtension(this.control.getId());
    this.controlLabel = this.translateService.translateInstant(this.extension.label);
    this.controlTooltip = this.translateService.translateInstant(this.extension.tooltip);
    this.compileControlExtension(this.extension);
  }

  compileControlExtension(extension) {
    if (this.extensionScope) {
      this.extensionScope.$destroy();
    }
    this.extensionScope = this.$scope.$new();

    let html = `<${extension.component}`;
    html += ' control="modelFieldControlView.control"';
    html += ' context="modelFieldControlView.context"';
    html += ' extension="modelFieldControlView.extension"';
    html += ' editable="modelFieldControlView.isEditable()"';
    html += ' on-change="modelFieldControlView.onControlParamChange(attribute)"';
    html += `></${extension.component}>`;

    let compiledExtension = this.$compile(html)(this.extensionScope)[0];
    let extensionElement = this.$element.find('.panel');
    extensionElement.append(compiledExtension);
  }

  onControlParamChange(attribute) {
    return this.onAttributeChange && this.onAttributeChange({attribute});
  }

  isDirty() {
    return this.control.isDirty();
  }

  isEditable() {
    return true;
  }
}

