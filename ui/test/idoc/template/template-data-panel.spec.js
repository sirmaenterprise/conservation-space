import {TemplateDataPanel} from 'idoc/template/template-data-panel';
import {PromiseStub} from 'test/promise-stub';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('TemplateDataPanel', () => {
  var dialog;
  var modelsService;
  var namespaceService;
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
    dialog = new TemplateDataPanel(mock$scope(), modelsService, namespaceService);
  });

  it('should configure object type select for single selection', () => {
    dialog.ngOnInit();

    expect(dialog.objectTypeConfig).to.exist;
    expect(dialog.objectTypeConfig.multiple).to.be.false;
  });

  it('should configure object type select to support class filtering', () => {
    const CLASS_FILTER = 'testType';
    dialog.config.typeFilter = CLASS_FILTER;

    dialog.ngOnInit();

    expect(dialog.objectTypeConfig).to.exist;
    expect(dialog.objectTypeConfig.classFilter).to.equal(CLASS_FILTER);
  });

  it('should configure object type select to automatically select first item', () => {
    const CLASS_FILTER = 'testType';
    dialog.config.typeFilter = CLASS_FILTER;

    dialog.ngOnInit();

    expect(dialog.objectTypeConfig).to.exist;
    expect(dialog.objectTypeConfig.defaultToFirstValue).to.be.true;
  });

  it('should configure object type select for single selection', () => {
    dialog.ngOnInit();

    expect(dialog.objectTypeConfig).to.exist;
    expect(dialog.objectTypeConfig.multiple).to.be.false;
  });

  it('should invoke the watch when the object type changes', () => {
    dialog.ngOnInit();

    dialog.$scope.$watch = sinon.spy();
    dialog.objectTypeConfig.publishCallback(dummyItems);
    dialog.config.type = 'image';
    expect(dialog.$scope.$watch.called).to.be.true;
  });

  it('should use parent ID, if the object ID is not full URI', () => {
    dialog.ngOnInit();

    dialog.config.type = 'image';
    namespaceService.isFullUri = () => {
      return false;
    };
    dialog.objectTypeConfig.publishCallback(dummyItems);
    dialog.$scope.$digest();
    expect(modelsService.getClassInfo.called).to.be.true;
    expect(modelsService.getClassInfo.getCall(0).args[0]).to.eq('imageParent');
  });

  it('should set the proper template parameters according to the class info', () => {
    var classInfo = {
      creatable: true,
      uploadable: false
    };
    dialog.updateTemplatePurpose(classInfo);
    expect(dialog.config.template.purpose).to.eq(TemplateDataPanel.CREATABLE);
    expect(dialog.config.isPurposeDisabled).to.be.true;

    classInfo.uploadable = true;
    dialog.updateTemplatePurpose(classInfo);
    expect(dialog.config.template.purpose).to.eq(TemplateDataPanel.CREATABLE);
    expect(dialog.config.isPurposeDisabled).to.be.false;

    classInfo.creatable = false;
    dialog.updateTemplatePurpose(classInfo);
    expect(dialog.config.template.purpose).to.eq(TemplateDataPanel.UPLOADABLE);
    expect(dialog.config.isPurposeDisabled).to.be.true;

    classInfo.uploadable = false;
    dialog.updateTemplatePurpose(classInfo);
    expect(dialog.config.template.purpose).to.eq(TemplateDataPanel.CREATABLE);
    expect(dialog.config.isPurposeDisabled).to.be.true;
  });
});