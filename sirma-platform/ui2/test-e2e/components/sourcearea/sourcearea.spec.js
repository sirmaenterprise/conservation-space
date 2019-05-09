'use strict';

let SourceareaSandboxPage = require('./sourcearea').SourceareaSandboxPage;

describe('Sourcearea', () => {

  let sourcearea;
  let page;

  beforeEach(function () {
    page = new SourceareaSandboxPage();
    page.open();
    sourcearea = page.getSourceArea();
  });

  it('should display source value in sourcearea field', () => {
    expect(sourcearea.getValue()).to.eventually.equal('${eval(<span>(MX1001) Default Header</span>)}');
  });

  it('should allow source value change', () => {
    sourcearea.setValue('<p class="paragraph">test</p>');
    expect(page.getChangedValue()).to.eventually.equal('<p class="paragraph">test</p>');
  });

});