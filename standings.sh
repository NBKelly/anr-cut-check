#!/bin/bash

javac com/nbkelly/outcomes/Outcomes.java
python3 Pairings.py $1 > pairings-out.txt
java com.nbkelly.outcomes.Outcomes -p pairings-out.txt -r $2 --cut-size $3
