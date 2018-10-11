import {Injectable, Inject} from 'app/app';
import {UserService} from 'services/identity/user-service';
import {ContentRestService} from 'services/rest/content-service';
import {EditorContentProcessor} from 'idoc/editor/editor-content-processor';
import {DynamicElementsRegistry} from 'idoc/dynamic-elements-registry';

import $ from 'jquery';
import 'common/lib/lazyload/jquery.lazyload';

/*
 Used when idoc is loaded to process the image attributes.
 Processes the attributes to allow lazy image loading.
 */
@Injectable()
@Inject(UserService, ContentRestService, DynamicElementsRegistry)
export class EditorContentImageProcessor extends EditorContentProcessor {

  constructor(userService, contentRestService, dynamicElementsRegistry) {
    super();
    this.contentRestService = contentRestService;
    this.userService = userService;
    this.dynamicElementsRegistry = dynamicElementsRegistry;
  }

  // First determines processor step, then checks if images in editor's content and processes content. If none returns content unmodified.
  preprocessContent(editorInstance, content) {
    let context = editorInstance.context;
    if (content && content.indexOf('<img') !== -1) {
      return this.processContentWithImages(context, content);
    }

    return content;
  }

  processContentWithImages(context, content) {
    let editorContent = $('<div>').append(content);

    return this.userService.getCurrentUser().then((user) => {
      editorContent.find('img').each((index, img) => {
        let image = $(img);
        if (context.isEditMode() || context.isPreviewMode()) {
          this.setImgLazyProperties(image, user.tenantId);
        } else {
          this.setImgPrintProperties(image, user.tenantId);
        }
      });
      return editorContent.html();
    });
  }

  setImgLazyProperties(image, tenantId) {
    if (this.getDataEmbeddedId(image)) {
      let imageUrl = this.contentRestService.getImageUrl(this.getDataEmbeddedId(image), tenantId);
      image.attr('data-original', imageUrl);
      // In order not to break CKEditor undo/redo ability, we have to set 'data-cke-saved-src' - an attribute of CKEditor that holds the image source.
      image.attr('data-cke-saved-src', imageUrl);
    } else {
      if (!image.attr('data-original') && image.attr('src') !== EditorContentImageProcessor.IMAGE_ONE_PIXEL) {
        image.attr('data-original', image.attr('src'));
        image.attr('data-cke-saved-src', image.attr('src'));
      }
    }
    // set dummy pixel src because img tag without src is not valid
    image.attr('src', EditorContentImageProcessor.IMAGE_ONE_PIXEL);
  }

  setImgPrintProperties(image, tenantId) {
    if (this.getDataEmbeddedId(image)) {
      image.attr('src', this.contentRestService.getImageUrl(this.getDataEmbeddedId(image), tenantId));
    } else {
      image.attr('src', image.attr('data-original'));
    }
    // In order not to break CKEditor undo/redo ability, we have to set 'data-cke-saved-src' - an attribute of CKEditor that holds the image source.
    image.attr('data-cke-saved-src', image.attr('src'));
    this.dynamicElementsRegistry.handleImage(image);
  }

  getDataEmbeddedId(image) {
    return image.attr('data-embedded-id');
  }

  // After CKEditor's content is set, applies images' lazy loading. In edit mode ensures CKEditor's undo/redo capability.
  postprocessContent(editorInstance) {
    let context = editorInstance.context;
    if (!context.isPrintMode()) {
      this.setLazyLoad(editorInstance.editor);
    }
    if (context.isEditMode()) {
      // Undo / Redo commands break images' lazy load and we have to reprocess editors content - if such images in tab.
      // see EditorContentImageProcessor 'idoc/editor/images/editor-content-image-processor'
      editorInstance.editor.on('afterCommandExec', (event) => {
        if (event.data.name === 'undo' || event.data.name === 'redo') {
          this.detachLazyload(editorInstance.editor);
          this.setLazyLoad(editorInstance.editor);
        }
      });
    }
  }

  // Applies lazy load to editor's instance window only.
  setLazyLoad(editor) {
    $(editor.element.getElementsByTag('img').$).lazyload({
      effect: 'fadeIn',
      threshold: 400
    });
  }

  detachLazyload(editor) {
    $(editor.element.getElementsByTag('img').$).each((index, img) => {
      let image = $(img);
      if (image.lazyload) {
        image.lazyload.destroy();
      }
    });
  }
}

EditorContentImageProcessor.IMAGE_ONE_PIXEL = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsQAAA7EAZUrDhsAAAANSURBVBhXYzh8+PB/AAffA0nNPuCLAAAAAElFTkSuQmCC';