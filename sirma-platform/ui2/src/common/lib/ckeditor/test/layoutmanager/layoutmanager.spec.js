describe("LayoutManager", function() {

  describe("LayoutBuilder", function() {

    var layoutBuilder;
    beforeEach(function() {
      layoutBuilder = new LayoutBuilder();
    });

    it("creates layout button with 2 columns", function() {
      var mockType = "6/6";
      var mockRowPos = 2;
      layoutBuilder.addLayout(mockType, 2);
      var layoutTemplate = layoutBuilder.getLayout(mockType);
      expect(layoutTemplate.template.match(/div/g).length).toBe(8);
    });

    it("creates layout button with 3 columns", function() {
      var mockType = "3/6/3";
      var mockRowPos = 2;
      layoutBuilder.addLayout(mockType, 2);
      var layoutTemplate = layoutBuilder.getLayout(mockType);
      expect(layoutTemplate.template.match(/div/g).length).toBe(10);
    });
  });

  describe("LayoutManager", function() {
    var layoutManager;
    var editor;

    describe("LayoutReplacementAlgorithm", function() {
      beforeEach(function() {
        editor = new CKEDITOR.editor;
        layoutManager = new LayoutManager(editor);
      });
      it("todo", function() {
        //TODO might need dom tool
      });
    });

    describe("Button's view generation", function() {
      beforeEach(function() {
        editor = new CKEDITOR.editor;
        layoutManager = new LayoutManager(editor);
      });

      it("creates '6/6' layout button view", function() {
        var mockType = "6/6";
        var mockAction = function() {};
        var buttonObject = layoutManager.createButton(mockType, mockAction);
        expect(buttonObject.html.match(/div/g).length).toBe(6);
      });

    });

  });

});