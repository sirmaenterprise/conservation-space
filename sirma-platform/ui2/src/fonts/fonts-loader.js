import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {BootstrapService} from 'services/bootstrap-service';
import {NavigatorAdapter} from 'adapters/navigator-adapter';

/**
 * Preloads integrated fonts before application bootstrap to ensure that all fonts will be available for printing.
 */
@Injectable()
@Inject(PromiseAdapter)
export class FontsLoader extends BootstrapService {

  constructor(promiseAdapter) {
    super();
    this.promiseAdapter = promiseAdapter;
  }

  initialize() {
    if (NavigatorAdapter.isChrome()) {
      let fontLoaders = [];
      Object.keys(AVAILABLE_FONTS).forEach((fontFamily) => {
        let fontsData = AVAILABLE_FONTS[fontFamily];
        fontsData.forEach((fontData) => {
          let fontLoader = new FontFace(fontFamily, `url(${fontData.url})`, {
            style: fontData.style,
            weight: fontData.weight
          }).load();
          fontLoaders.push(fontLoader);
        });
      });
      return this.promiseAdapter.all(fontLoaders).then((fonts) => {
        fonts.forEach((font) => {
          document.fonts.add(font);
        });
      }).catch(console.error);
    } else {
      return this.promiseAdapter.resolve();
    }
  }
}

const AVAILABLE_FONTS = {
  'Arimo': [{
    url: '/fonts/arimo/arimo-regular.woff2',
    style: 'normal',
    weight: 400
  }, {
    url: '/fonts/arimo/arimo-italic.woff2',
    style: 'italic',
    weight: 400
  },{
    url: '/fonts/arimo/arimo-bold.woff2',
    style: 'normal',
    weight: 700
  },{
    url: '/fonts/arimo/arimo-bolditalic.woff2',
    style: 'italic',
    weight: 700
  }],

  'Caladea': [{
    url: '/fonts/caladea/caladea-regular.woff2',
    style: 'normal',
    weight: 400
  }, {
    url: '/fonts/caladea/caladea-italic.woff2',
    style: 'italic',
    weight: 400
  },{
    url: '/fonts/caladea/caladea-bold.woff2',
    style: 'normal',
    weight: 700
  },{
    url: '/fonts/caladea/caladea-bolditalic.woff2',
    style: 'italic',
    weight: 700
  }],

  'Carlito': [{
    url: '/fonts/carlito/carlito-regular.woff2',
    style: 'normal',
    weight: 400
  }, {
    url: '/fonts/carlito/carlito-italic.woff2',
    style: 'italic',
    weight: 400
  },{
    url: '/fonts/carlito/carlito-bold.woff2',
    style: 'normal',
    weight: 700
  },{
    url: '/fonts/carlito/carlito-bolditalic.woff2',
    style: 'italic',
    weight: 700
  }],

  'Cousine': [{
    url: '/fonts/cousine/cousine-regular.woff2',
    style: 'normal',
    weight: 400
  }, {
    url: '/fonts/cousine/cousine-italic.woff2',
    style: 'italic',
    weight: 400
  },{
    url: '/fonts/cousine/cousine-bold.woff2',
    style: 'normal',
    weight: 700
  },{
    url: '/fonts/cousine/cousine-bolditalic.woff2',
    style: 'italic',
    weight: 700
  }],

  'Open Sans': [{
    url: '/fonts/open-sans/open-sans-regular.woff2',
    style: 'normal',
    weight: 400
  }, {
    url: '/fonts/open-sans/open-sans-italic.woff2',
    style: 'italic',
    weight: 400
  },{
    url: '/fonts/open-sans/open-sans-bold.woff2',
    style: 'normal',
    weight: 700
  },{
    url: '/fonts/open-sans/open-sans-bolditalic.woff2',
    style: 'italic',
    weight: 700
  }],

  'Tinos': [{
    url: '/fonts/tinos/tinos-regular.woff2',
    style: 'normal',
    weight: 400
  }, {
    url: '/fonts/tinos/tinos-italic.woff2',
    style: 'italic',
    weight: 400
  },{
    url: '/fonts/tinos/tinos-bold.woff2',
    style: 'normal',
    weight: 700
  },{
    url: '/fonts/tinos/tinos-bolditalic.woff2',
    style: 'italic',
    weight: 700
  }]
};
