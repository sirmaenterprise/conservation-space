import {IdocEditorSelectionListener} from 'idoc/editor/idoc-editor-selection-listener';
import {CKEditorElementMock, CKEditorTextElementMock} from 'test/idoc/editor/idoc-editor-mocks';

describe('Test IdocEditorSelectionListener', () => {

  var eventbus = {
    publish: sinon.spy(),
    subscribe: sinon.spy()
  };

  var config = {
    eventbus: {
      instance: eventbus
    }
  };

  it('should publish idoc:editor:selection-changed event', () => {
    let selectionListener = new IdocEditorSelectionListener(config);
    selectionListener.publishSelectionChangedEvent();
    expect(eventbus.publish.callCount).to.equal(1);
  });

  it('getClosestNodeElement should return first parent of type node', () => {
    let selectionListener = new IdocEditorSelectionListener(config);
    let testElement = new CKEditorTextElementMock('Text');
    let parentElement = new CKEditorElementMock('span');
    testElement.setParent(parentElement);
    expect(selectionListener.getClosestNodeElement(testElement)).to.equals(parentElement);
  });

  it('getTopNonEditableElement should return first parent which is not editable', () => {
    let selectionListener = new IdocEditorSelectionListener(config);
    let testElement = new CKEditorTextElementMock('Text');
    testElement.setEditable(false);
    let firstParentElement = new CKEditorElementMock('span');
    firstParentElement.setEditable(false);
    testElement.setParent(firstParentElement);
    let secondParentElement = new CKEditorElementMock('span');
    secondParentElement.setEditable(false);
    firstParentElement.setParent(secondParentElement);
    secondParentElement.setParent(new CKEditorElementMock('span'));
    expect(selectionListener.getTopNonEditableElement(testElement)).to.equals(secondParentElement);
  });

  describe('updateRangeForNotEditableElements', () => {
    it('should move range to first editable position after non editable element', () => {
      let startElement = new CKEditorElementMock('span');
      startElement.setEditable(false);
      startElement.setParent(new CKEditorElementMock('span'));

      let range = {
        getCommonAncestor: () => startElement,
        getBoundaryNodes: () => {
          return {
            startNode: startElement,
            endNode: startElement
          }
        },
        moveToClosestEditablePosition: sinon.spy()
      };

      let selection = {
        getRanges: () => [range],
        getStartElement: () => startElement,
        getSelectedElement: () => {
        },
        selectRanges: sinon.spy(),
        selectElement: sinon.spy()
      };

      let selectionListener = new IdocEditorSelectionListener(config);
      selectionListener.updateRangeForNotEditableElements(selection);
      expect(range.moveToClosestEditablePosition.callCount).to.equals(1);
      expect(range.moveToClosestEditablePosition.getCall(0).args).to.eql([startElement, true]);
      expect(selection.selectRanges.callCount).to.equals(1);
    });

    it('should select whole non editable element if boundary is expanded over multiple nodes', () => {
      let startElement = new CKEditorElementMock('span');
      startElement.setEditable(false);
      startElement.setParent(new CKEditorElementMock('span'));

      let range = {
        getCommonAncestor: () => startElement,
        getBoundaryNodes: () => {
          return {
            startNode: startElement,
            endNode: new CKEditorElementMock('span')
          }
        },
        moveToClosestEditablePosition: sinon.spy()
      };

      let selection = {
        getRanges: () => [range],
        getStartElement: () => startElement,
        getSelectedElement: () => {
        },
        selectRanges: sinon.spy(),
        selectElement: sinon.spy()
      };

      let selectionListener = new IdocEditorSelectionListener(config);
      selectionListener.updateRangeForNotEditableElements(selection);
      expect(selection.selectElement.callCount).to.equals(1);
      expect(selection.selectElement.getCall(0).args[0]).to.equals(startElement);
    });
  });
});
