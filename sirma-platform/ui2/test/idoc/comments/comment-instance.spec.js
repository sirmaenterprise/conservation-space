import {CommentInstance} from 'idoc/comments/comment-instance';

describe('Comment parser', function () {

  var comment = {
    '@context': 'http://iiif.io/api/presentation/2/context.json',
    '@type': 'oa:Annotation',
    'motivation': ['oa:commenting'],
    'resource': [{'@type': 'dctypes:Text', 'format': 'text/html', 'chars': '<p>DemoDesc</p>'}],
    'emf:mentionedUsers':'',
    'on': {
      '@type': 'oa:SpecificResource',
      'full': 'emf:5c6d438b-73d9-48e6-a1c1-7eff8a664947',
      'selector': {'@type': 'oa:FragmentSelector', 'value': '#'}
    },
    'emf:commentsOn': '2ca01da7-2e50-4242-c557-c64ac32cf243',
    '@id': 'dummyCommentId',
    'emf:isDeleted': false,
    'emf:modifiedBy': {
      '@type': 'emf:User',
      '@id': '',
      'emf:icon': '',
      'emf:label': ''
    },
    'emf:createdBy': {
      '@type': 'emf:User',
      '@id': 'emf:admin-radoslav.p1',
      'emf:icon': 'dummyAuthorIcon',
      'emf:label': 'dummyAuthor'
    },
    'emf:reply': [{
      'emf:modifiedOn': 'dummyModifiedOnReply',
      'resource': [{'@type': 'dctypes:Text', 'format': 'text/html', 'chars': 'dummyReply'}]
    }],
    'emf:createdOn': 'dummyCreatedOn',
    'emf:modifiedOn': 'dummyModifiedOn'

  };
  var commentInstance;

  it('should get the comment id', function () {
    commentInstance = new CommentInstance(comment);
    expect(commentInstance.getId()).to.equal('dummyCommentId');
  });

  it('should set the comment id', function () {
    commentInstance = new CommentInstance({});
    commentInstance.setId('testId');
    expect(commentInstance.getId()).to.equal('testId');
  });

  it('should get the comment description', function () {
    commentInstance = new CommentInstance(comment);
    expect(commentInstance.getDescription()).to.equal('<p>DemoDesc</p>');
  });

  it('should set the comment description', function () {
    commentInstance = new CommentInstance(comment);
    commentInstance.setDescription('testSetDescription');
    expect(commentInstance.getDescription()).to.equal('testSetDescription');
  });

  it('should get the comment author label', function () {
    commentInstance = new CommentInstance(comment);
    expect(commentInstance.getAuthorLabel()).to.equal('dummyAuthor');
  });

  it('should get the comment author icon', function () {
    commentInstance = new CommentInstance(comment);
    expect(commentInstance.getAuthorIcon()).to.equal('dummyAuthorIcon');
  });

  it('should get the comment modified date', function () {
    commentInstance = new CommentInstance(comment);
    expect(commentInstance.getModifiedDate()).to.equal('dummyModifiedOn');
  });

  it('should get the comment motivation', function () {
    commentInstance = new CommentInstance(comment);
    expect(commentInstance.getMotivation()).to.deep.equal(comment.motivation);
  });

  it('should set the comment motivation', function () {
    commentInstance = new CommentInstance(comment);
    commentInstance.setMotivation('editing');
    expect(commentInstance.getMotivation()).to.equal('editing');
  });

  it('should be reply', function () {
    commentInstance = new CommentInstance(comment, true);
    expect(commentInstance.isReply()).to.be.true;
  });

  it('should get the replies of comment as json', function () {
    commentInstance = new CommentInstance(comment);
    expect(commentInstance.getRepliesAsJSON()[0]['resource'][0]['chars']).to.equal('dummyReply');
  });

  it('should get the replies of comment', function () {
    commentInstance = new CommentInstance(comment);
    expect(commentInstance.getReplies()[0].getDescription()).to.equal('dummyReply');
  });

  it('should return undefined if comment does not have replies', function () {
    commentInstance = new CommentInstance({});
    expect(commentInstance.getReplies()).to.be.undefined;
  });

  it('should get the hash of comment', function () {
    commentInstance = new CommentInstance(comment);
    expect(commentInstance.hash(function (hash) {
      return hash
    })).to.equal('dummyCommentIddummyModifiedOndummyReplydummyModifiedOnReply');
  });

  it('should get the selector', function () {
    commentInstance = new CommentInstance(comment);
    expect(commentInstance.getSelector()).to.deep.equal({'@type': 'oa:FragmentSelector', 'value': '#'});
  });

  it('should get the selector type', function () {
    commentInstance = new CommentInstance(comment);
    expect(commentInstance.getSelectorType()).to.equal('oa:FragmentSelector');
  });

  it('should should add/get arbitrary data', function () {
    commentInstance = new CommentInstance(comment);
    commentInstance.addData('test', 'TESTDATA');
    expect(commentInstance.getData('test')).to.equal('TESTDATA');
  });

  it('should get author id', function () {
    commentInstance = new CommentInstance(comment);
    expect(commentInstance.getAuthorId()).to.equal('emf:admin-radoslav.p1');
  });

  it('should get the created date', function () {
    commentInstance = new CommentInstance(comment);
    expect(commentInstance.getCreatedDate()).to.equal('dummyCreatedOn');
  });

  it('should get the mentioned users', function () {
    commentInstance = new CommentInstance(comment);
    commentInstance.setMentionedUsers('[emf:user1]');
    expect(commentInstance.getMentionedUsers()).to.equal('[emf:user1]');
  });
});