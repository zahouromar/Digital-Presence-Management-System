#!/bin/bash
# Simple script to run the DPMS frontend

echo "=> Starting frontend on http://localhost:3000"
python3 -m http.server 3000
