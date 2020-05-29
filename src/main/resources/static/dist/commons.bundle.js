!function(e, t) {
    "object" == typeof exports && "object" == typeof module ? module.exports = t() : "function" == typeof define && define.amd ? define([], t) : "object" == typeof exports ? exports.commons = t() : e.commons = t()
}(this, (function() {
        return function(e) {
            function t(t) {
                for (var r, u, c = t[0], i = t[1], d = t[2], l = 0, f = []; l < c.length; l++)
                    u = c[l],
                    Object.prototype.hasOwnProperty.call(o, u) && o[u] && f.push(o[u][0]),
                        o[u] = 0;
                for (r in i)
                    Object.prototype.hasOwnProperty.call(i, r) && (e[r] = i[r]);
                for (s && s(t); f.length; )
                    f.shift()();
                return a.push.apply(a, d || []),
                    n()
            }
            function n() {
                for (var e, t = 0; t < a.length; t++) {
                    for (var n = a[t], r = !0, c = 1; c < n.length; c++) {
                        var i = n[c];
                        0 !== o[i] && (r = !1)
                    }
                    r && (a.splice(t--, 1),
                        e = u(u.s = n[0]))
                }
                return e
            }
            var r = {}
                , o = {
                0: 0
            }
                , a = [];
            function u(t) {
                if (r[t])
                    return r[t].exports;
                var n = r[t] = {
                    i: t,
                    l: !1,
                    exports: {}
                };
                return e[t].call(n.exports, n, n.exports, u),
                    n.l = !0,
                    n.exports
            }
            u.m = e,
                u.c = r,
                u.d = function(e, t, n) {
                    u.o(e, t) || Object.defineProperty(e, t, {
                        enumerable: !0,
                        get: n
                    })
                }
                ,
                u.r = function(e) {
                    "undefined" != typeof Symbol && Symbol.toStringTag && Object.defineProperty(e, Symbol.toStringTag, {
                        value: "Module"
                    }),
                        Object.defineProperty(e, "__esModule", {
                            value: !0
                        })
                }
                ,
                u.t = function(e, t) {
                    if (1 & t && (e = u(e)),
                    8 & t)
                        return e;
                    if (4 & t && "object" == typeof e && e && e.__esModule)
                        return e;
                    var n = Object.create(null);
                    if (u.r(n),
                        Object.defineProperty(n, "default", {
                            enumerable: !0,
                            value: e
                        }),
                    2 & t && "string" != typeof e)
                        for (var r in e)
                            u.d(n, r, function(t) {
                                return e[t]
                            }
                                .bind(null, r));
                    return n
                }
                ,
                u.n = function(e) {
                    var t = e && e.__esModule ? function() {
                            return e.default
                        }
                        : function() {
                            return e
                        }
                    ;
                    return u.d(t, "a", t),
                        t
                }
                ,
                u.o = function(e, t) {
                    return Object.prototype.hasOwnProperty.call(e, t)
                }
                ,
                u.p = "";
            var c = this.webpackJsonpcommons = this.webpackJsonpcommons || []
                , i = c.push.bind(c);
            c.push = t,
                c = c.slice();
            for (var d = 0; d < c.length; d++)
                t(c[d]);
            var s = i;
            return a.push([15, 1]),
                n()
        }({
            15: function(e, t, n) {
                "use strict";
                n.r(t),
                    n.d(t, "evaluateXpath", (function() {
                            return m
                        }
                    )),
                    n.d(t, "getXmlChildrenTags", (function() {
                            return O
                        }
                    )),
                    n.d(t, "evaluateXpathWithRhino", (function() {
                            return g
                        }
                    )),
                    n.d(t, "dateFns", (function() {
                            return j
                        }
                    )),
                    n.d(t, "math", (function() {
                            return k
                        }
                    ));
                var r = n(10)
                    , o = n(6)
                    , a = n(11)
                    , u = n(5)
                    , c = n(12)
                    , i = n(13)
                    , d = n(14)
                    , s = n(4)
                    , l = n(2)
                    , f = n(9)
                    , p = n(7)
                    , b = n.n(p);
                function m(e, t) {
                    return b.a.evaluate(e, function(e) {
                        return (new f.DOMParser).parseFromString(e)
                    }(t), null, b.a.XPathResult.ANY_TYPE, null)
                }
                var y = "ARRAY_ITEM"
                    , h = "SINGLE_ELEMENT"
                    , v = "ATTRIBUTE";
                function O(e, t) {
                    var n = Object(l.map)(m("".concat(e, "/*"), t).nodes, (function(e) {
                            return e.nodeName
                        }
                    ))
                        , r = Object(l.reduce)(n, (function(e, t) {
                            var n = Object(l.cloneDeep)(e)
                                , r = e[t] ? e[t].total + 1 : 1;
                            return Object(l.set)(n, t, {
                                index: 1,
                                total: r
                            })
                        }
                    ), {})
                        , o = Object(l.map)(n, (function(e) {
                            if (r[e].total > 1) {
                                var t = r[e].index++;
                                return {
                                    type: y,
                                    key: e,
                                    index: t,
                                    selector: "/".concat(e, "[").concat(t, "]")
                                }
                            }
                            return {
                                type: h,
                                key: e,
                                selector: "/".concat(e)
                            }
                        }
                    ))
                        , a = Object(l.map)(m("".concat(e, "/@*"), t).nodes, (function(e) {
                            var t = e.nodeName;
                            return {
                                type: v,
                                key: t,
                                selector: "/@".concat(t)
                            }
                        }
                    ));
                    return Object(l.concat)(o, a)
                }
                function g(e, t) {
                    var n, r = m(e, t);
                    switch (r.resultType) {
                        case 0:
                            n = "Any";
                            break;
                        case 1:
                            n = "Number";
                            break;
                        case 2:
                            n = "String";
                            break;
                        case 3:
                            n = "Boolean";
                            break;
                        case 4:
                            n = "UnorderedNodeIterator";
                            break;
                        case 5:
                            n = "OrderedNodeIterator";
                            break;
                        case 6:
                            n = "UnorderedNodeSnapshot";
                            break;
                        case 7:
                            n = "OrderedNodeSnapshot";
                            break;
                        case 8:
                            n = "AnyUnorderedNode";
                            break;
                        case 9:
                            n = "FirstOrderedNode";
                            break;
                        default:
                            throw new Error("Unsupported XPath result type ".concat(r.resultType))
                    }
                    return JSON.stringify({
                        resultType: n,
                        booleanValue: r.booleanValue,
                        stringValue: r.stringValue,
                        numberValue: r.numberValue,
                        matchingNodes: r.nodes ? r.nodes.map((function(e) {
                                return {
                                    name: e.name,
                                    stringValue: e.toString()
                                }
                            }
                        )) : []
                    })
                }
                var j = {
                    addYears: r.a,
                    addMonths: o.a,
                    addWeeks: a.a,
                    addDays: u.a,
                    addHours: c.a,
                    addMinutes: i.a,
                    addSeconds: d.a
                }
                    , k = {
                    add: function(e, t) {
                        return s.Decimal.add(e, t)
                    },
                    subtract: function(e, t) {
                        return s.Decimal.add(e, s.Decimal.mul(t, -1))
                    },
                    multiply: function(e, t) {
                        return s.Decimal.mul(e, t)
                    },
                    divide: function(e, t) {
                        return s.Decimal.div(e, t)
                    }
                }
            }
        })
    }
));
