import {IdocSanitizer} from 'idoc/idoc-sanitizer';
import $ from 'jquery';

describe('Idoc Sanitizer', function () {
  it('should sanitize the given content by calling all sanitizing functions', () => {
    let removeImageLazyloadDataStub = sinon.spy(IdocSanitizer, 'removeImageLazyloadData');
    IdocSanitizer.sanitize('content');
    expect(removeImageLazyloadDataStub.calledOnce).to.be.true;
    removeImageLazyloadDataStub.restore();
  });

  it('should remove the lazyload data from the img elements', () => {
    let originalSrc = 'original';

    let content = $(`<div class="content"><img src="src" data-original="${originalSrc}"></div>`);
    IdocSanitizer.removeImageLazyloadData(content);

    content.find('img').each(function () {
      expect($(this).attr('src')).to.equals(originalSrc);
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