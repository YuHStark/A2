import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;


public class LexicalAnalyzer {
    /* Global declarations */
    /* Variables */
    private static int charClass;
    private static StringBuilder lexeme = new StringBuilder(100);
    private static StringBuilder error = new StringBuilder(100);
    private static char nextChar;
    private static int lexLen;
    private static int token;
    private static int nextToken;
    private static BufferedReader in_fp;
    private static int lineNumber = 1;

    /* Character classes */
    private static final int EOF = -1;
    private static final int LETTER = 0;
    private static final int DIGIT = 1;
    private static final int UNDERSCORE = 2;
    private static final int UNKNOWN = 99;

    /* Token codes */
    private static final int INT_LIT = 10;
    private static final int FLOAT_LIT = 12;
    private static final int IDENT = 11;
    private static final int STR_LIT = 13;
    private static final int ASSIGN_OP = 20;
    private static final int ADD_OP = 21;
    private static final int SUB_OP = 22;
    private static final int MULT_OP = 23;
    private static final int DIV_OP = 24;
    private static final int LEFT_PAREN = 25;
    private static final int RIGHT_PAREN = 26;
    private static final int LEFT_BRACE = 27;
    private static final int RIGHT_BRACE = 28;
    private static final int SEMICOLON = 29;
    private static final int LESS_THAN = 30;
    private static final int GREATER_THAN = 31;
    private static final int EQUALS = 32;
    private static final int NOT_EQUALS = 33;
    private static final int AND_OP = 34;
    private static final int OR_OP = 35;
    private static final int IF = 36;
    private static final int ELSE = 37;
    private static final int FOR = 38;
    private static final int WHILE = 39;
    private static final int COMMENT = 40;
    private static final int QUESTION_MARK = 41;
    private static final int COLON = 42;

    private static Stack<Character> symbolStack = new Stack<>();


    public static void main(String[] args) {
        /* Open the input data file and process its contents */
        try {
            in_fp = new BufferedReader(new FileReader("/Users/yuhstark/Downloads/Assignment2_TestCases/input.txt"));
            getChar();
            do {
                lex();
            } while (nextToken != EOF);
        } catch (IOException e) {
            System.err.println("ERROR - cannot open front.in");
        }
        if (!symbolStack.isEmpty()) {
            System.err.println("Error - Unbalanced symbols in the file");
        }
    }
    private static boolean isMatchingPair(char open, char close) {
        return (open == '(' && close == ')') ||
                (open == '{' && close == '}') ||
                (open == '[' && close == ']');
    }

    /* lookup - a function to lookup operators and keywords and return the token */
    private static void lookup(char ch) throws IOException {
        switch (ch) {
            case '(':
            case '{':
            case '[':
                symbolStack.push(ch);
                addChar();
                nextToken = LEFT_PAREN;
                break;
            case ')':
            case '}':
            case ']':
                if (!symbolStack.isEmpty() && isMatchingPair(symbolStack.peek(), ch)) {
                    symbolStack.pop();
                } else {
                    error.append("Error - Unbalanced symbol at line ").append(lineNumber);
                    nextToken = EOF;
                }
                addChar();
                nextToken = RIGHT_PAREN;
                break;
            case '+':
                addChar();
                nextToken = ADD_OP;
                break;
            case '-':
                addChar();
                nextToken = SUB_OP;
                break;
            case '*':
                addChar();
                nextToken = MULT_OP;
                break;
            case '/':
                addChar();
                getChar();
                nextToken = isComment() ? COMMENT : DIV_OP;
                break;
            case '=':
                addChar();
                getChar();
                if (nextChar == '=') {
                    addChar();
                    nextToken = EQUALS;
                } else {
                    nextToken = ASSIGN_OP;
                }
                break;
            case ';':
                addChar();
                nextToken = SEMICOLON;
                break;
            case '<':
                addChar();
                getChar();
                if (nextChar == '=') {
                    addChar();
                    nextToken = LESS_THAN;
                } else {
                    nextToken = LESS_THAN;
                }
                break;
            case '>':
                addChar();
                getChar();
                if (nextChar == '=') {
                    addChar();
                    nextToken = GREATER_THAN;
                } else {
                    nextToken = GREATER_THAN;
                }
                break;
            case '!':
                addChar();
                getChar();
                if (nextChar == '=') {
                    addChar();
                    nextToken = NOT_EQUALS;
                } else {
                    nextToken = UNKNOWN;
                }
                break;
            case '&':
                addChar();
                getChar();
                if (nextChar == '&') {
                    addChar();
                    nextToken = AND_OP;
                } else {
                    nextToken = UNKNOWN;
                }
                break;
            case '|':
                addChar();
                getChar();
                if (nextChar == '|') {
                    addChar();
                    nextToken = OR_OP;
                } else {
                    nextToken = UNKNOWN;
                }
                break;
            case '?':
                addChar();
                nextToken = QUESTION_MARK;
                break;
            case ':':
                addChar();
                nextToken = COLON;
                break;
            case '\"':
                addChar();
                getChar();
                while (nextChar != '\"') {
                    addChar();
                    getChar();
                }
                addChar(); // Include the closing double quote

                nextToken = STR_LIT;
                break;
            default:
                addChar();
                nextToken = EOF;
                break;
        }
    }

