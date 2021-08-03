# General

Test the code:

    ../gradlew test

# Server-side

In the `server` directory.

Build the server app:

    ../gradlew clean shadowJar

Test the app manually:

    cat src/test/resources/diff.txt | java -jar ./build/libs/DiffSub2-0.1-all.jar

Set up the Git hook:

    cp src/test/resources/update %git_repo_path%/hooks/

Set up Docker-compose for `SenderTest`:

In the `src/test/resources` directory

    docker-compose up -d
    ...
    docker-compose down

# Client-side

In project root directory.

Start-up Docker-compose and (optionally) create a topic with Server app:

    java -jar ./server/build/libs/server-0.1-all.jar localhost:29092 diffsub2 create

Compile the source code and start the client:

    java -jar ./client/build/libs/client-0.1-all.jar localhost:29092 diffsub2 100

Send a test diff with server app:

    cat server/src/test/resources/diff.txt | java -jar ./server/build/libs/server-0.1-all.jar localhost:29092 diffsub2 send

Make sure you can see the diff received by the client app.

