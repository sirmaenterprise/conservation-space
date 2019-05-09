import {ModelManagementCodelistService} from 'administration/model-management/services/utility/model-management-codelist-service';
import {CodelistRestService} from 'services/rest/codelist-service';
import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';
import {Eventbus} from 'services/eventbus/eventbus';

import {CodeListSavedEvent} from 'administration/code-lists/services/events/code-list-saved-event';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelManagementCodelistService', () => {

  let eventBusStub;
  let codeListRestServiceStub;
  let modelManagementLanguageServiceStub;

  let modelManagementCodelistService;

  beforeEach(() => {
    eventBusStub = stub(Eventbus);
    codeListRestServiceStub = stub(CodelistRestService);
    modelManagementLanguageServiceStub = stub(ModelManagementLanguageService);

    codeListRestServiceStub.getCodeList.returns(PromiseStub.resolve(getCodeList()));
    codeListRestServiceStub.getCodeLists.returns(PromiseStub.resolve(getCodeLists()));
    modelManagementLanguageServiceStub.getApplicableDescription = extractor => extractor('en');

    modelManagementCodelistService = new ModelManagementCodelistService(eventBusStub, codeListRestServiceStub, modelManagementLanguageServiceStub, PromiseStub);
  });

  it('should properly extract and cache available code lists', () => {
    expect(modelManagementCodelistService.codeLists.size()).to.eq(0);

    let codeLists = modelManagementCodelistService.getCodeLists();

    expect(modelManagementCodelistService.codeLists.size()).to.eq(1);
    expect(codeLists).to.eventually.deep.eq([getTransformedCodeList(getCodeList())]);
  });

  it('should properly update code lists cache if existing code list is edited', () => {
    modelManagementCodelistService.getCodeLists();

    let updated = getCodeList();
    updated.descriptions[0].name = 'New project state';
    let expected = getTransformedCodeList(updated);

    modelManagementCodelistService.updateCodeList(updated);
    expect(modelManagementCodelistService.getCodeLists()).to.eventually.deep.eq([expected]);
  });

  it('should properly add code lists cache if new code list is added', () => {
    modelManagementCodelistService.getCodeLists();

    let created = getCodeList();
    created.value = 'New project state';
    let expected = getTransformedCodeList(created);

    modelManagementCodelistService.updateCodeList(created);
    expect(modelManagementCodelistService.getCodeLists()).to.eventually.deep.eq([getTransformedCodeList(getCodeList()), expected]);
  });

  it('should subscribe to code list change', () => {
    expect(eventBusStub.subscribe.calledWith(CodeListSavedEvent)).to.be.true;
  });

  it('should update code list cache through the subscription callback', () => {
    modelManagementCodelistService.codeLists = [];
    modelManagementCodelistService.updateCodeList = sinon.spy();

    let updated = getCodeList();
    let callback = eventBusStub.subscribe.args[0][1];

    callback([updated]);
    expect(modelManagementCodelistService.updateCodeList.calledOnce).to.be.true;
    expect(modelManagementCodelistService.updateCodeList.calledWith(updated)).to.be.true;
  });

  function getCodeEntry(code) {
    return {
      value: code.value,
      label: code.descriptions[0].name
    };
  }

  function getTransformedCodeList(code) {
    let transformed = getCodeEntry(code);
    transformed.values = code.values.map(value => getCodeEntry(value));
    return transformed;
  }

  function getCodeList() {
    // extract a single code list
    return getCodeLists().data[0];
  }

  function getCodeLists() {
    return {
      data: [
        {
          value: '1',
          descriptions: [{
            language: 'en',
            name: 'Project state'
          }, {
            language: 'bg',
            name: 'Състояние на проект'
          }],
          values: [{
            value: 'approved',
            descriptions: [{
              language: 'en',
              name: 'Approved'
            }, {
              language: 'bg',
              name: 'Прието'
            }]
          }, {
            value: 'rejected',
            descriptions: [{
              language: 'en',
              name: 'Rejected'
            }, {
              language: 'bg',
              name: 'Отказано'
            }]
          }]
        }
      ]
    };
  }
});