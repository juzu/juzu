$(function () {

  // Setup an event listener on the .collapse element when it is shown
  $('.collapse').on('show', function () {

    // Get the location attribute from the dom
    var location = $(this).attr("id");

    // Update the .accordion-inner fragment
    $(this).
      closest(".accordion-group").
      find(".accordion-inner").
      each(function () {

      // Load the fragment from the resource controller
      $(this).jzLoad(
        "Weather.getFragment()",
        {"location":location});

    });
  });

  // Setup the collapse component
  $(".collapse").collapse();
});
