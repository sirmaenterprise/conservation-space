import {View, Component, Inject, NgElement, NgTimeout} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import {DEFAULT_VALUE_PATTERN} from 'form-builder/validation/calculation/calculation';
import {IdocEditorChangeListener} from 'idoc/editor/idoc-editor-change-listener';
import {TranslateService} from 'services/i18n/translate-service';
import {ModelUtils} from 'models/model-utils';
import uuid from 'common/uuid';

import './rich-text.css!css';
import template from './rich-text.html!text';

const EDITOR_TOOLBAR = 'richtext-field-editor-toolbar';

@Component({
  selector: 'seip-rich-text',
  properties: {
    'formWrapper': 'form-wrapper',
    'identifier': 'identifier'
  }
})
@View({
  template
})
@Inject(TranslateService, NgTimeout, NgElement)
export class RichText extends FormControl {

  constructor(translateService, $timeout, $element) {
    super();
    this.$element = $element;
    this.$timeout = $timeout;
    this.translateService = translateService;
    this.fieldIdentifier = uuid();
    this.compositeFieldId = this.objectId + this.identifier;
    this.idocEditorChangeListener = new IdocEditorChangeListener({});
  }

  ngOnInit() {
    this.initElement();
  }

  ngAfterViewInit() {

    // configure ckeditor to wrap lines in paragraphs
    CKEDITOR.dtd.$cdata.p = 1;

    CKEDITOR.disableAutoInline = true;

    this.editor = CKEDITOR.inline(this.fieldIdentifier, {
      language: this.translateService.getCurrentLanguage(),
      colorButton_foreStyle: {
        element: 'span',
        styles: {'color': '#(color)', '-webkit-text-fill-color': '#(color)'},
        overrides: [{element: 'font', attributes: {'color': null}}]
      },
      colorButton_backStyle: {
        element: 'span',
        styles: {'background-color': '#(color) !important'},
        overrides: {element: 'span'}
      },
      toolbar: RichText.getEditorConfig(),
      extraPlugins: 'undo',
      allowedContent: true,
      title: false
    });

    this.editor.on('instanceReady', () => {
      this.editor.container.addClass(this.getCompositeFieldId());

      if (this.isPreviewField() || this.isPrintField()) {
        this.makeReadonly(true);
      }

      let valueToSet = this.validationModel[this.identifier].value;
      if (this.getRichtextValue()) {
        valueToSet = this.getRichtextValue();
      }
      // Set as updating when initializing the field to prevent to be marked as editedByUser. The updating
      // flag is later properly switched as needed.
      this.updating = true;
      this.editor.setData(valueToSet, {
        callback: () => {
          this.updating = false;
        }
      });

      // Store the original value in separate attribute to have it when object is going to be saved, where the stored
      // richtext value is extracted from it and put in the changeset if the field's actually changed.
      if (!this.getRichtextValue()) {
        this.setRichtextValue(this.validationModel[this.identifier].value);
      }

      let parentEditorId = this.$element.closest('.idoc-editor').attr('id');
      if (parentEditorId) {
        this.editor.on('focus', () => {
          CKEDITOR.instances[parentEditorId].setReadOnly(true);
        });

        this.editor.on('blur', () => {
          CKEDITOR.instances[parentEditorId].setReadOnly(false);
        });

        if (CKEDITOR.env.chrome || CKEDITOR.env.safari) {
          this.attachToolbarListeners();
        }

        if (CKEDITOR.env.chrome) {
          // Get range when selection is made using keyboard only
          this.editor.on('selectionChange', () => {
            this.ranges = this.editor.getSelection().getRanges();
          });
        }
      }
    });

    if (this.isEditField()) {
      this.initForEdit();
    }

    // Handle the viewModel.preview changes and switch the editor from edit to readonly mode and backwards.
    this.fieldViewModelSubscription = this.fieldViewModel.subscribe('propertyChanged', (propertyChanged) => {
      let changedProperty = Object.keys(propertyChanged)[0];
      if (changedProperty === 'preview') {
        this.makeReadonly(propertyChanged.preview);
      }

      this.executeCommonPropertyChangedHandler(propertyChanged);
    });

    // Super class implementation is called because it has implementation for this method which must be
    // executed as well.
    super.ngAfterViewInit();
  }

