$.noConflict();
$ = $ || jQuery;

$(function () {

	// Setup an event listener on the .collapse element when it is shown
  $(document).off('show').on('show', '.collapse', function() {
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
	
  $(document).off('click', '.accordion-toggle').on('click', '.accordion-toggle', function() {
	  if (!$(this).data('weather8')) {
		  $(this).closest('.accordion-group').find('.collapse').collapse();
		  $(this).data('weather8', true);
	  } else {
		  $(this).closest('.accordion-group').find('.collapse').collapse('toggle');
	  }
	  return false;
  });
});
