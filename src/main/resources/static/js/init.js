function Switch(i) {
    var js;
    clear_css();
    switch (i) {
        case 1:
            $('.a1').addClass("frame__demo--current");
            js = '<script src="js/demo.js"></script>';
            $('body').addClass("demo-1");
            break;
        case 2:
            $('.a2').addClass("frame__demo--current");
            js = '<script src="js/demo2.js"></script>';
            $('body').addClass("demo-2");
            break;
        case 3:
            $('.a3').addClass("frame__demo--current");
            js = '<script src="js/demo3.js"></script>';
            $('body').addClass("demo-3");
            break;
        case 4:
            $('.a4').addClass("frame__demo--current");
            js = '<script src="js/demo4.js"></script>';
            $('body').addClass("demo-4");
            break;
        case 5:
            $('.a5').addClass("frame__demo--current");
            js = '<script src="js/demo5.js"></script>';
            $('body').addClass("demo-5");
            break;
        case 6:
            $('.a6').addClass("frame__demo--current");
            js = '<script src="js/demo6.js"></script>';
            $('body').addClass("demo-6");
            break;
        default:
            alert(i);
            break;
    }
    $('#com_js').html(js);
}

function clear_css() {
    $('.a1').removeClass("frame__demo--current");
    $('.a2').removeClass("frame__demo--current");
    $('.a3').removeClass("frame__demo--current");
    $('.a4').removeClass("frame__demo--current");
    $('.a5').removeClass("frame__demo--current");
    $('.a6').removeClass("frame__demo--current");
    $('body').removeClass("demo-1");
    $('body').removeClass("demo-2");
    $('body').removeClass("demo-3");
    $('body').removeClass("demo-4");
    $('body').removeClass("demo-5");
    $('body').removeClass("demo-6");
}