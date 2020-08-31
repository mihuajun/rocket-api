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
let loadApiListUrl = baseUrl + "/api-list";
let saveApiUrl = baseUrl + "/api-info";
let getApiUrl = baseUrl + "/api-info/";
let lastApiUrl = baseUrl + "/api-info/last";
let deleteApiUrl = baseUrl + "/api-info";
let runApiUrl = baseUrl +"/api-info/run";
let getApiGroupNameUrl = baseUrl + "/group-name-list";
let getApiNameUrl = baseUrl + "/api-name-list";
let renameGroupUrl = baseUrl + "/api-info/group";
let saveExampleUrl = baseUrl + "/api-example";
let lastExampleUrl = baseUrl + "/api-example/last";
let deleteExampleUrl = baseUrl + "/api-example";
let apiDocPushUrl = baseUrl + "/api-doc-push";
let completionItemsUrl = baseUrl + "/completion-items";
let completionClazzUrl = baseUrl + "/completion-clazz";
let remoteSyncUrl = baseUrl + "/remote-sync";
let loginUrl = baseUrl + "/login";
let logoutUrl = baseUrl + "/logout";

let editor = "admin";

//当前apiInfo
let currApiInfo = null;
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

let defaultExample = {

}

let editorTextarea;
let exampleTextarea;
let originalModel;
let modifiedModel;
let settingTextarea;

let hasResponse;
let gdata = {
    apiList:null,
    exampleHistoryList:null,
    apiHistoryList:null,
    historyCurrPageNo:1,
    completionItems:null
}

//本地缓存信息
let rocketUser = {
    "user":{
        username:"admin"
    },
    "panel":{
        "left":"show",
        "leftWidth": "325px",
        "bottom":"show",
        "bottomHeight":"150px"
    },
    "setting":{
        "header":{},
        "options":{}
    }
}

function loadCurrApi() {
    if (currApi){
        loadDetailById(currApi,"#editor-section")
        //构建api history
    }else{
        newRequest();
    }
}


function initUser() {
    if (rocketUser.user.token){
        $("#top-section .login-btn").hide();
        $("#top-section .login-info").show();
        $("#top-section .login-info .name").text(rocketUser.user.username);
        $.ajaxSetup({
            headers:{"rocket-user-token":rocketUser.user.token}
        });
    }
}

function initPanel() {
    if (rocketUser.panel.left == "show"){
        $(".h-splitter").click();
    }else{
        $(".hide-pane-l").click();
    }

    if (rocketUser.panel.bottom == "show"){
        $(".v-splitter").click();
    }else{
        $(".bottom-down").click();
    }
}

//版本检测
function versionCheck() {
    let url = "https://img.shields.io/maven-central/v/com.github.alenfive/rocket-api-boot-starter.json";
    $.getJSON(url,function (data) {
        $("#top-section .center-version").show();
        $("#top-section .center-version span").text(data.value);
    });
}

$(function(){
    if (!localStorage.getItem("rocketUser")){
        localStorage.setItem("rocketUser",JSON.stringify(rocketUser));
    }else{
        rocketUser = JSON.parse(localStorage.getItem("rocketUser"));
    }
    $("#loader").hide();
    //加载API列表
    loadApiList(false);
    loadEvent();
    initUser();
    initPanel();
    versionCheck();
    initCompletionItems();

    editorTextarea = monaco.editor.create(document.getElementById('monaco-editor'), {
        language: languageName,
        wordWrap: 'on',  //自行换行
        verticalHasArrows: true,
        horizontalHasArrows: true,
        scrollBeyondLastLine: false,
        contextmenu:false,
        automaticLayout: true,
        fontSize:13,
        minimap: {
            enabled: false // 关闭小地图
        }

    });


    //run
    editorTextarea.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter, function () {
        runApi(false);
    });

    //debug
    editorTextarea.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyMod.Alt| monaco.KeyCode.Enter, function () {
        runApi(true);
    });

    //保存
    editorTextarea.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_S ,function () {
        saveEditor('#editor-section')
    });

    //快捷键提示
    editorTextarea.addCommand(monaco.KeyMod.Alt | monaco.KeyCode.US_SLASH ,function () {
        editorTextarea.trigger('', 'editor.action.triggerSuggest', {});
    });

    //多行注释
    editorTextarea.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyMod.Shift | monaco.KeyCode.US_SLASH ,function () {
        let selectRange = editorTextarea.getSelection();
        let prefixRange = new monaco.Range(selectRange.selectionStartLineNumber, selectRange.selectionStartColumn, selectRange.selectionStartLineNumber, selectRange.selectionStartColumn+2);
        let prefix = editorTextarea.getModel().getValueInRange(prefixRange);
        let postfixRange = new monaco.Range(selectRange.endLineNumber, selectRange.endColumn-2, selectRange.endLineNumber, selectRange.endColumn);
        let postfix = editorTextarea.getModel().getValueInRange(postfixRange);

        let prefixOp = {"range":prefixRange,"text":""};
        let postfixOp = {"range":postfixRange,"text":""};
        //取消注释
        if (prefix == '/*' && postfix == "*/"){
            editorTextarea.executeEdits('insert-code',[prefixOp])
            if (selectRange.selectionStartLineNumber == selectRange.endLineNumber){
                postfixOp.range.startColumn = postfixOp.range.startColumn - 2;
                postfixOp.range.endColumn = postfixOp.range.endColumn - 2;
            }
            editorTextarea.executeEdits('insert-code',[postfixOp])
            return;
        }

        prefixRange = new monaco.Range(selectRange.selectionStartLineNumber, selectRange.selectionStartColumn, selectRange.selectionStartLineNumber, selectRange.selectionStartColumn);
        postfixRange = new monaco.Range(selectRange.endLineNumber, selectRange.endColumn , selectRange.endLineNumber, selectRange.endColumn);
        prefixOp = {"range":prefixRange,"text":"/*"};
        postfixOp = {"range":postfixRange,"text":"*/"};
        editorTextarea.executeEdits('insert-code',[prefixOp])
        if (selectRange.selectionStartLineNumber == selectRange.endLineNumber){
            postfixOp.range.startColumn = postfixOp.range.startColumn + 2;
            postfixOp.range.endColumn = postfixOp.range.endColumn + 2;
        }
        editorTextarea.executeEdits('insert-code',[postfixOp])
    });

    //单行注释
    editorTextarea.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.US_SLASH ,function () {
        let selectRange = editorTextarea.getSelection();

        let isAdd = false;
        for(let i=selectRange.startLineNumber;i<=selectRange.endLineNumber;i++){
            let lineNumber = i;
            let range = new monaco.Range(lineNumber, 0, lineNumber, 3);
            let prefix = editorTextarea.getModel().getValueInRange(range);
            if (prefix != "//"){
                isAdd = true;
                break;
            }
        }

        for(let i=selectRange.startLineNumber;i<=selectRange.endLineNumber;i++){
            let lineNumber = i;
            let range = null;
            let text = null;
            if (isAdd){
                range = new monaco.Range(lineNumber, 0, lineNumber, 0);
                text = "//";
            }else{
                range = new monaco.Range(lineNumber, 0, lineNumber, 3);
                text = "";
            }
            editorTextarea.executeEdits('insert-code',[{"range":range,"text":text}])
        }
    });



    exampleTextarea = monaco.editor.create(document.getElementById('example-editor'), {
        language: 'json',
        values:"",
        verticalHasArrows: true,
        horizontalHasArrows: true,
        links:true,
        contextmenu:false,
        fontSize:12,
        lineHeight:18,
        Index: 'Advanced',
        scrollBeyondLastLine: false,
        minimap: {
            enabled: false // 关闭小地图
        }
    });

});


