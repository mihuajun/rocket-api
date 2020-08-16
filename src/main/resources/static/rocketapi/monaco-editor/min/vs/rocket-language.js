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
    triggerCharacters:['.'],
    provideCompletionItems(model, position,item,token) {

        //变量匹配
        word = model.getValueInRange({
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: position.column-1,
            endColumn: position.column
        });
        if (word == "."){
            let varName = model.getWordUntilPosition({"lineNumber": position.lineNumber, "column": position.column-1}).word;
            if (varName){
                return {
                    suggestions: provideCompletionFunc(varName)
                };
            }
        }

        //
        word = model.getWordUntilPosition(position);
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

        //类型匹配
        return {
            suggestions: provideCompletionTypes(range,word.word,model.getValue())
        };

    }
});

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

function provideCompletionTypes(range,word,fullValue){

    let suggestions = [];
    if (!gdata.completionItems){
        return suggestions;
    }

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

    //全局CLASS类型
    $.each(gdata.completionItems.clazzs,function (key,value) {
        let label = key.substring(key.lastIndexOf(".")+1);
        suggestions.push({
            label: label,
            kind: monaco.languages.CompletionItemKind.Class,
            detail: key,
            insertText: label,
            filterText: buildFilterText(label),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            range: range
        })
    })


    //当前上下文单词
    let regex = /[a-zA-Z_]+/gim;
    let match = null;
    let wordSet = new Set();
    while((match = regex.exec(fullValue)) != null){
        let item = match[0]
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
    return suggestions;
}

function provideCompletionFunc(varName){
    let suggestions = [];
    let clazz = gdata.completionItems.variables[varName];
    if (!clazz)return suggestions;

    let methods = gdata.completionItems.clazzs[clazz];
    if (!methods){
        return suggestions;
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
            insertText = item.varName+"(${1})";
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
    return suggestions;
}

function buildFilterText(label) {
    return label.split("").join(" ");
}
