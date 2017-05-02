import {Component, View, Inject, NgTimeout, NgElement} from 'app/app';
import {Configurable} from 'components/configurable';
import {Configuration} from 'common/application-config';
import {NavigatorAdapter} from 'adapters/navigator-adapter';

import 'common/lib/ckeditor/ckeditor';
import './comment-content-panel.css!';
import commentContentPanelTemplate from './comment-content-panel.html!text';

const editorToolbar = 'idoc-comment-editor-toolbar';
const editorSelector = 'comment-editor';
const tagMatcher = /<\/?[^>]+(>)|&\w+;/g;

@Component({
  selector: 'seip-comment-content-panel',
  properties: {
    'config': 'config',
    'comment': 'comment'
  }
})
@View({
  template: commentContentPanelTemplate
})
@Inject(NgTimeout, NgElement)
export class CommentContentPanel {

  constructor($timeout, $element) {
    this.$timeout = $timeout;
    this.$element = $element;
    this.initEditor();
  }

  getEditorConfig() {
    return PluginRegistry.get(editorToolbar)[0].data;
  }

  initEditor() {
    this.$timeout(() => {
      this.editor = CKEDITOR.replace(editorSelector, {
        toolbar: this.getEditorConfig(),
        extraPlugins: 'default_target',
        removePlugins: 'autogrow',
        allowedContent: true
      });


      this.editor.on('instanceReady', () => {
        if (this.comment) {
          this.editor.setData(this.comment.getDescription());
        }
        //apply workaround for IE11
        if (NavigatorAdapter.isInternetExplorer()) {
          //text selection is broken in ie. This positions the caret at the desired click position.
          $('.cke_wysiwyg_div').on('click', (e) => {
            //when you click on the div element caret is positioned properly,
            //but when you click on the element you want the caret to be, nothing happens.
            //Here its checked for the position of the selection and sets the appropriate range.
            if (!$(e.target).is('div')) {
              let selection = window.getSelection();
              //apply this only when you click, not select a lot of text
              if (selection.anchorOffset === selection.focusOffset) {
                let range = document.createRange();
                range.selectNodeContents(e.target);
                range.collapse(true);
                range.setStart(selection.focusNode, selection.focusOffset);
                selection.removeAllRanges();
                selection.addRange(range);
              }
            }
          });
          //In IE, the change event is not always fired, because the input event is fired at a the body element for some reason.
          //workaround for https://ittruse.ittbg.com/jira/browse/CMF-21017
          this.bodyBinding = $('body').on('keydown', (e) => {
            //moving to new line is forced when you press ENTER key because most of the time its not detected.
            if (e.keyCode === 13) {
              this.editor.execCommand('shiftEnter');
            }
            this.editor.fire('change');
          });
        }
      });

      this.editor.on('change', () => {
        // resolve problems related to invoking code outside angular-js lifecycle,
        // in this case we trigger timeout to update the save button visibility
        this.$timeout(() => {
          this.config.dialog.buttons[0].disabled = !this.isValid(this.getCommentContent());
        }, 0);
      });
    }, 0);
  }

  /**
   * Sets the caret position inside the comment editor, at the end of the written content if any,
   * so the user does not need to click inside before writing a comment.
   */
  static setFocus() {
    setTimeout(() => {
      CKEDITOR.instances[editorSelector].focus();
      let range = CKEDITOR.instances[editorSelector].createRange();
      range.moveToElementEditEnd(CKEDITOR.instances[editorSelector].editable());
      range.select();
      range.scrollIntoView();
    }, 0);
  }

  isValid(html) {
    return $.trim(html.replace(tagMatcher, "")).length !== 0;
  }

  getCommentContent() {
    return this.editor.getData();
  }

  getMentionedUsers() {
    let ids = [];
    $(this.getCommentContent()).find('*[data-mention-id]').each(
      (function () {
        ids.push({'@id': $(this).data('mention-id')});
      })
    );
    return ids;
  }

  ngOnDestroy() {
    if (this.bodyBinding) {
      this.bodyBinding.unbind('keydown');
    }
    $(this.editor.element.$).textcomplete('destroy');

    $(this.editor.element).off();
    $(this.editor.element).children().off();

    // manually remove listeners and custom data as they are not properly destroyed by CKEditor
    this.editor.removeAllListeners();
    this.editor.document.clearCustomData();
    this.editor.window.clearCustomData();
    this.editor.container.clearCustomData();
    this.editor.filter.destroy();

    // manually cleanup widget as they retain dom objects
    this.editor.widgets.removeAllListeners();
    this.editor.widgets.destroyAll();


    // Because the instance is replacing a DOM element, this parameter indicates not to update the element with the instance content.
    this.editor.destroy(false);

    // prevents a lot of memory retention
    // manually cleanup toolbar shared space
    $(`#cke_${this.editor.name}`).remove();

    // prevents a lot of memory retention
    Object.keys(this.editor).forEach((key) => {
      delete this.editor[key];
    });

    this.editor = null;
  }
}