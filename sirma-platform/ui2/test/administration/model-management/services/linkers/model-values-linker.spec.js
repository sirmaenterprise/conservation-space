import {ModelValuesLinker} from 'administration/model-management/services/linkers/model-values-linker';
import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';

import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelMultiAttribute} from 'administration/model-management/model/attributes/model-multi-attribute';

import {stub} from 'test/test-utils';

describe('ModelValuesLinker', () => {

  let modelValuesLinker;
  let modelManagementLanguageServiceStub;

  beforeEach(() => {
    modelManagementLanguageServiceStub = stub(ModelManagementLanguageService);

    modelManagementLanguageServiceStub.getSystemLanguage.returns('en');
    modelManagementLanguageServiceStub.getUserLanguage.returns('bg');
    modelManagementLanguageServiceStub.getDefaultLanguage.returns('en');

    modelValuesLinker = new ModelValuesLinker(modelManagementLanguageServiceStub);
  });

  it('should insert values when a single valued primitive is provided', () => {
    let attribute = new ModelSingleAttribute('identifier').setType('string');
    modelValuesLinker.insertValues(attribute, 'value');

    // should set the base value of the model
    expect(attribute.getValue().value).to.eq('value');
  });

  it('should insert values when multi language values are provided', () => {
    let attribute = new ModelMultiAttribute('label').setType('label');
    modelValuesLinker.insertValues(attribute, {'en': 'Label', 'bg': 'Етикет'});

    // should insert to model each of the provided values as values
    expect(attribute.getValueByLanguage('en').getValue()).to.eq('Label');
    expect(attribute.getValueByLanguage('bg').getValue()).to.eq('Етикет');

    // should set the base value as a reference based on the language
    expect(attribute.getValue()).to.eq(attribute.getValueByLanguage('en'));
  });

  it('should insert values when values object is empty or not present', () => {
    let attribute = new ModelMultiAttribute('label').setType('label');
    modelValuesLinker.insertValues(attribute, {});

    // should insert to model empty value in the default language
    expect(attribute.getValueByLanguage('en').getValue()).to.eq('');

    // should set the base value as a reference based on the language
    expect(attribute.getValue()).to.eq(attribute.getValueByLanguage('en'));
  });
});