"use strict"; // new 21/11/2014 héhé
function javascript_abort(){
    console.log("from function javascript_abort in form.js");
    alert("from function javascript_abort in form.js");
   throw new Error('This is not an error. This is just to abort javascript');
} 

/*
function convertYardtoMeter(frm) // used in hole.xhtml, id="hole"
{
    alert("from function!!");
    //document.getElementById('holeDistance');
    var labelElement = document.getElementById('holeDistance');

    alert("from function : element = " + labelElement.value);
    frm.holeDistance.value = Math.round(frm.holeDistance.value * 0.9144);

} //end function
 * 
 */
function init()
{
    console.info("from init javascript function");
    document.addEventListener("keypress", myFunction, false);
}
function myFunction(el)
{
    alert("You hit the validate hole button");
    alert("name  = " + el.name + " ,id =  " + el.id);
}
function total(frm) // used in form_hole
{ //alert(" holeDistance = " + frm.holeDistance.value);// option Modify
    frm.holeDistance.value = parseInt(frm.holeDistance_100.value)
            + parseInt(frm.holeDistance_10.value)
            + parseInt(frm.holeDistance_1.value);

    //document["form_hole"]["holeDistance_1"].value
}

function open_popup(URL)
{
    window.open(URL, "internal_name??", config = "width=600,height=600,top=30,left=50");
}

function replVirgule(el) // used in form_player(handicap) and form_tee(rating)
{
    //alert("field = " + nElement.value);
    var name = el.value.replace(",", "."); // on remplace les virgules par des points
    //alert(' var name = ' + name);
    el.value = name;
}
function repl(el) // used in score_stableford.xhtml(all holes)
{ // gestion du scratch !! le "X" est emplacé par 10 - qui ne donnera pas de points"
    //Triple-equal is different to double-equal because in addition to checking
    // whether the two sides are the same value, it also checks that they are the same data type.
    if ((el.value === "X") || (el.value === "x") || (el.value === "-"))
    {
        console.info(" valeur à X, x ou - : " + el.value);
        el.value = "10"; // score forcé à 10
        console.info(" valeur forcée à 10 : " + el.value);
    }
}

function setFocus()
{
    var flag = false;
    for (z = 0; z < document.forms.length; z++) {
        var form = document.forms[z];
        var elements = form.elements;
        for (var i = 0; i < elements.length; i++) {
            var element = elements[i];
            if (element.type === 'text' &&
                    !element.readOnly &&
                    !element.disabled) {
                element.focus();
                flag = true;
                break;
            }
        }
        if (flag)
            break;
    }
} // end function

function show(input)
{
    alert(input.value);
}
// jqPlot function : I want to show only the value (y-axis) in a line chart as datatip
function customExtender()
{
    this.cfg.highlighter =
            {
                useAxesFormatters: false,
                tooltipAxes: 'y'
            };
}
function ext01()  // without border
{
    //alert(" function ext " );
    this.cfg.axes.yaxis.tickOptions.showGridline = false;
    this.cfg.axes.xaxis.tickOptions.showGridline = false;
}

// used in statCourse.xhtml pour afficher le lineChart de primefaces
// voir également le primefacesLC.css pour fontsize legende
function ChartExtender() // used in charts.ChartsBarModel.java
{
    alert(" function Chart Extender ");
    this.cfg.axes.xaxis.tickOptions = {formatString: '%d'};
    this.cfg.axes.xaxis.tickOptions = {fontSize: '10pt', fontFamily: 'Tahoma'}; //taille des numéros trous
    this.cfg.axes.xaxis.labelOptions = {fontSize: '18pt', fontFamily: 'Helvetica'}; // taille texte

    this.cfg.axes.xaxis.min = 1;
    this.cfg.axes.xaxis.tickInterval = 1;
    this.cfg.axes.xaxis.max = 18;

    //   this.cfg.axes.yaxis.min = 1;
    this.cfg.axes.yaxis.tickInterval = 1;
    //   this.cfg.axes.yaxis.max = 12;

    this.cfg.seriesDefaults = {fontSize: '10pt'};
}

function BarChartExtender() // used in ChartsBarModel.java
{
    alert(" function BarChartExtender ");
    //  this.cfg.axes.xaxis.tickOptions = {formatString: '%d'};
    //  this.cfg.axes.xaxis.tickOptions = {fontSize: '10pt', fontFamily: 'Tahoma'}; //taille des numéros trous
    //  this.cfg.axes.xaxis.labelOptions = {fontSize: '18pt', fontFamily: 'Helvetica'}; // taille texte

    //   this.cfg.axes.xaxis.min = 1;
    //   this.cfg.axes.xaxis.tickInterval = 1;
    //   this.cfg.axes.xaxis.max = 18;

    //  this.cfg.axes.yaxis.min = 1;
    this.cfg.axes.yaxis.tickOptions = {formatString: '%d'};
    this.cfg.axes.yaxis.tickInterval = 1;
    //  this.cfg.axes.yaxis.max = 12;

    this.cfg.seriesDefaults = {fontSize: '10pt'};
}

function exportChart()
{
    alert(" exportChart from form.js");
    //export image
    $('#output').empty().append(PF('chart').exportAsImage());
    //show the dialog
    PF('dlg').show();
}

function jqueryVersion()
{
    if (window.jQuery)  // jQuery is available.
    {
        alert("Jquery version = " + jQuery.fn.jquery);
    }
}

function plotChart(param1, param2)
{
    alert("entering params = " + param1 + " " + param2);
    $.jqplot('chartdiv', [[[1, 2], [3, 5.12], [5, 13.1], [7, 33.6], [9, 85.9], [11, 219.9]]]);
}

function handleDrop(event, ui) {
    var droppedItem = ui.draggable;
    droppedItem.fadeOut('fast');
}

function __reset() {
    console.info("starting reset from forms.js");
    var elements = document.getElementsByTagName("input");
    for (var ii = 0; ii < elements.length; ii++) {
        if (elements[ii].type === "text") {
            elements[ii].value = "";
        }
    }
} // end function reset
 function handleMsg(msg){
        alert(msg);
    }
