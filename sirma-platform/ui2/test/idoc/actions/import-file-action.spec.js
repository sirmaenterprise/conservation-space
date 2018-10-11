import {ImportFileAction} from 'idoc/actions/import-file-action';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'models/instance-object';
import {IdocMocks} from '../idoc-mocks';
import {DialogService} from 'components/dialog/dialog-service';

describe('ImportFileAction', () => {
  var action;
  var currentObject;
  beforeEach(() => {
    action = new ImportFileAction(translateService, notificationService, dialogService, importService, instanceRestService, configurationService, authenticationService);
    currentObject = new InstanceObject("emf:123456", IdocMocks.generateModels(), IdocMocks.generateIntialContent());
  });


  it('should read and validate file', () => {
    const data = {
      config: {
        title: '',
      }, currentObject: {
        id: 'emf:123123123'
      }
    };
    action.triggerJnlp = () => { sinon.spy() };
    action.execute(currentObject, data);
    expect(importService.readFile.called).to.be.true;
    expect(notificationService.info.called).to.be.true;
  });

  let translateService = {
    translateInstant: sinon.spy()
  };

  let notificationService = {
    info: sinon.spy(),
    success: sinon.spy()
  };


  let componentScope = {
    contextSelector: {
      config: {
        parentId: '123456789'
      }
    }
  };

  let dialogService = {
    confirmation: (message, header, opts) => {
      let dialogConfig = {
        dismiss: () => {
        }
      };
      opts.onButtonClick(DialogService.CONFIRM, undefined, dialogConfig);
    },
    create: (message, header, opts) => {
      let dialogConfig = {
        dismiss: () => {
        }
      };
      opts.onButtonClick(DialogService.CONFIRM, componentScope, dialogConfig);
      opts.onButtonClick(DialogService.OK, componentScope, dialogConfig);
    }
  };

  let instanceRestService = {
    load: sinon.spy(() => {
      return PromiseStub.resolve({
        data: {
          id: 'emf:123123123',
          properties: {
            hasParent: ['emf:987654321']
          }
        }
      });
    })
  };

  let importService = {
    readFile: sinon.spy(() => {
      return PromiseStub.resolve({
        data: {
          report: {
            headers: {
              breadcrumb_header: 'this is a breadcrumb_header'
            }
          },
          data: [1, 2, 3]
        }
      });
    }),
    importFile: sinon.spy(() => {
      return PromiseStub.resolve({});
    })
  }

  let configurationService = {
    get: sinon.spy()
  }
  let authenticationService = {
    getToken: sinon.spy()
  }

});
