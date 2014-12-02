var IDocPrint = EMF || {};

/** Selectors used for the print. */
IDocPrint.selectors = {
	iDocEditor : '.idoc-editor',
	elementsToRemove : ['.idoc-comments-column', '.idoc-left-column', '#topHeader', '#header', '.tree-header.breadcrumb_header', 
	                    '.idoc-middle-column.pull-left.idoc-first-row', '.idoc-middle-column.pull-left', '.pull-left.text-center']

};

IDocPrint.print = {
	
	/**
	 * Modifies the page before printing it.
	 */
	modifyPrintPage: function(page) {
		// iterate all the elements that have to be removed from the page before the print and remove them.
		for (var i=0,  size = IDocPrint.selectors.elementsToRemove.length; i < size; i++) {
			  $(IDocPrint.selectors.elementsToRemove[i], page).remove();
		}
        // Apply additional styles.
        $(IDocPrint.selectors.iDocEditor, page).css('width', '80%');
        $(IDocPrint.selectors.iDocEditor, page).css('max-width', 'none');
        $(IDocPrint.selectors.iDocEditor, page).remove('font-size', '14px');
        $(IDocPrint.selectors.iDocEditor, page).remove('margin-left', '5%');
        $(IDocPrint.selectors.iDocEditor, page).remove('margin-right', '10%');
        $(IDocPrint.selectors.iDocEditor, page).remove('margin-top', '5%');
        $(IDocPrint.selectors.iDocEditor, page).remove('margin-bottom', '5%');
        // Fix for: https://ittruse.ittbg.com/jira/browse/CMF-7262
        $('a', page).removeAttr( "href" );
	},
	
	/**
	 * Executes the print. If browser is chrome or ff, load hidden frame. If IE modify current page and reload.
	 */
	executePrint: function(instanceLink) {
		var broswerType = $('html').attr('data-useragent');
		var msie = broswerType.indexOf("IE"),
			msie11 = broswerType.indexOf('IE 11') > -1;

		// If not IE create frame with source as the iDoc.
		if (msie < 0) {
			var frameSelector = '#print-iframe';
			var frameId = 'print-iframe';
			var iframeStyle = 'border:0;position:absolute;width:0px;height:0px;left:0px;top:0px;';
			var iframe;

			try {
				iframe = document.createElement('iframe');
				$(iframe).attr({ 
					style: iframeStyle, 
					id: frameId, 
					src: instanceLink
				});
				document.body.appendChild(iframe);
				iframe.doc = null;
				iframe.doc = iframe.contentDocument ? iframe.contentDocument : ( iframe.contentWindow ? iframe.contentWindow.document : iframe.document);
				if ( iframe.doc == null ) throw "Cannot find document.";
	        
				iframe.onload=function(){
					var tmp = iframe.doc;
					var tmp = (iframe.contentWindow || iframe.contentDocument);
					if (tmp.document) {
						tmp = tmp.document;
					}
	            
					$('html', tmp).addClass('print-override-overflow');
					IDocPrint.print.modifyPrintPage(tmp);
					var content = $('#idoc-editor', tmp);
					if(content && content.length){
						$('#content', tmp).replaceWith(content);
					}	            
					var _window = document.getElementById("print-iframe").contentWindow;
					function callPrint() {
						var ibody = $(tmp).find('body');
						var ready = ibody.hasClass('pace-done');
						if (ready) {
							_window.focus();
							_window.print();
						} else {
							setTimeout(function() {
								callPrint();
							}, 0);
						}
					}
					
					callPrint();
					
				};
			} catch( e ) { 
				throw e + ". iframes may not be supported in this browser."; 
			}
		} else {
			var wholePage = $('html');
			$('html').addClass('print-override-overflow');
			IDocPrint.print.modifyPrintPage(wholePage);
			var content = $('#idoc-editor');
			$('#content').replaceWith(content);

			window.onafterprint = function(link, ie11) {
				return function() {
					if (ie11) {
						window.location = link;
					} else {
						window.location.href = link;
						window.location.reload(true);
					}
				}
			}(instanceLink, msie11);
			
			window.print();
		}
	}

};