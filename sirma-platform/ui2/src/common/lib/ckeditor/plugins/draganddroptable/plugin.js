/**
 * Draganddroptable plugin allows the user o drag and drop multiple objects from a table cell to anywhere else.
 * If more than one cell is selected the drag and drop is disabled and tableselection plugin's fake selection is fired.
 */

( function() {
  'use strict';

  function dragstartHandlerOnEditor(evt) {
    var nativeHtml = evt.data.dataTransfer.getData('text/html');
    var selectedDataCells = nativeHtml.match(/<td/g) ? nativeHtml.match(/<td/g).length : 0;
    var selectedHeaderCells = nativeHtml.match(/<th/g) ? nativeHtml.match(/<th/g).length : 0;

    if (selectedDataCells + selectedHeaderCells > 0) {
      //Do not allow drag and drop of content of more than one cell. Do table selection instead.
      if (selectedDataCells + selectedHeaderCells > 1) {
        evt.cancel();
      }

      //Get all elements in the current selection
      var selectedNodes = evt.editor.getSelection().getRanges()[0].cloneContents().$.childNodes;
      var dataToBeSet = '';

      selectedNodes.forEach(function(node) {
        if (node.nodeType === Node.TEXT_NODE) {
          dataToBeSet += node.nodeValue;
        } else {
          dataToBeSet += node.outerHTML;
        }
      });
      evt.data.dataTransfer.setData('text/html', dataToBeSet);
    }
  }

  CKEDITOR.plugins.add( 'draganddroptable', {

    init: function( editor ) {

      editor.on( 'contentDom', function() {
        var editable = editor.editable();
        // Adding dragstart event to be fired by the editor by the clipboard plugin's fireDragEvent
        editable.attachListener( editor, 'dragstart', dragstartHandlerOnEditor);
      } );
    }
  } );
}() );
