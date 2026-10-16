# Builds the mod in a clean container with Java 21 and Gradle 9.8.1 preinstalled,
# so the Gradle wrapper does not have to download Gradle.
#
#   docker build -t icbm-build .
#   docker run --rm -v "$PWD":/src -v icbm-gradle-cache:/home/gradle/.gradle icbm-build
#
# The jar ends up in build/libs/ and the full output in build-log.txt, both in this folder.
# The named volume keeps downloaded Minecraft/NeoForge files between runs, so later builds are fast.
FROM gradle:9.8.1-jdk21

ENV GRADLE_USER_HOME=/home/gradle/.gradle
WORKDIR /src

CMD ["bash", "-c", "set -o pipefail; gradle build --no-daemon --stacktrace 2>&1 | tee build-log.txt"]