function loadHistoryScrollEvent() {
    $("#history-api-section .records").unbind("scroll").bind("scroll", function (e) {
        var sum = this.scrollHeight;
        if (sum <= $(this).scrollTop() + $(this).height()) {
            gdata.historyCurrPageNo = gdata.historyCurrPageNo +1;
            loadApiHistory(currApiInfo.id,gdata.historyCurrPageNo);
        }
    });
}

function loadKeyCodeEvent() {

    $(document).keydown(function (event) {
        if (event.keyCode == 87 && event.altKey){
            if (currPage == "example"){
                showEditorPanel();
            }else {
                showExamplePanel();
            }
        }
    });
}

function loadRemoteSyncChecboxEvent() {

    //隐藏
    $("#remote-sync").on("click",".level1 .icon-caret-down",function () {
        $(this).parents(".level1").find(">ul").hide();
        $(this).removeClass("icon-caret-down").addClass("icon-caret-right");
        return false;
    })

    $("#remote-sync").on("click",".level1 .icon-caret-right",function () {
        $(this).parents(".level1").find(">ul").show();
        $(this).removeClass("icon-caret-right").addClass("icon-caret-down");
        return false;
    })

    //根节点
    $("#remote-sync").on("click",".level0>.tree-entry input:checkbox",function (e) {
        let isChecked = $(this).prop('checked');
        if (isChecked){
            $(this).parents(".level0").find(".level1 .tree-entry input:checkbox").prop("checked","checked");
        }else{
            $(this).parents(".level0").find(".level1 .tree-entry input:checkbox").prop("checked","");
        }

        $("#remote-sync .items-count").text($("#remote-sync .level2>.tree-entry input:checkbox:checked").length+" item selected");
    });

    //二级节点
    $("#remote-sync ").on("click",".level1>.tree-entry input:checkbox",function (e) {
        let isChecked = $(this).prop('checked');
        if (isChecked){
            $(this).parents(".level1").find(".level2 .tree-entry input:checkbox").prop("checked","checked");
        }else{
            $(this).parents(".level1").find(".level2 .tree-entry input:checkbox").prop("checked","");
        }
        let checkedNum = $("#remote-sync .level1>.tree-entry input:checkbox:checked").length;
        if (checkedNum == 0){
            $("#remote-sync .level0>.tree-entry input:checkbox").prop("checked","");
        }else {
            $("#remote-sync .level0>.tree-entry input:checkbox").prop("checked","checked");
        }
        $("#remote-sync .items-count").text($("#remote-sync .level2>.tree-entry input:checkbox:checked").length+" item selected");
    });

    $("#remote-sync").on("click",".btn-link",function (e) {
        $(this).parent().find("input:checkbox").click();
    })

    //三级节点
    $("#remote-sync ").on("click",".level2>.tree-entry input:checkbox",function (e) {
        let checked2Num = $(this).parents(".level1").find(".level2>.tree-entry input:checkbox:checked").length;
        if (checked2Num == 0){
            $(this).parents(".level1").find(">.tree-entry input:checkbox").prop("checked","");
        }else{
            $(this).parents(".level1").find(">.tree-entry input:checkbox").prop("checked","checked");
        }

        let checkedNum = $("#remote-sync .level1>.tree-entry input:checkbox:checked").length;
        if (checkedNum == 0){
            $("#remote-sync .level0>.tree-entry input:checkbox").prop("checked","");
        }else{
            $("#remote-sync .level0>.tree-entry input:checkbox").prop("checked","checked");
        }
        $("#remote-sync .items-count").text($("#remote-sync .level2>.tree-entry input:checkbox:checked").length+" item selected");
    });
}

function loadEvent() {
    loadSelectBoxEvent();
    loadInputTypeEvent();
    loadEditAbleEvent();
    loadExampleMethodEvent();
    loadHistoryScrollEvent();
    loadLeftSideEvent();
    loadBottomSideEvent();
    loadRemoteSyncChecboxEvent()
    loadKeyCodeEvent()
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

function copyApi(e,id) {
    let cApi = null;
    $.each(gdata.apiList,function (index,item) {
        if (id == item.id){
            cApi = item;
            return false;
        }
    })
    delete cApi.id;
    cApi.comment = (cApi.comment?cApi.comment:cApi.path) +"-Copy"
    cApi.path = cApi.path+"-TEMP-"+uuid();
    cApi.script = editorTextarea.getValue();
    saveExecuter(cApi);
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
        "datasource":$("#editor-section").find(".api-info-datasource").attr("default-value"),
        "path": "TEMP-"+uuid(),
        "group": $(e).parents(".service").children(".name").attr("title"),
        "editor": editor,
        "comment": "Request",
        "script": "",
        "options":"{}"
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
                closeConfirmModal();
                data = unpackResult(data);
                if (data.code !=200){
                    openMsgModal(data.msg);
                    return;
                }
                $(e).parents(".request").remove();
                history.pushState(null,null,baseUrl);
            },complete:function () {
                hideSendNotify();
            }
        });
    });

    event.stopPropagation();    //  阻止事件冒泡
}

function buildExampleBodyJson() {
    let exampleBodyStr = buildExampleBodyStr();
    try {
        return JSON.parse(exampleBodyStr);
    }catch (e) {
        return {};
    }
}

function buildExampleBodyStr() {
    let type = $("#example-section .example-method").val();
    return (type == "POST" || type == "PUT")?exampleTextarea.getValue():"";
}

function runApi(debug) {
    let params = {
        "debug":debug,
        "script":editorTextarea.getValue(),
        "datasource":$("#editor-section .api-info-datasource").val(),
        "header": buildHeaderJson(getHeaderParams()),
        "body" : buildExampleBodyJson(),
        "pattern":$("#editor-section .api-info-path").val(),
        "options":buildApiOptionsJsonStr(),
        "url":$("#example-section .example-url").val()
    }

    showSendNotify("Running script");
    $("#bottom-side .console-bottom").html("");
    let startTime=new Date().getTime()
    $.ajax({
        type: "POST",
        url: runApiUrl,
        contentType : "application/json",
        data: JSON.stringify(params),
        success: function (data) {
            data = unpackResult(data);
            let content = "";
            if (data.data && data.data.logs){
                $.each(data.data.logs,function (index,item) {
                    content += item +"\r\n";
                })
            }

            if (data.code != 200){
                content += "<div style='color: #de6a53;background-color: none;letter-spacing: 1px;'>"+data.msg+"</div>";
            }else{
                content += "<a style='color:green;'>"+data.msg+"</a>";
            }

            content += "<p>--------------</p>";

            content += buildJsonStr((data.data && (data.data.data == 0 || data.data.data))?data.data.data:"There is no return value");

            $("#bottom-side .console-bottom").html(content);

        },complete:function () {
            hideSendNotify();
            hasConsole = true;
            let ms = (new Date().getTime()-startTime)+"ms";
            $("#bottom-side .el-time").attr("title",ms).text("Elapsed time: "+ms);
        }
    });

    if (debug){
        MtaH5.clickStat("debug_count")
    }else {
        MtaH5.clickStat("run_count")
    }
}

function buildJsonStr(obj) {
    if (typeof obj == "object"){
        return  JSON.stringify(obj,null, "\t");
    }
    return obj;
}

