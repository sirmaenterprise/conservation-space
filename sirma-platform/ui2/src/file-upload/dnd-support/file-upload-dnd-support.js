import {NavigatorAdapter} from 'adapters/navigator-adapter';
import _ from 'lodash';

const DEFAULT_ANIMATION_CLASS = 'drop-over';

/**
 * Util class which extends element with drag and drop files functionality.
 */
export class FileUploadDnDSupport {


  /**
   * Adds "drop files" support to <code>element</code>.
   * @param element - the element of which "drop files" functionality will be added
   * @param onDropCallback - when drop event occurred this callback will be called with parameter dropped files.
   * @param config - the configuration for "drop files" functionality. There are two possible configuration:
   * 1. animationClass - this class will be applied to <code>element</code> when "dragenter" event occurred. Default class is 'drop-over';
   * 2. animationDuration - how much time animation class will be applied on <code>element</code>. Default value is 3000 ms.
   */
  static addDropSupport(element, onDropCallback, config) {
    if (element && _.isFunction(onDropCallback)) {
      let animationClass = config && config.animationClass ? config.animationClass : DEFAULT_ANIMATION_CLASS;
      let animationDuration = config && config.animationDuration ? config.animationDuration : 3000;
      // Add handler to process event when dragged files go into the document.
      let dragEnterHandler = $(document).on('dragenter', _.partial(FileUploadDnDSupport._documentDragEnter.bind(this), element, animationClass, animationDuration));
      // Add handler to process event when dragged files go into <code>element</code>.
      element.on('dragover', _.partial(FileUploadDnDSupport._dragOver.bind(this)));
      // Add handler to process event when dragged files are dropped into <code>element</code>.
      element.on('drop', _.partial(FileUploadDnDSupport._onDrop.bind(this), element, onDropCallback, animationClass));
      // On destroy clear all handlers from element and document attached for <code>element</code>.
      element.on('$destroy', _.partial(FileUploadDnDSupport._destroy.bind(this), element, dragEnterHandler));
    }
  }

  /**
   * Checks if dragged source items are files.
   * If yes, add animation to <code>element</code> to notify the user, that he can drop files there.
   */
  static _documentDragEnter(element, animationClass, animationDuration, event) {
    if (this._sourcesAreFiles(event.originalEvent.dataTransfer)) {
      FileUploadDnDSupport._startTargetAnimation(element, animationClass, animationDuration);
    }
  }

  /**
   * By default, data/elements cannot be dropped in other elements.
   * To allow a drop, we must prevent the default handling of the element.
   * For more info see "https://www.w3schools.com/html/html5_draganddrop.asp"
   *
   * Returns function which checks if source item are files. If yes calls method preventDefault of event.
   */
  static _dragOver(event) {
    if (this._sourcesAreFiles(event.originalEvent.dataTransfer)) {
      if (event.preventDefault) {
        event.preventDefault();
      }
      event.originalEvent.dataTransfer.dropEffect = 'copy';
      return false;
    }
  }

  static _destroy(element, dragEnterHandler) {
    dragEnterHandler.off();
    element.off();
  }

  /**
   * Returns function which extract files from drop event and calls <code>onDropCallback</code> with it as argument.
   */
  static _onDrop(element, onDropCallback, animationClass, event) {
    if (event.stopPropagation) {
      event.stopPropagation();
    }
    let dataTransfer = event.originalEvent.dataTransfer;
    let files;
    if (FileUploadDnDSupport._canProcessFolders(dataTransfer)) {
      files = FileUploadDnDSupport._processFilesAndFolders(dataTransfer);
    } else {
      files = FileUploadDnDSupport._processFiles(dataTransfer);
    }
    FileUploadDnDSupport._stopTargetAnimation(element, animationClass);
    onDropCallback(files);
    return false;
  }

  /**
   * Check if we can scan for files in folders.
   * Check if a item have function "webkitGetAsEntry". This method is used to scan folders for files.
   *
   * More info "https://developer.mozilla.org/en-US/docs/Web/API/DataTransferItem/webkitGetAsEntry";
   *
   */
  static _canProcessFolders(dataTransfer) {
    let items = dataTransfer.items;
    if (items && items.length > 0) {
      return _.isFunction(items[0].webkitGetAsEntry);
    }
    return false;
  }

  /**
   * Return dragged files without scan folders.
   */
  static _processFiles(dataTransfer) {
    let files = dataTransfer.files || {};
    if (!NavigatorAdapter.isEdgeOrIE()) {
      files = FileUploadDnDSupport._getFilesOnly(files);
    }
    return files;
  }

  static _getFilesOnly(filesAndFolders = {}) {
    return Object.values(filesAndFolders).filter(fileOrFolder => {
      return fileOrFolder.type && fileOrFolder.type !== '';
    });
  }

  /**
   * Extract all files from dragged files and folders.
   */
  static _processFilesAndFolders(dataTransfer) {
    let items = dataTransfer.items;
    let files = [];
    for (let i = 0; i < items.length; i++) {
      let item = items[i].webkitGetAsEntry();
      if (item) {
        FileUploadDnDSupport._getFiles(item, files);
      }
    }
    return files;
  }

  static _getFiles(item, files) {
    if (item.isFile) {
      item.file((file) => {
        files.push(file);
      });
    } else if (item.isDirectory) {
      // Get folder contents
      item.createReader().readEntries(function (entries) {
        for (let i = 0; i < entries.length; i++) {
          FileUploadDnDSupport._getFiles(entries[i], files);
        }
      });
    }
  }

  /**
   * In Chrome, Firefox and Safari, the DataTransfer object at event.dataTransfer has a types property
   * which is a string array that will contain "Files" when files are dragged.
   *
   * Microsoft Internet Explorer and Edge will provide a DOMStringList instead of an array,
   * therefore using Array methods like includes or indexOf is not possible.
   * For DOMStringList, using the contains method yields the same result
   *
   * https://github.com/leonadler/drag-and-drop-across-browsers/blob/master/README.md
   */
  static _sourcesAreFiles(dataTransfer) {
    if (NavigatorAdapter.isEdgeOrIE()) {
      return dataTransfer.types && dataTransfer.types.contains('Files');
    }
    return dataTransfer.types && dataTransfer.types.indexOf('Files') >= 0;
  }

  static _startTargetAnimation(element, animationClass, animationDuration) {
    if (element.hasClass(animationClass)) {
      return;
    }
    element.addClass(animationClass);
    setTimeout(() => {
      FileUploadDnDSupport._stopTargetAnimation(element, animationClass);
    }, animationDuration);
  }

  static _stopTargetAnimation(element, animationClass) {
    element.removeClass(animationClass);
  }
}