import {PromiseStub} from 'test/promise-stub';
import {BusinessProcessDiagramWidget} from 'idoc/widget/business-process/business-process-diagram-widget';
import {CURRENT_OBJECT_TEMP_ID} from 'idoc/idoc-context';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {SELECT_OBJECT_CURRENT} from 'idoc/widget/object-selector/object-selector';

describe('BusinessProcessDiagramWidget', () => {

  let bpmService;
  let instanceRestService;
  let dialogService;
  let objectSelectorHelper;
  let translateService;
  let eventbus;
  let promiseAdapter = PromiseStub;
  let scope;
  let processWidget;

  beforeEach(() => {
    bpmService = mockBpmService();
    instanceRestService = mockInstanceRestService();
    dialogService = mockDialogService();
    objectSelectorHelper = mockObjectSelectorHelper();
    translateService = mockTranslateServise();
    eventbus = mockEventBus();
    scope = mock$scope();
    BusinessProcessDiagramWidget.prototype.viewer = {
      importXML: sinon.spy((xml, callback) => {}),
      destroy: sinon.spy()
    };
    BusinessProcessDiagramWidget.prototype.isWorkflow = () => {
      return PromiseStub.resolve(true);
    };
    BusinessProcessDiagramWidget.prototype.context = {
      getCurrentObject: () => {
        return "emf:id";
      },
      setCurrentObjectId: function(id) {
        this.currentObjectId = id;
      },
      isModeling: () => {
        return false;
      }
    };
    BusinessProcessDiagramWidget.prototype.control = mockControl();
    BusinessProcessDiagramWidget.prototype.onConfigConfirmed = {
      bind: (element) => {

      }
    };
    processWidget = new BusinessProcessDiagramWidget(bpmService, instanceRestService, dialogService, objectSelectorHelper, translateService, eventbus, promiseAdapter, scope);
  });

  it('message for not started process', () => {
    processWidget.config = {
      selectObjectMode: SELECT_OBJECT_CURRENT
    };
    processWidget.context.setCurrentObjectId("currentObjectTempId");
    processWidget.objectSelectorHelper = {
      getSelectedObject: sinon.spy(() => {
        return PromiseStub.resolve(CURRENT_OBJECT_TEMP_ID);
      })
    };
    processWidget.ngAfterViewInit();
    expect(processWidget.message).to.equal('widgets.process.not.started');
    expect(processWidget.viewer.destroy.calledOnce).to.be.true;
  });


  it('message undefined', () => {
    processWidget.config = {
      selectObjectMode: SELECT_OBJECT_CURRENT
    };
    processWidget.context.setCurrentObjectId("someId");
    processWidget.setDiagramContents = () => {};
    processWidget.ngAfterViewInit();
    expect(processWidget.message).to.equal(undefined);
  });

  it('message empty', () => {
    processWidget.config = {
      selectObjectMode: "Other mode"
    };
    processWidget.context.setCurrentObjectId("someId");
    processWidget.objectSelectorHelper = {
      getSelectedObject: sinon.spy(() => {
        return PromiseStub.resolve(undefined);
      })
    };
    processWidget.setDiagramContents = () => {};
    processWidget.ngAfterViewInit();
    expect(processWidget.message).to.equal('widgets.process.empty');
    expect(processWidget.viewer.destroy.calledOnce).to.be.true;
  });

  it('should show empty widget in modeling view', () => {
    processWidget.clearDiagram = sinon.spy();
    processWidget.config = {
      selectObjectMode: "automatically"
    };
    processWidget.context.isModeling = () => {
      return true;
    };
    processWidget.ngAfterViewInit();
    expect(processWidget.message).to.equal(undefined);
    expect(processWidget.clearDiagram.calledOnce).to.be.true;
  });

  it('setDiagramContents on version', () => {
    processWidget.loadDiagram = sinon.spy(() => {});
    processWidget.config = {
      bpmn: 'WF:1:id',
      activity: 'someActivity'
    };
    processWidget.context = {
      getCurrentObject: () => {
        return PromiseStub.resolve({
          isVersion: () => {
            return true;
          }
        });
      }
    };
    processWidget.setDiagramContents();
    expect(processWidget.loadDiagram.calledOnce);
    expect(processWidget.loadDiagram.getCall(0).args[0]).to.equal('<xml> No idea </xml>');
    expect(processWidget.loadDiagram.getCall(0).args[1]).to.equal('someActivity');
  });

  function mockBpmService() {
    return {
      getEngine: sinon.spy(() => {
        return PromiseStub.resolve({
          data: "meh"
        });
      }),
      generateXmlURL: sinon.spy((enginePath, definition) => {
        return enginePath + '/process-definition/key/' + definition + '/xml';
      }),
      generateActivityURL: sinon.spy((enginePath, instanceId) => {
        return enginePath + '/process-instance/' + instanceId + '/activity-instances';
      }),
      generateVersionXmlUrl: sinon.spy((enginePath, id) => {
        return enginePath + '/process-definition/' + id + '/xml';
      }),
      executeCustomProcessRequestGet: sinon.spy((url) => {
        // Returns the xml camunda data.
        if (url.endsWith('xml')) {
          return PromiseStub.resolve({
            data: {
              id: "bpmnID",
              bpmn20Xml: "<xml> No idea </xml>"
            }
          });
          // gets the activity details for the instance here we need two responces
        } else if (url.endsWith('activity-instances')) {
          return PromiseStub.resolve({
            data: {
              'prop1': 'value1'
            }
          });
        } else {
          // Just in case
          return PromiseStub.resolve({
            status: 200,
            data: {}
          });
        }

      })
    };
  }

  function mockBpmServiceError() {
    return {
      getEngine: sinon.spy(() => {
        return PromiseStub.resolve({
          data: "meh"
        });
      }),
      generateXmlURL: sinon.spy((enginePath, definition) => {
        return enginePath + '/process-definition/key/' + definition + '/xml';
      }),
      generateActivityURL: sinon.spy((enginePath, instanceId) => {
        return enginePath + '/process-instance/' + instanceId + '/activity-instances';
      }),
      generateVersionXmlUrl: sinon.spy((enginePath, id) => {
        return enginePath + '/process-definition/' + id + '/xml';
      }),
      executeCustomProcessRequestGet: sinon.spy((url) => {
        // Returns the xml camunda data.
        if (url.endsWith('xml')) {
          return PromiseStub.resolve({
            data: {
              id: "bpmnID",
              bpmn20Xml: "<xml> Rejected</xml>"
            }
          });
          // gets the activity details for the instance here we need two responces
        } else if (url.endsWith('activity-instances')) {
          return PromiseStub.reject({
            data: {
              'prop2': 'value2'
            }
          });
        } else {
          // Just in case
          return PromiseStub.resolve({
            status: 200,
            data: {}
          });
        }

      })
    };
  }

  function mockInstanceRestService() {
    return {
      load: () => {
        return PromiseStub.resolve({
          data: {
            properties: {
              'emf:definitionId': 'TYPE',
              'activityId': 'someId'
            }
          }
        });
      }
    };
  }

  function mockDialogService() {
    return {
      create: sinon.spy(() => {

      })
    };
  }

  function mockTranslateServise() {
    return {

    };
  }

  function mockEventBus() {
    return {
      publish: sinon.spy(() => {

      }),
      subscribe: sinon.spy(() => {

      })
    };
  }

  function mockControl() {
    return {
      getId: () => {
        return 'widget123456';
      },
      saveConfig: () => {

      },
      baseWidget: {
        saveConfigWithoutReload: () => {}
      }
    };
  }

  function mockObjectSelectorHelper() {
    return {
      getSelectedObject: sinon.spy(() => {
        return PromiseStub.resolve('emf:id');
      })
    };
  }
});