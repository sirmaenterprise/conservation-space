import {Component, Inject, NgElement} from 'app/app';
import {NavigatorAdapter} from 'adapters/navigator-adapter';

@Component({
  selector: '[seip-resizable-textarea]',
  properties: {
    'alignTop': 'align-top',
    'fieldViewModel': 'field-view-model'
  }
})
@Inject(NgElement)
export class ResizableTextarea {
  constructor($element) {
    this.$element = $element;
    if (NavigatorAdapter.isEdgeOrIE()) {
      this.init();
    }
  }

  /**
   * Initializes the resizable. It's styles are initialized and resizable is created.
   * Different widths are set on the element, depending on the browser it is created, because of visual differences.
   */
  init() {
    setTimeout(() => {
      let parentElement = $(this.$element[0].parentElement);
      // added to remove ugly grey scrolls around textareas in DTW under IE
      this.$element.css('overflow', 'hidden');
      this.$element.resizable({
        handles: 's',
        minWidth: 34,
        // resizable slightly change element width so after create it should be set again to 100%
        create: () => {
          let uiWrapper = this.$element.closest('.ui-wrapper');
          //edge needs a slightly bigger height.
          this.handleVisibility(!this.fieldViewModel.preview, uiWrapper);
          uiWrapper.css('width', '100%');
          this.fieldViewModelSubscription = this.fieldViewModel.subscribe('propertyChanged', (propertyChanged) => {
            if (Object.keys(propertyChanged)[0] === 'preview') {
              this.handleVisibility(!propertyChanged.preview, uiWrapper);
            }
          });
        },
        resize: () => {
          this.$element.css('min-width', "100%");
          this.$element.css({'height': (parentElement.height() - 2) + 'px'});
        }
      });
    }, 0);
  }

  /**
   * Switches the display property of the element depending on the flag.
   * @param isVisible flag whether the element should be visible or not.
   * @param element element that the style will be applied to.
   */
  handleVisibility(isVisible, element) {
    if (isVisible) {
      element.css('display', 'block');
      if (NavigatorAdapter.isInternetExplorer()) {
        this.$element.css({'width': '100%', 'height': '46px'});
        element.css({'width': '100%', 'height': '46px'});
      } else {
        this.$element.css({'width': '100%', 'height': '50px'});
        element.css({'width': '100%', 'height': '50px'});
      }
    } else {
      element.css('display', 'none');
    }
  }

  ngOnDestroy() {
    if (this.fieldViewModelSubscription) {
      this.fieldViewModelSubscription.unsubscribe();
    }
  }
}