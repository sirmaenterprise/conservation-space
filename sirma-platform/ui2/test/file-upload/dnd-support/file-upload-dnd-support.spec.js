import {FileUploadDnDSupport} from 'file-upload/dnd-support/file-upload-dnd-support';
import {NavigatorAdapter} from 'adapters/navigator-adapter';

describe('FileUploadDnDSupport', () => {
  let element;
  let onDropCallback;
  let animationDuration = 3000;
  beforeEach(() => {
    element = createElement();
    onDropCallback = sinon.spy();
    FileUploadDnDSupport.addDropSupport(element, onDropCallback);
  });

  describe('addDropSupport', () => {
    it('should add handles when method "addDropSupport" is called.', () => {
      expect(element.on.getCall(0).args[0]).equal('dragover');
      expect(element.on.getCall(1).args[0]).equal('drop');
      expect(element.on.getCall(2).args[0]).equal('$destroy');
    });

    it('should not add handlers when method "addDropSupport" is called without onDropCallback function.', () => {
      let element = createElement();

      FileUploadDnDSupport.addDropSupport(element, null);

      expect(element.on.called).to.be.false;
    });
  });

  describe('_documentDragEnter', () => {
    it('should not add animation if dropped sources are not files', () => {
      let event = createEvent();

      FileUploadDnDSupport._documentDragEnter(element, 'animationClass', animationDuration, event);

      expect(element.hasClass.called).to.be.false;
    });

    it('should add animation if dropped sources are files', () => {
      let event = createEvent(true);
      element.hasClass = () => {return false;};

      FileUploadDnDSupport._documentDragEnter(element, 'animationClass', animationDuration, event);

      expect(element.addClass.getCall(0).args[0]).equal('animationClass');
    });

    it('should not add animation if dropped sources are not files (IE browser)', () => {
      let event = createIEorEdgeEvent();
      let isEdgeOrIEStub = sinon.stub(NavigatorAdapter, 'isEdgeOrIE', sinon.spy(() => {
        return true;
      }));

      FileUploadDnDSupport._documentDragEnter(element, 'animationClass', animationDuration, event);

      expect(element.hasClass.called).to.be.false;

      isEdgeOrIEStub.restore();
    });

    it('should add animation if dropped sources are files (files IE browser)', () => {
      let event = createIEorEdgeEvent(true);
      let isEdgeOrIEStub = sinon.stub(NavigatorAdapter, 'isEdgeOrIE', sinon.spy(() => {
        return true;
      }));
      element.hasClass = () => {return false;};

      FileUploadDnDSupport._documentDragEnter(element, 'animationClass', animationDuration, event);

      expect(element.addClass.getCall(0).args[0]).equal('animationClass');
      isEdgeOrIEStub.restore();
    });
  });

  describe('_getOnDragOverHandler', () => {
    it('should prevent event if drag source is file', () => {
      let event = createEvent(true);
      event.preventDefault = sinon.spy();

      FileUploadDnDSupport._dragOver(event);

      expect(event.preventDefault.calledOnce).to.be.true;
      expect(event.originalEvent.dataTransfer.dropEffect).to.equal('copy');

    });

    it('should not prevent event if drag source is not file', () => {
      let event = createEvent(false);
      event.preventDefault = sinon.spy();

      FileUploadDnDSupport._dragOver(event);

      expect(event.preventDefault.calledOnce).to.be.not;
      expect(event.originalEvent.dataTransfer.dropEffect).to.not.equal('copy');

    });
  });

  it('should destroy all handlers', () => {
    // Given:
    // we have dragenter event handler
    let dragEnterHandler = {off: sinon.spy()};
    // and element with attached handlers

    // When destroy method is called
    FileUploadDnDSupport._destroy(element, dragEnterHandler);

    // Then:
    // all handlers have to be off.
    expect(element.off.calledOnce).to.be.true;
    expect(dragEnterHandler.off.calledOnce).to.be.true;


  });

  describe('_onDrop', () => {
    it('should calls "onDropCallback" with extracted files from event without folders extraction', () => {
      // When "drop" event with file and folder occurred.
      let event = createEvent(true);
      event.stopPropagation = sinon.spy();
      let file = createFile('testFile.txt', 'text/plain');
      let folder = createFile('testFolder', '');
      event.originalEvent.dataTransfer.files = {'0': file, '2': folder};

      // When call method with the event
      FileUploadDnDSupport._onDrop(element, onDropCallback, 'animationClass', event);

      // Then:
      // 1. "stopPropagation" method of event have to be called.
      expect(event.stopPropagation.calledOnce).to.be.true;

      // 2. "onDropCallback" function have to be called.
      expect(onDropCallback.calledOnce).to.be.true;

      // 3. "onDropCallback" function have to be called only for files.
      let argumentFiles = onDropCallback.getCall(0).args[0];
      expect(argumentFiles.length).to.equal(1);
      expect(argumentFiles[0].name).to.equal('testFile.txt');
    });

    it('should calls "onDropCallback" with extracted files from event without folers extraction (IE)', () => {
      // When "drop" event with file occurred.
      let event = createIEorEdgeEvent(true);
      event.stopPropagation = sinon.spy();
      let file = createFile('testFile.txt', 'text/plain');
      event.originalEvent.dataTransfer.files = {'0': file};

      // When call method with the event
      FileUploadDnDSupport._onDrop(element, onDropCallback, 'animationClass', event);

      // Then:
      // 1. "stopPropagation" method of event have to be called.
      expect(event.stopPropagation.calledOnce).to.be.true;

      // 2. "onDropCallback" function have to be called.
      expect(onDropCallback.calledOnce).to.be.true;

      // 3. "onDropCallback" function have to be called only for files.
      let argumentFiles = onDropCallback.getCall(0).args[0];
      expect(argumentFiles.length).to.equal(1);
      expect(argumentFiles[0].name).to.equal('testFile.txt');
    });

    it('should calls "onDropCallback" with extracted files from event with folders extraction', () => {
      // Given:
      let event = createEvent(true);
      event.stopPropagation = sinon.spy();

      // A file and a folder.
      let itemFile = createItem('testFile.txt', 'text/plain', true);
      let itemFolder = createItem('testFolder', '', false);
      // The folder have a file
      let itemInnerFile = createItem('innerTestFile.txt', 'text/plain', true);

      itemFolder.createReader = () => {
        return {
          readEntries: (callback) => {
            callback([itemInnerFile]);
          }
        };
      };

      // When: we dragged the file and folder
      event.originalEvent.dataTransfer.items = [itemFile, itemFolder];
      FileUploadDnDSupport._onDrop(element, onDropCallback, 'animationClass', event);

      // Then:
      // 1. "stopPropagation" method of event have to be called.
      expect(event.stopPropagation.calledOnce).to.be.true;

      // 2. "onDropCallback" function have to be called.
      expect(onDropCallback.calledOnce).to.be.true;

      // 3. "onDropCallback" function have to be called for both files.
      let argumentFiles = onDropCallback.getCall(0).args[0];
      JSON.stringify(argumentFiles);
      expect(argumentFiles.length).to.equal(2);
      expect(argumentFiles[0].name).to.equal('testFile.txt');
      expect(argumentFiles[1].name).to.equal('innerTestFile.txt');
    });

    function createItem(itemName, itemType, isFile) {
      let item = createFile(itemName, itemType);
      item.webkitGetAsEntry = () => {
        return item;
      };
      item.isFile = isFile;
      item.isDirectory = !isFile;
      item.file = (callback) => {
        callback(item);
      };
      return item;
    }
  });

  describe('_startTargetAnimation', () => {
    it('should not add animationClass if class is already added', () => {
      element.hasClass = () => {return true;};

      FileUploadDnDSupport._startTargetAnimation(element, 'animation-class', animationDuration);

      expect(element.addClass.called).to.be.false;
    });

    it('should add animationClass if class is not already added', () => {
      element.hasClass = () => {return false;};

      FileUploadDnDSupport._startTargetAnimation(element, 'animation-class', animationDuration);

      expect(element.addClass.called).to.be.true;
    });
  });

  function createElement() {
    return {
      addClass: sinon.spy(),
      removeClass: sinon.spy(),
      hasClass: sinon.spy(),
      on: sinon.spy(),
      off: sinon.spy()
    };
  }

  function createFile(fileName, type) {
    return {name: fileName, type};
  }

  function createIEorEdgeEvent(fileSourceType, clientX, clientY) {
    let event = {
      originalEvent: {
        dataTransfer: {
          types: {
            contains: () => {
              return fileSourceType;
            }
          }
        },
        clientX,
        clientY
      }
    };
    return event;
  }

  function createEvent(fileSourceType, clientX, clientY) {
    let event = {
      originalEvent: {
        dataTransfer: {
          types: [fileSourceType ? 'Files' : 'OtherType']
        },
        clientX,
        clientY
      }
    };
    return event;
  }
});