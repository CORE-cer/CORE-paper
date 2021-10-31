# Usage: sh start_client.sh [<host>]

javac -cp '.:core-1.0-SNAPSHOT.jar' Client.java;
java -cp '.:core-1.0-SNAPSHOT.jar' Client $1
