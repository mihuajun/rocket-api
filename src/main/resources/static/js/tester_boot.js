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
let deleteApiUrl = "/dataway2/api-info";
let getApiGroupNameUrl = "/dataway2/group-name-list";
let getApiNameUrl = "/dataway2/api-name-list";
let renameGroupUrl = "/dataway2/api-info/group";

let indexUrl = "/api-ui";
let detailUrl = "/api-ui/";
let editor = "admin";

let sqlCodeMirror;
let gdata = {

}

function loadCurrApi() {
    if (currApi){
        loadDetail(currApi,"#editor-action")
    }else{
        newRequest();
    }
}

$(function(){
    loadApiList();
    loadEvent();

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

function openConfirmModal(msg,fun) {
    $("#confirmModal").show();
    $("#confirmModal .gwt-HTML").text(msg);
    $("#confirmModal .r-btn-danger").unbind('click').bind("click",fun);
    $("#modal-backdrop").show();
}

/**
 * 关闭确认框
 */
function closeConfirmModal() {
    $("#confirmModal").hide();
    $("#modal-backdrop").hide();
}

//API移动
function moveApi(e,id) {
    showDialogGroup(id);
}
function uuid() {
    var s = [];
    var hexDigits = "0123456789abcdef";
    for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
    }
    s[14] = "4"; // bits 12-15 of the time_hi_and_version field to 0010
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1); // bits 6-7 of the clock_seq_hi_and_reserved to 01
    s[8] = s[13] = s[18] = s[23] = "-";

    var uuid = s.join("");
    return uuid;
}
/**
 * 添加一个请求
 * @param e
 */
function addARequest(e) {
    newRequest();
    saveExecuter({
        "method": "GET",
        "datasource":$("#editor-action").find(".api-info-datasource").attr("default-value"),
        "path": "TEMP-"+uuid(),
        "group": $(e).parents(".service").children(".name").attr("title"),
        "editor": editor,
        "comment": "Request",
        "script": "",
    });
}

//重命名组
function renameApi(e) {
    $(".authenticated>.service").removeClass("renameing");
    $(e).parents(".service").addClass("renameing");
    let group = $(".authenticated .renameing").children(".name").attr("title");
    $("#rename-dialog").show();
    $("#rename-dialog").find(".newname").val(group);
    $("#rename-dialog").find(".oldname").val(group);
}
//确认重命名组
function confirmRenameDialog(e) {
    let newGroup = $("#rename-dialog").find(".newname").val();
    let oldGroup = $("#rename-dialog").find(".oldname").val();
    $.ajax({
        type: "put",
        url: renameGroupUrl,
        contentType : "application/json",
        data: JSON.stringify({"newGroup":newGroup,"oldGroup":oldGroup}),
        success: function (data) {
            data = unpackResult(data);
            if (data.code !=200){
                openMsgModal(data.msg);
                return;
            }
            $(".authenticated .renameing").children(".name").attr("title",newGroup);
            $(".authenticated .renameing").children(".name").children(".gwt-InlineHTML").text(newGroup);
            cancelDialog('#rename-dialog');
        }
    });
}

function removeApi(e,id) {

    openConfirmModal("The request will be permanently deleted. Are you sure?",function () {
        $.ajax({
            type: "delete",
            url: deleteApiUrl,
            contentType : "application/json",
            data: JSON.stringify({"id":id}),
            success: function (data) {
                $(e).parents(".request").remove();
                history.pushState(null,null,indexUrl);
                closeConfirmModal();
            }
        });
    });

    event.stopPropagation();    //  阻止事件冒泡
}

//comment 编辑事件
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

