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
base = [
    "psql",
    "-h", env.get("DB_HOST", "127.0.0.1"),
    "-p", env.get("DB_PORT", "5432"),
    "-U", env.get("DB_USER", "postgres"),
    "-d", env.get("DB_NAME", "papv5"),
    "-A",
    "-t",
]

print("DB", env.get("DB_NAME"), env.get("DB_USER"), env.get("DB_HOST"), env.get("DB_PORT"))

queries = {
    "TABLES": """
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
        ORDER BY 1;
    """,
    "COLUMNS": """
        SELECT table_name || '|' || column_name || '|' || data_type || '|' ||
               COALESCE(character_maximum_length::text, '') || '|' || is_nullable || '|' ||
               COALESCE(column_default, '')
        FROM information_schema.columns
        WHERE table_schema = 'public'
        ORDER BY table_name, ordinal_position;
    """,
    "PK": """
        SELECT tc.table_name || '|' || kcu.column_name
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name
         AND tc.table_schema = kcu.table_schema
        WHERE tc.table_schema = 'public' AND tc.constraint_type = 'PRIMARY KEY'
        ORDER BY tc.table_name, kcu.ordinal_position;
    """,
    "FK": """
        SELECT tc.table_name || '|' || kcu.column_name || '->' ||
               ccu.table_name || '.' || ccu.column_name
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name
         AND tc.table_schema = kcu.table_schema
        JOIN information_schema.constraint_column_usage ccu
          ON ccu.constraint_name = tc.constraint_name
         AND ccu.table_schema = tc.table_schema
        WHERE tc.table_schema = 'public' AND tc.constraint_type = 'FOREIGN KEY'
        ORDER BY 1;
    """,
    "INDEXES": """
        SELECT tablename || '|' || indexname || '|' || indexdef
        FROM pg_indexes
        WHERE schemaname = 'public'
        ORDER BY 1, 2;
    """,
}

for title, sql in queries.items():
    print(f"==== {title} ====")
    result = subprocess.run(base + ["-c", sql], capture_output=True, text=True)
    print(result.stdout)
    if result.stderr.strip():
        print("ERR:", result.stderr)
    if result.returncode != 0:
        print("exit", result.returncode)
