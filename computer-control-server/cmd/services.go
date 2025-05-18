package main

import (
	"os/exec"
	"golang.org/x/text/encoding/charmap"
)

func ExecuteCommand(cmd, dir string) (string, error) {
	command := exec.Command("cmd.exe", "/C", cmd)
	command.Dir = dir

	outputBytes, err := command.CombinedOutput()
	if err != nil {
		return string(outputBytes), err
	}

	decoder := charmap.CodePage866.NewDecoder()
	utf8Bytes, err := decoder.Bytes(outputBytes)
	if err != nil {
		return "", err
	}

	return string(utf8Bytes), nil
}