import {ModelControlExtensionProviderService} from 'administration/model-management/components/field/control/model-control-extension-provider-service';
import {PluginsService} from 'services/plugin/plugins-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelControlExtensionProviderService', () => {

  let service;
  let pluginsServiceStub;

  beforeEach(() => {
    pluginsServiceStub = stub(PluginsService);
    pluginsServiceStub.loadComponentModules.returns(PromiseStub.resolve(getExtensions()));

    service = new ModelControlExtensionProviderService(pluginsServiceStub, PromiseStub);
  });

  it('should load and cache extensions if not present', () => {
    service.loadModelControlExtensions().then(extensions => {
      expect(pluginsServiceStub.loadComponentModules.calledOnce).to.be.true;
      expect(service.extensions).to.eql(getExtensions());
      expect(extensions).to.eql(getExtensions());
    });

    service.loadModelControlExtensions().then(extensions => {
      expect(pluginsServiceStub.loadComponentModules.calledOnce).to.be.true;
      expect(service.extensions).to.eql(getExtensions());
      expect(extensions).to.eql(getExtensions());
    });
  });

  function getExtensions() {
    return {
      'DEFAULT_VALUE_PATTERN': {
        'order': 10,
        'type': 'DEFAULT_VALUE_PATTERN',
        'supportedBy': ['ALPHA_NUMERIC_TYPE', 'ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE', 'ALPHA_NUMERIC_FIXED_TYPE', 'FLOATING_POINT_TYPE', 'FLOATING_POINT_FIXED_TYPE', 'NUMERIC_TYPE', 'NUMERIC_FIXED_TYPE', 'DATE_TYPE', 'DATETIME_TYPE', 'BOOLEAN', 'CODELIST', 'URI'],
        'name': 'model-default-value-pattern-control',
        'component': 'model-default-value-pattern-control',
        'module': 'administration/model-management/components/field-controls/default-value-pattern/model-default-value-pattern-control',
        'label': 'administration.models.management.field.controls.default_value_pattern.label',
        'link': 'administration.models.management.field.controls.default_value_pattern.link'
      },
      'RICHTEXT': {
        'order': 20,
        'type': 'RICHTEXT',
        'supportedBy': ['ALPHA_NUMERIC_TYPE', 'ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE'],
        'name': 'model-richtext-control',
        'component': 'model-richtext-control',
        'module': 'administration/model-management/components/field-controls/richtext/model-richtext-control',
        'label': 'administration.models.management.field.controls.richtext.label',
        'link': 'administration.models.management.field.controls.richtext.link'
      }
    };
  }

});