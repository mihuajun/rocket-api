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
let saveApiUrl = "/dataway2/api-info";
let getApiUrl = "/dataway2/api-info/";
let sqlCodeMirror;
let gdata = {

}

function loadCurrApi() {
    if (currApi){
        loadDetail(currApi,"#editor-action")
    }
}

$(function(){
    loadApiList();
    loadEvent();
    loadCurrApi();
    $("#loader").hide();
    let myTextarea = document.getElementById('CodeMirror1');
    sqlCodeMirror = CodeMirror.fromTextArea(myTextarea,{
        mode:"text/x-sql",
        scrollbarStyle:null,
        lineWrapping:true,
        lineNumbers: true,//是否显示行号
        styleActiveLine:true,
        matchBrackets: true,
        autoRefresh: true,
        smartIndent : true,  // 是否智能缩进
        tabSize : 4,  // Tab缩进，默认4
        extraKeys:{
            "Alt-/": "autocomplete"
        }
    });

    sqlCodeMirror.on("change", function(editor, change) {
    });

});



function loadEvent() {
    loadSelectBoxEvent();
    loadInputTypeEvent();
    loadEditAbleEvent();
}

function loadEditAbleEvent() {
    $(".editable-input>input").on("click",function () {
        $(this).removeAttr("readonly");
        $(this).parent().removeClass("read-only");
        $(this).parent().find(".add-on>i").removeClass("icon-pencil");
        $(this).parent().find(".add-on>i").addClass("icon-ok");
    });

    $(".editable-input>input").on("blur",function () {
        $(this).attr("readonly","readonly");
        $(this).parent().addClass("read-only");
        $(this).parent().find(".add-on>i").removeClass("icon-ok");
        $(this).parent().find(".add-on>i").addClass("icon-pencil");
    });
}

function loadInputTypeEvent() {
    $(".gwt-TextBox").on("blur",function () {
        let value = $(this).val();
        $(this).parents(".uri-input").children(".uri-length").text("length: "+value.length+" byte(s) ");
    });
}

function loadSelectBoxEvent() {
    $(".dropdown-menu>li").on("click",function (e) {
        let value = $(this).text().trim();
        $(this).parents(".btn-group").find(".input-append>input").val(value);
    });
}

function loadDetail(id,form) {

    $(".request").removeClass("selected");
    $(".service").removeClass("parent-selected");
    $(".request"+id).addClass("selected");
    $(".request"+id).parents(".service").addClass("parent-selected");
    $('.draft-ribbon-text').text("Edit");

    let url = "/api-ui/"+id;
    history.pushState(null,null,url);
    $.getJSON(getApiUrl+id,function (data) {
        data = unpackResult(data);
        $(form).find(".api-info-id").val(data.id);
        $(form).find(".api-info-method").val(data.method);
        $(form).find(".api-info-path").val(data.path);
        $(form).find(".api-info-group").val(data.group);
        $(form).find(".api-info-editor").val(data.editor);
        $(form).find(".api-info-comment").val(data.comment);
        sqlCodeMirror.setValue(data.script);
    })
}
function saveAsEditor(form) {
    let params={
        "method": $(form).find(".api-info-method").val(),
        "path": $(form).find(".api-info-path").val(),
        "group": $(form).find(".api-info-group").val(),
        "editor": $(form).find(".api-info-editor").val(),
        "comment": $(form).find(".api-info-comment").val(),
        "script": sqlCodeMirror.getValue(),
    };
    saveExecuter(params);
}
function saveEditor(form) {

    let params={
        "id":$(form).find(".api-info-id").val(),
        "method": $(form).find(".api-info-method").val(),
        "path": $(form).find(".api-info-path").val(),
        "group": $(form).find(".api-info-group").val(),
        "editor": $(form).find(".api-info-editor").val(),
        "comment": $(form).find(".api-info-comment").val(),
        "script": sqlCodeMirror.getValue(),
    };
    saveExecuter(params);
}

function saveExecuter(params) {
    $.ajax({
        type: "post",
        url: saveApiUrl,
        contentType : "application/json",
        data: JSON.stringify(params),
        success: function (data) {
            data = unpackResult(data);
            loadDetail(data,"#editor-action")
        }
    });
}

function searchApi(e) {
    let keyword = $(e).val();
    let searchResult = [];
    $.each(gdata.apiList,function (index,item) {
        if (item.comment.indexOf(keyword) >=0 || item.path.indexOf(keyword)>=0){
            searchResult.push(item);
        }
    });
    buildApiTree(searchResult,"");
}

function buildApiTree(list,collapsed) {
    let group = {};
    $.each(list,function(index,item){
        let arrVal = group[item.group];
        if (!arrVal){
            arrVal = [];
            group[item.group] = arrVal;
        }
        arrVal.push(item);
    });
    //if (true)return;
    $(".authenticated").html("");
    //生成tree
    $.each(group,function (key,value) {
        let $lev1 = $('<li class="'+collapsed+' service level1"><div class="name" title="'+key+'"><i onclick="collapsedTree(this)" class="fa fa-caret-right"\n' +
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
            $lev2.append('<li class="'+collapsed+' request level2 request'+item.id+'" onclick="loadDetail('+item.id+',\'#editor-action\')"><div class="name" title="'+(item.comment?item.comment:item.path)+'"><i\n' +
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
                '                                                        <ul class="pull-right dropdown-menu"><li class="dropdown-item"><a><i class="fa fa-copy"></i><span class="gwt-InlineHTML">Copy</span></a></li><li class="dropdown-item"><a><i class="fa fa-random"></i><span class="gwt-InlineHTML">Move</span></a></li><li class="dropdown-item"><a><i class="fa fa-edit"></i><span class="gwt-InlineHTML">Rename</span></a></li><li class="dropdown-item"><a><i class="fa fa-trash-o"></i><span class="gwt-InlineHTML">Remove</span></a></li></ul>\n' +
                '                                                    </div>\n' +
                '                                                </div></li>');
        })
        $lev1.append($lev2);
        $(".authenticated").append($lev1);
    })
}

function loadApiList() {
    $.getJSON(loadApiListUrl,function (data) {
        data = unpackResult(data);
        gdata.apiList = data;
        buildApiTree(gdata.apiList,"collapsed");
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
    let form = "#editor-action";
    history.pushState(null,null,"/api-ui");
    $(form).find(".api-info-id").val("");
    $(form).find(".api-info-method").val("GET");
    $(form).find(".api-info-path").val("");
    $(form).find(".api-info-group").val("公共API");
    $(form).find(".api-info-editor").val("admin");
    $(form).find(".api-info-comment").val("Request");
    sqlCodeMirror.setValue("");
}