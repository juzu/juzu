define("Bar", ["Foo"], function(foo) {
	return {
		text : foo.text + " World"
	};
});