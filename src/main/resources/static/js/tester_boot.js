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
let saveExampleUrl = "/dataway2/api-example";
let lastExampleUrl = "/dataway2/api-example/last";

let indexUrl = "/api-ui";
let detailUrl = "/api-ui/";
let editor = "admin";

//当前example
let currExample = {
    apiInfoId:null,
    url:null,
    method:null,
    requestHeader:null,
    requestBody:null,
    responseHeader:null,
    responseBody:null,
    status:null,
    time:0,
    options:null
}

let editorTextarea;
let exampleTextarea;
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

    editorTextarea = CodeMirror.fromTextArea(document.getElementById('CodeMirror1'),{
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

    editorTextarea.on("change", function(editor, change) {
    });

    editorTextarea.setSize('100%','100%');

    exampleTextarea = CodeMirror.fromTextArea(document.getElementById('CodeMirror2'),{
        mode:"application/json",
        lineWrapping:false,
        foldGutter: true,
        gutters:["CodeMirror-linenumbers", "CodeMirror-foldgutter","CodeMirror-lint-markers"],
        //CodeMirror-lint-markers是实现语法报错功能
        lint: true,
        fullScreen:true,
        matchBrackets:true,
        lineNumbers: true,//是否显示行号
        styleActiveLine:true,
        matchBrackets: true,
        autoRefresh: true,
        theme:"eclipse",
        smartIndent : true,  // 是否智能缩进
        tabSize : 4,  // Tab缩进，默认4
        extraKeys:{
            "Alt-/": "autocomplete",
            "Esc": function(cm) {
                if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
            },
            "Ctrl-/": "toggleComment",
            "Ctrl-Z":function (editor) {
                editor.undo();
            },//undo
            "F8":function (editor) {
                editor.redo();
            },//Redo
            "F7": function autoFormat(cm) {
                let totalLines = cm.lineCount();
                cm.autoFormatRange({line:0, ch:0}, {line:totalLines});
            }//代码格式化
        }
    });

    exampleTextarea.setSize('100%','100%');

    CodeMirror.commands.autocomplete = function(cm) {
        cm.showHint({hint: CodeMirror.hint.anyword});
    };

    exampleTextarea.on("change", function(editor, change) {
    });


});



function loadEvent() {
    loadSelectBoxEvent();
    loadInputTypeEvent();
    loadEditAbleEvent();
    loadExampleMethodEvent();
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
    showSendNotify("Renameing group")
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
        },complete:function () {
            hideSendNotify();
        }
    });
}

