import hashlib
passwords = ['admin1234', 'Fitzone2026!']
for p in passwords:
    h = hashlib.sha256(p.encode('utf-8')).hexdigest()
    print(f"{p} => {h}")
