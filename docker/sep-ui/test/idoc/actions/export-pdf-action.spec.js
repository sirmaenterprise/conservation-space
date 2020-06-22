import {ExportPDFAction, NOTIFICATION_LABEL_ID} from 'idoc/actions/export-pdf-action';
import {InstanceObject} from 'models/instance-object';
import {IdocMocks} from '../idoc-mocks';
import {PromiseStub} from 'test/promise-stub';

describe('ExportPDFAction', () => {

  it('should return notification label id', () => {
    let action = new ExportPDFAction({}, {}, {}, {});
    let labelId = action.getNotificationLabelId();
    expect(labelId).to.equal(NOTIFICATION_LABEL_ID)
  });

  it('should unset active tab id in the action context before call of the super method', () => {
    let spySuperExecute = sinon.spy(ExportPDFAction.prototype, 'execute');
    let action = new ExportPDFAction({}, actionsService, translateService, notificationService, authenticationService, instanceRestService, PromiseStub);
    let currentObject = new InstanceObject("emf:123456", IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    const actionContext = {
      activeTabId: 'tab-123456',
      currentObject: currentObject
    };
    action.execute({}, actionContext);
    expect(spySuperExecute.getCall(0).args[1].activeTabId).to.be.undefined;
  });

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
