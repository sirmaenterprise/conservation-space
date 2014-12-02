var VerticalSplitterX = 0;
var VerticalSplitterY = 0;
var VerticalSplitterCount = 0;
var VerticalSplitterTimer;
var VerticalSplitterTracking = false;
	
var VerticalSplitter = (function() {
	// Vertical Splitter v 1.2.0
	var config = {
		containerId: "",
		firstItemId: "",
		secondItemId: "",
		splitterId: "splitter",
		splitterBehaviour: "track", // track (manually resize) or none (expand/collapse)
		splitterWidth: 10
	};
	
	return {
		ImportSettings: function (settings) {
			if (settings.containerId) config.containerId = settings.containerId;
			if (settings.firstItemId) config.firstItemId = settings.firstItemId;
			if (settings.secondItemId) config.secondItemId = settings.secondItemId;
			if (settings.splitterId) config.splitterId = settings.splitterId;
			if (settings.splitterBehaviour) config.splitterBehaviour = settings.splitterBehaviour;
			if (settings.splitterCount) config.splitterCount = settings.splitterCount;
			if (settings.splitterWidth) config.splitterWidth = settings.splitterWidth;
		},
		SetUpElement: function (settings) {
		
			if (settings) { this.ImportSettings(settings); }

			var splitterId = config.splitterId + VerticalSplitterCount;

			var container = document.getElementById(config.containerId);
			var leftItem = document.getElementById(config.firstItemId);
			var rightItem = document.getElementById(config.secondItemId);
			
			var leftItemWidth = leftItem.offsetWidth - config.splitterWidth;
			var splitterHandleWidth = config.splitterWidth;
			
			var remainingWidth = (container.offsetWidth - leftItemWidth) - config.splitterWidth;			
			var height = Math.max(leftItem.offsetHeight, rightItem.offsetHeight);
			
			var leftContainer = this.CreateResizableContainer(leftItemWidth, leftItem);
			var rightContainer = this.CreateResizableContainer(remainingWidth, rightItem);
			
			var splitterHandle = document.createElement("div");
			splitterHandle.id = splitterId;
			splitterHandle.className = config.splitterId;
			splitterHandle.style.cssFloat = "left";
			splitterHandle.style.styleFloat = "left";
			splitterHandle.style.width = splitterHandleWidth + "px";
			splitterHandle.style.height = height + "px";
			splitterHandle.innerHTML = "<hr>";
			if (config.splitterBehaviour == "track") {
				splitterHandle.onmousedown = function () { VerticalSplitter.StartWithTracking(leftContainer, rightContainer, container, splitterHandle); return false; }
			} else {
				splitterHandle.onmousedown = function () { VerticalSplitter.Start(leftContainer, rightContainer, container, splitterHandle); return false; }
			}
			splitterHandle.ondraggesture = function () { return false; }
			splitterHandle.ondrag = function () { return false; }
			
			container.innerHTML = "";
			container.appendChild(leftContainer);
			container.appendChild(splitterHandle);
			container.appendChild(rightContainer);
			
			this.AddEventHandler(window, "resize", function () { VerticalSplitter.Resize(container, leftContainer, rightContainer); });
			
			if (config.splitterBehaviour == "none") {
				var stateCookie = this.GetCookie("splitterstate" + container.id);
				this.DeleteCookie("splitterstate" + container.id);
				if (typeof(stateCookie) != "undefined") {
					if (stateCookie == "HIDE") {
						VerticalSplitter.Start(leftContainer, rightContainer, container, splitterHandle);
						this.SetCookie("splitterstate" + container.id, "HIDE", 1);
					}
				}
			}
			
			VerticalSplitterCount++;
		},
		CreateResizableContainer: function (width, child) {
			var resizableContainer = document.createElement("div");
			// SV: resizableContainer.style.overflow = "hidden";
			resizableContainer.style.cssFloat = "left";
			resizableContainer.style.styleFloat = "left";
			resizableContainer.style.width = width + "px";
			
			child.style.cssFloat = "none";
			child.style.styleFloat = "none";
			child.style.width = "auto";
			child.style.margin = "0";
			resizableContainer.appendChild(child);
			
			return resizableContainer;
		},
		Start: function (leftItem, rightItem, container, splitter) {
	        // This implementation is for open/close, without tracking
			this.DeleteCookie("splitterstate" + container.id);
			if (leftItem.offsetWidth > 0) {
				leftItem.title = leftItem.offsetWidth;
				leftItem.style.width = 0;
				this.Resize(container, leftItem, rightItem);
				this.SetCookie("splitterstate" + container.id, "HIDE", 1);
			} else {
			    leftItem.style.width = leftItem.title + "px";
			    this.Resize(container, leftItem, rightItem);
			    this.SetCookie("splitterstate" + container.id, leftItem.title, 1);
			}
			return false;
		},
		StartWithTracking: function (leftItem, rightItem, container, splitter) {
			if (!VerticalSplitterTracking) {
				VerticalSplitterTracking = true;
				document.getElementsByTagName("body")[0].style.cursor = "e-resize";
				VerticalSplitterTimer = window.setTimeout(function () {VerticalSplitter.Track(container, splitter, leftItem, leftItem.offsetWidth, rightItem, rightItem.offsetWidth, VerticalSplitterX); }, 50);
			}
			return false;
		},
		Track: function (container, splitter, leftItem, firstItemWidth, rightItem, secondItemWidth, originalPosition) {
			if (VerticalSplitterTracking) {
				
				var movement = VerticalSplitterX - originalPosition;
			
				var leftItemWidth = firstItemWidth + movement;
				
				// TODO: this is fast hack for restricting
				// left width and will be removed
				if(leftItemWidth+40 > 600 || leftItemWidth < 100){
					return;
				}
				
				// Width cannot be less than 0
				if (leftItemWidth < 0) {
					leftItemWidth = 0;
				}
				
				// Width can't be more than the container width
				var remainingWidth = container.offsetWidth - config.splitterWidth;
				if (leftItemWidth > remainingWidth) {
					leftItemWidth = remainingWidth;
				}
				
				var remainingWidth = remainingWidth - leftItemWidth;
				if (remainingWidth < 0) {
					remainingWidth = 0;
				}
				
				splitter.style.height = Math.max(leftItem.offsetHeight, rightItem.offsetHeight) + "px";
				leftItem.style.width = leftItemWidth + "px";
				rightItem.style.width = remainingWidth + "px";

				VerticalSplitterTimer = window.setTimeout(function () {VerticalSplitter.Track(container, splitter, leftItem, firstItemWidth, rightItem, secondItemWidth, originalPosition); }, 50);
			}
		},
		Stop: function () {
			if (VerticalSplitterTracking) {
				VerticalSplitterTracking = false;
				document.getElementsByTagName("body")[0].style.cursor = "auto";
				window.clearTimeout(VerticalSplitterTimer);
				return false;
			}
		},
		Resize: function (container, leftItem, rightItem) {
			var remainingWidth = container.offsetWidth - config.splitterWidth;
			var leftWidth = leftItem.offsetWidth;
			if (leftWidth > remainingWidth) {
				leftWidth = remainingWidth;
			}
			var rightWidth = remainingWidth - leftWidth;
			
			leftItem.style.width = leftWidth + "px";
			rightItem.style.width = rightWidth + "px";
		},
		AddEventHandler: function (target, eventName, handlerName) {
			if (target.addEventListener) {
				target.addEventListener(eventName, handlerName, false);
			} else if ( target.attachEvent ) {
				target.attachEvent("on" + eventName, handlerName);
			} else {
				target["on" + eventName] = handlerName;
			}
		},
		SetCookie: function (name, value, daysToSurvive) {
            var expires = new Date();
            expires.setTime(expires.getTime() + (daysToSurvive*24*60*60*1000));

            var value = escape(value) + "; path=/; expires=" + expires.toUTCString();
            document.cookie = name + "=" + value;
        },
        GetCookie: function (name) {
            var i, x, y;
            var cookies = document.cookie.split(";");
            for (i = 0; i < cookies.length; i++) {
                x = cookies[i].substr(0,cookies[i].indexOf("="));
                x = x.replace(/^\s+|\s+$/g,"");
                if (x == name) {
                    y = cookies[i].substr(cookies[i].indexOf("=") + 1);
                    return unescape(y);
                }
            }
        },
		DeleteCookie: function (name) {
			document.cookie = name + '=; path=/; expires=Thu, 01-Jan-70 00:00:01 GMT;';
		} 
	};
}());

document.body.onmousemove = function (e) {
	if (!e) var e = window.event;
	if (e.pageX || e.pageY) {
		VerticalSplitterX = parseInt(e.pageX, 10);
		VerticalSplitterY = parseInt(e.pageY, 10);
	} else if (e.clientX || e.clientY) {
		VerticalSplitterX = e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
		VerticalSplitterY = e.clientY + document.body.scrollTop + document.documentElement.scrollTop;
	}
};

document.body.onmouseup = VerticalSplitter.Stop;