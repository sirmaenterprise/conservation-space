import {CommentParser} from 'idoc/comments/comment-parser';

describe('CommentParser', () => {
  describe('getTarget', () => {
    it('should return proper target for comments created through the system', () => {
      let comment = {
        on: {
          full: 'emf:user'
        }
      };
      expect(CommentParser.getTarget(comment)).to.equals('emf:user');
    });

    it('should return proper target for image annotations', () => {
      let comment = {
        on: [{
          full: 'emf:user'
        }]
      };
      expect(CommentParser.getTarget(comment)).to.equals('emf:user');
    });
  });
});
