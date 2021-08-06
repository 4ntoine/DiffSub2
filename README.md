# General

Test the code:

    ../gradlew test

# HTTP server-side

In the `http_server` directory.

Build the server app:

    ../gradlew clean shadowJar

Run the server app:

    java -jar ./build/libs/http-server-all.jar -r /tmp/repo -p 8081

Note:
* pass `-j` to use JSON converted instead of UnifiedDiffConverter
* pass `-c %path%` to use file system cache (make sure the directory exists)

Request the changes:

    curl "localhost:8081?from=87998e1759898200e386db4e29c7706a865adcd0&current=0"

Note:
* "from" - from revision changes are required (release)
* "current" - current revision the client is on
* both arguments are required

In the response: the first line is actual HEAD revision (to be passed as "current" next time),
the following lines are diff. If client is already on HEAD there will be no diff lines.

# Kafka server-side

In the `kafka_server` directory.

Build the server app:

    ../gradlew clean shadowJar

Test the app manually:

    cat src/test/resources/diff.txt | java -jar ./build/libs/kafka-server-all.jar

Set up the Git hook:

    cp src/test/resources/update %git_repo_path%/hooks/

Set up Docker-compose for `SenderTest`:

In the `src/test/resources` directory

    docker-compose up -d
    ...
    docker-compose down

# Kafka Client-side

In project root directory.

Start-up Docker-compose and (optionally) create a topic with Server app:

    java -jar ./kafka_server/build/libs/kafka-server-all.jar -h localhost -p 29092 -t diffsub2 -a create

Compile the source code and start the client:

    java -jar ./client/build/libs/client-all.jar -h localhost -p 29092 -t diffsub2 -d 100

Send a test diff with server app:

    cat server/src/test/resources/diff.txt | java -jar ./server/build/libs/kafka-server-all.jar -h localhost -p 29092 -t diffsub2 -a send

Use `-k` to pass a specific message key to be used while sending.

Make sure you can see the diff received by the client app.

**Warning**: replace hardcoded (my) IP-address from 192.168.1.120 to "localhost" or your IP-address in "docker-compose.yml".
Otherwise Kafka will not de reachable for non-local consumers.

### Offset

The clients can be interested in receiving of the changes starting some point (eg. release)
and already having some state bundled (so no diff changed are needed until release time point).

Use `-o` cmdline argument for client app for it:

    java -jar ./client/build/libs/client-all.jar -h localhost -p 29092 -t diffsub2 -d 100 -o 5