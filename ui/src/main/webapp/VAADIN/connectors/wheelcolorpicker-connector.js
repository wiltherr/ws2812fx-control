//reference: https://github.com/vaadin/book-examples/blob/master/src/com/vaadin/book/examples/client/js/mycomponent-connector.js
window.de_wiltherr_ws2812fx_ui_components_wheelcolorpicker_WheelColorPicker = function () {
    //$(this.getElement()).text("Hallo");

    var uid = "clrpckr_" + Math.random().toString(36).substr(2, 9); //source https://gist.github.com/gordonbrander/2230317
    this.getElement().outerHTML = "<input id='" + uid + "' class='clrpckr v-textfield v-widget' type='text'>"; //TODO replace with vaadin text field


    var element = $("#" + uid);
    //this.getElement().wheelColorPicker();
    //debugger;
    var connector = this;
    //$('.clrpckr').wheelColorPicker();
    element.wheelColorPicker({
        //more options: https://github.com/fujaru/jquery-wheelcolorpicker/wiki/Plugin%3AOptions
        'format': 'hex', //we use rgba for compatibility with vaadin color format
        //TODO interface to set options server side via java.
        //Apperiance options
        'layout': 'popup',
        'cssClass': 'colorpicker',
        'quality': 2, //1 default, 0.2 lowest
        'sliders': 'whsvrgbp', //'sliders': 'whsp'
        'live': true,
        'preview': true,
        'snap': true,
        'snapTolerance': 0.05,
        //Mobile options
        'mobile': true,
        //'mobileWith': 300, TODO Max screen width to use mobile layout instead of default one.
        'hideKeyboard': false
    }).on('sliderup', function () {
        //$.plot($(this).wheelColorPicker('getColor'), connector.getState().colorValue);
        connector.onSliderUp($(this).wheelColorPicker('getColor'));
    }).on('slidermove', function () {
        connector.onSliderMove($(this).wheelColorPicker('getColor'));
    }).on('change', function () {
        var color = $(this).wheelColorPicker('getColor');
        connector.onChange(color);
        connector.onValueChange(color); //triggers serverside value change listener
    }).on('focus', function () {
        var color = $(this).wheelColorPicker('getColor');
        console.log("FOCUS");
        connector.onFocus(color);
    }).on('blur', function () {
        var color = $(this).wheelColorPicker('getColor');
        console.log("BLUR");
        connector.onBlur(color);
    });


    this.onStateChange = function () {
        element.wheelColorPicker("setColor", connector.getState().jsonValue, /*update input: */true);
        console.error("state changed ");
        //$.plot(element, this.getState());
    }
}