//输入框占用字节显示事件
function loadInputTypeEvent() {
    $(".gwt-TextBox").on("blur",function () {
        let value = $(this).val();
        $(this).parents(".uri-input").children(".uri-length").text("length: "+value.length+" byte(s) ");
    });
}
//method下拉框事件
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
    $(".request"+id).parents(".service").removeClass("collapsed");
    $(".request"+id).parents(".service").find(".fa-caret-right").addClass("fa-caret-down").removeClass("fa-caret-right");
    $('.draft-ribbon-text').text("Edit");

    let url = detailUrl+id;
    history.pushState(null,null,url);
    $.getJSON(getApiUrl+id,function (data) {
        data = unpackResult(data).data;
        $(form).find(".api-info-id").val(data.id);
        $(form).find(".api-info-method").val(data.method);
        $(form).find(".api-info-datasource").val(data.datasource),
        $(form).find(".api-info-path").val(data.path.startsWith("TEMP-")?"":data.path);
        $(form).find(".api-info-group").val(data.group);
        $(form).find(".api-info-editor").val(data.editor);
        $(form).find(".api-info-comment").val(data.comment);

        $(form).find(".api-info-method").removeAttr("readonly");
        $(form).find(".api-info-method").parent().removeClass("disabled");
        $(form).find(".api-info-path").removeAttr("readonly");
        $(form).find(".api-info-datasource").removeAttr("readonly");
        $(form).find(".api-info-datasource").parent().removeClass("disabled");

        if(data.type == 'Code'){
            $(form).find(".api-info-method").attr("readonly","readonly");
            $(form).find(".api-info-method").parent().addClass("disabled");
            $(form).find(".api-info-path").attr("readonly","readonly");
            $(form).find(".api-info-datasource").attr("readonly","readonly");
            $(form).find(".api-info-datasource").parent().addClass("disabled");
        }


        sqlCodeMirror.setValue(data.script);
    })


}

function saveAsEditor() {
    showDialogGroup();
}

function closeModal() {
    $("#modal-backdrop").hide();
    $("#msgModal").hide();
}

function openMsgModal(msg) {
    $("#modal-backdrop").show();
    $("#msgModal").show();
    $("#msgModal .modal-body").text(msg);
}

function confirmDialog(form) {
    let group = $(form).find("#save-dialog .path-buttons .active").attr("title");
    let params={
        "id": $("#save-dialog").attr("data-id"),
        "method": $(form).find(".api-info-method").val(),
        "datasource":$(form).find(".api-info-datasource").val(),
        "path": $(form).find(".api-info-path").val(),
        "group": group?group:"公共API",
        "editor": $(form).find(".api-info-editor").val(),
        "comment": $(form).find("#save-dialog .input-xlarge").val(),
        "script": sqlCodeMirror.getValue(),
    }
    saveExecuter(params);
}

function confirmGroup() {
    cancelGroup();
    let value = $(".new-path .new-item-name").val();
    $("#save-dialog .local-drive").append("<li><a class='curr-add' onclick='listRequest(this)'><i class=\"api-tester-icon api-tester-project\"></i><span>"+value+"</span></a></li>")
    $("#save-dialog .curr-add").click();
}

function addGroup() {
    $(".new-path>a").hide();
    $(".new-path>div").show();
    $(".new-path .left-label").text("Group");
}

function cancelGroup() {
    $(".new-path>a").show();
    $(".new-path>div").hide();
}

function cancelDialogGroup() {
    $("#modal-backdrop").hide();
    $("#save-dialog").hide();
}

function showDialogGroup(id) {
    $("#save-dialog").attr("data-id",id);
    $("#save-dialog").show();
    $("#save-dialog .input-xlarge").val($("#editor-action .api-info-comment").val());
    $("#save-dialog .path-buttons .r-btn").addClass("active");
    $("#save-dialog .button-path-selector").remove();

    $("#save-dialog .local-drive").html("");

    $.getJSON(getApiGroupNameUrl,function (data) {
        data = unpackResult(data).data;
        $.each(data,function (index,item) {
            $("#save-dialog .local-drive").append("<li><a onclick='listRequest(this)'><i class=\"api-tester-icon api-tester-project\"></i><span>"+item+"</span></a></li>")
        })
    })
}

function listRequest(e) {
    let value = $(e).text();
    $("#save-dialog .r-btn").removeClass("active");
    $("#save-dialog .path-buttons").append("<a class=\"r-btn button-path-selector active\" title=\""+value+"\"><i class=\"api-tester-icon api-tester-project\"></i><span>"+value+"</span></a>");

    $("#save-dialog .local-drive").html("");
    $.getJSON(getApiNameUrl+"?group="+value,function (data) {
        data = unpackResult(data).data;
        if (data.length == 0){
            $("#save-dialog .local-drive").append("<li><p class=\"navbar-text\">Empty</p></li>")
            return;
        }
        $.each(data,function (index,item) {
            $("#save-dialog .local-drive").append("<li><a><i class=\"api-tester-icon api-tester-request\"></i><span>"+item+"</span></a></li>")
        })
    })
}