function insertExample(e) {
    let position = editorTextarea.getPosition();
    let express = $(e).attr("express");
    editorTextarea.executeEdits('', [
        {
            range: {
                startLineNumber: position.lineNumber,
                startColumn: position.column,
                endLineNumber: position.lineNumber,
                endColumn: position.column
            },
            text: express
        }
    ]);
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

function loadDetailByHistoryId(id, form) {
    showEditorPanel();

    let apiHistory = null;
    for(let i=0;i<gdata.apiHistoryList.length;i++){
        if (gdata.apiHistoryList[i].id == id){
            apiHistory = jQuery.extend(true, {}, gdata.apiHistoryList[i]);
            break;
        }
    }
    apiHistory.id = apiHistory.apiInfoId;

    loadDetail(apiHistory,form,function () {
        //构建example history
        loadExampleHistory(currApiInfo,true);

        //构建 api history
        loadApiHistory(currApiInfo.id,1);
    });

}

function loadDetailById(id,form,callback) {
    $.getJSON(getApiUrl+id,function (data) {
        data = unpackResult(data).data;
        loadDetail(data,form,callback);
    });

}

function loadDetail(apiInfo,form,callback) {

    cancelDiff();

    //init curr data
    hasConsole = null;
    hasResponse = null;
    currApiInfo = apiInfo;
    currApi = currApiInfo.id;
    //------构建editor
    $(".request").removeClass("selected");
    $(".service").removeClass("parent-selected");
    $(".request"+currApiInfo.id).addClass("selected");
    $(".request"+currApiInfo.id).parents(".service").addClass("parent-selected");
    $(".request"+currApiInfo.id).parents(".service").removeClass("collapsed");
    $(".request"+currApiInfo.id).parents(".service").find(".fa-caret-right").addClass("fa-caret-down").removeClass("fa-caret-right");
    $('#editor-section .draft-ribbon-text').text("Edit");

    let url = baseUrl+"?id="+currApiInfo.id+"&page="+(currPage?currPage:'example');
    history.pushState(null,null,url);
    removeAllQueryParameterForm("#bottom-side");

    $(form).find(".api-info-id").val(currApiInfo.id);
    $(form).find(".api-info-method").val(currApiInfo.method);
    $(form).find(".api-info-datasource").val(currApiInfo.datasource),
    $(form).find(".api-info-path").val(currApiInfo.path.startsWith("TEMP-")?"":currApiInfo.path).blur();
    $(form).find(".api-info-group").val(currApiInfo.group);
    $(form).find(".api-info-editor").val(currApiInfo.editor);
    $(form).find(".api-info-comment").val(currApiInfo.comment);

    $(form).find(".api-info-method").removeAttr("readonly").parent().removeClass("disabled");
    $(form).find(".api-info-path").removeAttr("readonly");
    $(form).find(".api-info-datasource").removeAttr("readonly");
    $(form).find(".api-info-datasource").parent().removeClass("disabled");

    if(currApiInfo.type == 'Code'){
        $(form).find(".api-info-method").attr("readonly","readonly");
        $(form).find(".api-info-method").parent().addClass("disabled");
        $(form).find(".api-info-path").attr("readonly","readonly");
        $(form).find(".api-info-datasource").attr("readonly","readonly");
        $(form).find(".api-info-datasource").parent().addClass("disabled");
    }

    document.title = currApiInfo.comment?currApiInfo.comment:currApiInfo.path;
    buildApiOptionsDom(currApiInfo.options);
    editorTextarea.setValue(currApiInfo.script?currApiInfo.script:"");

    //构建example history
    loadExampleHistory(currApiInfo,true);

    //构建 api history
    loadApiHistory(currApiInfo.id,1);

    //回调
    if (callback){
        callback();
    }
}

function apiOptionAdd(key,value) {
    key = key?key:"";
    value = value?value:"";
    let $form = $("#bottom-side");
    $form.find(".query-parameters-form-block").append("<div class=\"query-parameter-row active\" e2e-tag=\"query-parameter\"><span class=\"gwt-CheckBox query-parameter-cell\" title=\"Enable/Disable\" e2e-tag=\"query-parameter-state\"><label for=\"gwt-uid-2020\"></label></span><span class=\"expression-input input-append query-parameter-cell-name query-parameter-cell\"><input type=\"text\" class=\"gwt-TextBox key\" onchange='buildUrlInput()' value='"+key+"' placeholder=\"name\" e2e-tag=\"query-parameter-name\"><span class=\"add-on\" data-original-title=\"\" title=\"\"><i class=\"icon-magic\"></i></span></span><span class=\"gwt-InlineLabel query-parameter-cell\">=</span><span class=\"expression-input input-append query-parameter-cell-value query-parameter-cell\"><input type=\"text\" onchange='buildUrlInput()' class=\"gwt-TextBox value\" value='"+value+"' placeholder=\"value\" e2e-tag=\"query-parameter-value\"><span class=\"add-on\" data-original-title=\"\" title=\"\"><i class=\"icon-magic\"></i></span></span><button class=\"r-btn r-btn-link query-parameter-cell\" onclick='queryParameterRemove(this,\"#editor-section\")' title=\"Remove\" e2e-tag=\"query-parameter-remove\"><i class=\"fa fa-times-thin\"></i><span></span><span class=\"r-btn-indicator\" aria-hidden=\"true\" style=\"display: none;\"></span></button><div class=\"btn-group ctrls dropdown-secondary query-parameter-encoding query-parameter-cell\" e2e-tag=\"query-parameter-additional-actions\"><ul class=\"pull-right dropdown-menu\"><li class=\"dropdown-item\" e2e-tag=\"query-parameter-encode\"><a><i class=\"fa fa-check\"></i> <span>Encode before sending</span></a></li></ul></div></div>");
    $form.find(".query-parameters-form-block .query-parameter-row .key").focus();
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
    let group = $("#save-dialog .path-buttons .active").attr("title");
    let params={
        "id": $("#save-dialog").attr("data-id"),
        "method": $(form).find(".api-info-method").val(),
        "datasource":$(form).find(".api-info-datasource").val(),
        "path": $(form).find(".api-info-path").val(),
        "group": group?group:"公共API",
        "editor": $(form).find(".api-info-editor").val(),
        "comment": $("#save-dialog .input-xlarge").val(),
        "script": editorTextarea.getValue(),
        "options":buildApiOptionsJsonStr()
    }
    saveExecuter(params);
}

function buildApiOptionsJsonStr() {
    let list = $("#bottom-side .query-parameters-form-block>.active");
    let map = {};
    $.each(list,function (index,item) {
        let key = $(item).find(".key").val();
        if (!key)return;
        let value = $(item).find(".value").val();
        map[key] = value;
    })
    return JSON.stringify(map);
}

function buildApiOptionsDom(optionsJsonStr) {
    $("#bottom-side .query-parameters-form-block").html("")
    let map = {};
    if (optionsJsonStr){
        map = JSON.parse(optionsJsonStr);
    }
    map = $.extend({}, rocketUser.setting.options, map);
    $.each(map,function (key,value) {
        apiOptionAdd(key,value);
    })
}

function removeAllQueryParameterForm(e) {
    $(e).find(".query-parameters-form-block").html("");
    $(e).find(".subtitle-counter").text("["+$(e).find(".query-parameters-form-block>.active").length+"]");
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
    $("#save-dialog .input-xlarge").val($("#editor-section .api-info-comment").val());
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
        "options":buildApiOptionsJsonStr()
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
            currApi = data.data;
            loadApiList(false);
        },complete:function (req,data) {
            hideSendNotify();
        }
    });
    if (params.id){
        MtaH5.clickStat("api_save")
    }else{
        MtaH5.clickStat("api_new")
    }
}

