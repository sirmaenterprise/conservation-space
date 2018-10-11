import {IdocEditorChangeListener} from 'idoc/editor/idoc-editor-change-listener';
import {Eventbus} from 'services/eventbus/eventbus';
import {stub} from 'test/test-utils';

describe('IdocEditorChangeListener', () => {
  let idocEditorChangeListener;

  before(() => {
    let config = {
      eventbus: {
        channel: 'testChannel',
        instance: stub(Eventbus)
      }
    };
    idocEditorChangeListener = new IdocEditorChangeListener(config);
  });

  it('should find first text node in given element', () => {
    let element = document.createElement('div');
    element.innerHTML = '<div><span>test</span></div>';
    expect(idocEditorChangeListener.getFirstTextNode(element).textContent).to.equal('test');
  });

  it('should extract given element style', () => {
    let element = document.createElement('div');
    element.style.color = 'red';
    expect(idocEditorChangeListener.extractElementStyle(element)).to.equal('color: red;');
  });

  it('should apply span style to parent li element', () => {
    let element = document.createElement('li');
    element.innerHTML = '<span style="font-size: 25px;">test</span>';

    let iterator = {
      getNextParagraph: () => {}
    };
    let getNextParagraphStub = sinon.stub(iterator, 'getNextParagraph');

    getNextParagraphStub.onCall(0).returns({
      getName: () => {
        return 'li';
      },
      $: element
    });
    getNextParagraphStub.onCall(1).returns(null);

    let range = {
      createIterator: () => {
        return iterator;
      }
    };
    let editor = mockEditor(range);
    idocEditorChangeListener.updateListStyles(editor);
    expect(element.style.cssText).to.equal('font-size: 25px;');
  });

  it('should check widgets on change', () => {
    let range = {
      createIterator: () => {
        return {
          getNextParagraph: () => {
          }
        };
      }
    };
    let editor = mockEditor(range);
    let checkWidgetsWithDebounceStub = sinon.stub(idocEditorChangeListener, 'checkWidgetsWithDebounce');
    idocEditorChangeListener.listen(editor);
    editor.fire('change', {
      editor
    });
    expect(checkWidgetsWithDebounceStub.callCount).to.equals(1);
    expect(idocEditorChangeListener.eventbus.instance.publish.callCount).to.equals(1);
    expect(idocEditorChangeListener.eventbus.instance.publish.getCall(0).args[0]).to.eql({
      channel: 'testChannel',
      topic: 'idoc:editor:content-changed'
    });
  });

  function mockEditor(range) {
    let editor = {
      listeners: {},
      on: (eventName, func) => {
        editor.listeners[eventName] = func;
      },
      fire: (eventName, event) => {
        if (editor.listeners[eventName]) {
          editor.listeners[eventName](event);
        }
      },
      getSelection: () => {
        return {
          getRanges: () => {
            return [range];
          }
        };
      },
      getData: () => {
        return 'Test data';
      },
      widgets: {}
    };
    return editor;
  }
});
