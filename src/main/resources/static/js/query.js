$("#button_query_generate").click(function() {
    $(this).button('loading');
    queryAjax("#query_generate");
});

$("#button_query_draw").click(function() {
    $(this).button('loading');
    queryAjax("#query_draw");
});

$("#button_query_offset").click(function() {
    $(this).button('loading');
    queryAjax("#query_offset");
});

$("#button_query_offset_random").click(function() {
    $(this).button('loading');
    queryAjax("#query_offset_random");
});

function queryAjax(type) {
    if (type == "#query_draw") {
        var queryStr = '', i, chart = $('#container').highcharts();
        for (i = 0; i < chart.series[0].data.length; i++) {
            if (i > 0) queryStr += "|";
            queryStr += chart.series[0].data[i].x + "," + chart.series[0].data[i].y;
        }
        var formData = new FormData();
        formData.append('queryStr', queryStr);
        $.ajax({
            type: "POST",
            url: "queryDraw",
            data: formData,
            processData: false,
            contentType: false,
            success: function(data) {
                window.location.href = "result?token=" + data;
            }
        });
    } else {
        $.ajax({
            type: "POST",
            url: "query",
            data: $(type).serialize(),
            success: function(data) {
                window.location.href = "result?token=" + data;
            }
        });
    }
}

$(function () {
    $('#container').highcharts({
        chart: {
            type: 'scatter',
            spacingLeft: 20,
            spacingRight: 20,
            events: {
                click: function (e) {
                    // find the clicked values and the series
                    var x = e.xAxis[0].value, y = e.yAxis[0].value, series = this.series[0];

                    x = Math.round(x);
                    // Add it
                    series.addPoint([x, y]);

                    if (series.data.length > 1) {
                        // grab last point object in array
                        var point = series.data[series.data.length - 1];
                        // remove duplicate x
                        var i;
                        for (i = 0; i < series.data.length - 1; i++) {
                            if (point.x == series.data[i].x) {
                                series.data[i].y = point.y;
                                point.remove();
                                return;
                            }
                        }
                        // sort the point objects on their x values
                        series.data.sort(function (a, b) {
                            return a.x - b.x;
                        });
                        // force a redraw
                        point.update();
                    }
                }
            }
        },
        title: {
            text: null
        },
        subtitle: {
            text: 'Click the plot area to add a point. Click a point to remove it.'
        },
        xAxis: {
            allowDecimals: false,
            gridLineWidth: 1,
            minPadding: 0.2,
            maxPadding: 0.2,
            minRange: 1,
            min: 1
        },
        yAxis: {
            minPadding: 0.2,
            maxPadding: 0.2,
            minRange: 1,
            title: {
                text: null
            }
        },
        tooltip: {
            crosshairs: true,
            shared: true
        },
        legend: {
            enabled: false
        },
        exporting: {
            enabled: false
        },
        plotOptions: {
            series: {
                lineWidth: 1,
                point: {
                    events: {
                        'click': function () {
                            if (this.series.data.length > 1) {
                                this.remove();
                            }
                        }
                    }
                }
            }
        },
        series: [{
            name: 'Query series',
            data: [[1, 0], [256, -5], [512, 5]]
        }]
    });

    $('#modal_draw').on('show.bs.modal', function() {
        $('#container').css('visibility', 'hidden');
    });
    $('#modal_draw').on('shown.bs.modal', function() {
        $('#container').css('visibility', 'initial');
        $('#container').highcharts().reflow();
    });

    // $('input[type=radio][name=param_R]').change(function() {
    //     paramAjax('R', 'Integer', this.value);
    // });
    //
    // $('input[type=radio][name=param_Wu]').change(function() {
    //     paramAjax('Wu', 'Integer', this.value);
    // });

    $("#param_Path").change(function() {
        paramAjax('param_path', 'String', this.value);
    });

    $('input[type=number][id=param_Epsilon]').on('input', function() {
        paramAjax('param_epsilon', 'Double', this.value);
    });
});


function paramAjax(param, type, value) {
    var formData = new FormData();
    formData.append('param', param);
    formData.append('type', type);
    formData.append('value', value);
    $.ajax({
        type: "POST",
        url: "setParameter",
        data: formData,
        processData: false,
        contentType: false,
        success: function() {
            $('.param_status').text('Saved');
            setTimeout(function () {
                $('.param_status').text('');
            }, 2000);
        }
    });
}

// function getDataAjax(token, index, unique) {
//     $.ajax({
//         type: "POST",
//         url: "/getDataByIndex",
//         data: '{"token":"' + token + '","index":"' + index + '","unique":"' + unique + '"}',
//         datatype: "json",
//         success: function (data) {
//             $('#container').highcharts({
//                 chart: {
//                     zoomType: 'xy',
//                     height: 555,
//                     marginTop: 80
//                 },
//                 title: {
//                     text: 'Rank [[${index+1}]] - Position: [[${answers[index].left}]], Distance: [[${answers[index].right}]]'
//                 },
//                 subtitle: {
//                     text: document.ontouchstart === undefined ?
//                         'Click and drag in the plot area to zoom in' : 'Pinch the chart to zoom in'
//                 },
//                 xAxis: {
//                     type: 'datatime',
//                     dataTimeLabelFormats: {
//                         month: '%e.%b',
//                     }
// //                minTickInterval: 1
//                 },
//                 yAxis: {
//                     title: {
//                         text: 'value'
//                     }
//                 },
//                 tooltip: {
//                     crosshairs: true,
//                     shared: true
//                 },
//                 legend: {
//                     enabled: true
//                 },
//                 plotOptions: {
//                     area: {
//                         fillColor: {
//                             linearGradient: {
//                                 x1: 0,
//                                 y1: 0,
//                                 x2: 0,
//                                 y2: 1
//                             },
//                             stops: [
//                                 [0, Highcharts.getOptions().colors[0]],
//                                 [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
//                             ]
//                         },
//                         marker: {
//                             radius: 2
//                         },
//                         lineWidth: 1,
//                         states: {
//                             hover: {
//                                 lineWidth: 1
//                             }
//                         },
//                         threshold: null
//                     }
//                 },
//                 series: [{
//                     name: 'Query series',
//                     type: 'line',
//                     pointStart: [[${answers[index].left}]],
//                     zIndex: 1,
//                     data: [[${query}]]
//                 },{
//                     name: 'Data series',
//                     type: 'line',
//                     pointStart: [[${answers[index].left}]],
//                     zIndex: 0,
//                     data: [[${data}]]
//                 }]
//             });
//
//
//         },
//         error: function () {
//             alert("WHY");
//         }
//     });
// }