function searchApi(e) {
    let keyword = $(e).val().trim();
    let searchResult = [];
    $.each(gdata.apiList,function (index,item) {
        if (keyword.split("=").length == 2){
            if (!item.options){
                return;
            }
            let kv = keyword.split("=");
            let options = JSON.parse(item.options);
            if (options[kv[0]] == kv[1]){
                searchResult.push(item);
            }
        }else if (item.comment.indexOf(keyword) >=0 || item.path.indexOf(keyword)>=0 || item.group.indexOf(keyword)>=0 || !keyword){
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
    $("#repository .api-counter").text("["+list.length+"]");
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
            $lev2.append('<li class="'+collapsed+' request level2 request'+item.id+'" ><div class="name" onclick="loadDetailById(\''+item.id+'\',\'#editor-section\')" title="'+(item.comment?item.comment:item.path)+'"><i\n' +
                '                                                        class="fa fa-caret-right invisible" aria-hidden="true"\n' +
                '                                                        e2e-tag="drive|'+(item.comment?item.comment:item.path)+'|expand"\n' +
                '                                                        style="display: none;"></i>\n' +
                '                                                    <div class="play-icon" title="Launch request"\n' +
                '                                                         e2e-tag="drive|'+(item.comment?item.comment:item.path)+'|play"><i\n' +
                '                                                            class="fa fa-play"></i></div>\n' +
                '                                                    <span class="gwt-InlineHTML node-text '+(item.type=='Code'?'fa fa-file-o':'')+'"\n' +
                '                                                          e2e-tag="drive|'+(item.comment?item.comment:item.path)+'">'+(item.comment?item.comment:item.path)+'<span style=\'margin-left:10px;color:#8a8989;\'>'+('['+item.path+']')+'</span></span>\n' +
                '                                                    <div class="status" aria-hidden="true" style="display: none;"></div>\n' +
                '                                                    <div class="btn-group ctrls dropdown-primary"  data-id="'+item.id+'" ><a\n' +
                '                                                            class="btn-mini dropdown-toggle" data-toggle="dropdown"\n' +
                '                                                            e2e-tag="drive|'+(item.comment?item.comment:item.path)+'|more"><i\n' +
                '                                                            class="sli-icon-options-vertical"></i></a>\n' +
                '                                                        <ul class="pull-right dropdown-menu">' +
                '<li class="dropdown-item"  title="复制" onclick="copyApi(this,\''+item.id+'\')"><a><i class="fa fa-copy"></i><span class="gwt-InlineHTML">Copy</span></a></li>' +
                '<li class="dropdown-item"  title="移动" onclick="moveApi(this,\''+item.id+'\')"><a><i class="fa fa-random"></i><span class="gwt-InlineHTML">Move</span></a></li>' +
                '<li class="dropdown-item"  title="文档同步" onclick="apiPush(\''+item.id+'\')"><a><i class="fa fa-cloud-upload"></i><span class="gwt-InlineHTML">Doc</span></a></li>' +
                '<li class="dropdown-item"  title="移除" onclick="removeApi(this,\''+item.id+'\')"><a><i class="fa fa-trash-o"></i><span class="gwt-InlineHTML">Trash</span></a></li>' +
                '</ul>\n' +
                '                                                    </div>\n' +
                '                                                </div>' +
                '</li>');
        })
        $lev1.append($lev2);
        $(".authenticated").append($lev1);
    })
}

