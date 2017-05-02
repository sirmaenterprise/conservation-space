import {Component, View} from 'app/app';
import $ from 'jquery';
import 'filters/remove-whitespaces';
import './generic-dialog.css!';

@Component({
  selector: 'seip-generic-dialog',
  properties: {
    content: 'content'
  }
})
@View({
  template: '<div ng-bind-html="genericDialog.content | translate | toTrustedHtml"></div>',
})
/**
 * Generic dialog controller.
 */
export class GenericDialog {

}

// Fix for "Cannot place mouse cursor in search field textbox" from https://ittruse.ittbg.com/jira/browse/CMF-16987
// This issue is related to select2 placed inside bootstrap modal dialog with tabindex="-1".
// Inspired from https://github.com/select2/select2/issues/1278 and https://github.com/select2/select2/issues/1436
if($.fn.modal){
  $.fn.modal.Constructor.prototype.enforceFocus = function() {};
}
