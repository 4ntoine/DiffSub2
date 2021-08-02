Build the server app:

    ./gradlew clean shadowJar

Test the code:

    ./gradlew test

Test the app manually:

    cat src/test/resources/diff.txt | java -jar ./build/libs/DiffSub2-0.1-all.jar

Set up the Git hook:

    cp src/test/resources/update %git_repo_path%/hooks/