/**
 * Copyright 2010 Bing Ran<bing_ran@hotmail.com> 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package cn.bran.japid.compiler;

import java.util.Stack;

/**
 * Template parser
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 * @author Play! framework original authors
 */
public class JapidParser {

	private String pageSource;

	public JapidParser(String pageSource) {
		this.pageSource = pageSource;
		this.len = pageSource.length();
	}

	// bran keep track of nested state tokens, eg. nested function calls in
	// expressions
	// what inside is not used for now, we only are interested in the depth
	Stack<JapidParser.Token> emthodCallStackInExpr = new Stack<JapidParser.Token>();

	//
	public enum Token {

		EOF, //
		PLAIN, //
		SCRIPT, // %{...}% or {%...%} bran: or ~{}~,  ~[]~ the open wings directives
		SCRIPT_LINE, // single back-quote ` will turn the rest if the line in to script. 
		EXPR, // ${...}
		START_TAG, // #{...}
		END_TAG, // #{/...}
		MESSAGE, // &{...}
		ACTION, // @{...}
		ABS_ACTION, // @@{...}
// bran: to indicate { in action arguments
		ACTION_CURLY, // @@{...}
		COMMENT, // *{...}*
		// bran expression without using {}, such as ~_;
		EXPR_WING, // ~{...}
		EXPR_NATURAL, // ~xxx or $xxx
		EXPR_NATURAL_METHOD_CALL, // bran function call in expression:
		// ~user?.name.format( '###' )
		EXPR_NATURAL_ARRAY_OP, // bran : ~myarray[-1].val
		EXPR_NATURAL_STRING_LITERAL, // bran ~user?.name.format( '#)#' ) or
		// $'hello'.length
		TEMPLATE_ARGS, // bran ~( )
	}

	// end2/begin2 for mark the current returned token while the begin is the
	// start pos of next token
	private int end, begin, end2, begin2, len;
	private JapidParser.Token state = Token.PLAIN;
	private JapidParser.Token lastState;

	private JapidParser.Token found(JapidParser.Token newState, int skip) {
		begin2 = begin;
		end2 = --end;
		begin = end += skip;
		lastState = state == Token.EXPR_NATURAL ? Token.EXPR : state;
		state = newState;
		return lastState;
	}

	private void skip(int skip) {
		end2 = --end;
		end += skip;
	}

	public Integer getLine() {
		String token = pageSource.substring(0, begin2);
		if (token.indexOf("\n") == -1) {
			return 1;
		} else {
			return token.split("\n").length;
		}
	}

	public String getToken() {
		String tokenString = pageSource.substring(begin2, end2);
		if (lastState == Token.PLAIN) {
			// unescape special sequence
			tokenString = tokenString.replace("``", "`");
		}
		return tokenString;
	}

	public String checkNext() {
		if (end2 < pageSource.length()) {
			return pageSource.charAt(end2) + "";
		}
		return "";
	}

