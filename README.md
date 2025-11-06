Overview
This SyntaxAnalyser class implements the core recursive-descent parsing logic for a small programming language.
The base structure and abstract classes were provided by my professor, but all parsing methods and error-handling logic in this file were written by me.

My contributions
I implemented all of the following:
The full set of grammar rule methods (e.g. ifStatement(), whileStatement(), assignmentStatement(), forStatement(), etc.)
Recursive parsing logic for:
Statement structures
Expressions, terms, and factors
Conditions and operators
Procedure calls and argument lists
Error handling and reporting, including:
Descriptive syntax error messages
Line number tracking via a HashMap<String, Integer> (recentCall)
Integration with the output generator (myGenerate)
The custom logic in acceptTerminal() to check expected tokens and report mismatches
Helper methods updateLine() and updateStatementLine() for better debugging output
