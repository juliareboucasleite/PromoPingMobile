#!/usr/bin/env python3
import os
import subprocess
from pathlib import Path

env_path = Path("/root/PromoPing/.env")
text = env_path.read_text(encoding="utf-8", errors="replace").replace("\r", "")
env = {}
for line in text.splitlines():
    line = line.strip()
    if not line or line.startswith("#") or "=" not in line:
        continue
    key, value = line.split("=", 1)
    env[key.strip()] = value.strip().strip('"').strip("'")

os.environ["PGPASSWORD"] = env.get("DB_PASSWORD", "")
cmd = [
    "pg_dump",
    "-h", env.get("DB_HOST", "127.0.0.1"),
    "-p", env.get("DB_PORT", "5432"),
    "-U", env.get("DB_USER", "postgres"),
    "-d", env.get("DB_NAME", "papv5"),
    "--schema-only",
    "--no-owner",
    "--no-privileges",
    "-f", "/tmp/papv5_schema_server.sql",
]
result = subprocess.run(cmd, capture_output=True, text=True)
print(result.stdout)
print(result.stderr)
print("exit", result.returncode)
print("size", Path("/tmp/papv5_schema_server.sql").stat().st_size if result.returncode == 0 else "n/a")
