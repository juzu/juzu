Object.prototype.toJava = function() {
  var m = JSON.json();
  for (var k in this)
    if (this.hasOwnProperty(k))
      m.set(k, this[k] == null ? null : this[k].toJava());
  return m;
};
Array.prototype.toJava = function() {
  var l = this.length;
  var a = new java.util.ArrayList();
  for (var i = 0;i < l;i++)
    a.add(this[i].toJava());
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
