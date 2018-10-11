import {Editor} from 'idoc/editor/idoc-editor';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {Eventbus} from 'services/eventbus/eventbus';
import {EditorResizedEvent} from 'idoc/editor/editor-resized-event';
import {ResizeDetectorAdapterMock} from 'test/adapters/resize-detector-adapter-mock';

describe('IdocEditor', () => {
  it('should unsubscribe from events when editor is destroyed', () => {
    let unsubscribeSpy = {
      unsubscribe: sinon.spy()
    };
    let eventbus = {
      subscribe: () => {
        return unsubscribeSpy
      }
    };
    let idocEditor = instantiateEditor(eventbus);
    idocEditor.idocEditorFactory.destroy = () => {
    };
    idocEditor.editorIdocEditorResizeListener = sinon.spy();
    idocEditor.ngOnDestroy();
    expect(unsubscribeSpy.unsubscribe.callCount).to.equal(8);
    expect(idocEditor.editorIdocEditorResizeListener.callCount).to.equal(1);
  });

  it('onIdocEditorResize should fire EditorResizedEvent', () => {
    let eventbus = {
      subscribe: () => {
      },
      publish: sinon.spy()
    };
    let idocEditor = instantiateEditor(eventbus);
    idocEditor.editorDimensions = {
      width: 100,
      height: 100
    };
    let editorWrapper = {
      width: () => 200,
      height: () => 100
    };
    idocEditor.calculateMinimumEditorHeight = sinon.spy();
    idocEditor.onIdocEditorResize(editorWrapper);
    expect(idocEditor.calculateMinimumEditorHeight.callCount).to.equals(1);
    expect(idocEditor.eventbus.publish.callCount).to.equals(1);
    let event = idocEditor.eventbus.publish.getCall(0).args[0];
    expect(event).to.be.an.instanceof(EditorResizedEvent);
    expect(event.getData()).to.eql({
      editorId: event.getData().editorId,
      width: 200,
      height: 100,
      widthChanged: true,
      heightChanged: false
    });
  });
});

function instantiateEditor(eventbus) {
  let $element = IdocMocks.mockElement();
  $element.controller = () => {
  };
  $element.addClass = () => {
  };
  $element.attr = () => {
  };
  $element.parent = () => {
    return {
      0: document.createElement("DIV"),
      width: () => {
      },
      height: () => {
      }
    }
  };
  return new Editor(IdocMocks.mockScope(), {}, $element, {}, eventbus || IdocMocks.mockEventBus(), {}, {}, {}, ResizeDetectorAdapterMock.mockAdapter());
}