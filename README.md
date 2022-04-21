## Objective
Find out information on how safe it is to ID, or your odds of making it into a cut, during the final round of a tournament. Alternatively, if the tournament is concluded, calculate the SoS/ESoS/placings.

Byes are accounted for, but duplicate player names, or any jokesters that want to name themselves (Bye), are not. As far as I can tell, all results line up with the algorithms used by cobr.ai.

## Usage

Compile the java class
```
javac com/nbkelly/outcomes/Outcomes.java
```

Generate pairings (scrape from cobra.ai) with Pairings.py and a tournament ID, ie:
```
python3 Pairings.py 2337 > pairings.txt
```

Run the script with the file you generated, the number of rounds, and the cut size (in this case, 4, 4).
```
java com.nbkelly.outcomes.Outcomes -p pairings.txt -r 4 --cut-size 4
```

## Output

Imagine a tournament with a structure like this, part way through the fourth (final) round:

left | score | right
-----| :---: |-
nbkelly         | 6 - 0 | Matuszczak 
luke            |   -   | jtfq
dessert_cactus  |   -   | bowlsley
armin           | 6 - 0 | milla
deer            | 0 - 6 | sebastiank
shorty          | 0 - 6 | enkoder
guiot           | 3 - 3 | echo
yonato          |   -   | knorpule
Sanjay          | 3 - 3 | Baa Ram Wu
rural_octopus047| 0 - 6 | shruthless
thepatrician    |   -   | Larrea
OF15-15         |   -   | AN2
functor         |   -   | seebasss7
techgin         | 0 - 6 | gilesdavis

The standings table for the tournament after three rounds looks like this:
place | name | score | SoS | ESoS
------|------|:--:|:--:|:--:
1	|Matuszczak |	18|	2.6667|	3.5556
2	|nbkelly	  |15	|2.6667	|4.1667
3	|lukevanryn |	15|	2.6667|	3.5556
4	|jtfq99999	|15|	2.3333|	3.8333
5	|dessert_cactus	|12|	3.6667|	3.3889
6	|milla|	12|	3.3333|	3.5556
7	|DeeR |12	|3.3333|	3.2778
8	|ArminFirecracker|	12|	3.3333	|3.1111
9	|sebastiank|	12	|3.0000	|3.5000
10|	bowlsley|	12|	3.0000|	3.4444
11|	yonato|	9	|4.5000	|2.6667
12|	enkoder	|9	|4.0000|	3.0000
13|	guiot|	9	|4.0000|	2.5556
14|	Shorty|	9	|3.6667	|3.1667
15|	echo|	9	|3.3333	|2.8889
16|	Baa Ram Wu|	9	|3.0000	|3.3333
17|	knorpule3000|	9	|2.6667|	3.4444
18|	techgin|	6	|4.5000|	3.1667
19|	functor|	6|	3.6667|	2.5556
20|	OF15-15|	6|	3.6667|	2.4444
21|	rural_octopus047|	6	|3.5000|	3.5000
22|	crowphie|	6	|3.3333	|2.4444
23|	gilesdavis|	6	|3.3333	|2.4444
24|	Sanjay|	6	|3.0000	|2.3333
25|	thepatrician|	6	|2.6667	|2.8889
26	|AN2	|6	|2.3333	|3.4444
27|	seebasss7|	6|	2.3333	|3.1111
28|	shruthless|	6	|2.3333	|2.6667
29|	Larrea|	6|	2.0000	|3.3333

It's possible to come up with good odds for who should id, who should try to 241, who what the odds are of each player making it into the cut (4 spots for this tournament).

```
ODDS FOR TOP 4 CUT (all outcomes):
nbkelly              100.000%
ArminFirecracker      73.251%
sebastiank            69.136%
Matuszczak            58.848%
jtfq99999             56.379%
lukevanryn            39.369%
dessert_cactus         1.920%
bowlsley               1.097%

PLAYERS UP FOR CONTENTION
                         SWEEP   SPLIT     FOLD
bowlsley               3.292%   0.000%   0.000%
dessert_cactus         5.761%   0.000%   0.000%
jtfq99999            100.000%  69.136%   0.000%
lukevanryn           100.000%  18.107%   0.000%
```

## Issues/TODO
I need to filter out cut games for concluded tournaments.

If we can find players who 100% should ID, then we can make more inferences about how the other players should behave.

Later, this will be hooked up to a web frontend.
