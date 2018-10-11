import {Injectable} from 'app/app';
import ResizeDetector from 'sdecima/javascript-detect-element-resize';
import application from 'app/app';

/**
 * Adapter for ResizeDetector
 */
@Injectable()
export class ResizeDetectorAdapter {
  /**
   * Adds resize listener to given element.
   * Remove the listener when element is removed to avoid memory leaks.
   *
   * @param element to attach resize detector on
   * @param callback function to be called on element resize
   * @returns {Function} used to remove the listener
   */
  addResizeListener(element, callback) {
    let callbackFunc = () => {
      application.$rootScope.$evalAsync(callback);
    };
    ResizeDetector.addResizeListener(element, callbackFunc);
    return () => {
      if(element.__resizeTriggers__) {
        if(element.__resizeTriggers__.parentElement) {
          ResizeDetector.removeResizeListener(element, callbackFunc);
        } else {
          // __resizeTriggers__ are detached from its parent so we can remove them
          $(element.__resizeTriggers__).remove();
        }
      }
    };
  }
}
