/**
 * Utility class that returns the properties of comment
 * build upon the concept of open annotation model
 */
export class CommentParser {
  static getId(comment) {
    return comment['@id'];
  }

  static getDescription(comment) {
    return comment.resource[0].chars;
  }

  static getAuthor(comment) {
    return comment['emf:createdBy'];
  }

  static getAuthorLabel(comment) {
    return CommentParser.getAuthor(comment)['emf:label'];
  }

  static getAuthorIcon(comment) {
    return CommentParser.getAuthor(comment)['emf:icon'];
  }

  static getAuthorId(comment) {
    return CommentParser.getAuthor(comment)['@id'];
  }

  static getCreatedDate(comment) {
    return comment['emf:createdOn'];
  }

  static getModifiedDate(comment) {
    return comment['emf:modifiedOn'];
  }

  static getMotivation(comment) {
    return comment['motivation'];
  }

  static getSelector(comment) {
    return comment['on']['selector'];
  }

  static getSelectorType(comment) {
    if (CommentParser.getSelector(comment)) {
      return CommentParser.getSelector(comment)['@type'];
    }
  }

  static getActions(comment) {
    return comment['actions'];
  }

  static getHeader(comment){
    return comment['header'];
  }

  static getStatus(comment) {
    return comment['emf:status'];
  }

  static getTarget(comment) {
    // "on" property for annotations is stored as array while the same property for comments is stored as object
    if (comment['on'] instanceof Array && comment['on'].length > 0) {
      return comment['on'][0]['full'];
    }
    return comment['on']['full'];
  }

  static getMentionedUsers(comment) {
    return comment['emf:mentionedUsers'];
  }
}