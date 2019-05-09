import {ModelDescriptionLinker} from 'administration/model-management/services/linkers/model-description-linker';
import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelAttributeMetaData} from 'administration/model-management/meta/model-attribute-meta'

import {stub} from 'test/test-utils';

describe('ModelDescriptionLinker', () => {

  let model;
  let modelDescriptionLinker;
  let modelManagementLanguageServiceStub;

  beforeEach(() => {
    modelManagementLanguageServiceStub = stub(ModelManagementLanguageService);

    modelManagementLanguageServiceStub.getSystemLanguage.returns('en');
    modelManagementLanguageServiceStub.getUserLanguage.returns('bg');
    modelManagementLanguageServiceStub.getDefaultLanguage.returns('en');

    model = new ModelDefinition('PR0001');
    modelDescriptionLinker = new ModelDescriptionLinker(modelManagementLanguageServiceStub);
  });

  it('should insert model descriptions when multi language labels are provided', () => {
    modelManagementLanguageServiceStub.getApplicableDescription = sinon.spy(() => model.getDescriptionByLanguage('en'));
    modelDescriptionLinker.insertDescriptions(model, {'en': 'Label', 'bg': 'Етикет'});

    // should insert to model each of the provided languages as descriptions
    expect(model.getDescriptionByLanguage('en').getValue()).to.eq('Label');
    expect(model.getDescriptionByLanguage('bg').getValue()).to.eq('Етикет');

    // should set the base description as a reference based on the language
    expect(model.getDescription()).to.eq(model.getDescriptionByLanguage('en'));
    expect(modelManagementLanguageServiceStub.getApplicableDescription.calledOnce).to.be.true;
  });

  it('should insert default model description when no labels are provided', () => {
    modelManagementLanguageServiceStub.getApplicableDescription = sinon.spy(() => model.getDescriptionByLanguage('en'));
    modelDescriptionLinker.insertDescriptions(model, {});

    // should insert to model default description built based on model id
    expect(model.getDescriptionByLanguage('en').getValue()).to.eq('PR0001');

    // should set the base description as a reference based on the language
    expect(model.getDescription()).to.eq(model.getDescriptionByLanguage('en'));
    expect(modelManagementLanguageServiceStub.getApplicableDescription.calledOnce).to.be.true;
  });

  it('should insert model tooltips when multi language tooltips are provided', () => {
    model = new ModelAttributeMetaData('PR0001');
    modelDescriptionLinker.insertTooltips(model, {'en': 'Tooltip value', 'bg': 'Подсказка'});

    expect(model.getTooltipByLanguage('en').getValue()).to.eq('Tooltip value');
    expect(model.getTooltipByLanguage('bg').getValue()).to.eq('Подсказка');
  });

});