import {EventEmitter} from 'common/event-emitter';
import {ModelHeaders} from 'administration/model-management/sections/header/model-headers';

import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelHeader} from 'administration/model-management/model/model-header';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelHeaderMetaData} from 'administration/model-management/meta/model-header-meta';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';
import {ModelMultiAttribute} from 'administration/model-management/model/attributes/model-multi-attribute';

import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';
import {ConfirmationDialogService} from 'components/dialog/confirmation-dialog-service';
import {stub} from 'test/test-utils';

describe('ModelHeadersSection', () => {

  let model;
  let modelHeadersSection;
  let modelManagementLanguageServiceStub;
  let confirmationDialogServiceStub;

  beforeEach(() => {
    model = createModel();

    modelManagementLanguageServiceStub = stub(ModelManagementLanguageService);
    modelManagementLanguageServiceStub.getSystemLanguage.returns('en');
    modelManagementLanguageServiceStub.getUserLanguage.returns('bg');
    modelManagementLanguageServiceStub.getDefaultLanguage.returns('en');

    confirmationDialogServiceStub = stub(ConfirmationDialogService);

    modelHeadersSection = new ModelHeaders(confirmationDialogServiceStub, modelManagementLanguageServiceStub);

    modelHeadersSection.emitter = stub(EventEmitter);
    modelHeadersSection.model = model;

    modelHeadersSection.notifyForSectionStateChange = sinon.spy();
    modelHeadersSection.notifyForModelAttributeChange = sinon.spy();
    modelHeadersSection.notifyForModelStateCalculation = sinon.spy();
  });

  it('should initialize definition model', () => {
    modelHeadersSection.ngOnInit();
    expect(modelHeadersSection.model).to.eq(model);
    expect(modelHeadersSection.selectedLanguage).to.equal('en');
    expect(modelHeadersSection.languages).to.deep.equal(['ro', 'en', 'bg', 'de', 'fi']);
  });

  it('should sort the header types based on the order in provided options', () => {
    modelHeadersSection.ngOnInit();
    let mapping = modelHeadersSection.getHeaders().map(header => header.getId());
    expect(mapping).to.deep.equal(['default_header', 'compact_header', 'breadcrumb_header', 'tooltip_header']);
  });

  it('should subscribe to model change or reload', () => {
    modelHeadersSection.ngOnInit();
    expect(modelHeadersSection.emitter.subscribe.calledOnce).to.be.true;
  });

  it('should notify for model state re-calculation', () => {
    modelHeadersSection.ngOnInit();
    expect(modelHeadersSection.notifyForModelStateCalculation.callCount).to.eq(4);
  });

  function createModel() {
    let model = new ModelDefinition('definition');

    model.addHeader(createHeader('breadcrumb_header', [
      {locale: 'ro', value: '<span>Header5</span>'}
    ]));

    model.addHeader(createHeader('default_header', [
      {locale: 'en', value: '<span>Header1</span>'},
      {locale: 'bg', value: '<span>Header1</span>'}
    ]));

    model.addHeader(createHeader('compact_header', [
      {locale: 'en', value: '<span>Header2</span>'},
      {locale: 'de', value: '<span>Header2</span>'}
    ]));

    model.addHeader(createHeader('tooltip_header', [
      {locale: 'en', value: '<span>Header3</span>'},
      {locale: 'fi', value: '<span>Header3</span>'}
    ]));

    return model;
  }

  function createHeader(id, labels) {
    let header = new ModelHeader(id);

    let labelAttribute = new ModelMultiAttribute(ModelAttribute.LABEL_ATTRIBUTE);
    labels.forEach(label => labelAttribute.addValue(new ModelValue(label.locale, label.value)));
    header.addAttribute(labelAttribute);

    let headerType = new ModelAttribute(ModelAttribute.HEADER_TYPE_ATTRIBUTE);
    headerType.setValue(new ModelValue().setValue(id));
    header.addAttribute(headerType);

    headerType.setMetaData(createModelHeaderMetaData(
      ModelAttribute.HEADER_TYPE_ATTRIBUTE,
      ModelAttributeTypes.SINGLE_VALUE.MODEL_OPTION_TYPE,
      0, '',
      [
        getOption('default_header'),
        getOption('compact_header'),
        getOption('breadcrumb_header'),
        getOption('tooltip_header')
      ]
    ));

    return header;
  }

  function getOption(value) {
    return {value};
  }

  function createModelHeaderMetaData(id, type, order, defaultValue, options) {
    return new ModelHeaderMetaData(id)
      .setType(type)
      .setOrder(order)
      .setDefaultValue(defaultValue)
      .setOptions(options || []);
  }
});