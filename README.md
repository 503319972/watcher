# watcher
A watcher watches the file. With this component in the project, it can scan the target file directory and build some interfaces
automatically. Now it can only be run in Springboot.

# Quick start
## step 1: build this project and import it to your project
```
mvn install
```


## step 2: add the annotation in the Application start class
just add @EnableFileController 
```
@SpringBootApplication
@EnableFileController
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
```

## step 3: add the file directory and compiled class name in the .properties

```
watcher.file-path=C:\\api\\TS\\example
watcher.controller-name=CompiledController2
```

## step 4: start the Springboot project which imports this module

## step 5: find your interfaces
for example, your directory is like 
/usr/local/test.json, 
/usr/local/test2.json
/usr/local/new/test2.json
and your root path is /usr/local,
then you can find your interface such as http://localhost/file/test, http://localhost/file/test2, http://localhost/file/new/test2
