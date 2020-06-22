import {IdocSanitizer} from 'idoc/idoc-sanitizer';
import $ from 'jquery';

describe('Idoc Sanitizer', function () {
  it('should sanitize the given content by calling all sanitizing functions', () => {

    let removeImageLazyloadDataStub = sinon.spy(IdocSanitizer, 'removeImageLazyloadData');
    IdocSanitizer.sanitize('content');
    expect(removeImageLazyloadDataStub.calledOnce).to.be.true;
    removeImageLazyloadDataStub.restore();
  });

  // lazyload sets the base64 encoded image in the data-original attribute which has to be used for the restoration
  it('should restore original src attribute value from data-original if it contains base64 encoded content', () => {
    let originalSrc = 'data:image/jpeg;base64,/abcde';

    let content = $(`<div class="content"><img src="https://server.com/remote/api/emf:123456?tenant=sep.release" data-original="${originalSrc}"></div>`);
    IdocSanitizer.removeImageLazyloadData(content);

    content.find('img').each(function () {
      expect($(this).attr('src')).to.equals(originalSrc);
    });
  });

  // copy-pasted from another environment images have url to the images in their data-original attribute and not base64
  // encoded content
  it('should not change src when data-original does not contain base64 encoded content', () => {
    let originalSrc = 'https://server.com/remote/api/emf:123456?tenant=sep.release';
    let src = 'data:image/jpeg;base64,/abcde';

    let content = $(`<div class="content"><img src="${src}" data-original="${originalSrc}"></div>`);
    IdocSanitizer.removeImageLazyloadData(content);

    content.find('img').each(function () {
      expect($(this).attr('src')).to.equals(src);
    });
  });

  it('should remove invalid layouts', () => {
    let invalidContainer = $('<div class="wrapper"><div class="layoutmanager"><br></div></div>');
    let validContent = '<div class="layoutmanager "><div class="container-fluid layout-container">Some Content</div></div>';
    let validContainer = $(`<div class="wrapper">${validContent}</div>`);

    IdocSanitizer.removeInvalidLayouts(invalidContainer);
    IdocSanitizer.removeInvalidLayouts(validContainer);

    expect(invalidContainer.html()).to.equal('');
    expect(validContainer.html()).to.equal(validContent);
  });

});