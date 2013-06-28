/**
 * Post and rename from GateIn eXo.js 
 */
var Wrapper = {};

/**
 * This method is internal used for Juzu to simulate requirejs
 * 2 difference method signs
 * require(depName)
 * require(array, callback) 
 */
Wrapper.require = function() {
	if (arguments.length == 1) {
		//Wrapper.define.names and Wrapper.define.deps are defined in JS wrapped
		var ctxDepNames = Wrapper.define.names;
		var ctxDeps = Wrapper.define.deps;
		
		var idx = Wrapper.inArray(ctxDepNames, arguments[0]);
		if (idx !== -1) {
			return ctxDeps[idx];
		} else {
			return window.require(arguments[0]);
		}
	} else {
		return Wrapper.define.apply(this, arguments);
	}
};

Wrapper.require.config = require.config;
Wrapper.require.undef = require.undef;
Wrapper.require.toUrl = require.toUrl;

/**
 * This method is internal used for Juzu to simulate requirejs
 * 3 difference method signs
 * define(name, array, callback)
 * define(array, callback)
 * define(callback) 
 */
Wrapper.define = function() {
	var reqList = [], callback = null;
	
	if (arguments.length == 1) {
		callback = arguments[0];
		if (callback instanceof Function) {
			reqList = ["require", "exports", "module"];
		}
	} else {		
		for (var i = 0; i < arguments.length; i++) {	
			var arg = arguments[i];
			if (arg instanceof Array) {
				reqList = arg;
			} else if (arg instanceof Function) {
				callback = arg;
			}
		}
	}
	
	//Wrapper.define.names and Wrapper.define.deps are defined in JS wrapped
	var ctxDepNames = Wrapper.define.names;
	var ctxDeps = Wrapper.define.deps;
	
	var deps = [];	
	for (var i = 0; i < reqList.length; i++) {
		var idx = Wrapper.inArray(ctxDepNames, reqList[i]);
		if (idx !== -1) {
			deps[i] = ctxDeps[idx];
		} else {
			deps[i] = null;
		}
	}
	
	var result;
	if (callback instanceof Function) {
		var result = callback.apply(this, deps);
		if (!result) {
			var idx = Wrapper.inArray(reqList, "module");
			if (idx !== -1) {
				result = deps[idx].exports;
			} else if ((idx = Wrapper.inArray(reqList, "exports")) != -1) {
				result = deps[idx];
			}			
		}
	} else {
		result = callback;
	}
	return result;
};

//IE doesn't support Array#indexOf
Wrapper.inArray = function(arr, itm) {
	if (!arr) return -1;
	for (var i = 0; i < arr.length; i++) {
		if (arr[i] === itm) {
			return i;
		} 
	}
	return -1;
};