  attachToolbarListeners() {
    $('.' + this.editor.id).find('.cke_button, .cke_combo_button').on('click', () => {
      let dropdownElement = $('.' + this.editor.id + ' iframe').contents();
      dropdownElement && dropdownElement.find('.cke_colorblock, .cke_panel_container, .cke_ltr, .cke_panel_list').each((index, elem) => {
        $(elem).one('mouseup', () => {
          this.editor.getSelection().selectRanges(this.ranges);
        });
      });
    });
    // Get range when selection is made using mouse
    $('.' + this.editor.id).on('mouseup', () => {
      this.ranges = this.editor.getSelection().getRanges();
    });
  }

  initForEdit() {
    // We need to strip any tag which contains ckeditor specific attribute. Probably in future the list will be extended.
    this.editor.on('paste', (evt) => {
      evt.removeListener();

      let dataValue = $('<div>' + evt.data.dataValue + '</div>');
      let editables = dataValue.find('[data-cke-widget-editable]');
      if (editables.length > 0) {
        editables.children().unwrap();
        evt.data.dataValue = dataValue.html();
      }
    });

    this.editor.on('change', (evt) => this.editorDataChangeHandler(evt));

    if (CKEDITOR.env.ie) {
      this.editor.on('key', (evt) => {
        if (evt.editor.getData().length === 0) {
          this.validationModel[this.identifier].value = '';
          this.setRichtextValue(evt.editor.getData());
        }
      });

      $(this.$element).on('click', () => {
        this.editor.fire('focus');
      });
    }
    this.validationModelSubscription = this.validationModel[this.identifier].subscribe('propertyChanged', (propertyChanged) => this.propertyValueChangeHandler(propertyChanged));
  }

  propertyValueChangeHandler(propertyChanged) {
    let changedProperty = Object.keys(propertyChanged)[0];
    if (changedProperty === 'richtextValue') {
      let currentEditorData = this.editor.getData();
      let isSameData = currentEditorData === propertyChanged.richtextValue;

      if (!isSameData) {
        this.updating = true;
        // As the editor.setData is async, we need a way to track when the the editor is done updating itself. That's
        // why we set a flag which is muted in the setData callback. The flag is used in the editor change handler to
        // prevent model updating before the editor to complete the view update.
        this.editor.setData(propertyChanged.richtextValue, {
          callback: () => {
            this.updating = false;
          }
        });
      }
    }

    this.executeCommonPropertyChangedHandler(propertyChanged);
  }

  editorDataChangeHandler(evt) {
    this.idocEditorChangeListener.updateListStyles(evt.editor);

    let currentEditorData = evt.editor.getData();
    let currentRichValue = this.getRichtextValue();
    let isSameData = currentEditorData === currentRichValue;

    // The property value is set to stripped data in order validation to work as expected. Original not value is
    // placed back before save.
    if (!isSameData && !this.updating) {
      // Handle behavior of the calculation validator (static value suggestions)
      if (this.isControl(DEFAULT_VALUE_PATTERN)) {
        this.fieldViewModel.editedByUser = true;
      }

      // Setting the model value triggers the validation process.
      this.validationModel[this.identifier].value = this.stripFormatting(currentEditorData);

      this.setRichtextValue(currentEditorData);
    }
  }

  makeReadonly(isPreview) {
    this.editor.setReadOnly(isPreview);
  }

  stripFormatting(data) {
    return ModelUtils.stripHTML(data);
  }

  setRichtextValue(richtext) {
    this.validationModel[this.identifier].richtextValue = richtext;
  }

  getRichtextValue() {
    return this.validationModel[this.identifier].richtextValue;
  }

  static getEditorConfig() {
    return PluginRegistry.get(EDITOR_TOOLBAR)[0].data;
  }

  getCompositeFieldId() {
    return this.compositeFieldId;
  }

  notifyWhenReady() {
    return true;
  }

  ngOnDestroy() {
    this.editor && this.editor.destroy(true);

    super.ngOnDestroy();
  }
}