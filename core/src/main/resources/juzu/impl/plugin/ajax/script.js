(function($) {
  $.fn.jz = function() {
    return this.closest(".jz");
  };
  $.fn.jzURL = function(mid) {
    return this.
      jz().
      children().
      filter(function() { return $(this).data("method-id") == mid; }).
      map(function() { return $(this).data("url"); })[0];
  };
  var re = /^(.*)\(\)$/;
  $.fn.jzAjax = function(url, options) {
    if (typeof url === "object") {
      options = url;
      url = options.url;
    }
    var match = re.exec(url);
    if (match != null) {
      url = this.jzURL(match[1]);
      if (url != null) {
        options = $.extend({}, options || {});
        options.url = url;
        return $.ajax(options);
      }
    }
  };
  $.fn.jzLoad = function(url, data, complete) {
    var match = re.exec(url);
    if (match != null) {
      var repl = this.jzURL(match[1]);
      url = repl || url;
    }
    if (typeof data === "function") {
      complete = data;
      data = null;
    }
    return this.load(url, data, complete);
  };
  $.fn.jzFind = function(arg) {
    return this.jz().find(arg);
  }
})(jQuery);