	public JapidParser.Token nextToken() {
		for (;;) {

			int left = len - end;
			if (left == 0) {
				end++;
				return found(Token.EOF, 0);
			}

			char c = pageSource.charAt(end++);
			char c1 = left > 1 ? pageSource.charAt(end) : 0;
			char c2 = left > 2 ? pageSource.charAt(end + 1) : 0;

			switch (state) {
			case PLAIN:
				if (c == '%' && c1 == '{') {
					return found(Token.SCRIPT, 2);
				}
				if (c == '{' && c1 == '%') {
					return found(Token.SCRIPT, 2);
				}
				// bran open wings
				if (c == '~' && c1 == '{') {
					// deprecated use ~[
					return found(Token.SCRIPT, 2);
				}
				
				if (c == '~' && c1 == '[') {
					return found(Token.SCRIPT, 2);
				}
				
				if (c == '$' && c1 == '{') {
					return found(Token.EXPR, 2);
				}

				if (c == '~' && c1 == '(') {
					// deprecated in favor of args directive in a script
					return found(Token.TEMPLATE_ARGS, 2);
				}
				// bran: shell like expression: ~_, ~user.name (this one is diff
				// from sh, which requires ${user.name}
				// 
				if (c == '~' && c1 != '~' && (Character.isJavaIdentifierStart(c1) || '\'' == c1)) {
					return found(Token.EXPR_NATURAL, 1);
				}
				if (c == '$' && c1 != '$' && (Character.isJavaIdentifierStart(c1) || '\'' == c1)) {
					return found(Token.EXPR_NATURAL, 1);
				}
				if (c == '#' && c1 == '{' && c2 == '/') {
					return found(Token.END_TAG, 3);
				}
				if (c == '#' && c1 == '{') {
					return found(Token.START_TAG, 2);
				}
				if (c == '&' && c1 == '{') {
					return found(Token.MESSAGE, 2);
				}
				if (c == '@' && c1 == '@' && c2 == '{') {
					return found(Token.ABS_ACTION, 3);
				}
				if (c == '@' && c1 == '{') {
					return found(Token.ACTION, 2);
				}
				if (c == '*' && c1 == '{') {
					return found(Token.COMMENT, 2);
				}
				if (c == '`')
					if (c1 == '`') {
						skip(2);
					}
					else 
						return found(Token.SCRIPT_LINE, 1);
				break;
			case SCRIPT:
				if (c == '}' && c1 == '%') {
					return found(Token.PLAIN, 2);
				}
				if (c == '%' && c1 == '}') {
					return found(Token.PLAIN, 2);
				}
				// bran
				if (c == '}' && c1 == '~') {
					return found(Token.PLAIN, 2);
				}
				if (c == ']' && c1 == '~') {
					return found(Token.PLAIN, 2);
				}
				break;
			case SCRIPT_LINE:
				if (c == '\r') {
					if ( c1 == '\n') {
						return found(Token.PLAIN, 2);
					}
					else 
						return found(Token.PLAIN, 1);
				}
				else if ( c == '\n') {
					return found(Token.PLAIN, 1);
				}
				break;
			case COMMENT:
				if (c == '}' && c1 == '*') {
					return found(Token.PLAIN, 2);
				}
				break;
			case START_TAG:
				if (c == '}') {
					return found(Token.PLAIN, 1);
				}
				if (c == '/' && c1 == '}') {
					return found(Token.END_TAG, 1);
				}
				break;
			case END_TAG:
				if (c == '}') {
					return found(Token.PLAIN, 1);
				}
				break;
			case EXPR:
				if (c == '}') {
					return found(Token.PLAIN, 1);
				}
				break;
			case TEMPLATE_ARGS:
				if (c == ')') {
					return found(Token.PLAIN, 1);
				}
				break;
			// bran
			// special characters considered an expression: '?.()
			// break characters: space, other punctuations, new lines, returns
			case EXPR_NATURAL:
				if ('(' == c) {
					skipAhead(Token.EXPR_NATURAL_METHOD_CALL, 1);
				} else if ('[' == c) {
					skipAhead(Token.EXPR_NATURAL_ARRAY_OP, 1);
				} else if ('\'' == c) {
					// \' is valid only at the beginning
					// FIXME
					// start of literal
					skipAhead(Token.EXPR_NATURAL_STRING_LITERAL, 1);
				} else if (Character.isWhitespace(c)) {
					state = Token.EXPR;
					return found(Token.PLAIN, 0); // it ea
				} else if (!Character.isJavaIdentifierPart(c)) {
					if (c != '?' && c != '.' && c != ':' && c != '=') {
						state = Token.EXPR;
						return found(Token.PLAIN, 0); // it ea
					} else if (!Character.isJavaIdentifierStart(c1)) {
						if (c == '=' && c1 == '=') {
							if (Character.isWhitespace(c2)) {
								state = Token.EXPR;
								return found(Token.PLAIN, 0); // it ea
							}
							else {
								skip(2);
							}
						} else {
							state = Token.EXPR;
							return found(Token.PLAIN, 0); // it ea
						}
					}
				}
				break;
			case EXPR_NATURAL_METHOD_CALL:
				if ('(' == c) {
					// nested call
					skipAhead(Token.EXPR_NATURAL_METHOD_CALL, 1);
				} else if (')' == c) {
					state = this.emthodCallStackInExpr.pop();
					skip(1);
				}
				break;
			case EXPR_NATURAL_ARRAY_OP:
				if ('[' == c) {
					// nested call
					skipAhead(Token.EXPR_NATURAL_ARRAY_OP, 1);
				} else if (']' == c) {
					state = this.emthodCallStackInExpr.pop();
					skip(1);
				}
				break;
			case EXPR_NATURAL_STRING_LITERAL:
				if ('\\' == c && '\'' == c1) {
					// the escaped ' in a literal string
					skip(2);
				}
				if ('\'' == c) {
					// end of literal
					state = this.emthodCallStackInExpr.pop();
					skip(1);
				}
				break;
			case ACTION:
				if (c == '}') {
					return found(Token.PLAIN, 1);
				}
				else if (c == '{') { // bran: weak logic: assuming no "{" in string literals
					skipAhead(Token.ACTION_CURLY, 1);
				}
				break;
			case ABS_ACTION:
				if (c == '}') {
					return found(Token.PLAIN, 1);
				}
				else if (c == '{') {
					skipAhead(Token.ACTION_CURLY, 1);
				}
				break;
			case ACTION_CURLY:
				if (c == '}') {
					state = this.emthodCallStackInExpr.pop();
					skip(1);
				}
				else if (c == '{') {
					skipAhead(Token.ACTION_CURLY, 1);
				}
				break;
			case MESSAGE:
				if (c == '}') {
					return found(Token.PLAIN, 1);
				}
				break;
			}
		}
	}

	/**
	 * push a nested token to the stack
	 * @param token
	 * @param i number of chars to skip
	 */
	private void skipAhead(JapidParser.Token token, int i) {
		this.emthodCallStackInExpr.push(state);
		state = token;
		skip(i);
	}

	void reset() {
		end = begin = end2 = begin2 = 0;
		state = Token.PLAIN;
	}
}