(function ($) {

    $(document).ready(function () {

        //Var to know the number of image in the line
        var counterImg = 0;
        //Var to know the place taken by previous image in the line
        var totalWidthLine = 0;

        function getRangeRandom(min, max) {
            return Math.ceil(Math.random() * (max - min) + min);
        }

        function randSecretBoxWidth() {
            var randBoxNum = getRangeRandom(23, 43);
            //Test if we are on th third image of the line
            if (counterImg >= 2) {
                //The third image of the line fill all the remaining place
                randBoxNum = 100 - totalWidthLine;
                //counter place taken by previous image in the line set to 0
                counterImg = 0;
                totalWidthLine = 0;
            }
            else {
                //Increase counter and the place taken by previous image in the line
                counterImg++;
                totalWidthLine += randBoxNum;
            }
            //Return the width of the secret box
            return randBoxNum;
        }

        //Get all secrets boxes
        $('.secret').each(function(idx, listItem) {
          //Set a random width
          listItem.style.width = randSecretBoxWidth() + "%";          
        });
    });

    //Ajax for managing like function
    $(document).on('click.juzu.secret.addLike', '.btn-like', function () {
        var jLike = $(this);
        var jSecret = jLike.closest('.secret');
        var secretId = jSecret.attr('data-secretId');

        jLike.jzAjax('JuZcretApplication.addLike()', {
            data: {'secretId': secretId},
            success: function (data) {
                //jSecret.find('.like-list').html(data);
                var jLikeIcon = jSecret.find('.btn-like');
                jLikeIcon.find('.numb').text($(data).size());
            }
        });
        return false;
    });

    //Open the popover for displaying and adding comments
    $(document).on('click.juzu.secret.openPopover', '.btn-popup-comment', function () {
        var jComment = $(this);
        var jSecret = jComment.closest('.secret');
        jSecret.addClass('open-popover');
    });

    //Close the popover for displaying and adding comments
    $(document).on('click.juzu.secret.closePopover', '.closePopover', function () {
        var jComment = $(this);
        var jSecret = jComment.closest('.secret');
        jSecret.removeClass('open-popover');
    });

    //Ajax for managing comment function
    $(document).on('click.juzu.secret.addComment', '.btn-comment', function () {
        var jComment = $(this);
        var jSecret = jComment.closest('.secret');
        var secretId = jSecret.attr('data-secretId');

        jComment.jzAjax('JuZcretApplication.addComment()', {
            data: {'secretId': secretId, 'content': jSecret.find('.secret-add-comment').val()},
            success: function (data) {
                if (typeof(data) == 'string') {
                    //error response
                    alert(data);
                } else {
                    //update html
                    var cList = "";
                    var cCounter = 0;
                    $(data).each(function (idx, elem) {
                        if (elem.content) {
                            cList +=
                                "<div class='media'>" +
                                    "<a class='pull-left' href='http://localhost:8080/portal/intranet/profile/" + elem.userId + "'>" +
                                        "<img src='http://localhost:8080/social-resources/skin/images/ShareImages/UserAvtDefault.png' alt='avatar'>" +
                                    "</a>" +
                                    "<div class='media-body'>" +
                                        "<div>" +
                                            "<a class='cm-user-name' href='http://localhost:8080/portal/intranet/profile/" + elem.userId + "'>" + elem.userId + "</a> " +
                                            "<span class='cm-time'>" + elem.createdDate + "</span>" +
                                        "</div>" +
                                        "<div class='cm-content'>" + elem.content + "</div>" +
                                    "</div>" +
                                "</div>";
                            cCounter++;
                        }
                    });
                    var html = jSecret.find('.secr-comments-list').html();
                    jSecret.find('.secr-comments-list').html(html + cList);
                    var jCommentIcon = jSecret.find('.btn-popup-comment');
                    var jCommentNumb = jCommentIcon.find('.numb').text();
                    jCommentIcon.find('.numb').text(parseInt(jCommentNumb)+cCounter);
                }
            }
        });
        return false;
    });
})($);
