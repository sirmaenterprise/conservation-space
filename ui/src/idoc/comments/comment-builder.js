export class CommentBuilder {

  static constructComment(id, tabId, text, replyTo, mentioned) {
    let comment = {
      '@context': 'http://iiif.io/api/presentation/2/context.json',
      '@type': 'oa:Annotation',
      'motivation': ['oa:commenting'],
      'emf:mentionedUsers': mentioned,
      'resource': [{
        '@type': 'dctypes:Text',
        'format': 'text/html',
        'chars': text
      }]
    };

    if (id) {
      comment['on'] = {
        '@type': 'oa:SpecificResource',
        'full': id,
        'selector': {
          '@type': 'oa:FragmentSelector',
          'value': '#xywh=-1000000,-1000000,1,1'
        }
      };
    }
    if (tabId) {
      comment['emf:commentsOn'] = tabId;
    }
    if (replyTo) {
      comment['emf:replyTo'] = replyTo;
    }
    return comment;
  }

}