;
(function($, window, document, undefined) {

	var pluginName = 'tasktree';

	// defaults
	var defaults = {
		treeStringElements : {
			expand : '<span class="toggler expanded">&nbsp;</span>',
			node   : '<span class="node-link">&nbsp;</span>'
		}

	};

	/**
	 * Plugin constructor.
	 */
	function Tasktree(container, options) {

		this.plugin = container;

		// merge the default options with provided once
		this.options = $.extend({}, defaults, options);

		this._defaults = defaults;

		this._name = pluginName;

		// call init to build the plugin view and handlers
		this.init();
	}

	/**
	 * Initialize the plugin.
	 */
	Tasktree.prototype.init = function() {
		this.buildPluginContainer(JSON.parse(this.options.data));
		this.bindHandlers();
	};

	/**
	 * Creates the plugin container DOM.
	 */
	Tasktree.prototype.buildPluginContainer = function(data) {
        var root = $('<ul class="root tasktree-panel"></ul>').appendTo(this.plugin);
        var tree = this.buildTree(data, root);
	};

	Tasktree.prototype.buildTree = function(data, tree, parentFlag) {
		var parentFlag = parentFlag || null;
        var len = data.length;
		for ( var i = 0; i < len; i++) {
            var item = data[i];
			if (item.children) {
                var li = this.getLI(item);
                var span = this.getSPAN(item);
                var ul = this.getUL(item);
                if(parentFlag){ 
                	li.append(this.options.treeStringElements.node);
                }
					li.append(this.options.treeStringElements.expand);
                li.append(span);
                li.append(ul);
                tree.append(li);
                this.buildTree(item.children, ul, true);
			} else {
                var li = this.getLI(item);
                var span = this.getSPAN(item);
                if(parentFlag){
                	span.prepend(this.options.treeStringElements.node);
                }
                li.append(span);
				tree.append(li);
			}
		}
		var shorteningElementFitler = $('.data').find(':first-child:first');
		EMF.shorteningPlugin.shortElementText(shorteningElementFitler);
	};
	
    Tasktree.prototype.getUL = function(item) {
        var node = $('<ul class="node"></ul>').addClass('node');
        return node;
    };     
    
    Tasktree.prototype.getLI = function(item) {
        var node = $('<li class="item"></li>');
        return node;
    };    
    
    Tasktree.prototype.getSPAN = function(item) {
        var row = $('<span class="row"></span>');
        var icon = $('<span class="icon"></span>');
        var data = $('<span class="data"></span>').html(item.header);
        row.append(icon);
        row.append(data);
        return row;
    };

	/**
	 * Binds the event handlers.
	 */
	Tasktree.prototype.bindHandlers = function() {
		var pluginContainer = this.plugin;

	};

	/**
	 * Checks if the provided container id exists and the container itself is in
	 * the DOM.
	 */
	Tasktree.prototype.checkPluginContainer = function() {

		var containerIsOk = true;
		// if container is not provided then return with error
		if (!this.container || $(this.container).length === 0) {
			console.log('Error! Container id must be provided!');
			containerIsOk = false;
		}
		return containerIsOk;
	};

	/**
	 * Extend jQuery with our function. The plugin is instantiated only once
	 * (singleton).
	 */
	$.fn[pluginName] = function(options) {

		var pluginObject = $.data(this, 'plugin_' + pluginName);
		if (!pluginObject) {
			pluginObject = $.data(this, 'plugin_' + pluginName, new Tasktree(
					this, options));
		}
		return pluginObject;
	};

})(jQuery, window, document);