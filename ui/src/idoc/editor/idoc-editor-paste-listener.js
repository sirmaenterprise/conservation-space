const METHOD_PASTE = 'paste';

/**
 * When a widget is copy-pasted the editor duplicate its id.
 * In order to preserve the editor with proper code this listener cleans all duplicate ids.
 * Later the widget itself generate id if it is not presented.
 */
export class IdocEditorPasteListener {
  listen(editor) {
    editor.on('paste', (event)=> {
      let editor = event.editor;
      let editorContent = editor.getData();
      let dataValue = event.data.dataValue;
      let method = event.data.method;
      let result;
      if (method === METHOD_PASTE) {
        result = this.clearWidgetIds(dataValue);
      }
      result = this.clearDuplicateID(result || dataValue, editorContent) || result;
      if (result) {
        editor.insertHtml(result);
        return false;
      }
      //set a large priority of the listener in order to handle the event first.
    }, null, null, 9);
  }

  /**
   * All pasted widget ids must be removed and generated again,
   * otherwise it may cause problems when copying widgets between different tabs.
   * @param pastedContent
   * @returns {*} new content with removed widget ids or false if there are no widgets
   */
  clearWidgetIds(pastedContent) {
    let pasteContentElement = $('<div></div>').append(pastedContent);
    let widgets = pasteContentElement.find('.widget');
    if (widgets.length) {
      widgets.removeAttr('id');
      return pasteContentElement.html();
    }
    return false;
  }

  /**
   * Finds id duplications with RegEx and clears them.
   * @param pasteContent {String} The pasted content which will be processed for clearing.
   * @param pageContent {String} The page content.
   *
   * @return {Object} Whether there was id removed or not.If there was it returns the cleared content.
   */
  clearDuplicateID(pasteContent, pageContent) {
    let idAttrRegex = /(\s+id=)(".*?)("\s*)/g;
    let idAttrRegexMatched = idAttrRegex.exec(pasteContent);
    //Check if the pasted object has id attr
    if (idAttrRegexMatched && idAttrRegexMatched[1]) {
      let id = idAttrRegexMatched[2];
      if (id && pageContent.indexOf(id) !== -1) {
        //check if the id is presented in the current content
        return pasteContent.replace(idAttrRegex, '');
      }
    }
    return false;
  }
}