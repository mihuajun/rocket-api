/**
 * Minified by jsDelivr using UglifyJS v3.1.10.
 * Original file: /npm/codemirror-formatting@1.0.0/formatting.js
 *
 * Do NOT use SRI with dynamically generated files! More information: https://www.jsdelivr.com/using-sri-with-dynamic-files
 */
!function(e) {
    "object" == typeof exports && "object" == typeof module ? e(require("codemirror/lib/codemirror")) : "function" == typeof define && define.amd ? define(["codemirror/lib/codemirror"], e) : e(CodeMirror)
}(function(e) {
    e.extendMode("css", {
        commentStart: "/*",
        commentEnd: "*/",
        newlineAfterToken: function(e, t) {
            return /^[;{}]$/.test(t)
        }
    }),
        e.extendMode("javascript", {
            commentStart: "/*",
            commentEnd: "*/",
            newlineAfterToken: function(e, t, n, o) {
                return this.jsonMode ? /^[\[,{]$/.test(t) || /^}/.test(n) : (";" != t || !o.lexical || ")" != o.lexical.type) && (/^[;{}]$/.test(t) && !/^;/.test(n))
            }
        });
    var t = /^(a|abbr|acronym|area|base|bdo|big|br|button|caption|cite|code|col|colgroup|dd|del|dfn|em|frame|hr|iframe|img|input|ins|kbd|label|legend|link|map|object|optgroup|option|param|q|samp|script|select|small|span|strong|sub|sup|textarea|tt|var)$/;
    e.extendMode("xml", {
        commentStart: "\x3c!--",
        commentEnd: "--\x3e",
        newlineAfterToken: function(e, n, o, r) {
            var i = !1;
            return "html" == this.configuration && (i = !!r.context && t.test(r.context.tagName)),
            !i && ("tag" == e && />$/.test(n) && r.context || /^</.test(o))
        }
    }),
        e.defineExtension("commentRange", function(t, n, o) {
            var r = this
                , i = e.innerMode(r.getMode(), r.getTokenAt(n).state).mode;
            r.operation(function() {
                if (t)
                    r.replaceRange(i.commentEnd, o),
                        r.replaceRange(i.commentStart, n),
                    n.line == o.line && n.ch == o.ch && r.setCursor(n.line, n.ch + i.commentStart.length);
                else {
                    var e = r.getRange(n, o)
                        , a = e.indexOf(i.commentStart)
                        , c = e.lastIndexOf(i.commentEnd);
                    a > -1 && c > -1 && c > a && (e = e.substr(0, a) + e.substring(a + i.commentStart.length, c) + e.substr(c + i.commentEnd.length)),
                        r.replaceRange(e, n, o)
                }
            })
        }),
        e.defineExtension("autoIndentRange", function(e, t) {
            var n = this;
            this.operation(function() {
                for (var o = e.line; o <= t.line; o++)
                    n.indentLine(o, "smart")
            })
        }),
        e.defineExtension("autoFormatRange", function(t, n) {
            function o() {
                m += "\n",
                    d = !0,
                    ++l
            }
            for (var r = this, i = r.getMode(), a = r.getRange(t, n).split("\n"), c = e.copyState(i, r.getTokenAt(t).state), s = r.getOption("tabSize"), m = "", l = 0, d = 0 === t.ch, f = 0; f < a.length; ++f) {
                for (var g = new e.StringStream(a[f],s); !g.eol(); ) {
                    var u = e.innerMode(i, c)
                        , p = i.token(g, c)
                        , b = g.current();
                    g.start = g.pos,
                    d && !/\S/.test(b) || (m += b,
                        d = !1),
                    !d && u.mode.newlineAfterToken && u.mode.newlineAfterToken(p, b, g.string.slice(g.pos) || a[f + 1] || "", u.state) && o()
                }
                !g.pos && i.blankLine && i.blankLine(c),
                !d && f < a.length - 1 && o()
            }
            r.operation(function() {
                r.replaceRange(m, t, n);
                for (var e = t.line + 1, o = t.line + l; e <= o; ++e)
                    r.indentLine(e, "smart");
                r.setSelection(t, r.getCursor(!1))
            })
        })
});