function loadApiList(isDb) {
    $.getJSON(loadApiListUrl+"?isDb="+(isDb?true:false),function (data) {
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
            return false;
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
    newEditor();
    newExample();
    showEditorPanel();
    cancelDiff();
    loadApiHistory(null,1);
}

function newEditor() {
    //clean editor
    let form = "#editor-section";
    history.pushState(null,null,baseUrl);
    $(form).find(".api-info-id").val("");
    $(form).find(".api-info-method").val("GET");

    $(form).find(".api-info-path").val("");
    $(form).find(".api-info-datasource").val($(form).find(".api-info-datasource").attr("default-value"));
    $(form).find(".api-info-group").val("公共API");
    $(form).find(".api-info-editor").val("admin");
    $(form).find(".api-info-comment").val("Request");
    editorTextarea.setValue("");

    removeAllQueryParameterForm(form);

    //css
    $(form).find('.draft-ribbon-text').text("NEW");
    $(form).find(".api-info-method").removeAttr("readonly").parent().removeClass("disabled");
    $(form).find(".api-info-path").removeAttr("readonly");
    $(form).find(".api-info-datasource").removeAttr("readonly");
    $(form).find(".api-info-datasource").parent().removeClass("disabled");

    $("#editor-section .draft-ribbon").show();
    $("#example-section .draft-ribbon").show();

    hasConsole = null;
    currApiInfo = {};
    buildApiOptionsDom()
}

function newExample() {
    //clean example
    let form = "#example-section";
    $(form).find(".example-method").val("GET");
    $(form).find(".example-url").val(buildDefaultUrl("")).blur();
    $(form).find(".headers-form-block").html("");
    exampleTextarea.setValue("");
    $("#response").hide();
    currExample = {};
    hasResponse = null;
}




//--------------------------------example start -----------------------------------
function buildDefaultUrl(path) {
    let defaultUrl = baseUrl.substring(0,baseUrl.lastIndexOf("/"));
    return defaultUrl+(path.indexOf("TEMP-") == 0?"":path);
}

function loadExampleById(exampleId) {
    let example = null;
    for(let i=0;i<gdata.exampleHistoryList.length;i++){
        if (gdata.exampleHistoryList[i].id == exampleId){
            example = gdata.exampleHistoryList[i];
            break;
        }
    }
    loadExample(currApiInfo,example);
}

function loadExample(apiInfo,example) {

    let $form = $("#example-section");
    $form.find(".save-example-btn .changes-indicator").remove();

    //------构建example
    currExample = example?example:{
        apiInfoId:apiInfo.id,
        url:buildDefaultUrl(apiInfo.path),
        method:apiInfo.method,
        requestHeader:"{}",
        requestBody:"",
        responseHeader:"{}",
        responseBody:"",
        status:200,
        time:0,
        options:"{}"
    };
    $form.find(".example-method").val(currExample.method);
    $form.find(".example-url").val(currExample.url).blur();

    if (example){
        hasResponse = true;
    }
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
    $("#response #responseBody").text(formatJson(currExample.responseBody));

    if (currPage == 'example'){
        showExamplePanel();
    }else{
        showEditorPanel();
    }

}

function toNotSave() {
    let $form = $("#example-section");
    $form.find(".save-example-btn .changes-indicator").remove();
    $form.find(".save-example-btn").append('<div class="changes-indicator"></div>');
}

function formatJson(body){
    try{
        return JSON.stringify(JSON.parse(body), null, "    ")
    }catch (e) {
        return body;
    }
}

function saveExample() {
    let apiInfoId = $("#editor-section .api-info-id").val();
    if (apiInfoId == ""){
        openMsgModal("API is not stored ");
        return;
    }
    let params = {
        apiInfoId:apiInfoId,
        url:$("#example-section .example-url").val(),
        method:$("#example-section .example-method").val(),
        requestHeader:JSON.stringify(buildHeaderJson(getHeaderParams())),
        requestBody:buildExampleBodyStr(),
        responseHeader:currExample.responseHeader,
        responseBody:currExample.responseBody,
        status:currExample.status,
        time:currExample.time,
        options:currExample.options
    }
    showSendNotify("Saveing example")
    $.ajax({
        type: "POST",
        url: saveExampleUrl,
        contentType : "application/json",
        data: JSON.stringify(params),
        success: function (data) {
            data = unpackResult(data);
            if (data.code !=200){
                openMsgModal(data.msg);
                return;
            }
            $("#example-section .save-example-btn .changes-indicator").remove();

            //
            params.id = data.data;
            params.createTime = "now";
            gdata.exampleHistoryList.splice(0,0,params);
            let template = buildHistoryItemStr(params);
            $("#history-section .history tbody").prepend(template);
        },complete:function () {
            hideSendNotify();
        }
    });

    MtaH5.clickStat("example_save")
}
function requextUrlExample(ableRedirect) {
    let $form = $("#example-section");
    let url = $form.find(".example-url").val();
    requestExample(url,ableRedirect);
}

function buildHeaderJson(headerArrs) {
    let headers = {};
    $.each(headerArrs,function (index,item) {
        let split = item.indexOf(":");
        if (split == -1)return;
        let key = item.substring(0,split);
        let value = item.substring(split + 1);
        headers[key] = encodeURI(value);
    })
    return headers;
}

function requestExample(url,ableRedirect){

    toNotSave();

    let bodyParam = buildExampleBodyStr();
    let headers = buildHeaderJson(getHeaderParams());
    let type = $("#example-section .example-method").val();
    let startTime = new Date().getTime();
    $("#response").show();
    $("#response #responseBody").text("");
    setResponseHeader({});
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
            let responseHeader = buildHeaderJson(req.getAllResponseHeaders().split("\r\n"));
            let status = req.status;
            let currTime = new Date().getTime();
            currExample = {
                apiInfoId:$("#editor-section .api-info-id").val(),
                url:url,
                method:type,
                requestHeader:JSON.stringify(headers),
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
            $("#response #responseBody").text(formatJson(req.responseText));
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
    $.each(headers,function (key,value) {
        if (key.length == 0)return;
        $("#response .headers-form-table>tbody").append('<tr>\n' +
            '<td><a><span title="'+key+'">'+key+':</span></a></td>\n' +
            '<td><span class="gwt-InlineHTML" title="'+decodeURIComponent(value)+'">'+decodeURIComponent(value)+'</span></td></tr>')
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
    let $form = $("#example-section");
    $form.find(".query-parameters-form-block").html("");
    $("#example-section .subtitle-counter").text("")

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

function queryParameterRemove(e,form){
    $(e).parents(".query-parameter-row").remove();
    buildUrlInput();
    $(form).find(".subtitle-counter").text("["+$(form).find(".query-parameters-form-block>.active").length+"]");
}

function buildUrlInput() {
    let url = $("#example-section .example-url").val();
    url = url.substring(0,url.indexOf("?") == -1?url.length:url.indexOf("?"));
    $.each($("#example-section .query-parameters-form-block>.active"),function (index,item) {
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
    $("#example-section .example-url").val(url);
}

function queryParameterAdd(key,value) {
    key = key?key:"";
    value = value?value:"";
    let $form = $("#example-section");
    $form.find(".query-parameters-form-block").append("<div class=\"query-parameter-row active\" e2e-tag=\"query-parameter\"><span class=\"gwt-CheckBox query-parameter-cell\" title=\"Enable/Disable\" e2e-tag=\"query-parameter-state\"><input type=\"checkbox\" value=\"on\" onclick='urlTriggerEnable(this)'  tabindex=\"0\" checked=\"\"><label for=\"gwt-uid-2020\"></label></span><span class=\"expression-input input-append query-parameter-cell-name query-parameter-cell\"><input type=\"text\" class=\"gwt-TextBox key\" onchange='buildUrlInput()' value='"+key+"' placeholder=\"name\" e2e-tag=\"query-parameter-name\"><span class=\"add-on\" data-original-title=\"\" title=\"\"><i class=\"icon-magic\"></i></span></span><span class=\"gwt-InlineLabel query-parameter-cell\">=</span><span class=\"expression-input input-append query-parameter-cell-value query-parameter-cell\"><input type=\"text\" onchange='buildUrlInput()' class=\"gwt-TextBox value\" value='"+value+"' placeholder=\"value\" e2e-tag=\"query-parameter-value\"><span class=\"add-on\" data-original-title=\"\" title=\"\"><i class=\"icon-magic\"></i></span></span><button class=\"r-btn r-btn-link query-parameter-cell\" onclick='queryParameterRemove(this,\"#example-section\")' title=\"Remove\" e2e-tag=\"query-parameter-remove\"><i class=\"fa fa-times-thin\"></i><span></span><span class=\"r-btn-indicator\" aria-hidden=\"true\" style=\"display: none;\"></span></button><div class=\"btn-group ctrls dropdown-secondary query-parameter-encoding query-parameter-cell\" e2e-tag=\"query-parameter-additional-actions\"><a class=\"btn-mini dropdown-toggle\" data-toggle=\"dropdown\"><i class=\"sli-icon-options-vertical\"></i></a> <ul class=\"pull-right dropdown-menu\"><li class=\"dropdown-item\" e2e-tag=\"query-parameter-encode\"><a><i class=\"fa fa-check\"></i> <span>Encode before sending</span></a></li></ul></div></div>");
    $form.find(".query-parameters-form-block .query-parameter-row .key").focus();
    $("#example-section .subtitle-counter").text("["+$(".query-parameters-form-block>.active").length+"]");
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
    $(e).parents(".query-parameters-form").find(".subtitle-counter").text("["+$(e).parents(".query-parameters-form-block").find(">.active").length+"]");
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
    $("#example-section .headers-form-block").html("");
    $("#example-section .mode-raw textarea").val("");
}

function getHeaderParams() {
    let isForm = $("#example-section .headers-form-title .dropdown-toggle").text().trim() == 'Form';
    let headersParams = [];
    if (isForm){
        let headers = $("#example-section .headers-form-block>.active");
        $.each(headers,function (index,item) {
            let key = $(item).find(".key").val();
            let value = $(item).find(".value").val();
            headersParams.push(key+":"+value);
        });
    }else{
        headersParams = $("#example-section .mode-raw textarea").val().split(/[(\r\n)\r\n]+/);
    }
    return headersParams;
}

function setHeaderParams(headersParams) {
    headersParams = $.extend({},rocketUser.setting.header,headersParams);
    let isForm = $("#example-section .headers-form-title .dropdown-toggle").text().trim() == 'Form';
    if (isForm){
        $("#example-section .headers-form-block").html("");
        $.each(headersParams,function (key,value) {
            headerAdd(key,decodeURIComponent(value));
        });
    }else{
        let content = "";
        $.each(headersParams,function (key,value) {
            content +=(key+":"+decodeURIComponent(value)+"\r\n");
        });
        $("#example-section .mode-raw textarea").val(content);
    }
}

function getRawHeaders() {
    let headers = $("#example-section .mode-raw textarea").val().split(/[(\r\n)\r\n]+/);
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
    $("#example-section .headers-form-block").append(buildHeadItem(key,value));
}


function buildHeadItem(key,value) {
    key = key?key:"";
    value = decodeURIComponent(value?value:"");
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
    let isForm = $("#example-section .headers-form-title .dropdown-toggle").text().trim() == 'Form';
    if (isForm){
        return;
    }

    $("#example-section .mode-form").show();
    $("#example-section .mode-raw").hide();
    $(e).parents(".dropdown").children(".dropdown-toggle").html('<i></i> Form <span class="caret"></span>');
    $("#example-section .headers-form-block>.active").remove();

    let headers = $("#example-section .mode-raw textarea").val().split(/[(\r\n)\r\n]+/);
    let activeHeader = "";
    $.each(headers,function (index,item) {
        let split = item.indexOf(":");
        if (split == -1)return;
        let key = item.substring(0,split);
        let value = item.substring(split + 1);
        activeHeader += buildHeadItem(key,value)
    });
    $("#example-section .headers-form-block").prepend(activeHeader);

}
function showHeaderRaw(e) {
    let isForm = $("#example-section .headers-form-title .dropdown-toggle").text().trim() == 'Form';
    if (!isForm){
        return;
    }

    $("#example-section .mode-form").hide();
    $("#example-section .mode-raw").show();
    $(e).parents(".dropdown").children(".dropdown-toggle").html('<i></i> Raw <span class="caret"></span>');

    let content = "";
    let headers = $("#example-section .headers-form-block>.active");
    $.each(headers,function (index,item) {
        content += $(item).find(".key").val();
        content += ":";
        content += encodeURI($(item).find(".value").val());
        content += "\r\n";
    });
    $("#example-section .mode-raw textarea").val(content);
}

function formatExample() {
    exampleTextarea.setValue(formatJson(exampleTextarea.getValue()));
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
    let isForm = $("#example-section .headers-form-title .dropdown-toggle").text().trim() == 'Form';
    let key = "Content-Type";
    let value = "application/json";

    if (isJsonBody){
        $("#example-section .note").hide();
        $("#example-section .b-container").show();
        if (isForm){
            let exists = false;
            $.each($("#example-section .headers-form-block>.header-row"),function (index,item) {
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
            $("#example-section .mode-raw textarea").val(headers.join("\r\n"));
        }
    }else{
        $("#example-section .note").show();
        $("#example-section .note .note-method").text(option);
        $("#example-section .b-container").hide();
        if (isForm){
            $.each($("#example-section .headers-form-block input[value='"+key+"']"),function (index,item) {
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
            $("#example-section .mode-raw textarea").val(headerNews.join("\r\n"));
        }
    }
}

function loadExampleMethodEvent() {
    $("#example-section .uri-method .dropdown-menu>li").on("click",function () {
        switchExampleMethod($(this).text().trim());
    })
}

function showExamplePanel() {
    $("#example-panel").show();
    $("#editor-panel").hide();

    let urlParam = buildUrlParam();
    let url = window.location.href;
    if(urlParam.page){
        url = url.replace("page=editor","page=example");
        history.pushState(null,null,url);
    }
    currPage = "example";
    monaco.editor.setTheme("vs");
}

function buildUrlParam() {
    let query = window.location.search.substring(1);
    let vars = query.split("&");
    let urlParam = {};
    for (let i=0;i<vars.length;i++) {
        let pair = vars[i].split("=");
        urlParam[pair[0]] = pair[1];
    }
    return urlParam;
}

function showEditorPanel() {
    $("#example-panel").hide();
    $("#editor-panel").show();

    let urlParam = buildUrlParam();
    let url = window.location.href;
    if(urlParam.page){
        url = url.replace("page=example","page=editor");
        history.pushState(null,null,url);
    }
    currPage = "editor";
    monaco.editor.setTheme(languageTheme);
}
//--------------------------------example end -----------------------------------


//--------------------------------login start -----------------------------------
function hideLoginDialog() {
    $("#top-section .login-dialog").hide();
    $("#modal-backdrop").hide();
}

function showLoginDialog() {
    $("#top-section .login-error-message").text("");
    $("#top-section .login-dialog").show();
    $("#modal-backdrop").show();
}


function logout() {

    showSendNotify("logout ...");
    $.ajax({
        type: "post",
        url: logoutUrl,
        contentType : "application/json",
        success: function (data) {
            data = unpackResult(data);
            if (data.code !=200){
                openMsgModal(data.msg);
                return;
            }
            rocketUser.user.username = "";
            rocketUser.user.token = "";
            $.ajaxSetup({
                headers:{"rocket-user-token":""}
            });
            localStorage.setItem("rocketUser",JSON.stringify(rocketUser));
            $("#top-section .login-btn").show();
            $("#top-section .login-info").hide();
        },complete:function (req,data) {
            hideSendNotify();
        }
    });
}

function login() {
    let username = $("#top-section .username").val();
    let password = $("#top-section .password").val();

    showSendNotify("login ...");
    $.ajax({
        type: "post",
        url: loginUrl,
        contentType : "application/json",
        data: JSON.stringify({
            "username":username,
            "password":password
        }),
        success: function (data) {
            data = unpackResult(data);
            if (data.code !=200){
                $("#top-section .login-error-message").text(data.msg);
                return;
            }
            rocketUser.user.username = username;
            rocketUser.user.token = data.data;
            localStorage.setItem("rocketUser",JSON.stringify(rocketUser));
            $.ajaxSetup({
                headers:{"rocket-user-token":data.data}
            });
            $("#top-section .login-btn").hide();
            $("#top-section .login-info").show();
            $("#top-section .login-info .name").text(rocketUser.user.username);
            hideLoginDialog();
        },complete:function (req,data) {
            hideSendNotify();
        }
    });
}
//--------------------------------login end -----------------------------------

//--------------------------------example history start -----------------------------------
function showHistory() {
    $("#left-side .history").addClass("active");
    //$("#history-section").show();
    $("#history-api-section").show();

    $("#left-side .repository").removeClass("active");
    $("#repository").hide();
}

function showRepository() {
    $("#left-side .history").removeClass("active");
    //$("#history-section").hide();
    $("#history-api-section").hide();

    $("#left-side .repository").addClass("active");
    $("#repository").show();
}

function loadExampleHistory(apiInfo, isLoadExample) {
    $.getJSON(lastExampleUrl+"?limit=1&apiInfoId="+apiInfo.id,function (data) {
        data = unpackResult(data);
        gdata.exampleHistoryList = data.data;
        if (isLoadExample){
            loadExample(apiInfo,data.data.length>0?data.data[0]:null);
        }
        //buildExampleHistory();
    });

}

function buildExampleHistory(filter) {
    let list = gdata.exampleHistoryList;

    let $form = $("#history-section");
    $form.find(".history tbody").html("");
    $.each(list,function (index,item) {
        if (filter && (
            item.url.indexOf(filter) == -1
            && item.editor.indexOf(filter) == -1
            && item.method.indexOf(filter) == -1
            && item.status != filter
        )){
            return;
        }
        let template = buildHistoryItemStr(item);
        $form.find(".history tbody").append(template);
    })
}

function buildHistoryItemStr(item){
    return '<tr><td>' +
        '<div class="item">' +
        '   <label class="checkbox selector" >' +
        '   <input type="checkbox" value="on" onclick="selectExampleItem()" data-id="'+item.id+'" tabindex="0"><span></span>' +
        '   </label> ' +
        '   <div class="method method-'+(item.method.toLowerCase())+'" title="GET">'+item.method+'</div> ' +
        '   <div class="item-body"> ' +
        '       <div> ' +
        '           <a href="javascript:;" onclick="loadExampleById(\''+item.id+'\')" class="btn path btn-link" title="'+item.url+'"><i></i> '+item.url+' </a> ' +
        '       </div> ' +
        '       <div class="count">'+item.editor+'</div> ' +
        '       <div class="el-time" title="'+item.time+'ms">'+item.time+'ms</div> ' +
        '       <div class="el-time" title="'+item.createTime+'">'+item.createTime+'</div> ' +
        '       <div class="status response-ok" title="'+item.status+'">'+item.status+'</div> ' +
        '</div></div></td></tr>';
}

function selectExampleItem() {

    let num = $("#history-section .history tbody .selector>input:checked").length;

    if (num == 0){
        $("#history-section .ellipsis").addClass("disabled");
    }else{
        $("#history-section .ellipsis").removeClass("disabled");
    }
}

function exampleDeselect() {
    $("#history-section .history tbody .selector>input:checked").removeAttr("checked")
    selectExampleItem();
}

function exampleRemoveSelect() {
    exampleRemove($("#history-section .history tbody .selector>input:checked"))
}

function exampleRemoveAll() {
    exampleRemove($("#history-section .history tbody .selector>input"));
}

function exampleRemove(exList) {

    let apiExampleList = [];
    $.each(exList,function (index,item) {
        apiExampleList.push({"id":$(item).attr("data-id")});
    })

    openConfirmModal("The request will be permanently deleted. Are you sure?",function () {
        if (apiExampleList.length == 0){
            closeConfirmModal();
            return;
        };
        showSendNotify("Removeing example");
        $.ajax({
            type: "delete",
            url: deleteExampleUrl,
            contentType : "application/json",
            data: JSON.stringify({"apiExampleList":apiExampleList}),
            success: function (data) {
                closeConfirmModal();
                if (data.code !=200){
                    openMsgModal(data.msg);
                    return;
                }
                loadExampleHistory(currApiInfo,false);
                selectExampleItem();
            },complete:function () {
                hideSendNotify();
            }
        });
    });
}

function searchExample(e) {
    buildExampleHistory($(e).val())
}

//--------------------------------example history end -----------------------------------

//--------------------------------api history start -----------------------------------

function loadApiHistory(apiInfoId,pageNo) {
    if (!apiInfoId){
        apiInfoId = "";
    }
    gdata.historyCurrPageNo = pageNo;
    $.getJSON(lastApiUrl+"?pageSize=30&pageNo="+pageNo+"&apiInfoId="+apiInfoId,function (data) {
        data = unpackResult(data);
        if (pageNo == 1){
            gdata.apiHistoryList = [];
            $("#history-api-section .history tbody").html("");
        }
        $.each(data.data,function (index,item) {
            gdata.apiHistoryList.push(item);
        })
        buildApiHistory(data.data,null);
    });

}

function buildApiHistory(list,filter) {
    let $form = $("#history-api-section");
    $.each(list,function (index,item) {
        if (filter && (
            item.path.indexOf(filter) == -1
            && item.editor.indexOf(filter) == -1
            && item.method.indexOf(filter) == -1
            && item.datasource.indexOf(filter) == -1
        )){
            return;
        }
        let template = buildApiHistoryItemStr(item);
        $form.find(".history tbody").append(template);
    })
}

function buildApiHistoryItemStr(item){
    return '<tr><td>' +
        '<div class="item">' +
        '   <label class="checkbox selector" >' +
        '   <input type="checkbox" value="on" style="display: none" data-id="'+item.id+'" tabindex="0"><span></span>' +
        '   </label> ' +
        '   <div class="method method-'+(item.method.toLowerCase())+'" title="GET">'+item.method+'</div> ' +
        '   <div class="item-body"> ' +
        '       <div> ' +
        '           <a href="javascript:;" onclick="loadDetailByHistoryId(\''+item.id+'\',\'#editor-section\')" class="btn path btn-link" title="'+item.path+'"><i></i> '+item.path+' </a> ' +
        '       </div> ' +
        '       <div class="el-time" title="'+item.createTime+'">'+item.createTime+'</div> ' +
        '       <div class="count">'+item.editor+'</div> ' +
        '       <div class="el-time"><a onclick="showDiff(\''+item.id+'\')">Show Diff</a></div> ' +
        '       <div class="status response-ok" title="'+item.datasource+'">'+item.datasource+'</div> ' +
        '</div></div></td></tr>';
}

function searchApiHistory(e) {
    $("#history-api-section .history tbody").html("");
    buildApiHistory(gdata.apiHistoryList,$(e).val())
}

function showDiff(id) {
    let apiHistory = null;
    for(let i=0;i<gdata.apiHistoryList.length;i++){
        if (gdata.apiHistoryList[i].id == id){
            apiHistory = jQuery.extend(true, {}, gdata.apiHistoryList[i]);
            break;
        }
    }

    loadDetailById(apiHistory.apiInfoId,"#editor-section",function () {
        $("#editor-section .diff-body").show();
        $("#editor-section .code-body").hide();
        $("#diff-editor").html("");
        originalModel = monaco.editor.createModel(decodeURIComponent(apiHistory.script), languageName);
        modifiedModel = monaco.editor.createModel(editorTextarea.getValue(), languageName);
        let diffEditor = monaco.editor.createDiffEditor(document.getElementById("diff-editor"), {
            // You can optionally disable the resizing
            scrollBeyondLastLine:false,
            automaticLayout: true,
            enableSplitViewResizing: false
        });
        diffEditor.setModel({
            original: originalModel,
            modified: modifiedModel
        });
    })

}

function acceptLeft() {
    modifiedModel.setValue(originalModel.getValue());
}
function confirmDiff() {
    cancelDiff();
    editorTextarea.setValue(modifiedModel.getValue());
}
function cancelDiff() {
    $("#editor-section .diff-body").hide();
    $("#editor-section .code-body").show();
}
//--------------------------------api history end -----------------------------------


//--------------------------------api left-side start -----------------------------------
function loadLeftSideEvent(){
    $(".h-splitter").click(function () {
        $(this).hide();
        $("#left-side").show();
        let leftPx = rocketUser.panel.leftWidth;
        $("#left-side").css("width",leftPx);
        $(".content-view").css("left",leftPx);
        $(".h-divider").css("left",leftPx);
        rocketUser.panel.left = "show";
        localStorage.setItem("rocketUser",JSON.stringify(rocketUser));
    });

    $(".hide-pane-l").click(function () {
        $(".h-splitter").show();
        $("#left-side").hide();
        $(".content-view").css("left",10);
        $(".h-divider").css("left",0);
        rocketUser.panel.left = "hide";
        rocketUser.panel.leftWidth = $("#left-side").css("width");
        localStorage.setItem("rocketUser",JSON.stringify(rocketUser));
    });

    let dividerIsDown = false;
    $(".h-divider").on("mousedown",function () {
        dividerIsDown = true;
        document.onselectstart=new Function("event.returnValue=false;");
    })

    $(document).on("mouseup",function () {
        dividerIsDown = false;
        document.onselectstart=new Function("event.returnValue=true;");
    })

    $(document).on("mousemove",function (e) {
        if (!dividerIsDown)return;
        let x = e.pageX;
        if (x < 325){
            $(".h-splitter").show();
            $("#left-side").hide();
            $(".content-view").css("left",10);
            $(".h-divider").css("left",0);
            rocketUser.panel.leftWidth = "325px";
            rocketUser.panel.left = "hide";
        }else{
            $(".h-splitter").hide();
            $("#left-side").show();
            $(".content-view").css("left",x);
            $(".h-divider").css("left",x);
            $("#left-side").css("width",x);
            rocketUser.panel.left = "show";
            rocketUser.panel.leftWidth = x +"px";
        }

        localStorage.setItem("rocketUser",JSON.stringify(rocketUser));
    });

}

function loadBottomSideEvent() {
    $(".v-splitter").click(function () {
        $(".v-splitter").hide();
        $("#bottom-side").show();
        let bottom = rocketUser.panel.bottomHeight;
        $("#bottom-side").css("height",bottom);
        $("#editor-panel .ui-lay-c").css("bottom",bottom);
        $(".v-divider").show().css("bottom",Number(bottom.replace("px",""))+7+"px");
        rocketUser.panel.bottom = "show";
        localStorage.setItem("rocketUser",JSON.stringify(rocketUser));
    });

    $(".bottom-down").click(function () {
        $(".v-splitter").show();
        $("#bottom-side").hide();
        $(".v-divider").hide();
        $("#editor-panel .ui-lay-c").css("bottom",0);
        rocketUser.panel.bottom = "hide";
        localStorage.setItem("rocketUser",JSON.stringify(rocketUser));
    });

    let dividerVIsDown = false;
    $(".v-divider").on("mousedown",function () {
        dividerVIsDown = true;
        document.onselectstart=new Function("event.returnValue=false;");
    })

    $(document).on("mouseup",function () {
        dividerVIsDown = false;
        document.onselectstart=new Function("event.returnValue=true;");
    })

    $(document).on("mousemove",function (e) {
        if (!dividerVIsDown)return;
        let y = e.pageY;
        let bottom = $(document).height()-y;
        if (bottom <= 150){
            bottom = 0;
            $(".v-splitter").show();
            $("#bottom-side").hide();
            $(".v-divider").hide();
            rocketUser.panel.bottom = "hide";
            rocketUser.panel.bottomHeight = "150px";
        }else{
            $(".v-splitter").hide();
            $("#bottom-side").show();
            $(".v-divider").show();
            rocketUser.panel.bottom = "show";
            rocketUser.panel.bottomHeight = bottom+"px";
        }

        localStorage.setItem("rocketUser",JSON.stringify(rocketUser));
        $("#editor-panel .ui-lay-c").css("bottom",bottom);
        $(".v-divider").css("bottom",bottom+7);
        $("#bottom-side").css("height",bottom);
    });
}

function showBottomTab(target,e) {
    $("#bottom-side .bottom-pane-selector>li").removeClass("active");
    $(e).addClass("active");
    $("#bottom-side .bottom-tab>div").hide();
    $(target).show();
}

//--------------------------------api left-side end -----------------------------------

//-------------------------------- global setting start -------------------------------
function showGlobalConfig() {
    $("#global-setting").show();
    $("#global-setting .modal-body").html("");
    settingTextarea = monaco.editor.create($("#global-setting .modal-body")[0], {
        language: 'json',
        theme:"myTheme",
        value:formatJson(JSON.stringify(rocketUser.setting)),
        wordWrap: 'on',  //自行换行
        verticalHasArrows: true,
        horizontalHasArrows: true,
        scrollBeyondLastLine: false,
        contextmenu:false,
        automaticLayout: true,
        fontSize:13,
        minimap: {
            enabled: false // 关闭小地图
        }

    });
}
function hideGlobalConfig() {
    $("#global-setting").hide();
}

function saveGlobalConfig() {
    try {
        rocketUser.setting = JSON.parse(settingTextarea.getValue());
        localStorage.setItem("rocketUser",JSON.stringify(rocketUser));
        hideGlobalConfig();
    }catch (e) {
        openMsgModal(e);
    }

}
//-------------------------------- global setting end -------------------------------

//-------------------------------- api push end -------------------------------
function apiPush(apiInfoId) {
    showSendNotify("Pushing Doc")
    $.ajax({
        type: "GET",
        url: apiDocPushUrl,
        data:{ "apiInfoId": apiInfoId },
        success: function (data) {
            data = unpackResult(data);
            if (data.code !=200){
                openMsgModal(data.msg);
                return;
            }
            $("#repository>.buttons>div").text(data.data);
        },complete:function () {
            hideSendNotify();
        }
    });
}
//-------------------------------- api push end -------------------------------

//-------------------------------- api push start -------------------------------
function initCompletionItems() {
    $.get(completionItemsUrl,function (data) {
        data = unpackResult(data);
        gdata.completionItems = data.data;
    })
}
function buildMethodsForClazz(clazz) {
    $.ajax({
        type: "post",
        url: completionClazzUrl,
        contentType : "application/json",
        data: JSON.stringify({"clazz":clazz}),
        success: function (data) {
            data = unpackResult(data);
            gdata.completionItems.clazzs[clazz] = data.data;
        }
    });
}
//-------------------------------- api push end -------------------------------


//-------------------------------- Remote Sync start ------------------------------
function showRemoteSync() {
    $("#remote-sync").show();
    buildSelectApiTree(gdata.apiList,"collapsed")
}

function hideRemoteSync() {
    $("#remote-sync").hide();
}

function remoteSync(increment) {
    let apiInfoIds = [];
    if (increment){
        $.each($("#remote-sync .level2 input:checkbox:checked"),function () {
            apiInfoIds.push($(this).val())
        })
    }

    let params = {
        "increment":increment,
        "remoteUrl":$("#remote-sync .remote-url").val(),
        "apiInfoIds":apiInfoIds,
        "secretKey":$("#remote-sync .secret-key").val()
    }
    showSendNotify("Remote release")
    $.ajax({
        type: "post",
        url: remoteSyncUrl,
        contentType : "application/json",
        data: JSON.stringify(params),
        success: function (data) {
            data = unpackResult(data);
            if (data.code !=200){
                openMsgModal(data.msg);
                return;
            }
            $("#remote-sync .error-message").text("Remote release successful size:"+apiInfoIds.length)
        },complete:function () {
            hideSendNotify();
        }
    });
}

function searchSelectApi(e) {
    let keyword = $(e).val().trim();
    let searchResult = [];
    $.each(gdata.apiList,function (index,item) {
        if (keyword.split("=").length == 2){
            if (!item.options){
                return;
            }
            let kv = keyword.split("=");
            let options = JSON.parse(item.options);
            if (options[kv[0]] == kv[1]){
                searchResult.push(item);
            }
        }else if (item.comment.indexOf(keyword) >=0 || item.path.indexOf(keyword)>=0 || item.group.indexOf(keyword)>=0 || !keyword){
            searchResult.push(item);
        }
    });
    $("#remote-sync .items-count").text("0 item selected");
    buildSelectApiTree(searchResult,"");
}

function buildSelectApiTree(list,collapsed) {

    let group = {};
    $.each(list,function(index,item){
        let arrVal = group[item.group];
        if (!arrVal){
            arrVal = [];
            group[item.group] = arrVal;
        }
        arrVal.push(item);
    });
    $("#remote-sync .api-list-body").html("");
    //生成tree
    $.each(group,function (key,value) {
        let $lev1 = $('    <li class="level1 api" style="display: block;">\n' +
            '        <div class="tree-entry">\n' +
            '            <label class="checkbox">\n' +
            '            <input type="checkbox" value="on" tabindex="0">\n' +
            '            </label>\n' +
            '            <a href="javascript:;"  class="btn btn-link name">\n' +
            '                <i class="'+(collapsed?'icon-caret-right':'icon-caret-down')+'"></i>\n' +
            '                <i class="node-icon api-tester-icon api-tester-project"></i>\n' +
            '                <span class="gwt-InlineHTML node-text" >'+key+'</span>\n' +
            '            </a>\n' +
            '        </div>\n' +
            '    </li>');

        let $lev2 = $('<ul style="'+(collapsed?'display: none;':'display: block;')+'"></ul>');
        $.each(value,function (index,item) {
            $lev2.append('  <li class="level2 request">\n' +
                '                <div class="tree-entry">\n' +
                '                    <label class="checkbox" >\n' +
                '                        <input type="checkbox" value="'+item.id+'" tabindex="0">\n' +
                '                    </label>\n' +
                '                    <a href="javascript:;" class="btn btn-link name"><i></i>\n' +
                '                        <i class="node-icon api-tester-icon api-tester-request"></i>\n' +
                '                        <span class="gwt-InlineHTML node-text" >'+(item.comment?item.comment:item.path)+'<span style="margin-left:10px;color:#8a8989;">['+item.path+']</span></span>\n' +
                '                    </a>\n' +
                '                </div>\n' +
                '            </li>');
        })
        $lev1.append($lev2);
        $("#remote-sync .api-list-body").append($lev1);
    })
}
//-------------------------------- Remote Sync end ------------------------------