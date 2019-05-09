import {ContentViewer} from 'idoc/widget/content-viewer/content-viewer';
import {Eventbus} from 'services/eventbus/eventbus';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import {InstanceObject} from 'models/instance-object';
import {MODE_EDIT} from 'idoc/idoc-constants';
import {PluginsService} from 'services/plugin/plugins-service';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {IdocContext} from 'idoc/idoc-context';
import {StatusCodes} from 'services/rest/status-codes';
import {stub} from 'test/test-utils';

describe('ContentViewer', () => {

  let scope;
  let widget;

  beforeEach(() => {

    scope = mock$scope();

    let objectSelectorHelper = stub(ObjectSelectorHelper);
    objectSelectorHelper.getSelectedObject.returns(PromiseStub.reject({reason:''}));

    let result = {
      contents: () => {
        return {
          on: (evt, callback) => {
            callback && callback();
          }
        };
      },
      append: () => sinon.spy(),
      empty: () => sinon.spy()
    };
    result.length = 1;

    let $element = {
      find: () => {
        return result;
      }
    };

    let eventbus = stub(Eventbus);

    let $compile = sinon.spy(() => sinon.spy());

    let mediaViewersDefinitions = [{
      'name': 'pdf-viewer',
      'component': 'seip-pdf-viewer',
      'module': 'components/media/pdf-viewer/pdf-viewer',
      'mimetypes': ['application/pdf']
    }, {
      'name': 'video-player',
      'component': 'seip-video-player',
      'module': 'components/media/video-player/video-player',
      'mimetypes': ['video/*']
    }];

    let pluginsService = stub(PluginsService);
    pluginsService.getDefinitions.returns(mediaViewersDefinitions);
    pluginsService.executeImport.returns(PromiseStub.resolve());

    widget = new ContentViewer(scope, $element, $compile, eventbus, objectSelectorHelper, pluginsService);

    widget.control = {
      getId: () => {
        return 'widgetuuid';
      },
      saveConfig: sinon.spy()
    };

    widget.context = stub(IdocContext);
    widget.context.isModeling.returns(false);
    widget.context.getMode.returns(MODE_EDIT);
  });

  it('should call object selector helper with proper arguments', () => {
    scope.$digest();
    let selectorSpy = widget.objectSelectorHelper.getSelectedObject;
    expect(selectorSpy.calledOnce).to.be.true;
    expect(selectorSpy.getCall(0).args[3]).to.deep.equal({ignoreNotPersisted: true});
  });

  it('should show empty widget in modeling view', () => {
    widget.clearMemory = sinon.spy();
    widget.context.isModeling = () => {
      return true;
    };
    widget.config.selectObjectMode = 'automatically';
    widget.changeHandler();
    expect(widget.errorMessage).to.equal(undefined);
    expect(widget.clearMemory.calledOnce).to.be.true;
  });

  it('should handle object selection change', () => {
    widget.config.selectedObject = 'emf:123';
    scope.$digest();
    expect(widget.objectSelectorHelper.getSelectedObject.calledOnce).to.be.true;
  });

  it('should fire widgetReadyEvent if no object is selected', () => {
    widget.changeHandler();
    checkWidgetReadyEventFired(widget);
  });

  it('should delete the error message if content is displayable', () => {
    widget.errorMessage = 'some.error';
    widget.displayContent(mockInstanceObject());
    expect(widget.errorMessage).to.not.exist;
  });

  it('should display pdf viewer if mimetype is not supported', () => {
    widget.errorMessage = undefined;
    let object = mockInstanceObject();
    object.models.validationModel.mimetype.value = 'application/zip';
    widget.displayContent(object);
    expect(widget.$compile.args[0][0]).to.equals('<seip-pdf-viewer instance-id="\'id\'" mode="\'edit\'" content-type="\'primaryContent\'" on-ready="contentViewer.fireWidgetReadyEvent()"></seip-pdf-viewer>');
  });

  it('should remove selected object from widget config and save it on 404 error', () => {
    widget.config.selectObjectMode = 'manually';
    widget.context.getSharedObject.returns(PromiseStub.reject({statusText: 'error', status: StatusCodes.NOT_FOUND}));
    widget.context.isModeling.returns(false);

    widget.checkIfPresentAndDisplay('objectId');

    expect(widget.objectSelectorHelper.removeSelectedObjects.calledOnce).to.be.true;
    expect(widget.objectSelectorHelper.removeSelectedObjects.getCall(0).args).to.eql([{contentType: 'primaryContent', selectObjectMode: 'manually'}, ['objectId']]);
    expect(widget.control.saveConfig.calledOnce).to.be.true;
    expect(widget.control.saveConfig.getCall(0).args).to.eql([{contentType: 'primaryContent', selectObjectMode: 'manually'}]);
  });

  it('should display error message and fire WidgetReadyEvent on any other error', () => {
    widget.config.selectObjectMode = 'manually';
    widget.errorMessage = undefined;
    widget.context.getSharedObject.returns(PromiseStub.reject({statusText: 'not authenticated', status: StatusCodes.UNAUTHORIZED}));

    widget.checkIfPresentAndDisplay('objectId');

    expect(widget.errorMessage).to.equal('not authenticated');
    checkWidgetReadyEventFired(widget);
  });

  it('should construct a PdfViewer if selected object is pdf and show primaryContent by default', () => {
    widget.pdfViewer = undefined;
    let clearMemorySpy = sinon.spy(widget, 'clearMemory');
    widget.displayContent(mockInstanceObject());
    expect(clearMemorySpy.callCount).to.equals(1);
    expect(widget.$compile.args[0][0]).to.equals('<seip-pdf-viewer instance-id="\'id\'" mode="\'edit\'" content-type="\'primaryContent\'" on-ready="contentViewer.fireWidgetReadyEvent()"></seip-pdf-viewer>');
  });

  it('should construct a PdfViewer if selected object is pdf and show OCR-ed content if requested', () => {
    widget.pdfViewer = undefined;
    widget.config.contentType='ocrContent';
    let clearMemorySpy = sinon.spy(widget, 'clearMemory');
    widget.displayContent(mockInstanceObject());
    expect(clearMemorySpy.callCount).to.equals(1);
    expect(widget.$compile.args[0][0]).to.equals('<seip-pdf-viewer instance-id="\'id\'" mode="\'edit\'" content-type="\'ocrContent\'" on-ready="contentViewer.fireWidgetReadyEvent()"></seip-pdf-viewer>');
  });

  it('clearMemory should dispose all in memory resources', () => {
    widget.displayContent(mockInstanceObject());
    let spy = sinon.spy(widget.viewerWrapper, 'empty');
    widget.clearMemory();
    expect(spy.callCount).to.equals(1);
  });

  it('getCompatibleViewer should return viewer based on instance mimetype', () => {
    let object = mockInstanceObject();
    object.models.validationModel.mimetype.value = 'video/*';
    let compatibleViewer = widget.getCompatibleViewer(object);
    expect(compatibleViewer.name).to.equals('video-player');
  });
});

function checkWidgetReadyEventFired(widget) {
  expect(widget.eventbus.publish.calledOnce).to.be.true;
  expect(widget.eventbus.publish.getCall(0).args[0] instanceof WidgetReadyEvent).to.be.true;
}

function mockInstanceObject() {
  return new InstanceObject('id',{
    validationModel: new InstanceModel({
      'mimetype': {
        value: 'application/pdf'
      }
    }),
    viewModel: new DefinitionModel({
      fields: [{
        identifier: 'mimetype',
        isDataProperty: true
      }]
    })
  });
}
