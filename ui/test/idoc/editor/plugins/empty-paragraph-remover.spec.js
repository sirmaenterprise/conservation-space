import {EmptyParagraphRemover} from 'idoc/editor/plugins/empty-paragraph-remover/empty-paragraph-remover';
import {CKEditorElementMock, CKEditorTextElementMock} from 'test/idoc/editor/idoc-editor-mocks';

describe('EmptyParagraphRemover', () => {
  let emptyParagraphRemover;
  before(() => {
    emptyParagraphRemover = new EmptyParagraphRemover();
  });

  describe('removeAfter', () => {
    it('should remove current element if it is empty paragraph between two widget', () => {
      let element = new CKEditorElementMock('p');
      element.addChild(new CKEditorElementMock('br'));
      element.setPrevious(new CKEditorElementMock('div', ['widget-wrapper']));
      element.setNext(new CKEditorElementMock('div', ['widget-wrapper']));

      emptyParagraphRemover.removeAfter(element);
      expect(element.remove.callCount).to.equals(1);
    });

    it('should remove next element if it is empty paragraph between two widgets', () => {
      let element = new CKEditorElementMock('div', ['widget-wrapper']);
      let nextElement = new CKEditorElementMock('p');
      nextElement.addChild(new CKEditorElementMock('br'));
      element.setNext(nextElement);
      nextElement.setNext(new CKEditorElementMock('div', ['widget-wrapper']));

      emptyParagraphRemover.removeAfter(element);
      expect(nextElement.remove.callCount).to.equals(1);
    });

    it('should not remove current element if it is not empty paragraph', () => {
      let element = new CKEditorElementMock('p');
      element.addChild(new CKEditorElementMock('div', ['widget-wrapper']));
      element.setPrevious(new CKEditorElementMock('div', ['widget-wrapper']));

      emptyParagraphRemover.removeAfter(element);
      expect(element.remove.callCount).to.equals(0);
    });
  });

  describe('removeBefore', () => {
    it('should remove current element if it is empty paragraph between two widget', () => {
      let element = new CKEditorElementMock('p');
      element.addChild(new CKEditorElementMock('br'));
      element.setPrevious(new CKEditorElementMock('div', ['widget-wrapper']));
      element.setNext(new CKEditorElementMock('div', ['widget-wrapper']));

      emptyParagraphRemover.removeBefore(element);
      expect(element.remove.callCount).to.equals(1);
    });

    it('should remove previous element if it is empty paragraph between two widgets', () => {
      let element = new CKEditorElementMock('div', ['widget-wrapper']);
      let prebiousElement = new CKEditorElementMock('p');
      prebiousElement.addChild(new CKEditorElementMock('br'));
      element.setPrevious(prebiousElement);
      prebiousElement.setPrevious(new CKEditorElementMock('div', ['widget-wrapper']));

      emptyParagraphRemover.removeBefore(element);
      expect(prebiousElement.remove.callCount).to.equals(1);
    });

    it('should not remove current element if it is not empty paragraph', () => {
      let element = new CKEditorElementMock('p');
      element.addChild(new CKEditorElementMock('div', ['widget-wrapper']));
      element.setPrevious(new CKEditorElementMock('div', ['widget-wrapper']));

      emptyParagraphRemover.removeBefore(element);
      expect(element.remove.callCount).to.equals(0);
    });
  });

  it('isEmptyParagraph should return true if element is empty paragraph', () => {
    let emptyParagraph = new CKEditorElementMock('p');
    emptyParagraph.addChild(new CKEditorElementMock('br'));
    expect(emptyParagraphRemover.isEmptyParagraph(emptyParagraph)).to.be.true;

    let notEmptyParagraph = new CKEditorElementMock('p');
    notEmptyParagraph.addChild(new CKEditorTextElementMock('Some text'));
    expect(emptyParagraphRemover.isEmptyParagraph(notEmptyParagraph)).to.be.false;
  });

  it('isEmptyElement should return true if element is either &nbsp; text node or br node', () => {
    let brElement = new CKEditorElementMock('br');
    expect(emptyParagraphRemover.isEmptyElement(brElement)).to.be.true;

    let nbspElement = new CKEditorTextElementMock(String.fromCharCode(160));
    expect(emptyParagraphRemover.isEmptyElement(nbspElement)).to.be.true;

    let notEmptyElement = new CKEditorElementMock('div');
    expect(emptyParagraphRemover.isEmptyElement(notEmptyElement)).to.be.false;
  });

  it('isWidget should return true if element is a widget', () => {
    let widgetElement = new CKEditorElementMock('div', ['widget-wrapper']);
    expect(emptyParagraphRemover.isWidget(widgetElement)).to.be.true;

    let notWidgetElement = new CKEditorElementMock('div');
    expect(emptyParagraphRemover.isWidget(notWidgetElement)).to.be.false;
  });
});


