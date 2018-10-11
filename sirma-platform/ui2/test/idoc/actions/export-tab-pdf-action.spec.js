import {ExportTabPDFAction, NOTIFICATION_LABEL_ID} from 'idoc/actions/export-tab-pdf-action';
import {InstanceObject} from 'models/instance-object';
import {IdocMocks} from '../idoc-mocks';
import {PromiseStub} from 'test/promise-stub';
import FileSaver from 'file-saver';

describe('ExportTabPDFAction', () => {

  before(() => {
    sinon.stub(FileSaver, 'saveAs', () => {});
  });

  it('should return notification label id', () => {
    let action = getActionHandlerInstance();
    let labelId = action.getNotificationLabelId();
    expect(labelId).to.equal(NOTIFICATION_LABEL_ID);
  });

  it('should set active tab id in the action context before call of the super method', () => {
    let action = getActionHandlerInstance();
    action.afterExportHandler = () => {
    };
    let spySuperExecute = sinon.spy(ExportTabPDFAction.prototype, 'execute');
    let spyJqueryAttr = sinon.stub($.fn, 'attr', () => {
      return '#tab-123456'
    });
    let currentObject = new InstanceObject("emf:123456", IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    const actionContext = {
      currentObject: currentObject,
      idocPageController: {
        tabsConfig: {
          getActiveTab: ()=> {
            return {
              system: false
            };
          }
        }
      }
    };
    action.execute({}, actionContext);
    expect(spySuperExecute.getCall(0).args[1].activeTabId).to.equal('123456');
    spyJqueryAttr.restore();
  });

  it('should send notification for restricted export', () => {
    let action = getActionHandlerInstance();
    ExportTabPDFAction.prototype.hasSystemContent = ()=> {
      return true;
    };
    action.execute({}, {});
    expect(action.notificationService.info).called;
  });

  it('should call decorator for download url', () => {
    let action = getActionHandlerInstance();
    let spyDownloadBlob = sinon.spy(action, 'downloadBlob');
    action.afterExportHandler({data: 'fileURL', config: {data: {fileName: 'testFile'}}});
    expect(spyDownloadBlob.calledOnce).to.be.true;
    expect(spyDownloadBlob.getCall(0).args[1]).to.equals('testFile.pdf');
  });

  function getActionHandlerInstance() {
    return new ExportTabPDFAction({}, actionsService, translateService, notificationService, authenticationService, instanceRestService, PromiseStub);
  }

  let actionsService = {
    exportPDF: () => {
      return Promise.resolve({data: 'fileURL'});
    }
  };
  let translateService = {
    translateInstant: sinon.spy()
  };
  let notificationService = {
    info: sinon.spy()
  };
  let authenticationService = {
    getToken: sinon.spy()
  };
  let instanceRestService = {
    load: () => PromiseStub.resolve({data: {properties: {title: 'idoc title'}}})
  };
});
