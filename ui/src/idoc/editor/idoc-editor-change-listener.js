import $ from 'jquery';

export class IdocEditorChangeListener {
  constructor(config) {
    this.eventbus = config.eventbus;
  }

  listen(editor) {
    editor.on('change', (event)=> {
      // a blank paragraph has to be inserted after each widget, and then focused.
      // That's because CKEditor's built-in functionality (autoParagraph) is a bit buggy
      if (event.editor.widgets.focused) {
          let paragraphToFocus = this.insertBlankParagraph(event.editor);
          this.focusElement(paragraphToFocus, event.editor);
      }
      this.eventbus.instance.publish({
        channel: this.eventbus.channel,
        topic: 'idoc:editor:content-changed'
      });
    });
  }

  insertBlankParagraph(editor) {
    let selectedElement = editor.getSelection().getStartElement();
    let nextElement = $(selectedElement.$).next();
    //if there is no <p> after the target element, insert one
    if (nextElement && nextElement.prop('tagName') && nextElement.prop('tagName').toLowerCase() !== 'p') {
      let pElement = new CKEDITOR.dom.element('p');
      let brElement = new CKEDITOR.dom.element('br');
      brElement.appendTo(pElement);
      pElement.insertAfter(selectedElement);
      return pElement.$;
    }
    //if the <p> is already there just return it as a native DOM object
    return nextElement[0];
  }

  /**
   * Puts the cursor inside the given element.
   * That method is needed, because inside CKEditor *One does not simply focus an element*.
   *
   * @param element
   *                the native DOM element
   * @param editor
   *               the editor instance
   */
  focusElement(element, editor) {
    if (!element || !element.childNodes) {
      return;
    }
    editor.getSelection().getStartElement().focus(true);
    let range = document.createRange();
    let selection = window.getSelection();
    range.setStart(element.childNodes[0], 0);
    range.collapse(true);
    selection.removeAllRanges();
    selection.addRange(range);
    element.focus();
  }
}