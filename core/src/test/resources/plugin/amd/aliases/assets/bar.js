define("Bar", ["foo"], function(foo) {
  return {
    text : "<bar>" + foo.text + "</bar>"
  };
});