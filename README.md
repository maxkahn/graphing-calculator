# graphing-calculator
Graphing calculator, run from the command line.

Basic structure is to take user input, modify it using a macro, and display using a JFrame. User input should take a specific format:

Terms should be grouped using parentheses, make each innermost group of terms a string. Examples: "3", "sinx", "+".

So x + y should be entered (("x") ("+") ("y")). x^2 + y^2 becomes (("x^2") ("+") ("y^2")).

Trig functions, constants, symbols x and y, powers, exp, log, and ln are supported.



The user inputs two expressions, say A and B, in the above format. The program graphs the equation A(x+y) = B(x+y). So if A is (("x")) and B is (("y")), then the program displays the graph x = y.
