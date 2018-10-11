import {CKEditorElementMock, CKEditorTextElementMock} from 'test/idoc/editor/idoc-editor-mocks';
import {ObjectLink} from 'idoc/editor/plugins/object-link/object-link';
import {PromiseStub} from 'test/promise-stub';

describe('ObjectLink', () => {
  var objectLink;
  before(() => {
    objectLink = new ObjectLink(mockTranslateService())
  });

  describe('openPicker', () => {
    it('should insert object link widget', () => {
      var selectedItems = [
        {id: "emf:111"}
      ];
      var editor = mockEditor();
      objectLink.pickerService = mockPickerService(selectedItems);
      objectLink.openPicker(editor);
      expect(editor.insertElement.calledOnce).to.be.true;
      expect(editor.insertElement.getCall(0).args[0].getAttribute('widget')).to.equals('object-link');
    });

    it('should invoke widget initialization', () => {
      var selectedItems = [
        {id: "emf:111"}
      ];
      var editor = mockEditor();
      objectLink.pickerService = mockPickerService(selectedItems);
      objectLink.openPicker(editor);
      expect(editor.widgets.initOn.called).to.be.true;
    });
  });

  it('should insert separator between widgets', () => {
    var selectedItems = [
      {id: "emf:111"},
      {id: "emf:111"}
    ];
    var editor = mockEditor();
    objectLink.pickerService = mockPickerService(selectedItems);
    objectLink.openPicker(editor);
    expect(editor.insertText.getCall(0).args[0]).to.be.equal(', ');
    expect(editor.insertText.getCall(1).args[0]).to.be.equal(' ');
  });

  describe('createWidgetTemplate', () => {
    it('should create object link template', () => {
      var selectedItems = [
        {id: "emf:111"}
      ];
      var template = objectLink.createWidgetTemplate("emf:123");
      expect(template).to.be.equal('<span widget="object-link" class="widget object-link" config="eyJzZWxlY3RlZE9iamVjdCI6ImVtZjoxMjMifQ==" data-new="true"></span>');
    });
  });
});

function mockEditor() {
  return {
    insertElement: sinon.spy((element) => {
      return element;
    }),
    widgets: {
      initOn: sinon.spy()
    },
    insertText: sinon.spy((element) => {
      return element;
    })
  };
}

function mockTranslateService() {
  return {
    translateInstant: () => {
      return 'translated ';
    }
  };
}

function mockPickerService(items) {
  return {
    configureAndOpen: sinon.spy(() => {
      return PromiseStub.resolve(items);
    })
  };
}