import {IdocEditorPasteListener} from 'idoc/editor/idoc-editor-paste-listener'

describe('IdocEditorPasteListener', () => {
  let idocEditorPasteListener;
  before(() => {
    idocEditorPasteListener = new IdocEditorPasteListener();
  });

  it('should register paste listener to the editor', () => {
    let editor = {
      on: sinon.spy()
    };
    idocEditorPasteListener.listen(editor);
    expect(editor.on.callCount).to.equals(1);
    expect(editor.on.getCall(0).args[0]).to.equals('paste');
    expect(editor.on.getCall(0).args[1]).to.equals(idocEditorPasteListener.onEditorPasteEvent);
  });

  it('should handle paste event and remove all ids unless dropped from the same editor', () => {
    let event = {
      data: {
        method: 'paste',
        dataTransfer: {
          getTransferType: () => CKEDITOR.DATA_TRANSFER_EXTERNAL
        },
        dataValue: '<h1 id="h1id">H1</h1><h2 id="h2id">H2</h2><div id="widgetid"><h1 id="innerh1id">Inner H1</h1></div>'
      }
    };
    idocEditorPasteListener.onEditorPasteEvent(event);
    expect(event.data.dataValue).to.equals('<h1>H1</h1><h2>H2</h2><div><h1>Inner H1</h1></div>');
  });

  it('should handle paste event and keep the ids when dropped from the same editor', () => {
    let event = {
      data: {
        method: 'drop',
        dataTransfer: {
          getTransferType: () => CKEDITOR.DATA_TRANSFER_INTERNAL
        },
        dataValue: '<h1 id="h1id">H1</h1><h2 id="h2id">H2</h2><div id="widgetid"><h1 id="innerh1id">Inner H1</h1></div>'
      }
    };
    idocEditorPasteListener.onEditorPasteEvent(event);
    expect(event.data.dataValue).to.equals('<h1 id="h1id">H1</h1><h2 id="h2id">H2</h2><div id="widgetid"><h1 id="innerh1id">Inner H1</h1></div>');
  });
});
