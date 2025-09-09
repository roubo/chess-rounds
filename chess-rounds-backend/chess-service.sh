#!/bin/bash

# Chess Rounds Backend Service Management Script
# Usage: ./chess-service.sh {start|stop|restart|status}

SERVICE_NAME="chess-rounds-backend"
JAR_FILE="target/chess-rounds-backend-1.0.0.jar"
PID_FILE="/tmp/${SERVICE_NAME}.pid"
LOG_FILE="logs/chess-rounds.log"
PROFILE="prod"

# Create logs directory if it doesn't exist
mkdir -p logs

# Function to get the PID of the running service
get_pid() {
    if [ -f "$PID_FILE" ]; then
        cat "$PID_FILE"
    else
        echo ""
    fi
}

# Function to check if service is running
is_running() {
    local pid=$(get_pid)
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
        return 0
    else
        return 1
    fi
}

# Function to start the service
start_service() {
    if is_running; then
        echo "$SERVICE_NAME is already running with PID $(get_pid)"
        return 1
    fi
    
    echo "Starting $SERVICE_NAME..."
    nohup java -jar "$JAR_FILE" --spring.profiles.active="$PROFILE" > "$LOG_FILE" 2>&1 &
    local pid=$!
    echo $pid > "$PID_FILE"
    
    # Wait a moment and check if the service started successfully
    sleep 3
    if is_running; then
        echo "$SERVICE_NAME started successfully with PID $pid"
        echo "Log file: $LOG_FILE"
        return 0
    else
        echo "Failed to start $SERVICE_NAME"
        rm -f "$PID_FILE"
        return 1
    fi
}

# Function to stop the service
stop_service() {
    local pid=$(get_pid)
    if [ -z "$pid" ]; then
        echo "$SERVICE_NAME is not running"
        return 1
    fi
    
    echo "Stopping $SERVICE_NAME (PID: $pid)..."
    kill "$pid"
    
    # Wait for the process to stop
    local count=0
    while kill -0 "$pid" 2>/dev/null && [ $count -lt 30 ]; do
        sleep 1
        count=$((count + 1))
    done
    
    if kill -0 "$pid" 2>/dev/null; then
        echo "Force killing $SERVICE_NAME..."
        kill -9 "$pid"
        sleep 2
    fi
    
    rm -f "$PID_FILE"
    echo "$SERVICE_NAME stopped"
}

# Function to show service status
show_status() {
    if is_running; then
        echo "$SERVICE_NAME is running with PID $(get_pid)"
        echo "Log file: $LOG_FILE"
    else
        echo "$SERVICE_NAME is not running"
    fi
}

# Function to restart the service
restart_service() {
    echo "Restarting $SERVICE_NAME..."
    stop_service
    sleep 2
    start_service
}

# Main script logic
case "$1" in
    start)
        start_service
        ;;
    stop)
        stop_service
        ;;
    restart)
        restart_service
        ;;
    status)
        show_status
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status}"
        echo "  start   - Start the $SERVICE_NAME service"
        echo "  stop    - Stop the $SERVICE_NAME service"
        echo "  restart - Restart the $SERVICE_NAME service"
        echo "  status  - Show the status of $SERVICE_NAME service"
        exit 1
        ;;
esac

exit $?