package main

import (
	"encoding/json"
	"net/http"
	"os"
	"path/filepath"
	"strings"
)

func ExecuteCommandHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Only POST requests", http.StatusMethodNotAllowed)
		return
	}

	var req CommandRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "JSON decode error", http.StatusBadRequest)
		return
	}

	session := getSession(w, r)
	response := CommandResponse{}

	if isCDCommand(req.Command) {
		handleCDCommand(req.Command, session, &response, w)
	} else {
		handleRegularCommand(req.Command, session, &response, w)
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}

func getSession(w http.ResponseWriter, r *http.Request) *Session {
	cookie, err := r.Cookie("session_id")
	if err != nil {
		return createNewSession(w)
	}

	sessions.RLock()
	session := sessions.m[cookie.Value]
	sessions.RUnlock()

	if session == nil {
		return createNewSession(w)
	}
	return session
}

func createNewSession(w http.ResponseWriter) *Session {
	sessionID := generateSessionID()
	session := &Session{
		ID:         sessionID,
		CurrentDir: getDefaultDir(),
	}

	sessions.Lock()
	sessions.m[sessionID] = session
	sessions.Unlock()

	http.SetCookie(w, &http.Cookie{
		Name:  "session_id",
		Value: sessionID,
		Path:  "/",
	})
	return session
}

func isCDCommand(cmd string) bool {
	return strings.HasPrefix(strings.TrimSpace(cmd), "CD ")
}

func handleCDCommand(cmd string, session *Session, response *CommandResponse, w http.ResponseWriter) {
	targetDir := strings.TrimSpace(strings.TrimPrefix(cmd, "CD "))
	if targetDir == "" {
		response.Error = "Invalid cd command"
		w.WriteHeader(http.StatusBadRequest)
		return
	}

	newDir := filepath.Join(session.CurrentDir, targetDir)
	if _, err := os.Stat(newDir); os.IsNotExist(err) {
		response.Error = "Directory does not exist: " + newDir
		w.WriteHeader(http.StatusBadRequest)
		return
	}

	absPath, err := filepath.Abs(newDir)
	if err != nil {
		response.Error = "Invalid path: " + err.Error()
		w.WriteHeader(http.StatusBadRequest)
		return
	}

	session.CurrentDir = absPath
	response.Output = "Current directory: " + absPath
}

func handleRegularCommand(cmd string, session *Session, response *CommandResponse, w http.ResponseWriter) {
	output, err := ExecuteCommand(cmd, session.CurrentDir)
	response.Output = output
	if err != nil {
		response.Error = err.Error()
		w.WriteHeader(http.StatusInternalServerError)
	}
}