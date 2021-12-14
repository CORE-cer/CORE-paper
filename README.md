# CORE
## Requirements
1) Install java 11
2) Install [gradle](https://gradle.org/) version 5.X.X

## Execution
- Compile with gradle each time changes are made in the code. In the case of linux, move to root project directory and execute in a terminal ```/usr/bin/gradle fatjar```. This will create a fatjar file (.jar) which path will need to be passed as argument when the code is ran.
- The following options to Java VM must be passed as arguments as well:
  `-Djava.security.manager -Djava.security.policy=path/to/java.policy`
- The following arguments for the program must be present, including at least [a queryfile and a streamsfile](#queryfile-and-streamsfile) with the options `-q` and `-s`:\
  `-q path/to/query.data -s path/to/streams.data`
- Aditionally, there are more parameters that you can pass to the program:\
  `-v`, `--verbose`: verbose mode.\
  `-f`, `--fastrun`: consume stream ignoring event timestamps.\
  `-l`, `--logmetrics`: register execution statistics. The files remain on `files/measure/`.\
  `-o`, `--offline`: Don't run a RMI server for remote execution.\
  The syntax of the CLI is detailed by the CLI itself.

#### From InteliJ IDEA

This can be configured in IntelliJ making a right click in Main > Edit, then choosing Run.

#### From command line (UNIX)
To execute from command line, a `fatJAR`from the project is needed. To obtain it, you must execute the gradle task called `fatJar` with the command ```/usr/bin/gradle fatjar```. The output is a file called `CORE-FAT-1.0-SNAPSHOT.jar` inside directory `build/libs/`. This `fatJAR` contains all the base code from the project, and also all dependencies to allow code to be executed.
Example from UNIX command line:
`java  -Djava.security.manager -Djava.security.policy=files/java.policy -jar build/libs/CORE-FAT-1.0-SNAPSHOT.jar -v -q ./example_queries/example2/query.data -s ./example_queries/example2/streams.data`

## QueryFile y StreamsFile

Two configuration files needed to run CORE.

#### QueryFile

A file wich points to where obtain the description of events of streams from and where to load queries when initializing the system from (the last one is optional). The file has, at most, two lines with the following structure:
- First line: Path to file with EVENT and STREAM declarations.
- Second line (optional): Path to initialization queries.

Syntax: `SOURCE_TYPE:SOURCE_ADDRESS`\
`SOURCE_TYPE` for line 1, only `FILE` type es supported. Line 2 supports `FILE` to load queryes from a file, and ` CLI` to start a command line interface, allowing to enter queries to the sistem in real time. In `CLI` mode you do not have to specify `SOURCE_ADDRESS`.\
QueryFile Example:
```
FILE:files/StreamDescription.txt
FILE:files/queries.txt
```

The EVENT and STREAM declaration file has a line per stream type, with its name and its parameters. Example of `StreamDescription.txt`:
```
DECLARE EVENT WindSpeed(id long, sitio string, value double)
DECLARE EVENT WindDirection(id long, sitio string, value double)
DECLARE EVENT GustSpeed(id long, sitio string, value double)
DECLARE EVENT RelativeAirHumidity(id long, sitio string, value double)
DECLARE STREAM S(WindSpeed, WindDirection, GustSpeed, RelativeAirHumidity)
```

The initialization queries file is the file that contains all the queries that shall be executed in the stream. Example of `queries.txt` (queries are separated by a newline):
```
SELECT RelativeAirHumidity
FROM S
WHERE ( RelativeAirHumidity )
FILTER ( RelativeAirHumidity[value > 27] );
SELECT GustSpeed, WindSpeed
FROM S
WHERE ( GustSpeed; WindSpeed )
FILTER ( WindSpeed[value > 20] )
WITHIN 1 MINUTE;
```

#### StreamsFile

Indicates from where to obtain the data streams. The file has `n`lines, `n`being the number of streams defined in the previous step. Each line has the syntax `STREAM_NAME:SOURCE_TYPE:SOURCE_ADDRESS`.
- `STREAM_NAME` is the name of the stream defined in the previous step, to which it will be assigned a data feed. If it is not defined, it will raise an exception.
- `SOURCE_TYPE` is the type of feed. `FILE`, `CSV`, `SOCKET` and `APICSV` are actually supported. To see more details, refer to the package `engine.streams`. The recommended option is `CSV`, the simplest, more robust and complete.
- `SOURCE_ADDRESS` is the path or direction of the specified stream.

Ejemplo:
```
S:CSV:./files/PolkuraAbrilMayo.csv
S2:CSV:./files/GruposQRyPolkura.csv
```

# Server y Client

CORE implements a Server/Client architecture. The main executable, described in [the first chapter](#core), acts as a server giving the possibility to remotely connect, interacting with the system. The communication is made by the [RMI protocol](https://es.wikipedia.org/wiki/Java_Remote_Method_Invocation).

## Server
When the main executable is started, it runs a RMI server in the port 1099. The server listens possible clients, which will be allowed to add and remove queries, ask for system state and obtain execution statistics.

The `BaseEngine` class implements a remote interface (`RemoteCOREInterface`), in which are defined all the possible actions that can be executed from a client. The `engine.rmi` package contains that interface, and also a class called `RemoteCOREClient` which is detailed in the [next subchapter](#client)

## Client
`RemoteCORECLient` encapsulates the connection logic to the server, allowing the implementation of a more elaborate client (for example, the CLI or the web). To use it, you can instantiate it or make a subclass out of it. The constructor receives an additional argument, that is the IP address of the machine which the server is running. In the case that no argument is passed, the client will search the server in the same machine. This makes that RMI client the best and more robust way to experiment with the server locally and in an interactive way.

To execute the client it is necessary to have the source code of CORE in a `JAR` file. To obtain it, you must use the `build` gradle task. Inside the `build/libs` directory, a file called `core-1.0-SNAPSHOT.jar` will be created. This is different from the `fatjar` because it does not include the project dependencies. Such `JAR` needs to be included in the classpath of the Java VM with the argument `-cp`. In the `COREClient` directory is included a bash script that shows how to include the `JAR` to the classpath.

#### COREClient
A directory called `COREClient` is included with a basic unctional example of a client, that instantiates `RemoteCOREClient` and makes some API calls through it. If the CORE code has changes, you must make sure that the most recent `JAR` is included. To execute it, [you must first run the server](#execution) and then run the file named `start_client.sh`, passing the address (IP or domain) of the server in the case it is running remotely.\
Example:\
`sh start_client.sh ciws.ing.puc.cl`\
If the server is running in the same computer:\
`sh start_client.sh`

#### MatchCallback
To obtain a description of the API, you must see `RemoteCOREInterface`. To add queries, you can specify how to enumerate the results of such query, through a `MatchCallbackType`. The class `MatchCallback` includes different ways to enumerate the results, which are represented by a corresponding `MatchCallbackType`. The client will need to import `MatchCallbackType` if it wishes to define how to enumerate the results. You can include more `MatchCallback` the the source code.\
`MatchCallback`s implemented now:
- `PRINT`: prints the matches in the server console.
- `WRITE`: writes the matches to a file. You can pass an argument to specify a new file route, or leave the default.
- `EMAIL`: Sends the matches through email. You must specify the email address where they must be sent.

# JavaDoc

IntelliJ IDEA offers a way to generate documentation of the project interactively. This is made from the `Run > Generate JavaDoc`menu. A directory in the same level as CORE is made, called `javadoc`. You must open the `index.html` file in a web browser. Also, you can complete the code documentation writing comments that follow the JavaDoc syntax.

# Tests

- Tests folders must be name 'Testn' with 'n' as the number of the test.
- If there is Testn, there must be Testn-1, Testn-2, ..., Test1
- Emphasis on Engine testing
- Each test must have a QueryFile: query_test.data, StreamFile: streamtest.data, and an OutputFile: output.txt
- QUERYFILE & STREAMSFILE: Same as Section QueryFile y StreamsFiles
- OUTPUTFILE: Arbitrary number of lines with:
```
    Line 1: Number of events given on the streamFile
    Line 2 and on: ouput of the query, not in a particular order
```

- If only wanna tests some inputs, can be changed in data() function
- To compile, it is just necessary to run the file EngineDeclaration.java (30 tests)
- Can be tested with gradle with the command: 'gradle clean test' (30 engine tests + others)
    
