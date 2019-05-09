import {Injectable, Inject} from 'app/app';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {PickerService, SEARCH_EXTENSION} from 'services/picker/picker-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NEW_ATTRIBUTE} from 'idoc/widget/widget';
import base64 from 'common/lib/base64';
import _ from 'lodash';

export const OBJECT_LINK_WIDGET = 'object-link';

const OBJECT_LINK_BUNDLE = 'objectlink.widget.name';
const OBJECT_LINK_TOOLBAR_BUTTON = 'Objectlink';
const OBJECT_LINK = 'objectlink';
const SPACE = ' ';
const COMMA = ', ';

/**
 * Plugin for idoc editor that insert object links inside the content.
 *
 * The object link itself is a instance compact header and depends on object selection from a modal dialog.
 * The component that manage the extraction and insertion is represented as inline widget and is not configurable.
 */
@Injectable()
@Inject(TranslateService, PickerService)
export class ObjectLink {
  constructor(translateService, pickerService) {
    this.pickerService = pickerService;

    var initPlugin = (editor)=> {
      editor.addCommand(OBJECT_LINK, {
        exec: (editor) => {
          this.openPicker(editor);
        }
      });
      editor.ui.addButton(OBJECT_LINK_TOOLBAR_BUTTON, {
        label: translateService.translateInstant(OBJECT_LINK_BUNDLE),
        command: OBJECT_LINK,
        toolbar: OBJECT_LINK
      });
    };
    CKEDITOR.plugins.add(OBJECT_LINK, {
      icons: OBJECT_LINK,
      init: initPlugin
    });
  }

  openPicker(editor) {
    this.pickerService.configureAndOpen(this.getPickerConfiguration(), undefined, {}).then((selectedItems)=> {

      _.forEach(selectedItems, (item, index) => {
        // by requirement, all object links should be separated with comma
        var separator = COMMA;
        if (index === selectedItems.length - 1) {
          separator = SPACE;
        }
        var widgetTemplate = this.createWidgetTemplate(item.id);
        var element = CKEDITOR.dom.element.createFromHtml(widgetTemplate);
        editor.insertElement(element);
        editor.widgets.initOn(element, OBJECT_LINK_WIDGET, {});
        editor.insertText(separator);
      });
    });
  }

  createWidgetTemplate(instanceId) {
    var widgetConfig = {
      selectedObject: instanceId
    };
    widgetConfig = base64.encode(JSON.stringify(widgetConfig));
    return `<span widget="${OBJECT_LINK_WIDGET}" class="widget ${OBJECT_LINK_WIDGET}" config="${widgetConfig}" ${NEW_ATTRIBUTE}="true"></span>`;
  }

  getPickerConfiguration() {
    var pickerConfig = {
      header: OBJECT_LINK_BUNDLE,
      extensions: {}
    };
    pickerConfig.extensions[SEARCH_EXTENSION] = {
      useRootContext: false,
      results: {
        config: {
          selection: MULTIPLE_SELECTION
        }
      }
    };
    return pickerConfig;
  }
}
