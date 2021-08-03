# General

Test the code:

    ../gradlew test

# Server-side

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

