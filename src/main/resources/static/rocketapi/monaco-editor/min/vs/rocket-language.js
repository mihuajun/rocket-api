// Register a new language
let languageName = "rocket-language";
let languageTheme = "rocket-theme";
monaco.languages.register({ id: languageName});

// Register a tokens provider for the language
monaco.languages.setMonarchTokensProvider(languageName, {
    ignoreCase:true,
    defaultToken: '',
    tokenPostfix: '.java',

    keywords: [
        'abstract', 'continue', 'for', 'new', 'switch', 'assert', 'default',
        'goto', 'package', 'synchronized', 'boolean', 'do', 'if', 'private',
        'this', 'break', 'double', 'implements', 'protected', 'throw', 'byte',
        'else', 'import', 'public', 'throws', 'case', 'enum', 'instanceof', 'return',
        'transient', 'catch', 'extends', 'int', 'short', 'try', 'char', 'final',
        'interface', 'static', 'void', 'class', 'finally', 'long', 'strictfp',
        'volatile', 'const', 'float', 'native', 'super', 'while', 'true', 'false',
        'select','from','where','update','delete','insert','ignore','into','group','by','left','join','right','order','and','or'
    ],

    operators: [
        '=', '>', '<', '!', '~', '?', ':',
        '==', '<=', '>=', '!=', '&&', '||', '++', '--',
        '+', '-', '*', '/', '&', '|', '^', '%', '<<',
        '>>', '>>>', '+=', '-=', '*=', '/=', '&=', '|=',
        '^=', '%=', '<<=', '>>=', '>>>='
    ],

    // we include these common regular expressions
    symbols: /[=><!~?:&|+\-*\/\^%]+/,
    escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,
    digits: /\d+(_+\d+)*/,
    octaldigits: /[0-7]+(_+[0-7]+)*/,
    binarydigits: /[0-1]+(_+[0-1]+)*/,
    hexdigits: /[[0-9a-fA-F]+(_+[0-9a-fA-F]+)*/,

    // The main tokenizer for our languages
    tokenizer: {
        root: [
            // identifiers and keywords
            [/[a-zA-Z_$][\w$]*/, {
                cases: {
                    '@keywords': { token: 'keyword.$0' },
                    '@default': 'identifier'
                }
            }],

            // whitespace
            { include: '@whitespace' },

            // delimiters and operators
            [/[{}()\[\]]/, '@brackets'],
            [/[<>](?!@symbols)/, '@brackets'],
            [/@symbols/, {
                cases: {
                    '@operators': 'delimiter',
                    '@default': ''
                }
            }],

            // @ annotations.
            [/@\s*[a-zA-Z_\$][\w\$]*/, 'annotation'],

            // numbers
            [/(@digits)[eE]([\-+]?(@digits))?[fFdD]?/, 'number.float'],
            [/(@digits)\.(@digits)([eE][\-+]?(@digits))?[fFdD]?/, 'number.float'],
            [/0[xX](@hexdigits)[Ll]?/, 'number.hex'],
            [/0(@octaldigits)[Ll]?/, 'number.octal'],
            [/0[bB](@binarydigits)[Ll]?/, 'number.binary'],
            [/(@digits)[fFdD]/, 'number.float'],
            [/(@digits)[lL]?/, 'number'],

            // delimiter: after number because of .\d floats
            [/[;,.]/, 'delimiter'],

            // strings
            [/"([^"\\]|\\.)*$/, 'string.invalid'],  // non-teminated string
            [/#{.*}/, 'string.invalid'],  // non-teminated string
            [/"/, 'string', '@string'],

            // characters
            [/'[^\\']'/, 'string'],
            [/(')(@escapes)(')/, ['string', 'string.escape', 'string']],
            [/'/, 'string.invalid']
        ],

        whitespace: [
            [/[ \t\r\n]+/, ''],
            [/\/\*\*(?!\/)/, 'comment.doc', '@javadoc'],
            [/\/\*/, 'comment', '@comment'],
            [/\/\/.*$/, 'comment'],
        ],

        comment: [
            [/[^\/*]+/, 'comment'],
            // [/\/\*/, 'comment', '@push' ],    // nested comment not allowed :-(
            // [/\/\*/,    'comment.invalid' ],    // this breaks block comments in the shape of /* //*/
            [/\*\//, 'comment', '@pop'],
            [/[\/*]/, 'comment']
        ],
        //Identical copy of comment above, except for the addition of .doc
        javadoc: [
            [/[^\/*]+/, 'comment.doc'],
            // [/\/\*/, 'comment.doc', '@push' ],    // nested comment not allowed :-(
            [/\/\*/, 'comment.doc.invalid'],
            [/\*\//, 'comment.doc', '@pop'],
            [/[\/*]/, 'comment.doc']
        ],

        string: [
            [/[^\\"]+/, 'string'],
            [/@escapes/, 'string.escape'],
            [/\\./, 'string.escape.invalid'],
            [/"/, 'string', '@pop']
        ],
    },
})

//主题定义
monaco.editor.defineTheme(languageTheme, {
    base: 'vs-dark',
    inherit: true,
    rules: [{ background: 'EDF9FA' }],
    colors: {
        'editor.background': '#2b2b2b'
    }
});

monaco.languages.registerCompletionItemProvider(languageName, {
    triggerCharacters:['.',' ',',','#'],
    provideCompletionItems(model, position,item,token) {

        //当前光标所在的单词
        let word = model.getWordUntilPosition(position);

        let range = {
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: word.startColumn,
            endColumn: word.endColumn
        }

        //import 匹配
        let lineContent = model.getLineContent(position.lineNumber);
        if (lineContent.indexOf("import ") == 0){
            return {
                suggestions: provideCompletionImport(range)
            };
        }

        //sql脚本提示
        //是否在SQL引号范围内
        let sqlStr = getSqlStrContext('"""',model,position);
        if(!sqlStr){
            sqlStr = getSqlStrContext("'''",model,position);
        }
        //纯sql模式sql提示
        if (!sqlStr){
            sqlStr = getSimpleSqlStrContext(model,position);
        }
        if (sqlStr){
            return {
                suggestions: provideCompletionSqlInfo(model,position,range,lineContent,sqlStr)
            }
        }

        //方法自动提示
        //最后输入是否为 "."
        let point = model.getValueInRange({
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: position.column-1,
            endColumn: position.column
        });

        //最后输入"."前的变量
        let varNamePoint = model.getValueInRange({
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: word.startColumn-1,
            endColumn: word.startColumn
        });

        if (point == "." || varNamePoint == "."){
            let varName = null;
            if (point == "."){
                varName = model.getWordUntilPosition({"lineNumber": position.lineNumber, "column": position.column-1}).word;
            }else{
                varName = model.getWordUntilPosition({"lineNumber": position.lineNumber, "column": word.startColumn-1}).word;
            }

            if (varName){
                return {
                    suggestions: provideCompletionFunc(model.getValue(),varName)
                };
            }
        }

        //变量命名自动生成
        let definition = model.getWordUntilPosition({
            column:word.startColumn-1,
            lineNumber:position.lineNumber
        })
        if (definition.word && definition.word.substring(0,1).match("[A-Z]")){
            return {
                suggestions: provideCompletionDefinition(range,definition.word)
            };
        }

        //类型匹配
        return {
            suggestions: provideCompletionTypes(range,word.word,model.getValue())
        };

    }
});

function provideCompletionSqlInfo(model,position,range,lineContent,sqlStr) {
    let suggestions = [];
    let linePreStr = lineContent.substring(0,position.column-1);

    //sql内变量提示 #{},${}
    if (linePreStr.match(/#$/gi)){
        let tableNames = getTablesForSql(sqlStr);
        tableNames.forEach(function (index,tableName) {
            //遍历数据源
            $.each(gdata.completionItems.dbInfos,function (key,value) {
                //遍历表
                $.each(value,function (index2,table) {
                    if(table.name == tableName){
                        //遍历字段
                        let allField = "";
                        $.each(table.fields,function (index3,field) {
                            let label = "#{"+toHump(field.name)+"}";
                            allField += label+",";
                            suggestions.push({
                                label: label,
                                kind: monaco.languages.CompletionItemKind.Enum,
                                detail: (field.comment?field.comment:"")+"("+table.name+")",
                                filterText:buildFilterText(label),
                                insertText: label,
                                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                                range:{
                                    startLineNumber: range.startLineNumber,
                                    endLineNumber: range.endLineNumber,
                                    startColumn: range.startColumn-1,
                                    endColumn: range.endColumn
                                }
                            })
                        })

                        suggestions.push({
                            label: "#{}",
                            kind: monaco.languages.CompletionItemKind.Enum,
                            detail: "all field ("+table.name+")",
                            filterText:buildFilterText("#"),
                            insertText: allField.substring(0,allField.length-1),
                            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                            range:{
                                startLineNumber: range.startLineNumber,
                                endLineNumber: range.endLineNumber,
                                startColumn: range.startColumn-1,
                                endColumn: range.endColumn
                            }
                        })
                    }
                })
            })
        })
        return suggestions
    }

    //select
    //表名 + 字段提示
    if (linePreStr.match(/(select +)$/gi)){
        $.each(gdata.completionItems.dbInfos,function (key,value) {
            $.each(value,function (index,table) {
                let allField = "";
                $.each(table.fields,function (index3,field) {
                    allField += (field.name+",");
                })
                suggestions.push({
                    label: table.name,
                    kind: monaco.languages.CompletionItemKind.Enum,
                    detail: "all field ("+(table.comment?table.comment:table.name)+")",
                    filterText:buildFilterText(table.name),
                    insertText: allField.substring(0,allField.length-1) +" from "+table.name,
                    insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
                })
            })
        })
        return suggestions
    }

    //#表名提示
    //"insert into #{table}"
    //"insert ignore into #{table}"
    //"update #{table}"
    //"delete from #{table}"
    //"select * from #{table}"

    if (linePreStr.match(/((from +)|(into +)|(update +)|(join +))$/gi)){
        $.each(gdata.completionItems.dbInfos,function (key,value) {
            $.each(value,function (index,item) {
                suggestions.push({
                    label: item.name,
                    kind: monaco.languages.CompletionItemKind.Enum,
                    detail: item.comment,
                    filterText:buildFilterText(item.name),
                    insertText: item.name,
                    insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                })
            })
        })
        return suggestions
    }

    //字段提示
    //获取范围内的表名
    let tableNames = getTablesForSql(sqlStr);
    tableNames.forEach(function (index,tableName) {
        //遍历数据源
        $.each(gdata.completionItems.dbInfos,function (key,value) {
            //遍历表
            $.each(value,function (index2,table) {
                if(table.name == tableName){
                    //遍历字段
                    let allField = "";
                    $.each(table.fields,function (index3,field) {
                        allField += (field.name+",");
                        suggestions.push({
                            label: field.name,
                            kind: monaco.languages.CompletionItemKind.Enum,
                            detail: (field.comment?field.comment:"")+"("+table.name+")",
                            filterText:buildFilterText(field.name),
                            insertText: field.name,
                            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                        })
                    })

                    suggestions.push({
                        label: "*",
                        kind: monaco.languages.CompletionItemKind.Enum,
                        detail: "all field ("+table.name+")",
                        filterText:buildFilterText("*"),
                        insertText: allField.substring(0,allField.length-1),
                        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                        range:{
                            startLineNumber: range.startLineNumber,
                            endLineNumber: range.endLineNumber,
                            startColumn: range.startColumn-1,
                            endColumn: range.endColumn
                        }
                    })
                }
            })
        })
    })

    return suggestions
}

function toHump(name) {
    return name.replace(/\_(\w)/g, function(all, letter){
        return letter.toUpperCase();
    });
}

function getTablesForSql(sqlStr) {
    let tables = new Set();
    //"insert into #{table}"
    //"update #{table}"
    //"delete from #{table}"
    //"select * from #{table}"
    let matchs = sqlStr.match(/(from +[a-zA-Z_0-9]+)|(into +[a-zA-Z_0-9]+)|(update +[a-zA-Z_0-9]+)|(join +[a-zA-Z_0-9]+)/gim);
    $.each(matchs,function (index,item) {
        tables.add(item.substring(item.lastIndexOf(" ")+1));
    })
    return tables;
}

function getSqlStrContext(quotation,model,position) {
    let preMatchAll = model.findPreviousMatch(quotation+"sql",position,true,false,null,true);
    let preMatch = model.findPreviousMatch(quotation,position,true,false,null,true);
    if (!preMatchAll || !preMatch || preMatchAll.range.startLineNumber != preMatch.range.startLineNumber || preMatchAll.range.startColumn != preMatch.range.startColumn){
        return null;
    }
    let nextMatch = model.findNextMatch(quotation,position,false,false,null,true);
    if (!nextMatch){
        return null;
    }
    let sqlStr = model.getValueInRange({
        startLineNumber: preMatchAll.range.startLineNumber,
        startColumn: preMatchAll.range.endColumn,
        endLineNumber: nextMatch.range.startLineNumber,
        endColumn: nextMatch.range.startColumn
    });
    return sqlStr.trim();
}

function getSimpleSqlStrContext(model,position) {
    let value = model.getValue();
    let selectSqlPattern = /^(\s*select\s+)/gi;
    let insertSqlPattern = /^(\s*(replace|insert)\s+into\s+)/gi;
    let updateSqlPattern = /^(\s*update\s+[A-Za-z\-0-9_]+\s+set\s+)/gi;
    let deleteSqlPattern = /^(\s*delete\s+from\s+[A-Za-z\-0-9_]+)/gi;
    if (selectSqlPattern.test(value) || insertSqlPattern.test(value) || updateSqlPattern.test(value) || deleteSqlPattern.test(value)){
        return value;
    }
    return null;
}


function provideCompletionImport(range) {
    let suggestions = [];
    if (!gdata.completionItems.clazzs){
        return suggestions;
    }

    $.each(gdata.completionItems.clazzs,function (key,value) {
        let label = key.substring(key.lastIndexOf(".")+1);
        let detail = key.substring(0,key.lastIndexOf("."));
        suggestions.push({
            label: label,
            kind: monaco.languages.CompletionItemKind.Class,
            detail: detail,
            insertText: key,
            filterText: buildFilterText(label),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            range: range
        })
    })
    return suggestions;
}

function provideCompletionDefinition(range,word) {
    let suggestions = [];
    //首字母小写
    let str = "";
    for (let i=word.length-1;i>=0;i--){
        str = word.charAt(i) + str;
        if (word.charAt(i).match("[A-Z]")){
            let itemWord = str.replace(str[0],str[0].toLowerCase());
            suggestions.push({
                label: itemWord,
                kind: monaco.languages.CompletionItemKind.Variable,
                detail: "",
                filterText:buildFilterText(itemWord),
                insertText: itemWord,
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            })
        }
    }

    return suggestions;
}

function provideCompletionTypes(range,word,fullValue){

    let suggestions = [];
    if (!gdata.completionItems){
        return suggestions;
    }

    //当前上下文变量
    let regex = /[a-zA-Z0-9_]+ +=/gim;
    let match = null;
    let wordSet = new Set();
    while((match = regex.exec(fullValue)) != null){
        let item = match[0].substring(0,match[0].indexOf(" "));
        if (word && word == item){
            continue;
        }
        wordSet.add(item)
    }
    wordSet.forEach(function (index,item) {
        suggestions.push({
            label: item,
            kind: monaco.languages.CompletionItemKind.Variable,
            detail: item,
            insertText: item,
            filterText: buildFilterText(item),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            range: range
        })
    })

    //语法
    $.each(gdata.completionItems.syntax,function (key,value) {
        suggestions.push({
            label: key,
            kind: monaco.languages.CompletionItemKind.Struct,
            detail: value,
            insertText: value,
            filterText: buildFilterText(key),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            range: range
        })
    });

    //内置变量
    $.each(gdata.completionItems.variables,function (key,value) {
        suggestions.push({
            label: key,
            kind: monaco.languages.CompletionItemKind.Variable,
            detail: value,
            insertText: key,
            filterText: buildFilterText(key),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            range: range
        })
    });

    //全局CLASS类型 import的类
    regex = new RegExp("^(import) +.*[;]?",'gm');
    let matches = fullValue.match(regex);
    $.each(matches,function (index,item) {
        let key = item.replace(/import +/,"").replace(";","").trim();
        key = key.substring(key.lastIndexOf(".")+1);
        suggestions.push({
            label: key,
            kind: monaco.languages.CompletionItemKind.Variable,
            detail: item,
            insertText: key,
            filterText: buildFilterText(key),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            range: range
        })
    })
    return suggestions;
}

function provideCompletionTables() {
    let suggestions = [];


    return suggestions;
}

function provideCompletionFunc(context,varName){
    let suggestions = [];

    //全局内置变量
    let clazz = gdata.completionItems.variables[varName];
    parseClazz(clazz,suggestions)

    //上下文定义的变量
    clazz = findClazz(context,varName);
    parseClazz(clazz,suggestions)

    //链式调用
    return suggestions;
}

function parseClazz(clazz,suggestions) {
    if (!clazz)return;

    let methods = gdata.completionItems.clazzs[clazz];
    if (!methods || methods.length == 0){
        buildMethodsForClazz(clazz);
        methods = gdata.completionItems.clazzs[clazz];
        if (!methods || methods.length == 0){
            return;
        }
    }

    $.each(methods,function (index,item) {
        let label  = "";
        let insertText = "";
        let kind = monaco.languages.CompletionItemKind.Method;
        if (item.type == 'field'){
            label = item.varName +" ";
            insertText = item.varName;
            kind = monaco.languages.CompletionItemKind.Property;
        }else{
            label = item.varName +"("+item.params+") ";
            if (item.params){
                insertText = item.varName+"(${1})";
            }else{
                insertText = item.varName+"()";
            }

        }
        suggestions.push({
            label: label,
            kind: kind,
            detail: item.resultType.substring(item.resultType.lastIndexOf(".")+1),
            filterText:buildFilterText(label),
            insertText: insertText,
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        })
    });
}

function findClazz(context,varName) {
    let matchItem = context.match("[A-Z]{1}[a-zA-z]* +"+varName);
    let clazz = null;

    if (!matchItem){
        $.each(gdata.completionItems.clazzs,function (key,value) {
            if(key.substring(key.lastIndexOf(".")+1) == varName){
                clazz = key;
                return false;
            }
        })
        return clazz;
    }
    let clazzSimpleName = matchItem[0].substring(0,matchItem[0].indexOf(" "));
    let regex = new RegExp("^(import) +.*("+clazzSimpleName+")[;]?",'m');

    let importName = context.match(regex);
    if (importName){
        return importName[0].replace(/import +/,"").replace(";","").trim();
    }
    return null;
}

function buildFilterText(label) {
    return label.split("").join(" ");
}
