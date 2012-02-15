var p=HTMLElement.prototype;
p.jz=function(){
  var e=this;
  while(e!=null&&(" "+e.className+" ").indexOf(" jz ")<0){
    e=e.parentNode;
  }
  return e;
};
p.foo=function(k){
  var e=this.jz();
  if(e!=null){
    var n=e.getElementsByTagName("div");
    for(var i=0;i<n.length;i++){
      var c=n[i];
      var d=c.getAttribute("data-method-id");
      if(d==k){
        return c.getAttribute("data-url");
      }
    }
  }
  return k;
};
p.$=function(){return $(this.jz());};
p.$find=function(){
  var elt=this.$();
  return elt.find.apply(elt, arguments);
};
