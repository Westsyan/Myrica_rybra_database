/**
 *
 * Create By XWQ
 * 2019-6-21
 *
 */

function extractor(query) {
    var result = /([^,]+)$/.exec(query);
    if (result && result[1])
        return result[1].trim();
    return '';
}

function AutoGene() {
    $.ajax({
        url: "/MRDB/utils/getAllGeneId",
        type: "post",
        success: function (data) {
            $('#gene').typeahead({
                source: data,
                updater: function (item) {
                    return this.$element.val().replace(/[^,]*$/, '') + item + ',';
                },
                matcher: function (item) {
                    var tquery = extractor(this.query);
                    if (!tquery) return false;
                    return ~item.toLowerCase().indexOf(tquery.toLowerCase())
                },
                highlighter: function (item) {
                    var query = extractor(this.query).replace(/[\-\[\]{}()*+?.,\\\^$|#\s]/g, '\\$&');
                    return item.replace(new RegExp('(' + query + ')', 'ig'), function ($1, match) {
                        return '<strong>' + match + '</strong>'
                    })
                }
            })
        }
    });
}

/**
 * 左边标签切换效果，使搜索结果隐藏
 */

function hideResult() {
    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        // 获取已激活的标签页的名称
        var activeTab = $(e.target).text();
        // 获取前一个激活的标签页的名称
        var previousTab = $(e.relatedTarget).text();
        $("#result").hide()
    });
}


function egGene() {
    $("#egGene").click(function () {
        $.ajax({
            url: "/MRDB/utils/getEgGene",
            type: "post",
            success: function (data) {
                $("#gene").val(data);
            }
        });
    });
}
