/*const gwtStartupScript = document.currentScript.getAttribute('data-gwt-startup-src');

const readFavoriteLanguageCallback = function (result) {
    const userFavoriteLanguage = (result && result.favoriteLanguage) || 'en';

    window.localStorage.setItem({ favoriteLanguage: userFavoriteLanguage }, () => {
        // create the GWT meta tag that allows the GWT bootstrap to choose the right permutation
        const gwtLocaleMetaTag = document.createElement('meta');
        gwtLocaleMetaTag.setAttribute('name', 'gwt:property');
        gwtLocaleMetaTag.content = `locale=${userFavoriteLanguage}`;
        document.head.appendChild(gwtLocaleMetaTag);

        // then inject the GWT bootstrap code
        const gwtBootstrapScriptTag = document.createElement('script');
        gwtBootstrapScriptTag.setAttribute('src', gwtStartupScript);

        document.body.appendChild(gwtBootstrapScriptTag);
    });
};

window.localStorage.getItem('favoriteLanguage', readFavoriteLanguageCallback);*/

let loadApiListUrl = "/dataway2/api-list";

let gdata = {

}
$(function(){
    loadApiList();
    $("#loader").hide();
    var myTextarea = document.getElementById('CodeMirror');
    var CodeMirrorEditor = CodeMirror.fromTextArea(myTextarea,{
        mode:"application/json",
        theme:"eclipse",
        lineNumbers: true,//是否显示行号
        value:"111111111",//经测试这个没用，如果要手动设置编辑器的内容，看下面
        smartIndent : true,  // 是否智能缩进
        tabSize : 4,  // Tab缩进，默认4
        readOnly : false , // 是否只读，默认false
    });
});
function loadApiList() {
    $.getJSON(loadApiListUrl,function (data) {
        data = unpackResult(data);
        gdata.apiList = data;
        let group = {};
        $.each(data,function(index,item){
            let arrVal = group[item.group];
            if (!arrVal){
                arrVal = [];
                group[item.group] = arrVal;
            }
            arrVal.push(item);
        });
        //if (true)return;

        //生成tree
        $.each(group,function (key,value) {
            let $lev1 = $('<li class="collapsed service level1"><div class="name" title="'+key+'"><i onclick="collapsedTree(this)" class="fa fa-caret-right"\n' +
                '                                                                               e2e-tag="drive|'+key+'|expand"></i>\n' +
                '                                            <div aria-hidden="true" e2e-tag="drive|'+key+'|play"\n' +
                '                                                 style="display: none;"><i class="fa fa-play"></i></div>\n' +
                '                                            <span class="gwt-InlineHTML node-text"\n' +
                '                                                  e2e-tag="drive|'+key+'">'+key+'</span>\n' +
                '                                            <div class="status" aria-hidden="true" style="display: none;"></div>\n' +
                '                                            <div class="btn-group ctrls dropdown-primary"><a\n' +
                '                                                    class="btn-mini dropdown-toggle" data-toggle="dropdown"\n' +
                '                                                    e2e-tag="drive|'+key+'|more"><i\n' +
                '                                                    class="sli-icon-options-vertical"></i></a>\n' +
                '                                                <ul class="pull-right dropdown-menu"></ul>\n' +
                '                                            </div>\n' +
                '                                        </div></li>');

            let $lev2 = $('<ul></ul>');
            $.each(value,function (index,item) {
                $lev2.append('<li class="collapsed request level2"><div class="name" title="'+(item.comment?item.comment:item.path)+'"><i\n' +
                    '                                                        class="fa fa-caret-right invisible" aria-hidden="true"\n' +
                    '                                                        e2e-tag="drive|'+(item.comment?item.comment:item.path)+'|expand"\n' +
                    '                                                        style="display: none;"></i>\n' +
                    '                                                    <div class="play-icon" title="Launch request"\n' +
                    '                                                         e2e-tag="drive|'+(item.comment?item.comment:item.path)+'|play"><i\n' +
                    '                                                            class="fa fa-play"></i></div>\n' +
                    '                                                    <span class="gwt-InlineHTML node-text"\n' +
                    '                                                          e2e-tag="drive|'+(item.comment?item.comment:item.path)+'">'+(item.comment?item.comment:item.path)+'</span>\n' +
                    '                                                    <div class="status" aria-hidden="true" style="display: none;"></div>\n' +
                    '                                                    <div class="btn-group ctrls dropdown-primary"><a\n' +
                    '                                                            class="btn-mini dropdown-toggle" data-toggle="dropdown"\n' +
                    '                                                            e2e-tag="drive|'+(item.comment?item.comment:item.path)+'|more"><i\n' +
                    '                                                            class="sli-icon-options-vertical"></i></a>\n' +
                    '                                                        <ul class="pull-right dropdown-menu"></ul>\n' +
                    '                                                    </div>\n' +
                    '                                                </div></li>');
            })
            $lev1.append($lev2);
            $(".authenticated").append($lev1);
        })
    })


}

function unpackResult(data){
    return data;
}

function collapsedTree(e) {
    if($(e).hasClass("fa-caret-right")){
        $(e).removeClass("fa-caret-right");
        $(e).addClass("fa-caret-down");
        $(e).parent().parent().removeClass("collapsed");
    }else {
        $(e).removeClass("fa-caret-down");
        $(e).addClass("fa-caret-right");
        $(e).parent().parent().addClass("collapsed");
    }
}

function newAction() {

}