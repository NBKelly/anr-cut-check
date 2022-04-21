## Objective
Find out information on how safe it is to ID, or your odds of making it into a cut, during the final round of a tournament. Alternatively, if the tournament is concluded, calculate the SoS/ESoS/placings.

## Usage

Generate pairings (scrape from cobra.ai) with Pairings.py and a tournament ID, ie:
```
python3 Pairings.py 2337 > pairings.txt
```

Compile the java class
```
javac com/nbkelly/outcomes/Outcomes.java
```

Run the script with the file you generated, the number of rounds, and the cut size (in this case, 4, 4).
```
java com.nbkelly.outcomes.Outcomes -p pairings.txt -r 4 --cut-size 4
```

## Issues
I need to filter out cut games for concluded tournaments.

I want to do a little pre-processing, and find the players who *1)* can only make the cut if they 241 (for both of them), or *2)* always make the cut if they ID, and then use that to produce slightly better data.

Later, this will be hooked up to a web frontnd.