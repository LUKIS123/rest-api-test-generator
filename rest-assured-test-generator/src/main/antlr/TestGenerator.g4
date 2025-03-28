grammar TestGenerator;

program : test* EOF ;

// Entry point
test    : 'TEST' NAME '{' request assert '}';

// Request definition
request : 'REQUEST' '{' method url (headers)? (queryParams)? (body)? '}';

// HTTP method
method  : 'METHOD' HTTP_METHOD;

// URL
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
assert  : 'ASSERT' '{' statusCode (responseBody)? (responseHeaders)? '}';
statusCode : 'STATUS' INT;
responseBody : 'BODY_CONTAINS' STRING;
responseHeaders : 'HEADER' '{' header+ '}';

// Tokens
HTTP_METHOD : 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
NAME    : [a-zA-Z_][a-zA-Z0-9_]*;
STRING  : '"' (~["])* '"';
INT     : [0-9]+;


//NEWLINE : [\r\n]+ -> skip;
NEWLINE : [\r\n]+ -> channel(HIDDEN);

//WS : [ \t]+ -> skip ;
WS : [ \t]+ -> channel(HIDDEN) ;

COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;
LINE_COMMENT : '//' ~'\n'* '\n' -> channel(HIDDEN) ;
