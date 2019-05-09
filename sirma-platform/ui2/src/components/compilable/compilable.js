import {View, Inject, Component, NgElement, NgCompile, NgScope} from 'app/app';

/**
 * Simple wrapper of any html fragment which needs to be compiled and embedded somewhere in the application.
 * Useful when some plane html which contains angular directives needs to be embedded in the DOM and those
 * directives to be initialized.
 * What it does - just compiles the provided html and appends it to the wrapper element.
 */
@Component({
  selector: 'seip-compilable',
  properties: {
    'html': 'html'
  }
})
@View({
  template: '<div class="seip-compilable"></div>'
})
@Inject(NgElement, NgCompile, NgScope)
export class Compilable {

  constructor($element, $compile, $scope) {
    this.$element = $element;
    this.$compile = $compile;
    this.$scope = $scope;
  }

  ngOnInit() {
    if (this.html) {
      this.innerScope = this.$scope.$new();
      let link = this.$compile(this.html)(this.innerScope);
      this.$element.append(link);
    }
  }
}