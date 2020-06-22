import {EditorContentWidgetProcessor} from 'idoc/editor/content/editor-content-widget-processor'

describe('EditorContentWidgetProcessor', function () {

  var editorContentWidgetProcessor;
  var editorInstance;
  var editorContent = `<div widget="widget" class="widget">Content</div>`;
  var editorContentNoWidget = `<div>Content</div>`;
  beforeEach(() => {
    editorContentWidgetProcessor = new EditorContentWidgetProcessor();
    editorContentWidgetProcessor.setLazyLoad = sinon.spy();
  });

  it('should register listeners to the editor in edit mode', () => {
    editorInstance = {
      context: {
        isPreviewMode: () => {
          return false;
        },
        isEditMode: () => {
          return true;
        }
      },
      editor: {
        on: sinon.spy()
      }
    };

    editorContentWidgetProcessor.postprocessContent(editorInstance);
    expect(editorInstance.editor.on.callCount).to.equals(3);
    expect(editorInstance.editor.on.getCall(0).args[0]).to.equals('afterCommandExec');
    expect(editorInstance.editor.on.getCall(1).args[0]).to.equals('paste');
    expect(editorInstance.editor.on.getCall(2).args[0]).to.equals('afterPaste');
  });

  it('should not register listeners to the editor in preview mode', () => {
    editorInstance = {
      context: {
        isPreviewMode: () => {
          return true;
        },
        isEditMode: () => {
          return false;
        }
      },
      editor: {
        on: sinon.spy()
      }
    };

    editorContentWidgetProcessor.postprocessContent(editorInstance);
    expect(editorInstance.editor.on.callCount).to.equals(0);
  });

  it('should call lazy load in edit mode', () => {
    editorInstance = {
      context: {
        isPreviewMode: () => {
          return false;
        },
        isEditMode: () => {
          return true;
        }
      },
      editor: {
        on: sinon.spy()
      }
    };

    editorContentWidgetProcessor.postprocessContent(editorInstance);
    expect(editorContentWidgetProcessor.setLazyLoad.callCount).to.equals(1);
  });

  it('should call lazy load in preview mode', () => {
    editorInstance = {
      context: {
        isPreviewMode: () => {
          return true;
        },
        isEditMode: () => {
          return false;
        }
      },
      editor: {
        on: sinon.spy()
      }
    };

    editorContentWidgetProcessor.postprocessContent(editorInstance);
    expect(editorContentWidgetProcessor.setLazyLoad.callCount).to.equals(1);
  });

  it('should add proper attribute to widget div element when pasted', () => {
    let editor = {
      fire: sinon.spy()
    };

    let event = {
      data: {
        dataValue: editorContent
      }
    };

    editorContentWidgetProcessor.processPastedContent(event, editor);
    expect(event.data.dataValue).to.equals('<head></head><body><div widget="widget" class="widget" data-render="true">Content</div></body>');
    expect(editor.fire.callCount).to.equals(1);
  });


  it('should not add attribute when no widget is pasted', () => {
    let editor = {
      fire: sinon.spy()
    };

    let event = {
      data: {
        dataValue: editorContentNoWidget
      }
    };

    editorContentWidgetProcessor.processPastedContent(event, editor);
    expect(event.data.dataValue).to.equals('<head></head><body><div>Content</div></body>');
    expect(editor.fire.callCount).to.equals(1);
  });



});
