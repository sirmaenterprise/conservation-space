/**
 * Patch for fixing events related issues with ExtJS 4.2 and Chrome 43.
 * See https://www.sencha.com/forum/showthread.php?301116-Submenus-disappear-in-Chrome-43-beta for details 
 */

Ext.apply(Ext.EventManager,{      
      normalizeEvent: function(eventName, fn) {
            //start fix
            var EventManager = Ext.EventManager,
                  supports = Ext.supports;
            if(Ext.chromeVersion >=43 && eventName == 'mouseover'){
                  var origFn = fn;
                  fn = function(){
                        var me = this,
                              args = arguments;
                        setTimeout(
                              function(){
                                    origFn.apply(me || Ext.global, args);
                              },
                        0);
                  };
            } 
            //end fix
            if (EventManager.mouseEnterLeaveRe.test(eventName) && !supports.MouseEnterLeave) {
                  if (fn) {
                        fn = Ext.Function.createInterceptor(fn, EventManager.contains);
                  }
                  eventName = eventName == 'mouseenter' ? 'mouseover' : 'mouseout';
            } else if (eventName == 'mousewheel' && !supports.MouseWheel && !Ext.isOpera) {
                  eventName = 'DOMMouseScroll';
            }
            return {
                  eventName: eventName,
                  fn: fn
            };
      }
});