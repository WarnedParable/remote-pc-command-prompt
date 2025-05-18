package main

import (
	"fmt"
	"log"
	"net/http"
	"os"
	"sync"
	"time"
)

var (
	sessions = struct {
		sync.RWMutex
		m map[string]*Session
	}{m: make(map[string]*Session)}
)

func main() {
	http.HandleFunc("/execute", ExecuteCommandHandler)
	http.HandleFunc("/health", HealthCheckHandler)

	log.Println("Server is listening on http://localhost:8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}

func HealthCheckHandler(w http.ResponseWriter, r *http.Request) {
	w.Write([]byte("Server is up"))
}

func generateSessionID() string {
	return fmt.Sprintf("%d", time.Now().UnixNano())
}

func getDefaultDir() string {
	dir, _ := os.Getwd()
	return dir
}
