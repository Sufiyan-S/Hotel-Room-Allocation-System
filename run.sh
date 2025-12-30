#!/usr/bin/env sh
set -e

# ---------------------------------------
# Configuration
# ---------------------------------------
MAVEN_VERSION="3.9.6"
APP_NAME="hotel-room-booking-system"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MAVEN_BASE="${SCRIPT_DIR}/.maven"
MAVEN_DIR="${MAVEN_BASE}/apache-maven-${MAVEN_VERSION}"
MAVEN_BIN="${MAVEN_DIR}/bin/mvn"

# ---------------------------------------
# Verify Java (Java 21 required)
# ---------------------------------------
if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: Java is not installed or not on PATH"
  exit 1
fi

echo "Java version:"
java -version

# ---------------------------------------
# Clean old/broken Maven
# ---------------------------------------
if [ -d "$MAVEN_DIR" ]; then
  echo "Removing existing Maven installation (possible corruption)..."
  rm -rf "$MAVEN_DIR"
fi

mkdir -p "$MAVEN_BASE"

# ---------------------------------------
# Download Maven (safe for Windows Git Bash)
# ---------------------------------------
echo "Downloading Apache Maven ${MAVEN_VERSION}..."

MAVEN_TGZ="${MAVEN_BASE}/maven.tar.gz"

curl -fsSL \
  "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
  -o "$MAVEN_TGZ"

tar -xzf "$MAVEN_TGZ" -C "$MAVEN_BASE"
rm -f "$MAVEN_TGZ"

# ---------------------------------------
# Hard isolate Maven runtime
# ---------------------------------------
export MAVEN_HOME="$MAVEN_DIR"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:/usr/bin:/bin"
unset MAVEN_OPTS
unset JAVA_TOOL_OPTIONS
unset CLASSPATH

echo "Using Maven: $MAVEN_BIN"
echo "Using Java: $JAVA_HOME"
"$MAVEN_BIN" -v

# ---------------------------------------
# Build Application
# ---------------------------------------
echo "Building ${APP_NAME}..."
"$MAVEN_BIN" -q -DskipTests clean package

# ---------------------------------------
# Resolve JAR
# ---------------------------------------
JAR_FILE="target/${APP_NAME}-0.0.1-SNAPSHOT.jar"

echo "Looking for JAR: $JAR_FILE"

if [ ! -f "$JAR_FILE" ]; then
  echo "ERROR: JAR not found: $JAR_FILE"
  ls -l target || true
  exit 1
fi

# ---------------------------------------
# Run Application
# ---------------------------------------
echo "Starting application on port 8080..."
"$JAVA_HOME/bin/java" ${JAVA_OPTS:-} -jar "$JAR_FILE"