import {Injectable, Inject, NgTimeout} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(PromiseAdapter, NgTimeout)
export class ImageService {
  constructor(promiseAdapter, $timeout) {
    this.promiseAdapter = promiseAdapter;
    this.$timeout = $timeout
  }

  getManifest(manifestId) {
    return this.promiseAdapter.promise((resolve) => {
      this.$timeout(() => {
        var response = {
          data: this.returnManifest()
        };
        resolve(response);
        // Small timeout to simulate real searching.
      }, 500);
    });
  }


  createManifest(manifestData) {
    return this.promiseAdapter.promise((resolve) => {
      var response = {
        data: 'http://dms-data.stanford.edu/data/manifests/BnF/jr903ng8662/manifest.json'
      };
      resolve(response);
    });
  }

  updateManifest(manifestData) {
    return this.promiseAdapter.promise((resolve) => {
      resolve(this.returnManifest());
    });
  }

  returnManifest() {
    return {
      "@context": "http://www.shared-canvas.org/ns/context.json",
      "@id": "https://ses.sirmaplatform.com/api/image/manifest/emf:26e42c6b-4127-4c3d-eb6e-8fb7c570ddf9",
      "@type": "sc:Manifest",
      "label": "",
      "sequences": [
        {
          "@id": "https://ses.sirmaplatform.com/api/image/manifest/emf:26e42c6b-4127-4c3d-eb6e-8fb7c570ddf9",
          "@type": "sc:Sequence",
          "label": "Current order",
          "canvases": [
            {
              "@id": "emf:25ab8cc9-dd58-4b6d-a5dd-b123fac22436",
              "@type": "sc:Canvas",
              "label": "Sirma Trade Mark.jpg",
              "height": 292,
              "width": 612,
              "images": [
                {
                  "@id": "http://image/uri1",
                  "@type": "oa:Annotation",
                  "motivation": "sc:painting",
                  "resource": {
                    "@id": "http://resource/uri1",
                    "@type": "dctypes:Image",
                    "format": "image/jpeg",
                    "height": 292,
                    "width": 612,
                    "service": {
                      "@id": "sandbox/idoc/widget/image-widget/"
                    }
                  }
                }
              ]
            }
          ]
        }
      ]
    }
  }
}