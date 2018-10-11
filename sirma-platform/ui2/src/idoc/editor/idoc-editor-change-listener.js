import $ from 'jquery';
import _ from 'lodash';

const DEBOUNCE_INTERVAL = 5000;
const LI_ELEMENT = 'li';
const STYLE = 'style';

export class IdocEditorChangeListener {
  constructor(config) {
    this.eventbus = config.eventbus;
    this.checkWidgetsWithDebounce = _.debounce(this.checkWidgets, DEBOUNCE_INTERVAL);
  }

  listen(editor) {
    editor.on('change', (event) => {
      this.checkWidgetsWithDebounce(event.editor);
      this.updateListStyles(event.editor);
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

  getFirstTextNode(node) {
    var children = node.childNodes;
    var nonWhitespace = /\S/;
    for (var i = 0; i < children.length; i++) {
      node = this.getFirstTextNode(children[i]);
      if (node.nodeType === 3 && nonWhitespace.test(node.nodeValue)) {
        return node;
      }
    }
    return node;
  }

  updateListStyles(editor) {
    // get selected DOM elements and create iterator
    let range = editor.getSelection().getRanges()[0];
    if (!range || !editor.getData()) {
      return;
    }
    let iterator = range.createIterator();
    let node;

    // find 'li' nodes
    while (node = iterator.getNextParagraph()) {
      if (node.getName() === LI_ELEMENT) {
        let style = '';
        let firstTextNodeParent = this.getFirstTextNode(node.$).parentNode;

        // If some styles are applied to li element and enter is pressed for creation of new li element
        // span with styles is not inserted to the new node - CMF-29208
        let emptyListItem = this.isListItem(firstTextNodeParent);
        if (CKEDITOR.env.chrome || CKEDITOR.env.safari) {
          emptyListItem = $(node.$).find('span').length === 0;
        }
        if (emptyListItem) {
          let listItemStyles = node.$.getAttribute(STYLE) || '';
          this.insertMissingStyles(node, listItemStyles, editor);
          return;
        }

        // extract all text node parent styles
        while (!this.isListItem(firstTextNodeParent)) {
          style += this.extractElementStyle(firstTextNodeParent);
          firstTextNodeParent = firstTextNodeParent.parentNode;
        }

        // apply or remove styles to li element
        if (style) {
          node.$.setAttribute(STYLE, style);
        } else {
          node.$.removeAttribute(STYLE);
        }
      } else if (node.getName() === 'p') {
        node.$.removeAttribute(STYLE);
      }
    }
  }

  insertMissingStyles(node, listItemStyles, editor) {
    if (listItemStyles) {
      let emptyTextNode = $(node.$).text();
      if (CKEDITOR.env.chrome || CKEDITOR.env.safari) {
        emptyTextNode = emptyTextNode.replace(/\u200B/g,'');
      }
      if (!emptyTextNode) {
        let spanElement = new CKEDITOR.dom.element('span');
        spanElement.$.setAttribute(STYLE, listItemStyles);
        $(node.$).empty();
        node.append(spanElement);
        let range = editor.createRange();
        range.moveToElementEditablePosition(spanElement, true);
        range.select();
      } else {
        node.$.removeAttribute(STYLE);
      }
    }
  }

  extractElementStyle(element) {
    if (element && element.style) {
      return element.style.cssText;
    }
    return '';
  }

  isListItem(node) {
    return node.tagName.toLowerCase() === LI_ELEMENT;
  }

  // When a widget is deleted via Del, Backspace, Cut, etc. it is not deregistered from CKEditor's widget repository.
  // This method check widgets periodically (with debounce) after some changes are made in the editor and deregister such stale widgets.
  checkWidgets(editor) {
    if (editor.widgets) {
      editor.widgets.checkWidgets();
    }
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