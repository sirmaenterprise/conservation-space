/*!
 * DOM elements observer jQuery plugin. Runs under Chrome browser only.
 * It's used to observe event listeners attached to DOM elements and all elements' children.
 * Useful to track for event listeners in detached DOM trees.
 *
 * COMMON USAGE:
 * 1. Go to start state web page.
 * 2. Invoke GC and take Heap Snapshot in chrome incognito window.
 * 3. Do some work.
 * 4. Return to start state. (Step 1)
 * 5. Invoke GC and take second Heap Snapshot.
 * 6. Repeat steps 3,4 and 5.
 * 7. Do make comparsion between snapshot 2 and 3.
 * 8. Examine detached Dom trees and decide which element will you observe.
 * 9. In the console type your dom elements observer command
 *
 * EXAMPLES:
 * Attaches to all div elements and observes dom changes (child elements included)
 * $('div').startObservation();
 *
 * Attaches to all div elements and observes dom changes (child elements excluded)
 * $('span').startObservation(false);
 *
 * Attaches to all body elements and observes dom changes with attributes changes (child elements excluded)
 * $('body').startObservation(false, {attributes: true, childList: true, characterData: true, attributeOldValue: true, characterDataOldValue: true});
 *
 * DESTROY:
 * Refresh the browser's tab or close it.
 *
 * WARNINGS:
 * DOM elements observer creates memory leaks! Do not take Heap Snapshots when started.
 * Do next element observation but destroy the started one first!
 */

(function ($, window) {
  'use strict';

  // IE11 returns undefined for window.chrome
  // Opera 18+ outputs true for window.chrome, so we need to check if window.opr
  // Edge outputs to true for window.chrome
  let isChromium = window.chrome;
  let winNav = window.navigator;
  let vendorName = winNav.vendor;
  let isOpera = typeof window.opr !== "undefined";
  let isEdge = winNav.userAgent.indexOf("Edge") > -1;

  if (isChromium === null || typeof isChromium === "undefined" || vendorName !== "Google Inc." || isOpera === true || isEdge === true) {
    return 'Run DomElementsObserver under Chrome browser only!';
  }

  let elementSelector;
  let observeChildren;

  /*! Mutation Observer options object
   {
   attributes: boolean,               // Set to true if mutations to target's attributes are to be observed.
   childList: boolean,                // Set to true if additions and removals of the target node's child elements (including text nodes) are to be observed.
   characterData: boolean,            // Set to true if mutations to target's data are to be observed.
   attributeOldValue: boolean,        // Set to true if attributes is set to true and target's attribute value before the mutation needs to be recorded.
   characterDataOldValue: boolean,    // Set to true if characterData is set to true and target's data before the mutation needs to be recorded.
   attributeFilter: Array,            // Set to an array of attribute local names (without namespace) if not all attribute mutations need to be observed.
   subtree: boolean                   // Set to true if mutations to target and target's descendants are to be observed.
   }
   */
  let mutationObserverOptions;

  $.fn.startObservation = function (doObserveChildren, observerOptions) {
    observeChildren = doObserveChildren || true;
    elementSelector = this;
    mutationObserverOptions = observerOptions;

    $(window).on('load', observeElements(elementSelector.get()));
    return 'DomElementsObserver attached and started. Refresh browser to disable.';
  };

  function observeElements(elements) {
    elements.forEach((element) => {
      let observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
          observeElement(element, mutation);
        });
      });

      if (mutationObserverOptions) {
        observer.observe(element, mutationObserverOptions);
      } else {
        observer.observe(element, {
          attributes: false,
          childList: true,
          characterData: true,
          attributeOldValue: false,
          characterDataOldValue: true
        });
      }
    });
  }

  // Logs object attached events and mutations.
  function observeElement(element, mutation) {
    if ($._data($(element)[0], 'events')) {
      console.groupCollapsed('Events in ', element.tagName, $._data($(element)[0], 'events'));
      console.log('Element:');
      console.log(element);
      console.log('Mutation data:');
      console.log(mutation);
      console.groupEnd();
    }

    let children = element.children;
    if (children.length > 0 && observeChildren === true) {
      for (let i = 0; i < children.length; i++) {
        observeElement(children[i], mutation);
      }
    }
  }
})(jQuery, window);
