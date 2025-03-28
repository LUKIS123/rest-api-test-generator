parser grammar TestGeneratorParser;
options { tokenVocab=TestGeneratorLexer; }

program
    : stat EOF
    | def EOF
    ;

stat: ID '=' expr ';'
    | expr ';'
    ;

def : ID '(' ID (',' ID)* ')' '{' stat* '}' ;

expr: ID
    | INT
    | func
    | 'not' expr
    | expr 'and' expr
    | expr 'or' expr
    | '(' expr ')' // Add parentheses
    | expr ('*'|'/') expr // Add arithmetic operators
    | expr ('+'|'-') expr // Add arithmetic operators
    ;

func : ID '(' expr (',' expr)* ')' ;
