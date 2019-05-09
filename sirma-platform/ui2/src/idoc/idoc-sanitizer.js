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
   * Before saved the images should regain their real src.
   *
   * Image lazyload sets the initial base64 in data-origin attribute and puts another to the src attribute.
   * For images copied from another tenant or server, data-original contains link for the image on that environment and
   * the src attribute contains the base64 encoded image (this is done in paste-base64 processor). In that case we don't
   * need to restore the src because that will effectively set the url fro the image to external system and our backend
   * wont process the image appropriately.
   *
   * @param domTree the jquery object wrapping the content
   *
   */
  static removeImageLazyloadData(domTree) {
    domTree.find('img').each(function () {
      let dataOriginal = $(this).attr('data-original');
      // lastIndexOf starting from backward to check if the string actually begins with "data:" a.k.a. is base64 encoded
      if (dataOriginal && dataOriginal.lastIndexOf('data:', 0) === 0) {
        $(this).attr('src', dataOriginal);
      }
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