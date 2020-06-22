import {Inject, Component, View, NgElement,NgTimeout} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';
import filesize from 'filesize';
import classIconsUploadTemplate from './class-icons-upload.html!text';
import './class-icons-upload.css!css';

export const MAX_SIZE_EXCEEDED = 'fileupload.maxSizeExceeded';
export const WRONG_FILE_TYPE = 'fileupload.wrongFileType';

@Component({
  selector: 'class-icons-upload',
  properties: {
    'config': 'config'
  }
})
@View({
  template: classIconsUploadTemplate
})
@Inject(NgElement, TranslateService, NgTimeout)
export class ClassIconsUpload {
  constructor(element, translateService, $timeout) {
    this.element = element;
    this.translateService = translateService;
    this.$timeout = $timeout;
    this.icons = [{label: 'iconupload.16x16header', isMandatory: true, size: 16},
      {label: 'iconupload.24x24header', size: 24},
      {label: 'iconupload.32x32header', size: 32},
      {label: 'iconupload.64x64header', isMandatory: true, size: 64}];
    this.fileReader = new FileReader();
  }

  ngAfterViewInit() {
    this.fileReader.onload = this.handleFileConversion.bind(this);
    this.element.find('.icon-upload-field').change(this.handleChangeIconEvent.bind(this));
  }

  openInput(index) {
    this.element.find('.icon-upload-field')[index].click();
  }

  /**
   * Removes selected icon. Then checks if the removed icon is not from mandatory field.
   *
   * @param index the index of the uploader.
   */
  removeIcon(index) {
    delete this.icons[index].file;
    this.checkMandatoryIcons();
  }

  /**
   * Checks if the selected file is iamge and its size is valid and converts it in base64 format.
   *
   * @param event the change event
   */
  handleChangeIconEvent(event) {
    //the index of the uploader
    this.lastSelectedIndex = $('.icon-uploader input').index(event.target);

    let selectedFile = event.target.files[0];
    if (selectedFile) {
      let maxSize = this.config.maxFileSize;
      this.$timeout(()=> {
        if (maxSize && selectedFile.size > maxSize) {
          this.icons[this.lastSelectedIndex].error = this.translateService.translateInstantWithInterpolation(MAX_SIZE_EXCEEDED, {
            max_size: filesize(maxSize)
          });
          this.removeIcon(this.lastSelectedIndex);
        } else if (selectedFile.type.indexOf('image') < 0 || selectedFile.type.indexOf('icon') >= 0) {
          this.icons[this.lastSelectedIndex].error = this.translateService.translateInstantWithInterpolation(WRONG_FILE_TYPE, {
            file_type: 'image'
          });
          this.removeIcon(this.lastSelectedIndex);
        } else {
          this.icons[this.lastSelectedIndex].name = selectedFile.name;
          this.fileReader.readAsDataURL(selectedFile);
          delete this.icons[this.lastSelectedIndex].error;
        }
      });
    }
  }

  /**
   * Obtains the image in base64 format and triggers a digest cycle in order to render it.
   *
   * @param event the conversion finished event
   */
  handleFileConversion(event) {
    this.$timeout(()=> {
      this.icons[this.lastSelectedIndex].file = event.target.result;
      this.checkMandatoryIcons();
    });
  }

  /**
   * Enables the upload button if the mandatory icons are selected.
   */
  checkMandatoryIcons() {
    this.config.buttons[0].disabled = !this.icons[0].file || !this.icons[3].file;
  }
}