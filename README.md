# CORE


## Como ejecutarlo
- Se deben definir las siguientes opciones para la Java VM:\
 `-Djava.security.manager -Djava.security.policy=path/to/java.policy`
- Se deben incluir los argumentos para el programa, como minimo incluyendo [un queryfile y un streamsfile](#queryfile-y-streamsfile) con las opciones `-q` y `-s`:\
`-q path/to/query.data -s path/to/streams.data`
- Adicionalmente, se pueden configurar mas opciones al programa:\
`-v`, `--verbose`: modo verbose.\
`-f`, `--fastrun`: consumir los streams ignorando los timestamps de los eventos.\
`-l`, `--logmetrics`: registrar estadisticas de ejecucion. Los archivos quedan en `files/measure/`.\
`-o`, `--offline`: no levantar un servidor RMI para conexiones remotas.\
La sintaxis de la CLI es detallada por esta misma.

#### Desde InteliJ IDEA

Esto se puede configurar en Intelli
J haciendo click derecho en el Main > Edit. Luego elegir Run.

#### Desde linea de comandos (UNIX)
Para ejecutar desde la linea de comandos se necesita el `jatJAR` del proyecto. Para obtenerlo, se debe ejecutar la task de Gradle llamda `fatJar`. El output es un archivo llamado `CORE-FAT-1.0-SNAPSHOT.jar`, dentro del directorio `build/libs/`. Este `fatJAR` contiene todo el codigo base del proyecto, ademas de todas las dependencias necesarias para que el codigo pueda ser ejecutado.
Ejemplo para ejecutar desde linea de comandos UNIX:\
`java  -Djava.security.manager -Djava.security.policy=java.policy -jar CORE-FAT-1.0-SNAPSHOT.jar -v -q ./example_queries/example2/query.data -s ./example_queries/example2/streams.data`

## QueryFile y StreamsFile

Son dos archivos de configuracion necesarios para correr CORE.

#### QueryFile

Indica de donde obtener la descripcion de los eventos y streams y desde donde cargar queries al iniciar el sistema (opcional). El archivo consta de, a lo mas, dos lineas con la siguiente estructura:
- Linea 1: Ruta a archivo con declaraciones de EVENT y STREAM
- Linea 2 (opcional): Ruta a queries de inicializacion

Sintaxis: `SOURCE_TYPE:SOURCE_ADDRESS`\
`SOURCE_TYPE` para la linea 1 solo soporta el tipo `FILE`, y la linea 2 soporta `FILE`, para cargar queries desde un archivo, y `CLI` para iniciar una interfaz por linea de comandos para ingresar queries al sistema. El modo `CLI` no necesita especificar `SOURCE_ADDRESS`.\
Ejemplo de un QueryFile:
```
FILE:files/StreamDescription.txt
FILE:files/queries.txt
```

Ejemplo de `StreamDescription.txt`:
```
DECLARE EVENT WindSpeed(id long, sitio string, value double)
DECLARE EVENT WindDirection(id long, sitio string, value double)
DECLARE EVENT GustSpeed(id long, sitio string, value double)
DECLARE EVENT RelativeAirHumidity(id long, sitio string, value double)
DECLARE STREAM S(WindSpeed, WindDirection, GustSpeed, RelativeAirHumidity)
```

Ejemplo de `queries.txt` (queries van separadas por `";\n"`):
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

Indica desde donde obtener los streams de datos. El archivo consta de `n` lineas, siendo `n` la cantidad de streams definidos en el paso anterior. Cada linea tiene la sintaxis `STREAM_NAME:SOURCE_TYPE:SOURCE_ADDRESS`.
- `STREAM_NAME` es el nombre del stream definido en al paso anterior al que se le asignara una fuente de datos.
- `SOURCE_TYPE` es el tipo de fuente, actualmente se soportan `FILE`, `CSV`, `SOCKET`, `APICSV`. Cada uno con diferentes formatos y utilidades. Referirse al paquete `engine.streams` para mas detalle. Se recomienda usar `CSV` pues es el mas simple, robusto y completo.
- `SOURCE_ADDRESS` es la ruta o direccion a la fuente especificada.

Ejemplo:
```
S:CSV:./files/PolkuraAbrilMayo.csv
S2:CSV:./files/GruposQRyPolkura.csv
```

# Server y Client

CORE implementa una arquitectura Server/Client. El ejecutable principal, descrito en [el primer capitulo](#core), actua de servidor dando la posibilidad de conectarse a el remotamente para interactuar con el sistema. La comunicacion se lleva a cabo a traves del [protocolo RMI](https://es.wikipedia.org/wiki/Java_Remote_Method_Invocation).

## Server
El ejecutable principal, al iniciar, levanta un servidor RMI en el puerto 1099. Este escucha para que potenciales clientes se conecten y puedan, entre otras cosas: agregar y quitar queries, consultar el estado del sistema y obtener estadisticas de ejecucion.

La clase `BaseEngine` implementa una interfaz remota (`RemoteCOREInterface`), en la que se definen todas las acciones posibles a ejecutar desde un cliente. El paquete `engine.rmi` contiene dicha interfaz, ademas de una clase llamada `RemoteCOREClient` que se detalla en el [siguiente subcapitulo](#client).

## Client
`RemoteCOREClient` simplemente encapsula la logica de conexion al servidor, de manera que la implementacion de un cliente mas elaborado (por ejemplo, por CLI o web) tenga ese paso solucionado. Para usarlo hay dos opciones: subclaseandolo e instanciandolo. El constructor recibe un argumento opcional, que es la direccon IP de la maquina donde el servidor esta corriendo. En caso de no entregarse argumento, el cliente buscara al servidor en la misma maquina. Esto convierte a este cliente RMI en la mejor y mas robusta forma de experimentar con el servidor localmente y de manera interactiva.

Para ejecutar el cliente es necesario tener el codigo fuente de CORE empaquetado en un `JAR`. Para obtenerlo, hay que correr la task `build` de Gradle. Dentro del directorio `build/libs/` se creara un archivo llamado `core-1.0-SNAPSHOT.jar`. Este difiere del `fatJar` mencionado anteriormente en que no incluye las dependencias del proyecto en el paquete. Dicho `JAR` debe incluirse al classpath de la Java VM con el argumento `-cp`. En el directorio `COREClient` se incluye un script bash que muestra como incluir el `JAR` al classpath.

#### COREClient
Se incluye un directorio llamado `COREClient` con un ejemplo basico funcional de un cliente, que instancia `RemoteCOREClient` y hace llamados a la API a traves de el. Si el codigo de CORE ha cambiado, hay que asegurarse de incluir en el directorio el `JAR` mas actualizado. Para ejecutarlo, [primero hay que levantar el servidor](#como-ejecutarlo), y luego correr el archivo `start_client.sh` entregandole, si esta corriendo remotamente, la direccion (IP o DNS) del servidor.\
Ejemplo:\
`sh start_client.sh ciws.ing.puc.cl`\
Si el servidor esta corriendo en el mismo computador:\
`sh start_client.sh`

#### MatchCallback
Para obtener la descripcion de la API, hay que referirse a `RemoteCOREInterface`. Para agregar queries, se ofrece la utilidad de especificar como enumerar los resultados de dicha query, a traves de un `MatchCallbackType`. La clase `MatchCallback` incluye diferentes formas de enumerar ya disponibles, las que son representadas por un `MatchCallbackType` correspondiente. El cliente debera, por lo tanto, importar `MatchCallbackType` si desea definir como enumerar los resultados.\
Se invita a incluir nuevos `MatchCallback` al codigo fuente.
`MatchCallback`s actualmente implementados:
- `PRINT`: imprime los matches a la consola del server.
- `WRITE`: escribe los matches a un archivo. Se le puede entregar un argumento para especificar un nombre particular al archivo, en caso acontrario se registra a un archivo comun.
- `EMAIL`: Envia los amtches por email. Se debe especificar la direccion email de destino como agumento.

# JavaDoc

IntelliJ IDEA ofrece la funcionalidad de generar documentacion interactiva del proyecto. Esto se hace en el menu `Run > Generate JavaDoc`. Esto crea un directorio al mismo nivel que CORE, llamado `javadoc`. Hay que abrir el `index.html` en un navegador.\
Igualmente, se invita a seguir complementando la documentacion del codigo escribiendo comentarios que sigan la sintaxis de JavaDoc.

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
    