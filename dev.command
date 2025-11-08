#!/bin/zsh
set -e

# MongoDB 시작
/opt/homebrew/bin/brew services start mongodb-community@7.0 || true

# Ollama 실행
/usr/bin/pgrep -x ollama >/dev/null 2>&1 || \
/Applications/Ollama.app/Contents/MacOS/Ollama serve >/tmp/ollama.log 2>&1 &

# 터미널 탭 열기
osascript <<'APPLESCRIPT'
tell application "Terminal"
  activate
  do script "cd \"$HOME/Projects/rabit/server\"; nvm use --lts >/dev/null 2>&1 || true; npm start"
  do script "cd \"$HOME/Projects/rabit/client\"; nvm use --lts >/dev/null 2>&1 || true; npx http-server -p 8080"
  do script "tail -f /opt/homebrew/var/log/mongodb/mongo.log"
end tell
APPLESCRIPT