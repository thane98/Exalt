# Exalt

Exalt is a WIP script editor for the CMB format used in most of the recent Fire Emblem games. At the moment, the project has two major components: the decompiler and the compiler.

The decompiler converts CMB files into Exalt scripts. The compiler converts Exalt scripts to CMB files. For more information on using the decompiler and compiler as well as the Exalt scripting language, read on.

## Installation and Usage
Before installing Exalt, install the latest version of Java for your operating system. Then, download the latest version of Exalt from this repository's releases section.

The release should come with three JAR files: "exc.jar", the compiler, "exd.jar", the decompiler. Both of these programs are command-line based, so you will need to run them through either a terminal or (for Windows users) command prompt or powershell.

### Decompiling
To decompile a CMB, navigate to the directory of "exd.jar" in your terminal. Next, find the path of the CMB you want to decompile. To decompile this CMB, run the following command:

```bash
java -jar exd.jar YOUR_PATH_HERE
```

This will produce a script file with the name "a.exl". You can edit this file using a standard text editor.

If you want to change the name of the output file, use the "-o" or "--output" flags. For example:

```bash
java -jar exd.jar -o MyScript.exl YOUR_PATH_HERE
```

This command will decompile the CMB at "YOUR_PATH_HERE", producing an Exalt script named "MyScript.exl".

For additional decompiling options, run "exd.jar" with the "-h" or "--help" flag.

### Compiling
Compiling is a similar process to decompiling. Find the path of your Exalt script and navigate to the directory of "exc.jar" in your terminal. You can compile this script using the command:

```bash
java -jar exc.jar YOUR_PATH_HERE
```

This will output a CMB file to "a.cmb" by default. Once again, the "-o" or "--output" flags can be used to change the name of the output.

If there are errors in your script, the decompiler will print out an error message. The message will include the file where the error occurred, the error message, and the location. For example:

```
/home/thane98/a.exl
Expected expression.
64 |     ev::VariableSet("MyVar", 2 + );
                                      ^
```
In this example, the error was in the file "/home/thane98/a.exl". The error occured on line 64 at the position indicated by the caret.

## Scripting
The following section will provide a brief overview of the features available in the Exalt scripting language. For additional resources, consider looking at scripts from the game you are editing or asking questions in one of the FE hacking communities.

### Events and Functions
Exalt scripts are based around events and functions. Events only run when something happens during a chapter or cutscene. When you declare events, you specify a type that determines what will trigger the event as well as arguments to configure the event. For instance, an argument might determine if only a certain character can trigger an event. Events can ONLY be run when something in a chapter or cutscene triggers them - you cannot run them from another event or function. Here are some examples of function declarations:

```
event [6]() {} # '6' is the event type. Events with this type run when the script loads.
event [16](1, 1, 0) {} # This event takes in arguments to determine which turn numbers trigger it.
event [20](-1, -1, -1, -1, -1, "Done") {} # Strings are also valid arguments!
```

On the other hand, functions can be run from anywhere. With functions, you specify parameters which other events and functions must pass in when calling it. The following example defines a function and calls it from an event:

```
func sum(a, b) {
  return a + b;
}

event [6]() {
  var my_var = sum(2, 2); # 4
  var my_other_var = sum(6, 8); # 14
}
```

In this example, "sum" is the name of the function and "a" and "b" are parameters.

Note the "return" statement at the end of the function. This specifies what value the function will send back to callers. If no return is given, a function will automatically return 0.

### Variables
In-game, variables for a function or event are all stored in one big array. Exalt provides two ways of accessing variables. The first allows you to access the underlying array directly. The other provides a more human-readable syntax like a typical programming language.

You can access a variable in array syntax by using the dollar sign like so:

```
func example() {
  $0 = 4; # Store 4 at index 0.
  $1 = $0; # Loads 4 from index 0 and stores it at index 1.
  $1 += 2; # Add 2 to index 1 and store it in index 1
  $0++; # Increment index 0 by 1. Index 0 now holds 5.
}
```

While the decompiler used array syntax for simplicity, there are other ways to define variables. For example, you can use the "let" syntax to create an alias for an index:

```
func example() {
  let my_var = $0;
  my_var = 4;
  ev::VariableSet("ExampleVar", my_var); # my_var is 4
  ev::VariableSet("ExampleVar", $0); # Also 4
}
```

You can avoid array syntax altogether by declaring variables using the keyword "var".

```
func example() {
  var my_var = 4;
  var my_other_var = my_var / 2;
  var third_var = my_var + my_other_var;
}
```

When you declare variables using this syntax, the compiler will place them in an index automatically.

### Arrays
Exalt also provides limited support for arrays (separate from the array syntax mentioned in the previous section). The syntax for accessing arrays is similar to C. Here are some examples:

```
func example() {
  $0[1] = 4; # This is the same as saying $1 = 4
  $0[$1] = 4; # You can also index into an array using expressions.
}
```

When declaring variables with the "var" syntax, you have two options for making arrays. One allocates empty space while the other initializes array elements. Here are examples of both options:

```
func example() {
  var my_arr = array[5]; # An array with five empty spaces.
  var my_other_arr = ["Hello", "Goodbye", "."]; # An array with three elements: "Hello", "Goodbye", and "."
}
```

### Constants
You can give names to constant values using the following syntax:

```
const MY_CHAR = "PID_MyCharacter";

func example() {
  ev::DoSomethingWithCharacter(MY_CHAR); # Passes in "PID_MyCharacter"
}
```

### If and Match Statements
If statements allow you to run code based on some condition. The syntax for if statements is similar to C:

```
func example() {
  if (ev::CheckSomething()) {
    # run whatever code you want here
  } else if (ev::CheckSomethingElse() || ev::CheckAnotherThing()) {
    # run something else
  } else {
    # if none of the above conditions were met, run this.
  }
  
  if (ev::OneLastCheck())
    $0 = 15; # Shorthand for only one statement after the if, no braces needed.
}
```

Match statements are Exalt's version of switch/case. You can use them like so:

```
func example(param) {
  var my_var = ev::ComputeSomeValue();
  match (my_var) {
    param -> ev::DoSomething(my_var);
    param / 2 -> ev::DoSomethingElse(my_var); # Note that you can use expressions as well!
    else -> ev::NothingElseWorked(); # Runs if my_var didn't equal any of the other cases.
  }
}
```

### Loops
Exalt also supports for and while loops using a c-like syntax.

```
func example(limit) {
  var index = 0;
  while (index < limit)
    index++;
    
  for (var i = 0; i < limit; i++) {
    ev::DoSomething();
  }
}
```

### Gotos and Labels
While you should try to stick to if, match, while, and for whenever possible, gotos are also supported. The syntax for them is as follows:

```
func example(limit) {
  var index = 0;
  label CHECK;
  if (index < limit) {
    index++;
    goto CHECK;
  }
}
```

### Include
You can use an include statement to pull code from one script into another. As far as compiling is concerned, this is the same as copying the script's text directly into your script. For example:

```
# In b.exl
func sum(a, b) {
  return a + b;
}

# In a.exl
include "b.exl"

func diff(a, b) {
  return a - b;
}
```

The compiler sees:

```
# In a.exl
func sum(a, b) {
  return a + b;
}

func diff(a, b) {
  return a - b;
}
```

Exalt will check if a file has already been included before adding it to the current script. If the file was included previously, it will ignore the include directive.

## Compatibility
At the moment, Exalt's compatibility is somewhat limited. The vast majority of scripts FE14's base game are fully compatible. Most scripts from FE13 and FE15 also work. Scripts from DLC maps vary since they use special functionality that the decompiler doesn't recognize yet. 
