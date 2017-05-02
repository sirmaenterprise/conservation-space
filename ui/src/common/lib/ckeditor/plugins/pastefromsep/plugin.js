(function () {
  'use strict';

  let notification;
  let progress = 0;
  let progressPercent = 0;

  CKEDITOR.plugins.add('pastefromsep', {
    requires: 'clipboard',
    icons: 'pastefromsep',
    init: function (editor) {
      let commandName = 'pastefromsep';

      editor.addCommand(commandName, {
        canUndo: false,
        async: true,
        exec: function (editor) {

          editor.getClipboardData({title: editor.lang.pastefromsep}, function (data) {
            if (!data) {
              return;
            }

            // Force html mode for incomming paste events sequence.
            editor.once('beforePaste', forceHtmlMode);

            let html = data.dataValue;
            let parser = new DOMParser();
            html = parser.parseFromString(html, "text/html");

            notification = editor.showNotification(editor.lang.processing, 'progress', 0);
            let images = [].slice.call(html.getElementsByTagName("img"), 0).filter(extractSepImages).map(removeAttributes);
            progressPercent = 1 / images.length;

            convertToBase64(images).then(function () {
              firePaste(editor, data, html);
            }).catch(function () {
              firePaste(editor, data, html);
            })
          });
        }
      });

      // Command is available if clipboard data.
      editor.on('pasteState', function (evt) {
        editor.getCommand(commandName).setState(evt.data);
      });
    }
  });

  function firePaste(editor, data, html) {
    data.dataValue = html.documentElement.innerHTML;

    // Do not use editor#paste, because it would start from beforePaste event.
    editor.fire('paste', {
      type: 'html',
      dataValue: data.dataValue,
      method: 'paste',
      dataTransfer: CKEDITOR.plugins.clipboard.initPasteDataTransfer()
    });

    hideNotification();
  }

  function convertToBase64(images) {
    return new Promise.all(images.map(convertImageToDataURL));
  }

  function extractSepImages(image) {
    return image.hasAttribute('data-embedded-id');
  }

  // Removes 'data-cke-saved-src' attribute to prevent CKEditor attribute changes.
  // Removes 'data-embedded-id' attribute to prevent back-end attempts to get the images.
  function removeAttributes(image) {
    image.removeAttribute('data-cke-saved-src');
    image.removeAttribute('data-embedded-id');
    return image;
  }

  function updateNotification() {
    progress += progressPercent;
    notification.update({progress: progress});
  }

  function hideNotification() {
    progress = 0;
    progressPercent = 0;
    notification.hide();
  }

  // Converts image to base64. Works with 'data-original' attribute, to get the proper src of lazy-not-loaded image.
  function convertImageToDataURL(image) {
    return new Promise(function (resolve) {
      let xhr = new XMLHttpRequest();

      xhr.onload = function () {
        let reader = new FileReader();

        reader.onloadend = function () {
          image.setAttribute('src', reader.result);
          updateNotification();
          resolve();
        };

        // Sets image with text 'Resource not available' if there is an error that prevents reading of the file.
        // Even though it's an error, we have to resolve, otherwise all promises will reject.
        reader.onerror = function () {
          image.setAttribute('src', RESOURCE_NOT_AVAILABLE);
          resolve();
        };

        reader.readAsDataURL(xhr.response);
      };

      // Sets image with text 'Resource not available' if there is an error that prevents the completion of the request.
      // Even though it's an error, we have to resolve, otherwise all promises will reject.
      xhr.onerror = function () {
        image.setAttribute('src', RESOURCE_NOT_AVAILABLE);
        resolve();
      };

      xhr.open('GET', image.getAttribute('data-original'));
      xhr.responseType = 'blob';
      xhr.send();
    });
  }

  function forceHtmlMode(evt) {
    evt.data.type = 'html';
  }
})();

// 'Resource not available' text on grey background.
RESOURCE_NOT_AVAILABLE = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAJYAAAAyCAMAAACJUtIoAAAAq1BMVEWAgID///99' +
  'fX2Dg4N6enqFhYWHh4d4eHiCgoJ2dnaJiYmLi4v8/Px1dXVycnL19fX5+fmNjY339/eQkJBvb2+SkpLu7u6VlZXy8vLp6em1tbWPj4/7+/vw8' +
  'PC6urqjo6OampqsrKzi4uLX19fT09O/v7/CwsLr6+vm5ubGxsaenp7b29unp6eXl5fJycnNzc1sbGxpaWnf39+vr6/Q0NCysrLd3d3Z2dm8vL' +
  'yE4tfJAAAKNElEQVRYw7yW6W4jOQyEi0VRlNS379hx7DjOfWeS2Xn/J1tngSywCPxvkQ+NhgSCJYpkE41/yYrBk+IAD+CHcIVDkY6YC7RBBp2' +
  'waIn4KSwxJRyDVMZYEBUkGPBjECCPhwWAwRUgNBb8FE4FcQwDlcSKMENwxw9hTBEwHIWRzFRVAMnwQxhVM6MfC6oQPo0ECTMlfgpXtDrlMWvl' +
  'YX4itRzo9hv8GIyap5vqeMtzVXcyqj+fbkukXBwVV60xMKmX0nLwkis2laEx0AIi2giwsB0zDGzUwOKpSSVHaCheOYfgQ3NwalM1ZNJINlEBT' +
  'YQhxEbXokxBy/cvsimag5yuhhDwcCEngVUJhkwFVSuqB1PStEEKDGGODNU0ialyOmJEO5BgThG5igzqoWEVCAZahWmuqvDarAz0VBQIJA0vmm' +
  '3snYRkRse3CUZFdJHWs5LbWTedZHsdrGhbxSoTbpoqNAnwZGpoJhZfwxju4WU+tD5Jpk0bMeQmJCbPpVmBjLQIC5MIJ3w7uCsSAwiU1KRxChZ' +
  'zEUF8ZUL1bV7GECqIgDYN7XYkOGz9472Wq3M4YJv9Yn1y8UCrmvhrL6cnS8t3cm2xiqvuZGrldDH//ZnkstzJ7nljscmrs+dO/jpniIGhwGN/' +
  'c3cxW9+cIWBo9OOiG90v4wS1iPQDQMvf5mVITpnNSeXmL9l7HPAs/3AL6IMc6GX24Rieuvpz/e4u68HZJpHYHF77rr5suJdP7mOj+Vb+4cm0y' +
  'moBKjIbicjonKD+kX+4ihA5PRYWQyFq6WTUjw6O84rtnezuSrvZyZnjpNsEjp/7GcNS6rNtPBx5TlmkFEOQGtBrOd1qni5ldr5aXchVM3z0u7' +
  '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' +
  'Cff8ZvciSjrzIIW2cAAAAASUVORK5CYII=';