import {ImageCommentsSection} from 'idoc/widget/image-widget/image-comments/image-comments-section';
import {EMPTY_FILTERS} from 'idoc/comments/comments-filter/comments-filter-const';

describe('ImageCommentsSection', () => {

  let eventbus = {
    subscribe: function () {
      return {
        unsubscribe: sinon.spy()
      };
    }
  };

  let control = {
    getId: function () {
      return 'widgetId';
    }
  };

  let eventsAdapter = {
    isLastSelectedSlotImageView: function () {
      return true;
    },
    getCurrentImageId: function () {
      return '1';
    },
    getImageIdBySlot: function (slotId) {
      return '1';
    }
  };

  let config = {
    dataProvider: {
      eventsAdapter: eventsAdapter
    }
  };

  let mockScope = {};
  let mockTimeout = function (callback) {
    callback();
  };

  let element = {
    addClass: sinon.spy(),
    removeClass: sinon.spy()
  };
  let mockElement = {
    remove: sinon.spy(),
    find: sinon.stub().returns(element)
  };

  let imageCommentsSection;
  ImageCommentsSection.prototype.control = control;
  ImageCommentsSection.prototype.config = config;

  beforeEach(() => {
    imageCommentsSection = new ImageCommentsSection(mockScope, mockElement, eventbus, mockTimeout);
  });

  it('should unsubscribe the events on destruction', () => {
    imageCommentsSection.ngOnDestroy();
    for (let event of imageCommentsSection.events) {
      expect(event.unsubscribe.callCount).to.equal(1);
    }
    expect(imageCommentsSection.element.remove.callCount).to.equal(1);
  });

  it('should load the comments', () => {
    let annotationList = [{
      '@type': 'oa:Annotation',
      '@id': 'commentId',
      'endpoint': {},
      'on': {
        'selector': {
          '@type': 'oa:SvgSelector'
        }
      }
    }];
    imageCommentsSection.loadComments(annotationList,1, true);
    expect(imageCommentsSection.comments[0].getId()).to.equal('commentId');
  });

  it('should load cached comments', () => {

    let annotationList = [{
      '@type': 'oa:Annotation',
      '@id': 'commentId',
      'endpoint': {},
      'on': {
        'selector': {
          '@type': 'oa:SvgSelector'
        }
      }
    }];
    imageCommentsSection.loadComments(annotationList, true);

    imageCommentsSection.loadComments(null);
    expect(imageCommentsSection.comments[0].getId()).to.equal('commentId');
  });

});