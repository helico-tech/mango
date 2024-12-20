package lang.mango.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.parser.Parser

object Grammar : Grammar<AST>() {

    val int by Tokens.number.use { AST.Constant.Integer(text.toInt()) }

    val constant: Parser<AST.Expression> = int

    val identifier : Parser<AST.Identifier> by Tokens.identifier.use { AST.Identifier(text) }

    // function calls
    val argumentsList: Parser<List<AST.Expression>> = (
            parser { expression } and zeroOrMore(Tokens.comma and parser { expression })
            )
        .map { (first, rest) ->
            listOf(first) + rest.map { (_, expression) -> expression }
        }

    val functionCall: Parser<AST.Expression> = (
            identifier and skip(Tokens.leftParenthesis) and optional(argumentsList) and skip(Tokens.rightParenthesis)
        )
        .map { (identifier, arguments) ->
            AST.FunctionCall(identifier, arguments ?: emptyList())
        }

    // expressions
    val notTerm: Parser<AST.UnaryOperation> = (
            Tokens.not and parser { expression }
            )
        .map { (_, expression) ->
            AST.UnaryOperation(Tokens.not.name!!, expression)
        }

    val parenthesesTerm: Parser<AST.Expression> = (
            skip(Tokens.leftParenthesis) and parser { expression } and skip(Tokens.rightParenthesis)
            )

    val primary: Parser<AST.Expression> = parenthesesTerm or functionCall or constant  or identifier

    val term: Parser<AST.Expression> = notTerm or primary

    val multiplyDivide: Parser<AST.Expression> = leftAssociative(term, Tokens.multiply or Tokens.divide or Tokens.modulo) { left, op, right ->
        AST.BinaryOperation(left, op.type.name!!, right)
    }

    val plusMinus: Parser<AST.Expression> = leftAssociative(multiplyDivide, Tokens.plus or Tokens.minus) { left, op, right ->
        AST.BinaryOperation(left, op.type.name!!, right)
    }

    val comparison: Parser<AST.Expression> = leftAssociative(plusMinus, Tokens.notEqual or Tokens.doubleEqual or Tokens.lessThan or Tokens.lessThanOrEqual or Tokens.greaterThan or Tokens.greaterThanOrEqual) { left, op, right ->
        AST.BinaryOperation(left, op.type.name!!, right)
    }

    val arithmetic = comparison

    val expression: Parser<AST.Expression> = arithmetic

    // statements
    val assignment: Parser<AST.Assignment> = (
            identifier and skip(Tokens.equal) and parser { expression }
        ).map { (identifier, expression) -> AST.Assignment(identifier, expression)
    }

    val valueDeclaration: Parser<AST.Declaration> = (
            Tokens.let and assignment
        )
        .map { (_, assignment) -> AST.ValueDeclaration(assignment.identifier, assignment.expression)
    }

    val parameterList: Parser<List<AST.Identifier>> = (
            identifier and zeroOrMore(Tokens.comma and identifier)
        )
        .map { (first, rest) -> listOf(first) + rest.map { (_, identifier) -> identifier }
    }

    val block: Parser<List<AST.Statement>> = (
        skip(Tokens.leftBrace) and
        zeroOrMore(parser { statement }) and
        skip(Tokens.rightBrace)
    )

    val statement : Parser<AST.Statement> by (
        valueDeclaration or expression
    )

    val functionDeclaration: Parser<AST.FunctionDeclaration> = (
        skip(Tokens.fn) and
        identifier and
        skip(Tokens.equal) and
        skip(Tokens.leftParenthesis) and
        optional(parameterList) and
        skip(Tokens.rightParenthesis) and
        block
    )
    .map { (identifier, parameters, block) -> AST.FunctionDeclaration(identifier, parameters ?: emptyList(), block) }

    val program = zeroOrMore(functionDeclaration).map { declarations -> AST.Program(declarations) }

    override val rootParser: Parser<AST> = program

    override val tokens: List<Token> = Tokens
}