import {Component, View} from 'app/app';
import {Splitter} from 'components/splitter/splitter';
import splitterStubTemplate from 'splitter.stub.html!text';

@Component({
  selector: 'idoc-splitter-stub'
})
@View({
  template: splitterStubTemplate
})
class SplitterStub {
  constructor() {

    this.shouldDestroyA = false;

    this.configA = {
      setupSizes: {
        callback: ()=> {
          return {
            sizes: ['200px', Splitter.AUTO, '100px']
          }
        },
        arguments: []
      },
      options: {
        gutterSize: 5
      },
      commands: {
        init: ()=> {
          return this.shouldInitA;
        },
        destroy: ()=> {
          return [this.shouldDestroyA];
        }
      }
    };

    this.shouldInitB = false;
    this.shouldDestroyB = false;

    this.configB = {
      setupSizes: {
        callback: ()=> {
          return {
            sizes: ['200px', Splitter.AUTO]
          }
        },
        arguments: []
      },
      commands: {
        init: ()=> {
          return this.shouldInitB;
        },
        destroy: ()=> {
          return [this.shouldDestroyB];
        }
      }
    };
  }

  initA() {
    this.shouldInitA = true;
  }

  initB() {
    this.shouldInitB = true;
  }

  destroyB() {
    this.shouldDestroyB = true;
  }

}
