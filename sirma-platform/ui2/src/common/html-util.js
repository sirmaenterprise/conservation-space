const TAGS = /<\/?([a-z][a-z0-9]*)\b[^>]*>/gi;
const COMMENTS_TAGS = /<!--[\s\S]*?-->/gi;

/**
 * Utility class for html content
 */
export class HtmlUtil {
  /**
   * Taken from: http://locutus.io/php/strings/strip_tags/,
   * Javascript interpretation of the php strip_tags() method.
   * @param input html text
   * @returns
   */
  static stripHtml(input) {
    return input.replace(COMMENTS_TAGS, '').replace(TAGS, '');
  }

  /**
   * Removes elements with the given tag name from HTML string.
   * For example removeElements('<span>Hello<script>alert("world")</script></span>', ['script']) will return '<span>Hello</span>'
   * @param input HTML string
   * @param elmentsToRemove an array with tags names to be removed from the input HTML
   * @returns HTML string without elements with the given tag names
   */
  static removeElements(input, elmentsToRemove) {
    let selector = elmentsToRemove.join(', ');
    let tmpDiv = $('<div></div>').append(input);
    tmpDiv.find(selector).remove();
    let result = tmpDiv.html();
    tmpDiv.remove();
    return result;
  }

  static escapeHtml(html) {
    if (html) {
      return html.replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }
  }
}
