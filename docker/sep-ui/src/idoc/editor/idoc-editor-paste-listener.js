const METHOD_PASTE = 'paste';

/**
 * When a widget is copy-pasted the editor duplicate its id.
 * In order to preserve the editor with proper code this listener cleans all duplicate ids.
 * Later the widget itself generate id if it is not presented.
 */
export class IdocEditorPasteListener {
  listen(editor) {
    editor.on('paste', this.onEditorPasteEvent);
  }

  onEditorPasteEvent(event) {
    let transferType = event.data.dataTransfer.getTransferType(event.editor);
    let method = event.data.method;

    // All paste events should be sanitized (current editor or external source)
    // All events from sources besides current editor should be sanitized
    // Drop event from current editor is skipped because it just moves the element and there is no risk of duplicated ids
    if (method === METHOD_PASTE || transferType !== CKEDITOR.DATA_TRANSFER_INTERNAL) {
      let fragment = CKEDITOR.htmlParser.fragment.fromHtml(event.data.dataValue);
      fragment.forEach((node) => {
        // Remove all ids of pasted DOM nodes. Our system should generate new ids where needed - ToC headings, widgets
        if (node.attributes.id && node.attributes.id !== '') {
          delete node.attributes.id;
        }
      }, CKEDITOR.NODE_ELEMENT);
      let writer = new CKEDITOR.htmlParser.basicWriter();
      fragment.writeHtml(writer);
      event.data.dataValue = writer.getHtml();
    }
  }
}
