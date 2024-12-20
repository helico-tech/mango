package lang.mango.parser

import com.github.h0tk3y.betterParse.grammar.parseToEnd

object MangoParser {
    fun parse(input: String): AST.Program {
        return Grammar.parseToEnd(input)
    }
}