function removeApi(e,id) {

    openConfirmModal("The request will be permanently deleted. Are you sure?",function () {
        showSendNotify("Removeing api")
        $.ajax({
            type: "delete",
            url: deleteApiUrl,
            contentType : "application/json",
            data: JSON.stringify({"id":id}),
            success: function (data) {
                $(e).parents(".request").remove();
                history.pushState(null,null,indexUrl);
                closeConfirmModal();
            },complete:function () {
                hideSendNotify();
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

    //------构建editor
    $(".request").removeClass("selected");
    $(".service").removeClass("parent-selected");
    $(".request"+id).addClass("selected");
    $(".request"+id).parents(".service").addClass("parent-selected");
    $(".request"+id).parents(".service").removeClass("collapsed");
    $(".request"+id).parents(".service").find(".fa-caret-right").addClass("fa-caret-down").removeClass("fa-caret-right");
    $('#editor-action .draft-ribbon-text').text("Editor");

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


        editorTextarea.setValue(data.script);

        //构建example
        loadExample(data);
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
        "script": editorTextarea.getValue(),
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
        "script": editorTextarea.getValue(),
    };
    saveExecuter(params);
}

function showSendNotify(msg) {
    $("#notification").show().find("#notification-message").text(msg);
}
function hideSendNotify() {
    $("#notification").hide();
}

function saveExecuter(params) {
    showSendNotify("Saveing api");
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
        },complete:function (req,data) {
            hideSendNotify();
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
    editorTextarea.setValue("");

    //css
    $(form).find(".api-info-method").removeAttr("readonly");
    $(form).find(".api-info-method").parent().removeClass("disabled");
    $(form).find(".api-info-path").removeAttr("readonly");
    $(form).find(".api-info-datasource").removeAttr("readonly");
    $(form).find(".api-info-datasource").parent().removeClass("disabled");
}




//--------------------------------example start -----------------------------------
function loadExample(apiInfo) {

    let $form = $("#example-action");
    $form.find(".save-example-btn .changes-indicator").remove();

    //------构建example
    $.getJSON(lastExampleUrl+"?limit=1&apiInfoId="+$("#editor-action .api-info-id").val(),function (data) {
        data = unpackResult(data).data;
        let basePath = window.location.href.substring(0,window.location.href.indexOf("/api-ui"));
        currExample = data[0]?data[0]:{
            apiInfoId:apiInfo.id,
            url:basePath+apiInfo.path,
            method:apiInfo.method,
            requestHeader:"[]",
            requestBody:"",
            responseHeader:"[]",
            responseBody:"",
            status:200,
            time:0,
            options:{}
        };
        $form.find("#gwt-uid-350").val(currExample.method);
        $form.find("#gwt-uid-349").val(currExample.url).blur();

        //请求header
        setHeaderParams(JSON.parse(currExample.requestHeader));

        switchExampleMethod(currExample.method);

        //请求体
        exampleTextarea.setValue(currExample.requestBody);
        formatExample();
        //响应状态码
        buildResponseStatus(currExample.status);
        //耗时
        $("#response .el-time").html('<span title="'+currExample.time+'ms">Elapsed time: '+currExample.time+'ms</span>');
        //响应header
        setResponseHeader(JSON.parse(currExample.responseHeader));
        //响应体
        $("#response #responseBody").text(formatResponseBody(currExample.responseBody));
    })
}

function toNotSave() {
    let $form = $("#example-action");
    $form.find(".save-example-btn .changes-indicator").remove();
    $form.find(".save-example-btn").append('<div class="changes-indicator"></div>');
}

function formatResponseBody(body){
    try{
        return JSON.stringify(JSON.parse(body), null, "    ")
    }catch (e) {
        return body;
    }
}

function saveExample() {
    showSendNotify("Saveing example")
    $.ajax({
        type: "POST",
        url: saveExampleUrl,
        contentType : "application/json",
        data: JSON.stringify(currExample),
        success: function (data) {
            data = unpackResult(data);
            if (data.code !=200){
                openMsgModal(data.msg);
                return;
            }
            $("#example-action .save-example-btn .changes-indicator").remove();
        },complete:function () {
            hideSendNotify();
        }
    });
}
function requextUrlExample(ableRedirect) {
    let $form = $("#example-action");
    let url = $form.find("#gwt-uid-349").val();
    requestExample(url,ableRedirect);
}
function requestExample(url,ableRedirect){

    toNotSave();

    let $form = $("#example-action");
    let type = $form.find("#gwt-uid-350").val();
    let bodyParam = (type == "POST" || type == "PUT")?exampleTextarea.getValue():"";
    let headerArrs = getHeaderParams();
    let headers = {};
    $.each(headerArrs,function (index,item) {
        let arr = item.split(":");
        headers[arr[0]] = arr[1];
    })
    let startTime = new Date().getTime();
    $("#response #responseBody").text("");
    setResponseHeader([]);
    showSendNotify("Sending request");
    $.ajax({
        url:url,
        type:type,
        data:bodyParam,
        headers: headers,
        beforeSend: function(request){

        },
        success: function(data){

        },error:function (req,msg,ex) {

        },complete:function (req,data) {
            hideSendNotify();
            let responseHeader = req.getAllResponseHeaders().split("\r\n");
            let status = req.status;
            let currTime = new Date().getTime();
            currExample = {
                apiInfoId:$("#editor-action .api-info-id").val(),
                url:url,
                method:type,
                requestHeader:JSON.stringify(headerArrs),
                requestBody:bodyParam,
                responseHeader:JSON.stringify(responseHeader),
                responseBody:req.responseText,
                status:status,
                time:(currTime-startTime),
                options:"{}"
            }

            let redirectUrl = req.getResponseHeader("redirectUrl");
            if (redirectUrl && ableRedirect){
                requestExample(redirectUrl,false);
                return;
            }

            buildResponseStatus(status);

            setResponseHeader(responseHeader)
            $("#response #responseBody").text(formatResponseBody(req.responseText));
        }});
}

function buildResponseStatus(status) {
    $("#response .response-status-line").removeClass("response-status-line-failure").removeClass("response-status-line-ok");

    if (status == 200){
        $("#response .response-status-line").addClass("response-status-line-ok");
    }else{
        $("#response .response-status-line").addClass("response-status-line-failure");
    }

    if (status == 0){
        $("#response .response-status-line .status").html("<a >No response </a>");
    }else{
        $("#response .response-status-line .status").html("<a >"+status+" </a>");
    }
}

function setResponseHeader(headers){
    $("#response .headers-form-table>tbody").html("");
    $.each(headers,function (index,item) {
        let arr = item.split(":");
        if (arr[0].length == 0)return;
        $("#response .headers-form-table>tbody").append('<tr>\n' +
            '<td><a><span title="'+arr[0]+'">'+arr[0]+':</span></a></td>\n' +
            '<td><span class="gwt-InlineHTML" title="'+arr[1]+'">'+arr[1]+'</span></td></tr>')
    })
}

function triggerQueryParameterForm(e) {
    let isCollapsed = $(e).parents(".query-parameters-form").hasClass("collapsed");
    if (isCollapsed){
        $(e).parents(".query-parameters-form").removeClass("collapsed");
        $(e).parents(".query-parameters-form-title").removeClass("collapsed-title");
        $(e).parent().children(".fa").removeClass("fa-caret-right").addClass("fa-caret-down");
        $(e).parent().children(".r-btn-link").show();
    }else{
        $(e).parents(".query-parameters-form").addClass("collapsed");
        $(e).parents(".query-parameters-form-title").addClass("collapsed-title");
        $(e).parent().children(".fa").removeClass("fa-caret-down").addClass("fa-caret-right");
        $(e).parent().children(".r-btn-link").hide();
    }
}

function parseUrlInput(e) {
    let $form = $("#example-action");
    $form.find(".query-parameters-form-block").html("");
    $("#example-action .subtitle-counter").text("")

    let path = $(e).val();
    if (path.indexOf("http://") != 0){
        path = "http://"+path;
        $(e).val(path);
    }

    if (path.length == 7){
        $(".uri-validation-error").show();
    }else{
        $(".uri-validation-error").hide();
    }

    if (path.indexOf("?") == -1){
        return;
    }
    let paramList = path.substring(path.indexOf("?")+1).split("&");
    if (!paramList || paramList.length == 0){
        return;
    }

    $.each(paramList,function (index,item) {
        let arr = item.split("=");
        queryParameterAdd(arr[0],arr[1]);
    })
}

function queryParameterRemove(e){
    $(e).parents(".query-parameter-row").remove();
    buildUrlInput();
    $("#example-action .subtitle-counter").text("["+$(".query-parameters-form-block>.active").length+"]");
}

function buildUrlInput() {
    let url = $("#gwt-uid-349").val();
    url = url.substring(0,url.indexOf("?") == -1?url.length:url.indexOf("?"));
    $.each($("#example-action .query-parameters-form-block>.active"),function (index,item) {
        let key = $(item).find(".key").val();
        if (key.length == 0){
            return;
        }
        let value = $(item).find(".value").val();
        if (index > 0){
            url += "&";
        }else{
            url += "?";
        }
        url += (key + "=" + value);
    })
    $("#gwt-uid-349").val(url);
}

function queryParameterAdd(key,value) {
    key = key?key:"";
    value = value?value:"";
    let $form = $("#example-action");
    $form.find(".query-parameters-form-block").append("<div class=\"query-parameter-row active\" e2e-tag=\"query-parameter\"><span class=\"gwt-CheckBox query-parameter-cell\" title=\"Enable/Disable\" e2e-tag=\"query-parameter-state\"><input type=\"checkbox\" value=\"on\" onclick='urlTriggerEnable(this)'  tabindex=\"0\" checked=\"\"><label for=\"gwt-uid-2020\"></label></span><span class=\"expression-input input-append query-parameter-cell-name query-parameter-cell\"><input type=\"text\" class=\"gwt-TextBox key\" onchange='buildUrlInput()' value='"+key+"' placeholder=\"name\" e2e-tag=\"query-parameter-name\"><span class=\"add-on\" data-original-title=\"\" title=\"\"><i class=\"icon-magic\"></i></span></span><span class=\"gwt-InlineLabel query-parameter-cell\">=</span><span class=\"expression-input input-append query-parameter-cell-value query-parameter-cell\"><input type=\"text\" onchange='buildUrlInput()' class=\"gwt-TextBox value\" value='"+value+"' placeholder=\"value\" e2e-tag=\"query-parameter-value\"><span class=\"add-on\" data-original-title=\"\" title=\"\"><i class=\"icon-magic\"></i></span></span><button class=\"r-btn r-btn-link query-parameter-cell\" onclick='queryParameterRemove(this)' title=\"Remove\" e2e-tag=\"query-parameter-remove\"><i class=\"fa fa-times-thin\"></i><span></span><span class=\"r-btn-indicator\" aria-hidden=\"true\" style=\"display: none;\"></span></button><div class=\"btn-group ctrls dropdown-secondary query-parameter-encoding query-parameter-cell\" e2e-tag=\"query-parameter-additional-actions\"><a class=\"btn-mini dropdown-toggle\" data-toggle=\"dropdown\"><i class=\"sli-icon-options-vertical\"></i></a> <ul class=\"pull-right dropdown-menu\"><li class=\"dropdown-item\" e2e-tag=\"query-parameter-encode\"><a><i class=\"fa fa-check\"></i> <span>Encode before sending</span></a></li></ul></div></div>");
    $form.find(".query-parameters-form-block .query-parameter-row .key").focus();
    $("#example-action .subtitle-counter").text("["+$(".query-parameters-form-block>.active").length+"]");
}

function urlTriggerEnable(e) {
    if ($(e).is(':checked')){
        $(e).parents(".query-parameter-row").addClass("active");
        $(e).parents(".query-parameter-row").find(".expression-input input").removeClass("disabled").removeAttr("disabled");
    }else {
        $(e).parents(".query-parameter-row").find(".expression-input input").addClass("disabled").attr("disabled","disabled");
        $(e).parents(".query-parameter-row").removeClass("active");
    }
    buildUrlInput();
    $("#example-action .subtitle-counter").text("["+$(".query-parameters-form-block>.active").length+"]");
}

function headerTriggerEnable(e) {
    if ($(e).is(':checked')){
        $(e).parents(".header-row").addClass("active");
        $(e).parents(".header-row").find(".expression-input input").removeClass("disabled").removeAttr("disabled");
    }else {
        $(e).parents(".header-row").find(".expression-input input").addClass("disabled").attr("disabled","disabled");
        $(e).parents(".header-row").removeClass("active");
    }
}

function headRemoveAll() {
    $("#example-action .headers-form-block").html("");
    $("#example-action .mode-raw textarea").val("");
}

function getHeaderParams() {
    let isForm = $("#example-action .headers-form-title .dropdown-toggle").text().trim() == 'Form';
    let headersParams = [];
    if (isForm){
        let headers = $("#example-action .headers-form-block>.active");
        $.each(headers,function (index,item) {
            let key = $(item).find(".key").val();
            let value = $(item).find(".value").val();
            headersParams.push(key+":"+value);
        });
    }else{
        headersParams = $("#example-action .mode-raw textarea").val().split(/[(\r\n)\r\n]+/);
    }
    return headersParams;
}

function setHeaderParams(headersParams) {
    let isForm = $("#example-action .headers-form-title .dropdown-toggle").text().trim() == 'Form';
    if (isForm){
        $("#example-action .headers-form-block").html("");
        $.each(headersParams,function (index,item) {
            let arr = item.split(":");
            headerAdd(arr[0],arr[1]);
        });
    }else{
        $("#example-action .mode-raw textarea").val(headersParams.join("\r\n"));
    }
}

function getRawHeaders() {
    let headers = $("#example-action .mode-raw textarea").val().split(/[(\r\n)\r\n]+/);
    let headerNews = [];
    $.each(headers,function (index,item) {
        item = item.trim();
        if (item.length > 0 && item.indexOf(":")>0){
            headerNews.push(item);
        }
    })
    return headerNews;
}

function headerRemove(e){
    $(e).parents(".header-row").remove();
}

function headerAdd(key,value) {
    $("#example-action .headers-form-block").append(buildHeadItem(key,value));
}


function buildHeadItem(key,value) {
    key = key?key:"";
    value = value?value:"";
    return "<div class=\"header-row active\" e2e-tag=\"header\"><span class=\"gwt-CheckBox header-cell\" title=\"Enable/Disable\" e2e-tag=\"header-state\"><input type=\"checkbox\" value=\"on\" onclick='headerTriggerEnable(this)' tabindex=\"0\" checked=\"\"><label for=\"gwt-uid-1412\"></label></span><span class=\"gwt-InlineHTML header-cell-name header-name-ro header-cell\" aria-hidden=\"true\" style=\"display: none;\"><a href=\"http://tools.ietf.org/html/rfc7231#section-3.1.1.5\" target=\"_blank\" class=\"header-link\"><span title=\"Content-Type\">Content-Type</span></a></span><span class=\"expression-input input-append header-cell-name header-cell\"><input type=\"text\" class=\"gwt-TextBox key\" value='"+key+"' placeholder=\"name\" e2e-tag=\"header-name\"><span class=\"add-on\" data-original-title=\"\" title=\"\"><i class=\"icon-magic\"></i></span></span><span class=\"gwt-InlineLabel header-cell\">:</span><span class=\"expression-input input-append header-cell-value header-cell\"><input type=\"text\" class=\"gwt-TextBox value\" value='"+value+"' placeholder=\"value\" e2e-tag=\"header-value\"><span class=\"add-on\" data-original-title=\"\" title=\"\"><i class=\"icon-magic\"></i></span></span>\n" +
        "                                    <button class=\"btn-remove-header r-btn r-btn-link header-cell\" onclick='headerRemove(this)' title=\"Remove\" e2e-tag=\"header-remove\"><i class=\"fa fa-times-thin\"></i><span></span><span class=\"r-btn-indicator\" aria-hidden=\"true\" style=\"display: none;\"></span></button>\n" +
        "                                    <div class=\"header-cell-fixed-action header-cell\">\n" +
        "                                        <button class=\"r-btn r-btn-link hide\" title=\"Edit\" e2e-tag=\"header-edit\"><i class=\"sli-icon-key\"></i><span></span><span class=\"r-btn-indicator\" aria-hidden=\"true\" style=\"display: none;\"></span></button>\n" +
        "                                        <button class=\"header-warning r-btn r-btn-link hide\" title=\"Show warnings\" e2e-tag=\"header-warning\"><i class=\"fa fa-exclamation-triangle\"></i><span></span><span class=\"r-btn-indicator\" aria-hidden=\"true\" style=\"display: none;\"></span>\n" +
        "                                        </button>\n" +
        "                                    </div>\n" +
        "                                </div>";
}

function showHeaderForm(e) {
    let isForm = $("#example-action .headers-form-title .dropdown-toggle").text().trim() == 'Form';
    if (isForm){
        return;
    }

    $("#example-action .mode-form").show();
    $("#example-action .mode-raw").hide();
    $(e).parents(".dropdown").children(".dropdown-toggle").html('<i></i> Form <span class="caret"></span>');
    $("#example-action .headers-form-block>.active").remove();

    let headers = $("#example-action .mode-raw textarea").val().split(/[(\r\n)\r\n]+/);
    let activeHeader = "";
    $.each(headers,function (index,item) {
        let kv = item.split(":");
        if (kv.length != 2){
            return;
        }
        activeHeader += buildHeadItem(kv[0],kv[1])
    });
    $("#example-action .headers-form-block").prepend(activeHeader);

}
function showHeaderRaw(e) {
    let isForm = $("#example-action .headers-form-title .dropdown-toggle").text().trim() == 'Form';
    if (!isForm){
        return;
    }

    $("#example-action .mode-form").hide();
    $("#example-action .mode-raw").show();
    $(e).parents(".dropdown").children(".dropdown-toggle").html('<i></i> Raw <span class="caret"></span>');

    let content = "";
    let headers = $("#example-action .headers-form-block>.active");
    $.each(headers,function (index,item) {
        content += $(item).find(".key").val();
        content += ":";
        content += $(item).find(".value").val();
        content += "\r\n";
    });
    $("#example-action .mode-raw textarea").val(content);
}

function formatExample() {
    let totalLines = exampleTextarea.lineCount();
    exampleTextarea.autoFormatRange({line:0, ch:0}, {line:totalLines});
    exampleTextarea.setValue(exampleTextarea.getValue());
}

function setModeExample(e,mode) {
    exampleTextarea.setOption("mode",mode);
    $(e).siblings("button").removeClass("selected");
    $(e).addClass("selected");
}
function cleanExample() {
    exampleTextarea.setValue("");
}

function switchExampleMethod(option) {
    let isJsonBody = false;
    if (option == 'POST' || option == 'PUT'){
        isJsonBody = true;
    }

    let isForm = $("#example-action .headers-form-title .dropdown-toggle").text().trim() == 'Form';
    let key = "Content-Type";
    let value = "application/json";

    if (isJsonBody){
        $("#example-action .note").hide();
        $("#example-action .b-container").show();
        if (isForm){
            let exists = false;
            $.each($("#example-action .headers-form-block>.header-row"),function (index,item) {
                if ($(item).find(".key").val() == key){
                    exists = true;
                    $(item).find(".value").val(value);
                }
            });
            if (!exists){
                headerAdd(key,value);
            }

        }else{
            let headers = getRawHeaders();
            let exists = false;
            for (let i=0;i<headers.length;i++){
                let item = headers[i];
                if (item.indexOf(key+":") == 0){
                    headers[i] = key+":"+value;
                    exists = true;
                }
            }
            if (!exists){
                headers.push(key+":"+value);
            }
            $("#example-action .mode-raw textarea").val(headers.join("\r\n"));
        }
    }else{
        $("#example-action .note").show();
        $("#example-action .note .note-method").text(option);
        $("#example-action .b-container").hide();
        if (isForm){
            $.each($("#example-action .headers-form-block input[value='"+key+"']"),function (index,item) {
                if ($(item).val() == key){
                    $(item).parents(".header-row").remove();
                }
            })

        }else {
            let headers = getRawHeaders();
            let headerNews = [];
            $.each(headers,function (index,item) {
                if (item.indexOf(key+":") == 0){
                    return;
                }
                headerNews.push(item);
            })
            $("#example-action .mode-raw textarea").val(headerNews.join("\r\n"));
        }
    }
}

function loadExampleMethodEvent() {
    $("#example-action .uri-method .dropdown-menu>li").on("click",function () {
        switchExampleMethod($(this).text().trim());
    })
}

function triggerEditorPanel() {
    $("#example-action").show();
    $("#response").show();
    $("#editor-action").hide();
    /*let url = window.location.href.replace("#editor","")+"#example";
    history.pushState(null,null,url);*/
}

function triggerExamplePanel() {
    $("#example-action").hide();
    $("#response").hide();
    $("#editor-action").show();
    editorTextarea.refresh();
    /*let url = window.location.href.replace("#example","")+"#editor";
    history.pushState(null,null,url);*/
}
//--------------------------------example end -----------------------------------