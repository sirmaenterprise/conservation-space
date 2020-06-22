/**
 * Mock class for CKEditor node element
 */
export class CKEditorElementMock {
  constructor(tagName, classes, text) {
    this.tagName = tagName;
    this.classes = classes || [];
    this.text = text;
    this.children = [];
    this.editable = true;
    this.type = CKEDITOR.NODE_ELEMENT;
    this.$ = {
      nodeType: CKEDITOR.NODE_ELEMENT
    };
    this.remove = sinon.stub()
  }

  is(tagName) {
    return this.tagName === tagName;
  }

  hasClass(className) {
    return this.classes.indexOf(className) !== -1;
  }

  getChildCount() {
    return this.children.length;
  }

  getChild(index) {
    return this.children[index];
  }

  addChild(element) {
    this.children.push(element);
  }

  setNext(element) {
    this.next = element;
  }

  getNext() {
    return this.next || new CKEditorTextElementMock('');
  }

  setPrevious(element) {
    this.previous = element;
  }

  getPrevious(element) {
    return this.previous || new CKEditorTextElementMock('');
  }

  getText() {
    return this.text;
  }

  getParent() {
    return this.parent;
  }

  setParent(parent) {
    this.parent = parent;
  }

  isEditable() {
    return this.editable;
  }

  setEditable(editable) {
    this.editable = editable;
  }

  equals(element) {
    return this === element;
  }
}

/**
 * Mock class for CKEditor text element
 */
export class CKEditorTextElementMock extends CKEditorElementMock {
  constructor(text) {
    super(undefined, undefined, text);
    this.type = CKEDITOR.NODE_TEXT;
    this.$ = {
      nodeType: CKEDITOR.NODE_TEXT
    };
    // Text nodes doesn't have is function
    delete this.is;
  }
}