    private static boolean isComment() throws IOException {
        if (nextChar == '/') {
            // This is the beginning of a single-line comment
            while (nextChar != '\n')
                getChar(); // Ignore characters in the comment
            nextToken = COMMENT;
            lexeme = new StringBuilder("a single line comment");
        } else if (nextChar == '*') {
            // This is the beginning of a block comment
            addChar();
            getChar();
            while (!(nextChar == '*' && in_fp.read() == '/')) {
                getChar();
            }
            getChar(); // Consume the '/'
            nextToken = COMMENT;
            lexeme = new StringBuilder("a block comment");
        } else {
            return false;
        }
        return true;
    }

    /* addChar - a function to add nextChar to lexeme */
    private static void addChar() {
        if (lexLen <= 98) {
            lexeme.append(nextChar);
        } else {
            System.err.println("Error - lexeme is too long");
        }
    }

    /* getChar - a function to get the next character of input and determine its character class */
    private static void getChar() throws IOException {
        int nextCharInt = in_fp.read();
        if (nextCharInt != -1) {
            nextChar = (char) nextCharInt;
            if (nextChar == '\n') {
                lineNumber++;
            }
            if (Character.isLetter(nextChar))
                charClass = LETTER;
            else if (nextChar == '_')
                charClass = UNDERSCORE;
            else if (Character.isDigit(nextChar))
                charClass = DIGIT;
            else
                charClass = UNKNOWN;
        } else {
            charClass = EOF;
        }
    }

    /* getNonBlank - a function to call getChar until it returns a non-whitespace character */
    private static void getNonBlank() throws IOException {
        while (Character.isWhitespace(nextChar))
            getChar();
    }

    /* lex - a simple lexical analyzer for arithmetic expressions */
    protected static void lex() throws IOException {
        lexLen = 0;
        lexeme = new StringBuilder();
        getNonBlank();
        String specialChar = "(+-/*<>)";
        switch (charClass) {
            /* Parse identifiers or keywords */
            case LETTER:
            case UNDERSCORE:
                addChar();
                getChar();
                while (charClass == LETTER || charClass == DIGIT || charClass == UNDERSCORE) {
                    addChar();
                    getChar();
                }
                // Check if the lexeme is a keyword
                String lexemeStr = lexeme.toString();
                if (lexemeStr.equals("if"))
                    nextToken = IF;
                else if (lexemeStr.equals("else"))
                    nextToken = ELSE;
                else if (lexemeStr.equals("for"))
                    nextToken = FOR;
                else if (lexemeStr.equals("while"))
                    nextToken = WHILE;
                else if (charClass == UNKNOWN && !Character.isWhitespace(nextChar) && !specialChar.contains(String.valueOf(nextChar))) {
                    addChar();
                    error = new StringBuilder("Error - illegal identifier");
                    nextToken = EOF;
                } else
                    nextToken = IDENT;
                break;
            // Parse integer literals
            case DIGIT:
                addChar();
                getChar();
                while (charClass == DIGIT) {
                    addChar();
                    getChar();
                }
                if (nextChar == '.') {
                    addChar(); // Include the decimal point
                    getChar();
                    while (charClass == DIGIT) {
                        addChar();
                        getChar();
                    }
                    nextToken = FLOAT_LIT;
                } else if (Character.isLetter(nextChar) || nextChar == '_') {
                    while (Character.isLetter(nextChar) || charClass == DIGIT || nextChar == '_') {
                        addChar();
                        getChar();
                    }
                    error = new StringBuilder("Error - illegal identifier");
                    nextToken = EOF;
                } else {
                    nextToken = INT_LIT;
                }
                break;
            case '\"':
                lookup(nextChar);
                getChar();
                break;
            /* Operators and punctuation */
            case UNKNOWN:
                lookup(nextChar);
                getChar();
                break;
            /* EOF */
            case EOF:
                nextToken = EOF;
                lexeme = new StringBuilder("EOF");
                break;
        }
        /* End of switch */
        System.out.printf("Next token is: %d, Next lexeme is %s", nextToken, lexeme);
        System.out.printf("\t%s\n", error);
    }
    private static void checkSyntax() {
        Stack<Character> stack = new Stack<>();
        int lineCount = 1;
        boolean syntaxError = false;

        for (int i = 0; i < lexeme.length(); i++) {
            char c = lexeme.charAt(i);

            if (c == '\n') {
                lineCount++;
            }

            if (c == '(' || c == '{') {
                stack.push(c);
            } else if (c == ')') {
                if (stack.isEmpty() || stack.pop() != '(') {
                    System.out.println("Syntax analysis failed: Unmatched closing symbol at line " + lineCount);
                    syntaxError = true;
                    break;
                }
            } else if (c == '}') {
                if (stack.isEmpty() || stack.pop() != '{') {
                    System.out.println("Syntax analysis failed: Unmatched closing symbol at line " + lineCount);
                    syntaxError = true;
                    break;
                }
            }
        }

        if (!stack.isEmpty()) {
            System.out.println("Syntax analysis failed: Unmatched opening symbol");
        } else if (!syntaxError) {
            System.out.println("Syntax analysis succeed");
        }
    }



}
