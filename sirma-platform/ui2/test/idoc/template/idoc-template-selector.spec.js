import {IdocTemplateSelector} from 'idoc/template/idoc-template-selector';
import {TemplateService} from 'services/rest/template-service';
import {PromiseStub} from 'test/promise-stub';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {stub} from 'test/test-utils';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {InstanceObject} from 'models/instance-object';

describe('IdocTemplateSelector', () => {

  let selectIdocTemplate;
  let templateService;

  const TEST_TEPLATE_ID = '123';

  beforeEach(() => {
    templateService = stub(TemplateService);

    let result = PromiseStub.resolve({
      data: [{
        id: TEST_TEPLATE_ID,
        title: 'Template 123'
      }]
    });

    templateService.loadTemplates.withArgs('testType').returns(result);

    templateService.loadContent.returns(PromiseStub.resolve({data: 'primary-content'}));

    selectIdocTemplate = new IdocTemplateSelector(templateService, mock$scope(), mockTimeout());

    selectIdocTemplate.onTemplateSelected = sinon.spy();
    selectIdocTemplate.onTemplateContentLoaded = sinon.spy();
    selectIdocTemplate.getEligibleFields = sinon.spy(() => {
      return [];
    });

  });

  describe('adaptTemplates', () => {

    it('should transform object to { id: <template-id>, text: <template-title> }', () => {
      let expected = [{
        id: '1',
        templateInstanceId: 'templateId',
        text: 'T1'
      }];
      let toConvert = [{
        id: '1',
        correspondingInstance: 'templateId',
        title: 'T1'
      }];

      expect(selectIdocTemplate.adaptTemplates(toConvert)).to.deep.eq(expected);
    });

    it('should move the primary template to be the first in the array', () => {
      let expected = [{
        id: '1',
        templateInstanceId: 'templateId',
        text: 'T1'
      }, {
        id: '2',
        templateInstanceId: 'templateId',
        text: 'T2'
      }, {
        id: '3',
        templateInstanceId: 'templateId',
        text: 'T3'
      }];
      let toConvert = [{
        id: '2',
        correspondingInstance: 'templateId',
        title: 'T2'
      }, {
        id: '3',
        correspondingInstance: 'templateId',
        title: 'T3'
      }, {
        id: '1',
        correspondingInstance: 'templateId',
        title: 'T1',
        primary: true
      }];

      expect(selectIdocTemplate.adaptTemplates(toConvert)).to.deep.eq(expected);
    });
  });

  it('should load templates providing filtering criteria if available', () => {
    selectIdocTemplate.objectType = 'testType';
    selectIdocTemplate.purpose = 'creatable';
    selectIdocTemplate.fetchTemplateFilterCriteria = sinon.spy(() => {
      return {'active': true};
    });

    selectIdocTemplate.loadAvailableTemplates();
    expect(templateService.loadTemplates.calledOnce).to.be.true;

    expect(templateService.loadTemplates.getCall(0).args[0]).to.equals('testType');
    expect(templateService.loadTemplates.getCall(0).args[1]).to.equals('creatable');
    expect(templateService.loadTemplates.getCall(0).args[2]).to.eql({'active': true});
  });

  it('should not load templates if no object type is provided in the configuration', () => {
    selectIdocTemplate.loadAvailableTemplates();
    expect(templateService.loadTemplates.called).to.be.false;
  });

  it('should construct select component configuration for displaying available templates', () => {
    selectIdocTemplate.objectType = 'testType';
    selectIdocTemplate.purpose = 'creatable';

    selectIdocTemplate.loadAvailableTemplates();
    expect(selectIdocTemplate.selectTemplateConfig).to.exist;
  });

  it('should fire an event when a template gets selected', () => {
    selectIdocTemplate.objectType = 'testType';
    selectIdocTemplate.purpose = 'creatable';

    selectIdocTemplate.loadAvailableTemplates();

    selectIdocTemplate.setTemplate(TEST_TEPLATE_ID);

    expect(selectIdocTemplate.onTemplateSelected.calledOnce).to.be.true;
    expect(selectIdocTemplate.onTemplateContentLoaded.calledOnce).to.be.true;

    let payloadTemplateId = selectIdocTemplate.onTemplateSelected.getCall(0).args[0];
    let payloadTemplateContent = selectIdocTemplate.onTemplateContentLoaded.getCall(0).args[0];

    expect(payloadTemplateId.event.template.id).to.equal(TEST_TEPLATE_ID);
    expect(payloadTemplateContent.event.content).to.equal('primary-content');
  });

  it('getTemplatePurpose should return uploadable when emf:contentId is set', () => {
    selectIdocTemplate.instanceObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    selectIdocTemplate.instanceObject.getPropertyValue = () => {
      return 'emf:123';
    };

    expect(selectIdocTemplate.getTemplatePurpose()).to.equals('uploadable');
  });

  it('getTemplatePurpose should return creatable when emf:contentId is undefined', () => {
    selectIdocTemplate.instanceObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    expect(selectIdocTemplate.getTemplatePurpose()).to.equals('creatable');
  });

});

function mockTimeout() {
  return (callbackFunction) => {
    callbackFunction();
  };
}