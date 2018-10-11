import {ExportHandler} from 'idoc/actions/export-handler';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'models/instance-object';
import {IdocMocks} from '../idoc-mocks';
import {ErrorCodes} from 'services/rest/error-codes';

describe('ExportHandler', () => {
  it('should check for mandatory arguments on init', () => {
    expect(function () {
      new ExportActionNoArguments();
    }).to.throw(Error, /Missing mandatory argument: actionsService, translateService or notificationService/);
  });

  it('should check for template methods implementations on init', () => {
    expect(function () {
      new ExportActionNoGetLabelIdMethod();
    }).to.throw(TypeError, /ExportHandler handlers must override the 'getNotificationLabelId' function!/);
    expect(function () {
      new ExportActionNoAfterExportHandlerMethod();
    }).to.throw(TypeError, /ExportHandler handlers must override the 'afterExportHandler' function!/);
  });

  it('should rise notification before export to begin', () => {
    let handler = new CustomExportHandler(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, PromiseStub);
    handler.execute(action, context);
    expect(notificationService.info.called).to.be.true;
    expect(notificationService.info.getCall(0).args).to.eql([NOTIFICATION_LABEL]);
  });

  it('should invoke the export service to export document to pdf when executed', () => {
    let handler = new CustomExportHandler(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, PromiseStub);
    handler.exportPDF(action, context);
    expect(spyExportPdf.called).to.be.true;
  });

  it('exportPDF should call notification service if server operation timeouts', () => {
    let actionsServiceWithReject = {
      exportPDF: () => {
        let timeoutRejection = {
          data: {
            code: ErrorCodes.TIMEOUT
          }
        };
        return PromiseStub.reject(timeoutRejection);
      }
    };
    let handler = new CustomExportHandler(logger, actionsServiceWithReject, translateService, notificationService, authenticationService, instanceRestService, PromiseStub);
    handler.exportPDF(action, context);
    expect(notificationService.error.called).to.be.true;
    expect(notificationService.error.getCall(0).args[0]).to.equal('export.timeout.error.message');
  });

  it('should invoke the export service to export document to word when executed', () => {
    let handler = new CustomExportHandler(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, PromiseStub);
    handler.exportWord(action, context);
    expect(spyExportWord.called).to.be.true;
  });

  it('exportWord should call notification service if server operation timeouts', () => {
    let actionsServiceWithReject = {
      exportWord: () => {
        let timeoutRejection = {
          data: {
            code: ErrorCodes.TIMEOUT
          }
        };
        return PromiseStub.reject(timeoutRejection);
      }
    };
    let handler = new CustomExportHandler(logger, actionsServiceWithReject, translateService, notificationService, authenticationService, instanceRestService, PromiseStub);
    handler.exportWord(action, context);
    expect(notificationService.error.called).to.be.true;
    expect(notificationService.error.getCall(0).args[0]).to.equal('export.timeout.error.message');
  });

  it('should invoke the afterExportHandler after export is completed', () => {
    let handler = new CustomExportHandler(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, PromiseStub);
    let spyAfterExportHandler = sinon.spy(handler, 'afterExportHandler');
    handler.exportPDF(action, context);
    expect(spyAfterExportHandler.calledOnce).to.be.true;
    expect(spyAfterExportHandler.getCall(0).args).to.eql([exportingResult]);

    spyAfterExportHandler.restore();
    handler.exportWord(action, context);
    expect(spyAfterExportHandler.calledOnce).to.be.true;
    expect(spyAfterExportHandler.getCall(0).args).to.eql([exportingResult]);
  });

  it('skipHttpInterceptor should return true if export timed out', () => {
    let handler = new CustomExportHandler(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, PromiseStub);
    let timeoutRejection = {
      data: {
        code: ErrorCodes.TIMEOUT
      }
    };
    expect(handler.skipHttpInterceptor(timeoutRejection)).to.be.true;

    let anotherRejection = {
      data: {
        message: 'Error message'
      }
    };
    expect(handler.skipHttpInterceptor(anotherRejection)).to.be.false;
  });

  describe('getInstanceObjectTitle', () => {
    it('should return current object title if object is loaded', () => {
      let handler = new CustomExportHandler(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, PromiseStub);
      let insanceObject = new InstanceObject("emf:123456", IdocMocks.generateModels(), IdocMocks.generateIntialContent());
      handler.getInstanceObjectTitle(insanceObject).then((title) => {
        expect(title).to.equals('Title');
      });
    });
    // When action is called from search results a dummy InstanceObject is used. Maybe we should change this
    it('should load object and return its title if object is not fully loaded', () => {
      let handler = new CustomExportHandler(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, PromiseStub);
      let insanceObject = new InstanceObject("emf:123456");
      handler.getInstanceObjectTitle(insanceObject).then((title) => {
        expect(title).to.equals('idoc title');
      });
    });
  });

  describe('hasSystemContent', () => {
    it('should return appropriate flag based on selected tab', () => {
      let handler = new CustomExportHandler(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, PromiseStub);
      CustomExportHandler.prototype.getActiveTabId = ()=> {
        return '#tab1'
      };
      context.idocPageController = {};
      expect(handler.hasSystemContent(context)).to.be.false;

      let tab = {
        system: false
      };

      // validate tab without system content
      context.idocPageController.tabsConfig = {
        getActiveTab: ()=> {
          return tab;
        }
      };
      expect(handler.hasSystemContent(context)).to.be.false;

      // validate tab with system content
      tab.system = true;
      expect(handler.hasSystemContent(context)).to.be.true;
    });
  });

  const NOTIFICATION_LABEL = 'Export begins';
  let action = {};
  let currentObject = new InstanceObject("emf:123456", IdocMocks.generateModels(), IdocMocks.generateIntialContent());
  let context = {
    currentObject: currentObject
  };
  let logger = {};
  let exportingResult = {
    data: 'test'
  };
  let actionsService = {
    exportPDF: () => {
      return PromiseStub.resolve(exportingResult)
    },
    exportWord: () => {
      return PromiseStub.resolve(exportingResult)
    }
  };
  let spyExportPdf = sinon.spy(actionsService, 'exportPDF');
  let spyExportWord = sinon.spy(actionsService, 'exportWord');
  let translateService = {
    translateInstant: (lblId) => {
      return lblId;
    }
  };
  let notificationService = {
    info: sinon.spy(),
    error: sinon.spy()
  };
  let authenticationService = {
    info: sinon.spy()
  };
  let instanceRestService = {
    load: () => PromiseStub.resolve({data: {properties: {title: 'idoc title'}}})
  };

  class CustomExportHandler extends ExportHandler {
    constructor(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, PromiseStub) {
      super(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, PromiseStub);
    }

    afterExportHandler() {
    }

    getNotificationLabelId() {
      return NOTIFICATION_LABEL;
    }
  }

  class ExportActionNoArguments extends ExportHandler {
    constructor() {
      super();
    }

    afterExportHandler() {
    }

    getNotificationLabelId() {
    }
  }

  class ExportActionNoGetLabelIdMethod extends ExportHandler {
    constructor() {
      super({}, {}, {}, {});
    }

    afterExportHandler() {
    }
  }

  class ExportActionNoAfterExportHandlerMethod extends ExportHandler {
    constructor() {
      super({}, {}, {}, {});
    }

    getNotificationLabelId() {
    }
  }
});