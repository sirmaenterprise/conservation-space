import 'johnny/jquery-sortable';

/**
 * Enables drag and drop support on a list (ul) using jquery-sortable plugin.
 */
export class DragAndDrop {

  /*
   * Makes an element draggable. For the available options see http://johnny.github.io/jquery-sortable/
   */
  static makeDraggable(element, options) {
    element.sortable(options);

    // When the angular view is destroyed and the element removed from the dom
    // the sortable plugin doesn't get destroyed
    element.on('remove', function () {
      let sortable = element.data('sortable');
      element.sortable('destroy');

      // When the sortable plugin gets destroyed manually, it doesn't cleanup all its memory. See:
      // https://github.com/johnny/jquery-sortable/issues/229
      sortable.rootGroup._destroy();
    });
  }

  static enable(element) {
    element.sortable('enable');
  }

  static disable(element) {
    element.sortable('disable');
  }

}