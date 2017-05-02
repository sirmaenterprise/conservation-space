import {IdocEditorPasteListener} from 'idoc/editor/idoc-editor-paste-listener'

describe('IdocEditorPasteListener', function () {

  describe('clearDuplicateID', function () {
    it('should clear only one duplicate match', function () {
      var pasteListener = new IdocEditorPasteListener;

      var pageContent = '<span id="1"></span>';
      var pasteContent = '<span id="1"></span>';

      var result = pasteListener.clearDuplicateID(pasteContent, pageContent);
      expect(result).to.equal('<span></span>');
    });

    it('should clear multiple duplicate matches', function () {
      var pasteListener = new IdocEditorPasteListener;

      var pageContent = '<span id="1"></span><span id="2"></span>';
      var pasteContent = '<span id="1"></span><span id="2"></span>';
      var result = pasteListener.clearDuplicateID(pasteContent, pageContent);
      expect(result).to.equal('<span></span><span></span>');
    });

    it('should not return success when no ids are duplicate', function () {
      var pasteListener = new IdocEditorPasteListener;

      var pageContent = '<span id="1"></span><span id="2"></span>';
      var pasteContent = '<span id="3"></span><span id="4"></span>';
      var result = pasteListener.clearDuplicateID(pasteContent, pageContent);
      expect(result).to.be.false;
    });
  });

  describe('clearWidgetIds', () => {
    it('should remove all widget ids and return processed content', () => {
      var pasteListener = new IdocEditorPasteListener;

      let result = pasteListener.clearWidgetIds('<div class="widget" id="widget1">Content widget 1</div><div class="widget" id="widget2">Content widget 2</div>');
      expect(result).to.equals('<div class="widget">Content widget 1</div><div class="widget">Content widget 2</div>');
    });
    it('should return false if there are no widgets', () => {
      var pasteListener = new IdocEditorPasteListener;

      let result = pasteListener.clearWidgetIds('Some free text <span>Text in a span</span>');
      expect(result).to.be.false;
    });
  });

});
