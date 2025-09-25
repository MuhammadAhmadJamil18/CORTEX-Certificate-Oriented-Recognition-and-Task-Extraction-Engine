#!/usr/bin/env python3
import json
import requests
import sys


TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJjbGllbnRJZCI6MSwibG9naW5UaW1lIjoxNzU1NjE1MzA4MDIxLCJ1c2VyUm9sZSI6MSwidXNlcklkIjoxLCJlbWFpbCI6Im1odXphaWZhQGRhdGFwdWxzZXRlY2hub2xvZ2llcy5vcmciLCJzdWIiOiJtaHV6YWlmYUBkYXRhcHVsc2V0ZWNobm9sb2dpZXMub3JnIiwiaWF0IjoxNzU1NjE1MzA4LCJleHAiOjE3NTU2MTg5MDh9.whjTFeGdWTMf1_THX82hoQcsEDdesd1NNyzaCjrPRoI"

URL = "http://localhost:8080/api/users/add"

USERS_FILE = "users.json"

HEADERS = {
    "Content-Type": "application/json",
    "token": TOKEN #
}


def load_users(path):
    """Load and return the list of users from the given JSON file."""
    try:
        with open(path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except Exception as e:
        print(f"Failed to read '{path}': {e}", file=sys.stderr)
        sys.exit(1)

def add_user(user):
    """POST a single user to the API and return the response."""
    return requests.post(URL, headers=HEADERS, json=user, timeout=10)

def main():
    users = load_users(USERS_FILE)
    print(f"Loaded {len(users)} users to add\n")

    for idx, user in enumerate(users, start=1):
        user_name = user.get("userName", "<no-name>")
        print(f"[{idx}/{len(users)}] Adding '{user_name}'…", end=" ")

        try:
            resp = add_user(user)
        except requests.RequestException as e:
            print(f"ERROR – network issue ({e})")
            # continue to next user without aborting
            continue
        except Exception as e:
            print(f"ERROR – unexpected issue ({e})")
            continue

        if resp.ok:
            try:
                data = resp.json()
                print(f"OK (id={data.get('id')})")
            except ValueError:
                print("OK (no JSON returned)")
        else:
            print(f"FAIL ({resp.status_code}) – issue adding user '{user_name}'")

if __name__ == "__main__":
    main()



# UPDATE qualitydxb.users
# SET isactive = TRUE;

# INSERT INTO notificationfrequency
#        (notificationfrequencyid, frequency, description)
# VALUES (0, 'ONCE', 'One-time notification (no repetition).');
