## Build-status
[![Build Status](https://travis-ci.com/NoFunny/Java_Compiler.svg?branch=master)](https://travis-ci.com/NoFunny/Java_Compiler)
# Java compiler

## Project download
```
git clone https://github.com/NoFunny/Java_Compiler.git
```

## Project build without tests
```
gradle build -x test
```

## Project build with tests
```
gradle build
```

## Run project 
```
./gradlew run
```

##### Don't forget to change the path to the file with the test programs!!!


#### You can also use IntelliJIDEA for running project
![Image](https://github.com/NoFunny/Java_Compiler/raw/master/src/main/resources/info.jpeg)

### Into program:
```
1 - <lexical analysis>
2 - <parse>
3 - <compile>
4 - <exit>
```

### To view the tree, select the contents of the AST.graph file and draw it on the [Site](http://www.webgraphviz.com/)