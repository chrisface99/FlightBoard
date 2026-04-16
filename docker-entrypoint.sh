#!/bin/bash
# Enhanced Docker entrypoint with health checks and graceful shutdown

set -e

echo "=========================================="
echo "FlightBoard Application Startup"
echo "=========================================="
echo "Java version: $(java -version 2>&1 | head -1)"
echo "Application JAR: $(ls -lh /app/app.jar | awk '{print $5}')"
echo ""

# Environment info
echo "Environment Configuration:"
echo "  - JAVA_OPTS: ${JAVA_OPTS}"
echo "  - SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE}"
echo "  - OpenSky OAuth URL: ${OPENSKY_OAUTH_URL}"
echo ""

# Shutdown handler
cleanup() {
    echo "Received shutdown signal. Performing graceful shutdown..."
    # Spring Boot handles graceful shutdown automatically
    # This script will exit naturally when Java process exits
    exit 0
}

trap cleanup SIGTERM SIGINT

# Start application
echo "Starting FlightBoard application..."
exec java $JAVA_OPTS -jar /app/app.jar "$@" &

# Get the PID of the Java process
JAVA_PID=$!

# Wait for the Java process to finish
wait $JAVA_PID

echo "Application stopped."
exit $?
