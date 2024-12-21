package lang.mango.parser

import com.github.h0tk3y.betterParse.grammar.parseToEnd

object MangoParser {
    fun parse(input: String): AST.Program {
        return validate(Grammar.parseToEnd(input))
    }

    private fun validate(program: AST.Program): AST.Program {
        val visitor = NestedFunctionValidatorVisitor()
        visitor.visit(program)
        return program
    }
}