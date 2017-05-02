/**
 * Abstract class for every editor content processor.
 */

export class EditorContentProcessor {
  constructor() {
    if (typeof this.preprocessContent !== 'function') {
      throw new TypeError('Content processors must override the \'preprocessContent\' function!');
    }

    if (typeof this.postprocessContent !== 'function') {
      throw new TypeError('Content processors must override the \'postprocessContent\' function!');
    }
  }
}