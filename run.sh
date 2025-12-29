#!/usr/bin/env sh
set -eu

# ---------------------------------------
# Configuration
# ---------------------------------------
MAVEN_VERSION="3.9.6"
MAVEN_DIR=".maven/apache-maven-${MAVEN_VERSION}"
MAVEN_BIN="${MAVEN_DIR}/bin/mvn"
APP_NAME="hotel-room-booking-system"

# ---------------------------------------
# Verify Java (Java 21 required by pom.xml)
# ---------------------------------------
if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: Java is not installed or not on PATH"
  exit 1
fi

echo "Java version:"
java -version

# ---------------------------------------
# Locate or Download Maven
# ---------------------------------------
if command -v mvn >/dev/null 2>&1; then
  MVN="mvn"
else
  if [ ! -x "$MAVEN_BIN" ]; then
    echo "Maven not found. Downloading Apache Maven ${MAVEN_VERSION}..."
    mkdir -p .maven

    download() {
      url="$1"
      echo "Trying: $url"
      curl -fsSL "$url" | tar -xz -C .maven
    }

    download "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
      || download "https://downloads.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
  fi

  MVN="$MAVEN_BIN"
fi

# ---------------------------------------
# Build Application
# ---------------------------------------
echo "Building ${APP_NAME} with Maven..."
"$MVN" -q -DskipTests clean package

# ---------------------------------------
# Resolve JAR (Spring Boot repackage)
# ---------------------------------------
JAR_FILE="target/${APP_NAME}-0.0.1-SNAPSHOT.jar"

if [ ! -f "$JAR_FILE" ]; then
  echo "ERROR: JAR not found at ${JAR_FILE}"
  echo "Contents of target/:"
  ls -l target
  exit 1
fi

# ---------------------------------------
# Run Application
# ---------------------------------------
echo "Starting application on port 8080..."
exec java ${JAVA_OPTS:-""} -jar "$JAR_FILE"