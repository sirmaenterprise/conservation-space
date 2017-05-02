/**
  * Sirma query language grammar
  */
  
grammar Sirma;
options {language=Java;}

@header {
package com.sirma.itt.emf.semantic.query.grammar;
import java.util.*;
import com.sirma.itt.emf.semantic.query.*;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.openrdf.repository.RepositoryConnection;
}
 	
@members {
private PredicateValidator predicateValidator;

public void setPredicateValidator(PredicateValidator validator) {
	this.predicateValidator = validator;
}
}
 	
query : FIND object WHERE orClause+ 
			| orClause+ ; //orderBy sort;
			
subStatement : LPAREN orClause+ RPAREN ;
	
clause : booleanClause (AND booleanClause)* ;

orClause : clause (OR clause)* ;

booleanClause : subject operator value {System.out.println($subject.v);System.out.println($operator.op);System.out.println($value.v);}
			| subject IN listOfValues 
			| relation value;

listOfValues : LPAREN value (',' value)* RPAREN ;

object returns [String v] : ALL {$v = "ALL"; }
				| WORD {$v = predicateValidator.getClassURI($WORD.text); } ;

subject returns [String v] : WORD {$v = predicateValidator.getPredicateURI($WORD.text); } ;

value returns [Serializable v] : WORD {$v = $WORD.text; } | date {$v = $date.dValue; } ;// | INTEGER {$v = Long.parseLong($INTEGER.text); };

/*
 * Parse the current operator.
 */
operator returns [Operator op] : EQUALS { $op = Operator.EQUALS; }
	| NOT_EQUALS { $op = Operator.NOT_EQUALS; }
	| LIKE { $op = Operator.LIKE; }
	| NOT_LIKE { $op = Operator.NOT_LIKE; }
	| LT { $op = Operator.LESS_THAN; }
	| GT { $op = Operator.GREATER_THAN; }
	| LTEQ { $op = Operator.LESS_THAN_EQUALS; }
	| GTEQ { $op = Operator.GREATER_THAN_EQUALS; }
	| NOT IN { $op = Operator.NOT_IN; }
	| IN { $op = Operator.IN; };
	
relation returns [String rel] : ('has Design'|'has Relation') {$rel = "ALL"; System.out.println($relation.text);} ;

date returns [Date dValue]
 : DATE
   {
     if($text.matches("^(0?[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)\\d\\d$")) {
     	SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
     	try {
        	$dValue = formatter.parse($text);
        } catch (ParseException e) {
			e.printStackTrace();
		}
     }
   } | DATEFULL {
   	if ($text.matches("^(?=\\d)(?:(?!(?:(?:0?[5-9]|1[0-4])(?:\\.|-|\\/)10(?:\\.|-|\\/)(?:1582))|(?:(?:0?[3-9]|1[0-3])(?:\\.|-|\\/)0?9(?:\\.|-|\\/)(?:1752)))(31(?!(?:\\.|-|\\/)(?:0?[2469]|11))|30(?!(?:\\.|-|\\/)0?2)|(?:29(?:(?!(?:\\.|-|\\/)0?2(?:\\.|-|\\/))|(?=\\D0?2\\D(?:(?!000[04]|(?:(?:1[^0-6]|[2468][^048]|[3579][^26])00))(?:(?:(?:\\d\\d)(?:[02468][048]|[13579][26])(?!\\x20BC))|(?:00(?:42|3[0369]|2[147]|1[258]|09)\\x20BC))))))|2[0-8]|1\\d|0?[1-9])([-.\\/])(1[012]|(?:0?[1-9]))\\2((?=(?:00(?:4[0-5]|[0-3]?\\d)\\x20BC)|(?:\\d{4}(?:$|(?=\\x20\\d)\\x20)))\\d{4}(?:\\x20BC)?)(?:$|(?=\\x20\\d)\\x20))?((?:(?:0?[1-9]|1[012])(?::[0-5]\\d){0,2}(?:\\x20[aApP][mM]))|(?:[01]\\d|2[0-3])(?::[0-5]\\d){1,2})?$")) {
     	SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
     	try {
        	$dValue = formatter.parse($text);
        } catch (ParseException e) {
			e.printStackTrace();
		}
     }
   }
 ;

/**
 * Some significant characters that need to be matched.
 */
LPAREN      : 	'(';
RPAREN		:	')';
COMMA		: 	',';
LBRACKET	:	'[';
RBRACKET 	: 	']';


/**
 * Operators
 */
FIND : ('F'|'f')('I'|'i')('N'|'n')('D'|'d') ;
WHERE : ('W'|'w')('H'|'h')('E'|'e')('R'|'r')('E'|'e') ;
BANG		:	'!';
LT		:	'<';
GT		:	'>';
GTEQ		:	'>=';
LTEQ 		:	'<=';
EQUALS		:	'=' ;
NOT_EQUALS	:	'!=';
LIKE		:	'~' | ('L'|'l')('I'|'i')('K'|'k')('E'|'e');
NOT_LIKE	:	'!~';		
IN		:	('I'|'i')('N'|'n');
IS		:	('I'|'i')('S'|'s');
AND 		:	('A'|'a')('N'|'n')('D'|'d') | AMPER | AMPER_AMPER;
OR		:	('O'|'o')('R'|'r') | PIPE | PIPE_PIPE;	
NOT		:	('N'|'n')('O'|'o')('T'|'t');
EMPTY		:	('E'|'e')('M'|'m')('P'|'p')('T'|'t')('Y'|'y') | ('N'|'n')('U'|'u')('L'|'l')('L'|'l');
WORD : [a-zA-Z] [a-zA-Z0-9:]* ;
INTEGER : DIGIT+ ;
DATEFULL : DATE WS TIME;
DATE : [0-9:/\\-]+ ;
TIME : [0-9:]+ ;
ALL		:	('A'|'a')('L'|'l')('L'|'l');
HAS		:	('H'|'h')('A'|'a')('S'|'s');
 
fragment QUOTE		:	'"' ;
fragment SQUOTE 	:	'\'';
fragment BSLASH		:	'\\';
fragment NL		:	'\r';
fragment CR		:	'\n';
fragment SPACE		:	' ';	
fragment AMPER	:	'&';
fragment AMPER_AMPER:	 '&&';
fragment PIPE	:	'|';	
fragment PIPE_PIPE	:	'||';	

fragment DIGIT
	:	'0'..'9'
	;

WS : (SPACE|'\t'|NEWLINE) -> skip; //Define white space rule, toss it out

fragment NEWLINE
    :   NL | CR;
    