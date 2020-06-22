import {Component,Inject,NgElement,NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {Eventbus} from 'services/eventbus/eventbus';
import {MODE_PRINT} from 'idoc/idoc-constants';
import Split from 'nathancahill/Split.js';
import './splitter.css!css';

@Component({
  selector: '[seip-splitter]',
  properties: {
    'config': 'config'
  }
})
@Inject(NgScope, NgElement, StateParamsAdapter, Eventbus)
export class Splitter extends Configurable {
  constructor($scope, element, stateParamsAdapter, eventbus) {
    let defaultConfig = {
      selectors: {},
      options: {
        paneClass: '.split',
        gutterSize: 5
      }
    };
    super(defaultConfig);
    this.$scope = $scope;
    this.element = element;
    this.eventbus = eventbus;

    if (stateParamsAdapter.getStateParam('mode') !== MODE_PRINT) {
      this.registerCondition();
    }
  }

  /**
   * In order to init Split.js a given condition should be fulfilled that is passed through the component config
   */
  registerCondition() {
    if (!this.config.commands) {
      throw new Error('You should provide callbacks that will be used in the command watchers');
    }
    this.registerInitWatch();
  }

  init() {
    // wait one loop in order to have the element child elements loaded
    setTimeout(()=> {
      let sizes = this.config.setupSizes.callback.apply(null, this.config.setupSizes.arguments);
      this.config.options.sizes = this.calculatePaneSizes(sizes.sizes);
      this.config.options.minSize = sizes.minSize;
      this.config.selectors = this.element.find(this.config.options.paneClass);
      this.splitter = Split(this.config.selectors, this.config.options);
    }, 1);
  }

  registerInitWatch() {
    let initWatcher = this.$scope.$watch(() => {
      return this.config.commands.init();
    },
    (newValue)=> {
      if (newValue) {
        this.init();
        if (this.config.commands.destroy) {
          this.registerDestroyWatch();
        }
        initWatcher();
      }
    });
  }

  destroy() {
    this.splitter.destroy();
  }

  registerDestroyWatch() {
    this.$scope.$watchCollection(()=> {
      return this.config.commands.destroy();
    }, (newValue, oldValue)=> {
      if (newValue !== oldValue) {
        this.destroy();
        this.init();
      }
    });
  }

  calculatePaneSizes(sizes) {
    let calculatedSizes = [];
    let takenSpace = 0;
    let autoPosition = -1;
    sizes.forEach((value, index)=> {
      if (value === Splitter.AUTO) {
        autoPosition = index;
      } else {
        takenSpace += this.parseSize(value);
        calculatedSizes.push(value);
        takenSpace += this.config.options.gutterSize;
      }
    });
    // auto keyword has been found in the passed sizes
    if (autoPosition !== -1) {
      let widthOfAutoPane = 'calc(100% - ' + takenSpace + 'px)';
      calculatedSizes.splice(autoPosition, 0, widthOfAutoPane);
    }

    return calculatedSizes;
  }

  parseSize(value) {
    return parseInt(value);
  }

}

Splitter.AUTO = 'auto';
