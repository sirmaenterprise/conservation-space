(function () {
  'use strict';
  let pastedHtml;

  CKEDITOR.plugins.add('pastebase64', {
    init: init
  });

  function init(editor) {
    editor.on("contentDom", function () {
      let editableElement = editor.editable ? editor.editable() : editor.document;
      editor.on("paste", editorOnPasteHandler);
      editableElement.on("paste", editableElementOnPasteHandler, null, {editor: editor});
    });
  }

  function editorOnPasteHandler(event) {
    return pastedHtml = new Promise(function (resolve) {
      let data = event.data && event.data.dataValue.trim();

      if (!data) {
        resolve('possiblyBase64')
      } else if (data.substring(0, 4) === '<img') {
        resolve(CKEDITOR.dom.element.createFromHtml(data));
      }
    });
  }

  function editableElementOnPasteHandler(event) {
    let editor = event.listenerData && event.listenerData.editor;
    let $event = event.data.$;
    let clipboardData = $event.clipboardData;
    let found = false;
    let imageType = /^image/;

    if (!clipboardData) {
      return;
    }

    return Array.prototype.forEach.call(clipboardData.types, function (type, i) {
      if (found) {
        return;
      }

      if (type.match(imageType) || clipboardData.items[i].type.match(imageType)) {
        readImageAsBase64(clipboardData.items[i]);
        return found = true;
      }
    });

    function readImageAsBase64(item) {

      if (!item || typeof item.getAsFile !== 'function') {
        return;
      }

      let file = item.getAsFile();
      let reader = new FileReader();

      reader.onload = function (evt) {
        pastedHtml.then(function (result) {
          if (result === 'possiblyBase64') {
            let imageSrc = evt.target.result;

            let element = editor.document.createElement('img', {
              attributes: {
                src: imageSrc
              }
            });

            // We use a timeout callback to prevent a bug where insertElement inserts at first caret position
            setTimeout(function () {
              editor.insertElement(element);
            }, 50);

          } else {
            [].slice.call((CKEDITOR.document.getBody().getElementsByTag("img").$), 0).filter(getPastedImage).map(setSrc);
          }

          function getPastedImage(image) {
            return image.src === result.getAttribute('src');
          }

          function setSrc(image) {
            image.src = evt.target.result;
            return image;
          }
        });
      };
      reader.readAsDataURL(file);
    }
  }
})();