function saveEditor(form) {

    let id = $(form).find(".api-info-id").val();

    if (!id){
        showDialogGroup();
        return;
    }

    let params={
        "id":$(form).find(".api-info-id").val(),
        "method": $(form).find(".api-info-method").val(),
        "datasource":$(form).find(".api-info-datasource").val(),
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
            if (data.code !=200){
                openMsgModal(data.msg);
                return;
            }
            cancelDialogGroup();
            loadDetail(data.data,"#editor-action")
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
            '                                                <ul class="pull-right dropdown-menu">' +
            '<li class="dropdown-item" onclick="addARequest(this)"><a><i class="fa fa-plus"></i><span class="gwt-InlineHTML">Add a request</span></a></li>' +
            '<li class="dropdown-item" onclick="renameApi(this)"><a><i class="fa fa-edit"></i><span class="gwt-InlineHTML">Rename</span></a></li>' +
            '</ul>\n' +
            '                                            </div>\n' +
            '                                        </div></li>');

        let $lev2 = $('<ul></ul>');
        $.each(value,function (index,item) {
            $lev2.append('<li class="'+collapsed+' request level2 request'+item.id+'" ><div class="name" onclick="loadDetail(\''+item.id+'\',\'#editor-action\')" title="'+(item.comment?item.comment:item.path)+'"><i\n' +
                '                                                        class="fa fa-caret-right invisible" aria-hidden="true"\n' +
                '                                                        e2e-tag="drive|'+(item.comment?item.comment:item.path)+'|expand"\n' +
                '                                                        style="display: none;"></i>\n' +
                '                                                    <div class="play-icon" title="Launch request"\n' +
                '                                                         e2e-tag="drive|'+(item.comment?item.comment:item.path)+'|play"><i\n' +
                '                                                            class="fa fa-play"></i></div>\n' +
                '                                                    <span class="gwt-InlineHTML node-text"\n' +
                '                                                          e2e-tag="drive|'+(item.comment?item.comment:item.path)+'">'+(item.comment?item.comment:item.path)+'</span>\n' +
                '                                                    <div class="status" aria-hidden="true" style="display: none;"></div>\n' +
                '                                                    <div class="btn-group ctrls dropdown-primary"  data-id="'+item.id+'" ><a\n' +
                '                                                            class="btn-mini dropdown-toggle" data-toggle="dropdown"\n' +
                '                                                            e2e-tag="drive|'+(item.comment?item.comment:item.path)+'|more"><i\n' +
                '                                                            class="sli-icon-options-vertical"></i></a>\n' +
                '                                                        <ul class="pull-right dropdown-menu"><li class="dropdown-item"  onclick="moveApi(this,\''+item.id+'\')"><a><i class="fa fa-random"></i><span class="gwt-InlineHTML">Move</span></a></li><li class="dropdown-item" onclick="removeApi(this,\''+item.id+'\')"><a><i class="fa fa-trash-o" onclick="moveApi(this,'+item.id+')"></i><span class="gwt-InlineHTML">Remove</span></a></li></ul>\n' +
                '                                                    </div>\n' +
                '                                                </div>' +
                '</li>');
        })
        $lev1.append($lev2);
        $(".authenticated").append($lev1);
    })
}

function loadApiList() {
    $.getJSON(loadApiListUrl,function (data) {
        data = unpackResult(data);
        gdata.apiList = data.data;
        buildApiTree(gdata.apiList,"collapsed");
        loadCurrApi();
    })
}

function unpackResult(data){
    if (data.unpack){
        return data;
    }

    $.each(data,function (key,value) {
        if ($.isPlainObject(value)){
            data = unpackResult(value);
            return;
        }
    })
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

function cancelDialog(e) {
    $(e).hide();
}

function newRequest() {
    let form = "#editor-action";
    history.pushState(null,null,indexUrl);
    $(form).find(".api-info-id").val("");
    $(form).find(".api-info-method").val("GET");

    $(form).find(".api-info-path").val("");
    $(form).find(".api-info-datasource").val($(form).find(".api-info-datasource").attr("default-value"));
    $(form).find(".api-info-group").val("公共API");
    $(form).find(".api-info-editor").val("admin");
    $(form).find(".api-info-comment").val("Request");
    sqlCodeMirror.setValue("");

    //css
    $(form).find(".api-info-method").removeAttr("readonly");
    $(form).find(".api-info-method").parent().removeClass("disabled");
    $(form).find(".api-info-path").removeAttr("readonly");
    $(form).find(".api-info-datasource").removeAttr("readonly");
    $(form).find(".api-info-datasource").parent().removeClass("disabled");
}