$(function() {

  // Search function
  var search = function(elt) {
    var listAction = elt.BookingApplication().list();
    elt.$find(".result").load(listAction, {search : elt.$find(".search").val(), size : elt.$find(".size").val(), page : elt.$find(".page").val()}, function() {
      // does not seem used
      // $('#content').css('visibility', 'visible');
    })
  };

  // Events handler
  $('.jz').on("click", ".submit", function() {
    this.$find('.page').val(0);
    search(this);
  });
  $(".jz").on("keyup", ".search", function() {
    this.$find('.page').val(0);
    search(this);
  });
  $('.jz').on("click", ".nextPage", function(e) {
    var p = $(this).attr('href');
    this.$find('.page').val(p);
    e.preventDefault();
    search(this);
  });
});
