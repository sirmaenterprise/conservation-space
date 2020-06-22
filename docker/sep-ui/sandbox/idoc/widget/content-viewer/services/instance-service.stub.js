import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(PromiseAdapter)
export class InstanceRestService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;

    this.files = {
      'pdf': '/sandbox/idoc/widget/content-viewer/documents/document.pdf',
      'image': '/sandbox/idoc/widget/content-viewer/documents/image.png',
      'video': '/sandbox/idoc/widget/content-viewer/documents/video.ogg',
      'audio': '/sandbox/idoc/widget/content-viewer/documents/audio.mp3'
    };
  }

  loadBatch() {
    return this.promiseAdapter.resolve({data: []});
  }

  getContentPreviewUrl(id) {
    return this.files[id];
  }

  getContentDownloadUrl(id) {
    return this.files[id];
  }

  preview(id) {
    return this.promiseAdapter.promise((resolve) => {
      let blob;
      let xhr = new XMLHttpRequest();
      xhr.open('GET', this.files[id]);
      xhr.responseType = 'blob';
      xhr.onload = () => {
        blob = xhr.response;
        var reader = new FileReader();
        reader.onloadend = () => {
          resolve({data: reader.result, headers: () => {}});
        };
        reader.readAsArrayBuffer(blob);
      };
      xhr.send();
    });
  }
}
