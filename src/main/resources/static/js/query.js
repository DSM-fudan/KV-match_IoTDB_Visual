// query time picker
$('#input_start_time').datetimepicker({
    showSecond: true,
    showMillisec: true,
    dateFormat: 'yy-mm-dd',
    timeFormat: 'HH:mm:ss.l'
});

$('#input_end_time').datetimepicker({
    showSecond: true,
    showMillisec: true,
    dateFormat: 'yy-mm-dd',
    timeFormat: 'HH:mm:ss.l'
});

// normalized switch
$("[name='param_normalized']").bootstrapSwitch();

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

    $("#param_Path").change(function() {
        paramAjax('param_path', 'String', this.value);
    });

    $('input[type=number][id=param_Epsilon]').on('input', function() {
        paramAjax('param_epsilon', 'Double', this.value);
    });

    $('input[type=checkbox][id=param_Normalize_switch]').on('switchChange.bootstrapSwitch', function(event, state) {
        console.log(state);
        paramAjax('param_normalized', 'Boolean', state);
    });

    $('input[type=number][id=param_Alpha]').on('input', function() {
        paramAjax('param_alpha', 'Double', this.value);
    });

    $('input[type=number][id=param_Beta]').on('input', function() {
        paramAjax('param_beta', 'Double', this.value);
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

function js_format_date(date) {
    year = "" + date.getFullYear();
    month = "" + (date.getMonth() + 1); if (month.length == 1) { month = "0" + month; }
    day = "" + date.getDate(); if (day.length == 1) { day = "0" + day; }
    hour = "" + date.getHours(); if (hour.length == 1) { hour = "0" + hour; }
    minute = "" + date.getMinutes(); if (minute.length == 1) { minute = "0" + minute; }
    second = "" + date.getSeconds(); if (second.length == 1) { second = "0" + second; }
    millisecond = "" + date.getMilliseconds(); if (millisecond.length == 1) { millisecond = "0" + millisecond; }
    return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second + "." + millisecond;
}
