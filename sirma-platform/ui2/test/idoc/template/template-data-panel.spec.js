import {TemplateDataPanel} from 'idoc/template/template-data-panel';
import {PropertiesRestService} from 'services/rest/properties-service';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('TemplateDataPanel', () => {
  var templateDataPanel;
  var modelsService;
  var namespaceService;
  var propertiesService;
  var dummyItems = [
    {
      id: 'image',
      uri: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Image',
      parent: 'imageParent'
    },
    {
      id: 'commonDocument',
      parent: 'commonDocumentParent'
    },
    {
      id: 'CASE0002',
      parent: 'CASE0002Parent'
    }
  ];

  beforeEach(() => {
    namespaceService = {
      isFullUri: sinon.spy(() => {
        return true;
      })
    };
    modelsService = {
      getClassInfo: sinon.spy(() => {
        return PromiseStub.resolve({
          data: {
            id: 'testUri',
            creatable: true,
            uploadable: true
          }
        });
      })
    };

    propertiesService = stub(PropertiesRestService);

    templateDataPanel = new TemplateDataPanel(mock$scope(), modelsService, namespaceService, propertiesService);
  });

  it('should configure object type select for single selection', () => {
    templateDataPanel.ngOnInit();

    expect(templateDataPanel.objectTypeConfig).to.exist;
    expect(templateDataPanel.objectTypeConfig.multiple).to.be.false;
  });

  it('should configure object type select to support class filtering', () => {
    const CLASS_FILTER = 'testType';
    templateDataPanel.config.typeFilter = CLASS_FILTER;

    templateDataPanel.ngOnInit();

    expect(templateDataPanel.objectTypeConfig).to.exist;
    expect(templateDataPanel.objectTypeConfig.classFilter).to.equal(CLASS_FILTER);
  });

  it('should configure object type select to automatically select first item', () => {
    const CLASS_FILTER = 'testType';
    templateDataPanel.config.typeFilter = CLASS_FILTER;

    templateDataPanel.ngOnInit();

    expect(templateDataPanel.objectTypeConfig).to.exist;
    expect(templateDataPanel.objectTypeConfig.defaultToFirstValue).to.be.true;
  });

  it('should configure object type select for single selection', () => {
    templateDataPanel.ngOnInit();

    expect(templateDataPanel.objectTypeConfig).to.exist;
    expect(templateDataPanel.objectTypeConfig.multiple).to.be.false;
  });

  it('should invoke the watch when the object type changes', () => {
    templateDataPanel.ngOnInit();

    templateDataPanel.$scope.$watch = sinon.spy();
    templateDataPanel.objectTypeConfig.publishCallback(dummyItems);
    templateDataPanel.config.type = 'image';
    expect(templateDataPanel.$scope.$watch.called).to.be.true;
  });

  it('should use parent ID, if the object ID is not full URI', () => {
    templateDataPanel.ngOnInit();

    templateDataPanel.config.type = 'image';
    namespaceService.isFullUri = () => {
      return false;
    };
    templateDataPanel.objectTypeConfig.publishCallback(dummyItems);
    templateDataPanel.$scope.$digest();
    expect(modelsService.getClassInfo.called).to.be.true;
    expect(modelsService.getClassInfo.getCall(0).args[0]).to.eq('imageParent');
  });

  it('should set the proper template parameters according to the class info', () => {
    var classInfo = {
      creatable: true,
      uploadable: false
    };
    templateDataPanel.updateTemplatePurpose(classInfo);
    expect(templateDataPanel.config.template.purpose).to.eq(TemplateDataPanel.CREATABLE);
    expect(templateDataPanel.config.isPurposeDisabled).to.be.true;

    classInfo.uploadable = true;
    templateDataPanel.updateTemplatePurpose(classInfo);
    expect(templateDataPanel.config.template.purpose).to.eq(TemplateDataPanel.CREATABLE);
    expect(templateDataPanel.config.isPurposeDisabled).to.be.false;

    classInfo.creatable = false;
    templateDataPanel.updateTemplatePurpose(classInfo);
    expect(templateDataPanel.config.template.purpose).to.eq(TemplateDataPanel.UPLOADABLE);
    expect(templateDataPanel.config.isPurposeDisabled).to.be.true;

    classInfo.uploadable = false;
    templateDataPanel.updateTemplatePurpose(classInfo);
    expect(templateDataPanel.config.template.purpose).to.eq(TemplateDataPanel.CREATABLE);
    expect(templateDataPanel.config.isPurposeDisabled).to.be.true;
  });

  it('should initially mark the title field as not valid since the title is empty', () => {
    expect(templateDataPanel.titleValid).to.be.false;
  });

  it('should validate the title for uniqueness when title is set', () => {
    const TITLE = "title1";

    propertiesService.checkFieldUniqueness.withArgs('template', undefined, 'title', TITLE).returns(PromiseStub.resolve({
      data: {
        unique: true
      }
    }));

    templateDataPanel.title = TITLE;
    templateDataPanel.onTitleChanged();

    expect(propertiesService.checkFieldUniqueness.calledOnce).to.be.true;
    expect(templateDataPanel.config.template.title).to.equal(TITLE);
    expect(templateDataPanel.titleValid).to.be.true;
  });

  it('should clean up the title in the config when validation fails and display error message', () => {
    const TITLE = "title1";

    propertiesService.checkFieldUniqueness.returns(PromiseStub.resolve({
      data: {
        unique: false
      }
    }));

    templateDataPanel.title = TITLE;
    templateDataPanel.config.template.title = TITLE;
    templateDataPanel.onTitleChanged();

    expect(propertiesService.checkFieldUniqueness.calledOnce).to.be.true;
    expect(templateDataPanel.config.template.title).to.be.null;
    expect(templateDataPanel.titleValid).to.be.false;
  });

  it('should clean up the title in the config when the title in the form is cleared', () => {
    const TITLE = "title1";

    templateDataPanel.title = null;
    templateDataPanel.config.template.title = TITLE;
    templateDataPanel.onTitleChanged();

    expect(propertiesService.checkFieldUniqueness.callCount).to.equal(0);
    expect(templateDataPanel.config.template.title).to.be.null;
    expect(templateDataPanel.titleValid).to.be.false;
  });

});