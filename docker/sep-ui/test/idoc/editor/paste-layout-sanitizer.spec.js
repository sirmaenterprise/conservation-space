import {PasteLayoutSanitizer} from 'idoc/editor/paste-layout-sanitizer';

describe('Test for PasteLayoutSanitizer', () => {

  let pasteLayoutSanitizer;
  before(() => {
    pasteLayoutSanitizer = new PasteLayoutSanitizer();
  });

  it('should register paste listener to the editor', () => {
    let editor = {
      on: sinon.spy()
    };
    pasteLayoutSanitizer.listen(editor);
    expect(editor.on.callCount).to.equals(1);
    expect(editor.on.getCall(0).args[0]).to.equals('paste');
  });

  it('should remove single broken layout', () => {
    let event = {
      data: {
        dataValue: '<div class="layout-column"><div class="layout-column-editable">content</div></div>'
      }
    };
    pasteLayoutSanitizer.onEditorPasteEvent(event);
    expect(event.data.dataValue).to.equals('content');
  });

  it('should remove the broken layout from the paste content but preserve the fully copied one (not nested)', () => {
    let event = {
      data: {
        dataValue: '<div class="layout-container"><div class="layout-row"><div class="layout-column"><div class="layout-column-editable">content</div></div></div></div><div class="layout-column"><div class="layout-column-editable">content</div></div>'
      }
    };
    pasteLayoutSanitizer.onEditorPasteEvent(event);
    expect(event.data.dataValue).to.equals('<div class="layout-container"><div class="layout-row"><div class="layout-column"><div class="layout-column-editable">content</div></div></div></div>content');
  });

  it('should remove the broken layout from the paste content but preserve the fully copied one (nested)', () => {
    let event = {
      data: {
        dataValue: '<div class="layout-container"><div class="layout-row"><div class="layout-column"><div class="layout-column-editable">content <div class="layout-column"><div class="layout-column-editable">content</div></div></div></div></div></div>'
      }
    };
    pasteLayoutSanitizer.onEditorPasteEvent(event);
    expect(event.data.dataValue).to.equals('<div class="layout-container"><div class="layout-row"><div class="layout-column"><div class="layout-column-editable">content content</div></div></div></div>');
  });

  it('should not remove layout when it is fully copied and pasted', () => {
    let event = {
      data: {
        dataValue: '<div class="layout-container"><div class="layout-row"><div class="layout-column"><div class="layout-column-editable">content</div></div></div></div>'
      }
    };
    pasteLayoutSanitizer.onEditorPasteEvent(event);
    expect(event.data.dataValue).to.equals('<div class="layout-container"><div class="layout-row"><div class="layout-column"><div class="layout-column-editable">content</div></div></div></div>');
  });

  it('should not change order of elements when removing layout', () => {
    let event = {
      data: {
        dataValue: '<div><div class="layout-column-editable">content</div><div> other content</div></div>'
      }
    };
    pasteLayoutSanitizer.onEditorPasteEvent(event);
    expect(event.data.dataValue).to.equals('<div>content<div> other content</div></div>');
  });

  it('should add layout-container when it is missing but layout-row is pasted', () => {
    let event = {
      data: {
        dataValue: '<div class="layout-row"><div class="layout-column"><div class="layout-column-editable">content</div></div></div>'
      }
    };
    pasteLayoutSanitizer.onEditorPasteEvent(event);
    expect(event.data.dataValue).to.equals('<div class="layout-container"><div class="layout-row"><div class="layout-column"><div class="layout-column-editable">content</div></div></div></div>');
  });

});