import re

with open('View/app.js', 'r', encoding='utf-8') as f:
    content = f.read()

# Show what's around handlePlanSatinAl
idx = content.find('handlePlanSatinAl')
print("Found at:", idx)
print(repr(content[idx:idx+300]))
