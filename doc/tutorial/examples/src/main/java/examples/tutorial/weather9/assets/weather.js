$(function () {
  $('.collapse').on('show', function () {
    var location = $(this).attr("id");
    $(this).closest(".accordion-group").find(".accordion-inner").each(function () {
      $(this).jzLoad("Weather.getFragment()", {"location":location});
    });
  });
  $(".collapse").collapse();
});
