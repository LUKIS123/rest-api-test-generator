grammar TestGenerator;

program : classDef* EOF;

// Class definition
classDef    : 'CLASS' CLASS_NAME '{' baseUrl? test* '}';

// Test definition
test    : 'TEST' NAME '{' request validate '}';

// Request definition
request : 'REQUEST' '{' method requestElement* '}';
requestElement  : url | headers | queryParams | body;

// HTTP method
method  : 'METHOD' HTTP_METHOD (STRING)?;

// URL
baseUrl : 'URL' STRING;
url     : 'URL' STRING;

// Headers
headers : 'HEADERS' '{' header+ '}';
header  : STRING ':' STRING;

// Query parameters
queryParams : 'QUERY_PARAMS' '{' queryParam+ '}';
queryParam  : STRING '=' STRING;

// Request body
body    : 'BODY' STRING;

// Assertions
validate       : 'ASSERT' '{' validateElement+ '}';
validateElement : statusCode | responseBody | responseHeaders;

statusCode     : 'STATUS' INT;
responseBody   : (bodyContains | bodyExact)+;
bodyContains   : 'BODY_CONTAINS' STRING+;       // Check if a field exists
bodyExact      : 'BODY_EXACT' bodyExactPair+;   // Check if a field has an exact value
bodyExactPair  : STRING '=' STRING;
responseHeaders: 'HEADER' header+;

// Tokens
HTTP_METHOD : 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
NAME    : [a-zA-Z_][a-zA-Z0-9_]*;
CLASS_NAME    : [a-zA-Z_][a-zA-Z0-9_.]*;
STRING  : '"' (~["])* '"';
INT     : [0-9]+;


//NEWLINE : [\r\n]+ -> skip;
NEWLINE : [\r\n]+ -> channel(HIDDEN);

//WS : [ \t]+ -> skip ;
WS : [ \t]+ -> channel(HIDDEN) ;

COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;
LINE_COMMENT : '//' ~'\n'* '\n' -> channel(HIDDEN) ;
