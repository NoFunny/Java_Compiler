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
./gradlew run --args="<filepath and name asm file> , <filepath and name executable file>"
```


#### You can also use IntelliJIDEA for running project
![Image](https://github.com/NoFunny/Java_Compiler/raw/master/src/main/resources/info.jpeg)

### Into the program:
```
1 - --dumb-token
2 - --dumb-parser
3 - --dumb-asm
4 - --compile
5 - <exit>
```

### To view the tree, select the contents of the AST.graph file and draw it on the [Site](http://www.webgraphviz.com/)
