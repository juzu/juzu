$(function() {

  // Search function
  var search = function(elt) {
    var root = $(elt).jz();
    root.find(".result").jzLoad("Hotels.list()", {
      search : $(elt).jzFind(".search").val(),
      size : $(elt).jzFind(".size").val(),
      page : $(elt).jzFind(".page").val()
    }, function() {
      // does not seem used
      // $('#content').css('visibility', 'visible');
    });
  };

  // Events handler
  $("body").on("click", ".jz .submit", function() {
    $(this).jzFind('.page').val(0);
    search(this);
  });
  $("body").on("keyup", ".jz .search", function() {
    $(this).jzFind('.page').val(0);
    search(this);
  });
  $("body").on("click", ".jz .nextPage", function(e) {
    var p = $(this).attr('href');
    $(this).jzFind('.page').val(p);
    e.preventDefault();
    search(this);
  });
});
