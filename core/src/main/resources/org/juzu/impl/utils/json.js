Object.prototype.toJava = function() {
  var m = new org.juzu.impl.utils.JSON();
  for (var key in this)
    if (this.hasOwnProperty(key))
      m.add(key, this[key].toJava());
  return m;
};
Array.prototype.toJava = function() {
  var l = this.length;
  var a = new java.util.ArrayList();
  for (var i = 0;i < l;i++)
    a.add(this[i]);
  return a;
};
String.prototype.toJava = function() {
  return new java.lang.String(this);
};
Boolean.prototype.toJava = function() {
  return java.lang.Boolean.valueOf(this);
};
Number.prototype.toJava = function() {
  return java.lang.Integer(this);
};
