import $ from 'jquery';

/**
 * Class for sanitizing UI specific data from the content of an idoc.
 */
export class IdocSanitizer {

  /**
   * Static method which sanitizes the idoc content.
   *
   * @param content the content to be sanitized
   */
  static sanitize(content) {
    // Wrap the content in a div in order to save the changes in the content string.
    let domTree = $('<div class ="wrapper" >' + content + '</div>');

    IdocSanitizer.removeImageLazyloadData(domTree);
    IdocSanitizer.removeInvalidLayouts(domTree);

    // Remove the wrapper and get only the content's html.
    return domTree.first().html();
  }

  /**
   * Image lazyload sets the initial base64 in data-origin attribute and puts another to the src attribute.
   * Before saved the images should regain their real src.
   *
   * @param domTree the jquery object wrapping the content
   *
   */
  static removeImageLazyloadData(domTree) {
    domTree.find('img').each(function () {
      // Copy the real src value from data-original
      $(this).attr('src', $(this).attr('data-original'));
    });
  }

  /**
   * Dragging layouts is prone to leaving the top layoutmanager empty.
   * After saving that results in addition of an empty paragrapth. Such empty containers must be sanitized.
   *
   * @param domTree the jquery object wrapping the content
   */
  static removeInvalidLayouts(domTree) {
    domTree.find('.layoutmanager:not(:has(.layout-container))').remove();
  }
}