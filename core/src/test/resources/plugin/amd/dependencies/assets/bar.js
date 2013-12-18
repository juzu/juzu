define("Bar", ["Foo"], function(foo) {
	return {
    text : "<bar>" + foo.text + "</bar>"
  };
});