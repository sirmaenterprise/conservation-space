import {Injectable} from 'app/app';
import _ from 'lodash';

const KEY_BACKSPACE = 8;
const KEY_DELETE = 46;

/**
 * Removes empty paragraph (added by magic line http://docs.ckeditor.com/#!/guide/dev_magicline) between two widgets or
 * at the beginning of the document if followed by widget
 */
@Injectable()
export class EmptyParagraphRemover {
  constructor() {
    CKEDITOR.on('instanceCreated', (instanceCreatedEvent) => {
      let editor = instanceCreatedEvent.editor;
      editor.on('contentDom', () => {
        // editor.document is global document so we actually register event on the document
        let editable = editor.editable();
        editable.attachListener(editor.document, 'keydown', (keydownEvent) => {
          // multiple editors register multiple events so the event is only handled once for focused editor
          if (editor.focusManager.hasFocus && editor.getSelection()) {
            let startElement = editor.getSelection().getStartElement();
            if (startElement) {
              if (keydownEvent.data.$.keyCode === KEY_BACKSPACE) {
                this.removeAfter(startElement);
              } else if (keydownEvent.data.$.keyCode === KEY_DELETE) {
                this.removeBefore(startElement);
              }
            }
          }
        });
      });
    });
  }

  /**
   * Handles Backspace key press and removes empty paragraph after widget
   * @param element current CKEditor selected element
   */
  removeAfter(element) {
    if (this.isEmptyParagraph(element) && (element.getPrevious() === null || this.isWidget(element.getPrevious())) && this.isWidget(element.getNext())) {
      element.remove();
    } else if (this.isWidget(element) && this.isEmptyParagraph(element.getNext()) && element.getNext() !== null && this.isWidget(element.getNext().getNext())) {
      element.getNext().remove();
    }
  }

  /**
   * Handles Delete keypress and removes empty paragraph before widget
   * @param element current CKEditor selected element
   */
  removeBefore(element) {
    if (this.isEmptyParagraph(element) && (this.isWidget(element.getPrevious()) || element.getPrevious() === null) && this.isWidget(element.getNext())) {
      element.remove();
    } else if (this.isWidget(element) && this.isEmptyParagraph(element.getPrevious()) && (element.getPrevious().getPrevious() === null || this.isWidget(element.getPrevious().getPrevious()))) {
      element.getPrevious().remove();
    }
  }

  /**
   * Checks if given element is empty paragraph.
   * Empty paragraph is p element which has either one or two childs which are either &nbsp; or br tag depending on the browser
   * @param element
   * @returns {boolean} true if element is empty paragraph
   */
  isEmptyParagraph(element) {
    if (!element || !_.isFunction(element.is)) {
      return false;
    }
    // In FF empty paragraphs has two children - &nbsp; and br
    // In Chrome it has only one child - ether &nbsp; or br depending of how the paragraph is created
    if (element.is('p') && (element.getChildCount() === 1 || element.getChildCount() === 2)) {
      let allEmpty = true;
      for (let i = 0; i < element.getChildCount() && allEmpty; i++) {
        allEmpty = this.isEmptyElement(element.getChild(i));
      }
      return allEmpty;
    }
    return false;
  }

  /**
   * Checks if an element is either &nbsp; text node or br tag
   * @param element
   */
  isEmptyElement(element) {
    return this.isEmptyTextNode(element) || this.isBr(element);
  }

  /**
   * Checks if element is an &nbsp; text node
   * @param element
   * @returns {boolean}
   */
  isEmptyTextNode(element) {
    return element.$.nodeType === CKEDITOR.NODE_TEXT && element.getText() === String.fromCharCode(160);
  }

  isBr(element) {
    return element.$.nodeType === CKEDITOR.NODE_ELEMENT && _.isFunction(element.is) && element.is('br');
  }

  isWidget(element) {
    if (!element || !_.isFunction(element.is)) {
      return false;
    }
    return element.is('div') && element.hasClass('widget-wrapper');
  }
}
