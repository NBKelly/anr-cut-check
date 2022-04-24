package com.nbkelly.outcomes;

/* imports */
import com.nbkelly.aux.Drafter;
import com.nbkelly.aux.Command;
import com.nbkelly.aux.FileCommand;
import com.nbkelly.aux.IntCommand;
import com.nbkelly.aux.StringCommand;
import com.nbkelly.aux.Timer;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Collections;

/**
 * Extension of Drafter directed towards a general case.
 *
 * @see <a href="https://nbkelly.github.io/Drafter/com/nbkelly/package-summary.html" target="_top">
 * here</a> for the up to date online javadocs
 */
public class Outcomes extends Drafter {
    /* WORKFLOW:
     *  set all needed commands with setCommands()
     *  post-processing can be performed with actOnCommands()
     *  the rest of your work should be based around the solveProblem() function
     */
    ArrayList<String> pairings;
    Integer roundCount;
    Integer cutSize;
    String inspectPlayer;
    Integer scenarioMax = 5;
    String showOpponents = null;
    
    /* solve problem here */
    @Override public int solveProblem() throws Exception {
	Timer t = makeTimer();

	var opps = new HashMap<String, TreeSet<String>>();
	var scores = new HashMap<String, Integer>();
	var free = new HashMap<String, String>();

	//every two pairings is a set
	for(int i = 0; i < pairings.size(); i+= 4) {
	    var left = pairings.get(i);
	    var left_score = parse_score(pairings.get(i+1));

	    var right = pairings.get(i+2);
	    var right_score = parse_score(pairings.get(i+3));

	    entry(opps, left, right);
	    entry(opps, right, left);

	    if(left_score != null)
		score(scores, left, left_score);
	    else
		free(free, left, right);

	    if(right_score != null)
		score(scores, right, right_score);
	    else
		free(free, left, right);
	}


	/* print out listings */
	DEBUG(2, "OPPONENTS:");
	for(var entry : opps.entrySet())
	    DEBUGF(1, "%s : %s%n", entry.getKey(), entry.getValue());

	DEBUG();

	DEBUG(2, "SCORES:");
	for(var entry : scores.entrySet())
	    DEBUGF(1, "%s : %d%n", entry.getKey(), entry.getValue());

	DEBUG();

	DEBUG(2, "OPEN RESULTS:");
	for(var entry : free.entrySet())
	    DEBUGF(1, "%s vs %s%n", entry.getKey(), entry.getValue());

	DEBUG();

	if(free.size() == 0)
	    calculate_outcomes(opps, scores, free, roundCount, cutSize, false);
	else {
	    println(calculate_outcomes(opps, scores, free, roundCount, cutSize, false));
	    println(calculate_outcomes(opps, scores, free, roundCount, cutSize, true));

	    var playersSafeToID = safeToId(opps, scores, free, roundCount, cutSize);
	    var ssf_simple = sweepSplitFold(opps, scores, free, roundCount, cutSize);

	    sweepSplitFold_display(ssf_simple);
	}

	if(inspectPlayer != null && free.size() > 0) {
	    println();
	    inspectPlayer(inspectPlayer, scenarioMax, opps, scores, free, roundCount, cutSize);
	}

	if(showOpponents != null) {
	    println();
	    showOpponents(showOpponents, opps, scores);
	}

	//println(ssf_simple);
	return DEBUG(1, t.split("Finished Processing"));
    }

    private void showOpponents(String player, HashMap<String, TreeSet<String>> opponents,
			       HashMap<String, Integer> scores) {
	var opps = opponents.get(player);

	if(opps != null) {
	    println("Opponents for " + player);
	    opps.stream().forEach(s -> printf("  vs. %s (%d points)%n", s, scores.get(s)));
	}
    }
    
    private void inspectPlayer(String player, Integer scenarioMax,
			       HashMap<String, TreeSet<String>> opponents,
			       HashMap<String, Integer> scores,
			       HashMap<String, String> free,
			       int num_rounds, int cut_size){
	if(free.size() == 0)
	    return;

	//how can this work? we need to produce a set of scenarios
	//to do this, we pass a 'scenario' into the recursive function
	//each recursive function returns a list of valid scenarios in which the player makes it
	ArrayList<String> baseScenario = new ArrayList<>();

	var scenarios = inspectPlayer_recursive(player, baseScenario,
				opponents, scores, free, num_rounds, cut_size);

	if(scenarios.size() == (int)(Math.pow(3, free.size()))) {
	    printf("%s makes it to the top cut in all %d scenarios%n",
		   player, scenarios.size());
	    return;
	}
	
	printf("There are %d scenarios where %s makes it to the top cut%n%n",
	       scenarios.size(), player);

	int index = 1;
	for(var scenario : scenarios) {
	    if(index >  scenarioMax && index <= scenarios.size()) {
		printf("... and %d other scenarios%n", scenarios.size() - index + 1);
		break;
	    }
	    
	    StringBuilder sc = new StringBuilder(String.format("Scenario %d:%n", index++));
	    scenario.stream().forEach(s -> sc.append("    " + s + "\n"));
	    println(sc);
	}
    }

    private ArrayList<ArrayList<String>>
	inspectPlayer_recursive(String player,
				ArrayList<String> scenario,
				HashMap<String, TreeSet<String>> opponents,
				HashMap<String, Integer> scores,
				HashMap<String, String> free,
				int num_rounds, int cut_size){
	var res = new ArrayList<ArrayList<String>>();
	
	//if free is empty, then see if we won
	if(free.size() == 0) {
	    var standings = new ArrayList<Standing>();

	    for(var entry : scores.entrySet())
		standings.add(new Standing(entry.getKey(), entry.getValue()));

	    standings.stream().forEach(x -> x.makeSOS(scores, opponents, num_rounds));
	    standings.stream().forEach(x -> x.makeESOS(opponents, standings, num_rounds));

	    Collections.sort(standings);

	    int index = 1;
	    for(; index <= cut_size && index < standings.size(); index++)
		if(standings.get(index-1).name.equals(player)) {
		    //we "won", which means this is a valid scenario
		    //so we return a new singleton arraylist with this scenario in it
		    res.add(scenario);
		    break;
		}
	    return res;
	}

	//resolve another pairing and see how it goes
	var new_free = new HashMap<String, String>(free);

	var s1 = new HashMap<String, Integer>(scores);
	var s2 = new HashMap<String, Integer>(scores);
	var s3 = new HashMap<String, Integer>(scores);

	String left_player = null;
	String right_player = null;
	for(var entry : new_free.entrySet()) {
	    left_player = entry.getKey();
	    right_player = entry.getValue();
	    new_free.remove(left_player);
	    break;
	}

	s1.put(left_player, s1.get(left_player) + 6); //sweep
	s2.put(left_player, s2.get(left_player) + 3); //split city
	s2.put(right_player, s2.get(right_player) + 3); //split city
	s3.put(right_player, s3.get(right_player) + 6); //sweep

	var scenario1 = new ArrayList<String>(scenario);
	var scenario2 = new ArrayList<String>(scenario);
	var scenario3 = new ArrayList<String>(scenario);

	scenario1.add(String.format("%-20s 6 - 0 %20s", left_player, right_player));
	scenario2.add(String.format("%-20s 3 - 3 %20s", left_player, right_player));
	scenario3.add(String.format("%-20s 0 - 6 %20s", left_player, right_player));

	res.addAll(inspectPlayer_recursive(player, scenario1, opponents, s1, new_free,
					   num_rounds, cut_size));
	res.addAll(inspectPlayer_recursive(player, scenario2, opponents, s2, new_free,
					   num_rounds, cut_size));
	res.addAll(inspectPlayer_recursive(player, scenario3, opponents, s3, new_free,
					   num_rounds, cut_size));

	return res;
    }

    private TreeSet<String> safeToId(HashMap<String, TreeSet<String>> opponents,
				     HashMap<String, Integer> scores,
				     HashMap<String, String> free,
				     int num_rounds, int cut_size) {
	TreeSet<String> free_players = new TreeSet<>();

	for(var entry : free.entrySet()) {
	    free_players.add(entry.getKey());
	    free_players.add(entry.getValue());
	}

	TreeSet<String> safe_to_id = new TreeSet<String>();
	for(var player : free_players) {
	    if(safeToId(player, opponents, scores, free, num_rounds, cut_size))
		safe_to_id.add(player);
	}

	return safe_to_id;
    }

    private HashMap<String, ArrayList<Double>>
	sweepSplitFold(HashMap<String, TreeSet<String>> opponents, HashMap<String, Integer> scores,
		       HashMap<String, String> free, int num_rounds, int cut_size) {
	TreeSet<String> free_players = new TreeSet<>();

	for(var entry : free.entrySet()) {
	    free_players.add(entry.getKey());
	    free_players.add(entry.getValue());
	}

	var res = new HashMap<String, ArrayList<Double>>();
	for(var player : free_players)
	    res.put(player, sweepSplitFold(player, opponents, scores, free, num_rounds, cut_size));

	return res;
    }

    private void sweepSplitFold_display(HashMap<String, ArrayList<Double>> res) {
	ArrayList<String> lines = new ArrayList<String>();

	for(var entry : res.entrySet()) {
	    if(entry.getValue().get(0) > 0d) {//there's a snowball's chance
		lines.add(String.format("%-20s %7.3f%% %7.3f%% %7.3f%%",
					entry.getKey(),
					entry.getValue().get(0),
					entry.getValue().get(1),
					entry.getValue().get(2)));
	    }}

	Collections.sort(lines);

	if(lines.size() > 0) {
	    println("PLAYERS UP FOR CONTENTION");
	    println("                         SWEEP   SPLIT     FOLD");
	    for(var line : lines)
		println(line);
	}
    }

    private ArrayList<Double> sweepSplitFold(String player,
					      HashMap<String, TreeSet<String>> opponents,
					      HashMap<String, Integer> scores, //clone
					      HashMap<String, String> free, //clone
					      int num_rounds, int cut_size) {
	var new_free = new HashMap<String, String>(free);

	var s1 = new HashMap<String, Integer>(scores);
	var s2 = new HashMap<String, Integer>(scores);
	var s3 = new HashMap<String, Integer>(scores);
	
	var left_player = player;
	String right_player = null;
	for(var entry : new_free.entrySet())
	    if(entry.getKey().equals(player) || entry.getValue().equals(player)) {
		new_free.remove(entry.getKey());
		if(entry.getKey().equals(player))
		    right_player = entry.getValue();
		else
		    right_player = entry.getKey();

		break;
	    }

	s1.put(left_player, s1.get(left_player) + 6); //sweep
	s2.put(left_player, s2.get(left_player) + 3); //split city
	s2.put(right_player, s2.get(right_player) + 3); //split city
	s3.put(right_player, s3.get(right_player) + 6); //sweep


	var outcomes1 = new HashMap<String, Integer>();
	var outcomes2 = new HashMap<String, Integer>();
	var outcomes3 = new HashMap<String, Integer>();

	var twofourone = false;
	    /* run all 3 simulations */
	calculate_recursive(opponents, s1, new_free, num_rounds, cut_size,
			    outcomes1, twofourone);
	calculate_recursive(opponents, s2, new_free, num_rounds, cut_size,
			    outcomes2, twofourone);
	calculate_recursive(opponents, s3, new_free, num_rounds, cut_size,
			    outcomes3, twofourone);

	var outcomes = new ArrayList<HashMap<String, Integer>>();
	outcomes.add(outcomes1);
	outcomes.add(outcomes2);
	outcomes.add(outcomes3);

	ArrayList<Double> res = new ArrayList<Double>();

	for(var outcome : outcomes) {
	    if(!outcome.containsKey(player)) {
		res.add(0d);
		continue;
	    }

	    var total = 0;
	    for(var entry : outcome.entrySet())
		total += entry.getValue();

	    var outcome_result = (outcome.get(player)/ (double)total) *(100*cut_size);
	    res.add(outcome_result);
	}

	return res;
    }

    private boolean safeToId(String player, HashMap<String, TreeSet<String>> opponents,
			     HashMap<String, Integer> scores, //clone
			     HashMap<String, String> free, //clone
			     int num_rounds, int cut_size) {
	//remove the player from the set of free rounds, and update scores
	var new_free = new HashMap<String, String>(free);
	var scenario = new HashMap<String, Integer>(scores);

	//pick the first game free
	for(var entry : new_free.entrySet())
	    if(entry.getKey().equals(player) || entry.getValue().equals(player)) {
		new_free.remove(entry.getKey());
		//update scores
		scenario.put(entry.getKey(), scenario.get(entry.getKey()) + 3); //split city
		scenario.put(entry.getValue(), scenario.get(entry.getValue()) + 3); //split city
		break;
	    }

	var outcomes = new HashMap<String, Integer>();

	//all scenarios are open (nobody is playing perfectly)
	calculate_recursive(opponents, scenario, new_free, num_rounds, cut_size,
			    outcomes, false);

	//find ourselves in outcomes if possible
	if(!outcomes.containsKey(player))
	    return false;

	var outcome = outcomes.get(player);

	//see if it's 100%
	var total = 0;
	for(var entry : outcomes.entrySet())
	    total += entry.getValue();

	DEBUGF("ID odds for %s: %6.3f%n", player, (outcome / (double)total) *(100*cut_size));

	return outcome * cut_size == total;
    }

    private String calculate_outcomes(HashMap<String, TreeSet<String>> opponents,
				    HashMap<String, Integer> scores,
				    HashMap<String, String> free,
				    int num_rounds, int cut_size,
				    boolean twofourone) {
	if(free.size() == 0) {
	    println(" FIXED RESULT");
	    println("==============");
	    /* just calculate SoS and ESoS
	       for netrunner, SoS is score of opponents / (3 * rounds) */
	    var standings = new ArrayList<Standing>();
	    for(var entry : scores.entrySet()) {
		standings.add(new Standing(entry.getKey(), entry.getValue()));
	    }

	    for(var standing : standings)
		standing.makeSOS(scores, opponents, num_rounds);

	    for(var standing : standings)
		standing.makeESOS(opponents, standings, num_rounds);

	    Collections.sort(standings);

	    int index = 1;
	    for(var standing : standings)
		printf("%2d: %s%n", index++, standing);

	    index = 1;

	    println();

	    for(var standing : standings)
		if(index > cut_size)
		    break;
		else
		    printf("%2dst seed: %s%n", index++, standing.name);

	    return null;
	}
	else {
	    //we become a first year cs student: do all posibilities,
	    //how do we do this, and what results do we get?
	    //we want a set of name -> number of times made the cut
	    StringBuilder res = new StringBuilder();

	    var outcomes = new HashMap<String, Integer>();

	    calculate_recursive(opponents, scores, free, num_rounds, cut_size,
				outcomes, twofourone);

	    int total = 0;
	    for(var entry : outcomes.entrySet())
		if(entry.getKey() != "(Bye)")
		    total += entry.getValue();

	    ArrayList<Odd> odds = new ArrayList<Odd>();

	    for(var entry : outcomes.entrySet())
		odds.add(new Odd(entry.getKey(), entry.getValue(), total, cut_size));

	    Collections.sort(odds);

	    if(twofourone)
		res.append(String.format("ODDS FOR TOP %d CUT (241's enforced):%n", cut_size));
	    else
		res.append(String.format("ODDS FOR TOP %d CUT (all outcomes):%n", cut_size));
	    for(var odd : odds)
		res.append(odd + "\n");

	    return res.toString();
	}
    }

    private class Odd implements Comparable<Odd>{
	String name;
	int count;
	Double pct;

	public Odd(String name, int count, int total, int cut_size) {
	    this.name = name;
	    this.count = count;
	    pct = (count / (double)total) * 100 * cut_size;
	}

	public String toString() {
	    return String.format("%-20s %7.3f%%", name, pct);
	}

	public int compareTo(Odd odd) {
	    return odd.pct.compareTo(pct);
	}
    }

    private void calculate_recursive(HashMap<String, TreeSet<String>> opponents, //const
				     HashMap<String, Integer> scores, //we will modify/clone this
				     HashMap<String, String> free, //we will modify/clone this
				     int num_rounds, int cut_size,
				     HashMap<String, Integer> outcomes,
				     boolean twofourone) {
	if(free.size() == 0) {
	    /* just calculate SoS and ESoS
	       for netrunner, SoS is score of opponents / (3 * rounds) */
	    var standings = new ArrayList<Standing>();
	    for(var entry : scores.entrySet()) {
		standings.add(new Standing(entry.getKey(), entry.getValue()));
	    }

	    for(var standing : standings)
		standing.makeSOS(scores, opponents, num_rounds);

	    for(var standing : standings)
		standing.makeESOS(opponents, standings, num_rounds);

	    Collections.sort(standings);

	    int index = 1;

	    /* record all outcomes */
	    for(var standing : standings)
		if(index++ > cut_size)
		    break;
		else
		    outcome(outcomes, standing.name);
	}
	else {
	    var new_free = new HashMap<String, String>(free);

	    //pick the first game free
	    var first_entry = new_free.entrySet().iterator().next();
	    new_free.remove(first_entry.getKey());

	    //run three scenarios: split, sweep (left), sweep (right)

	    var s1 = new HashMap<String, Integer>(scores);
	    var s3 = new HashMap<String, Integer>(scores);

	    var left_player = first_entry.getKey();
	    var right_player = first_entry.getValue();

	    s1.put(left_player, s1.get(left_player) + 6); //sweep
	    s3.put(right_player, s3.get(right_player) + 6); //sweep

	    /* run all 3 simulations */
	    calculate_recursive(opponents, s1, new_free, num_rounds, cut_size,
				outcomes, twofourone);
	    calculate_recursive(opponents, s3, new_free, num_rounds, cut_size,
				outcomes, twofourone);

	    if(!twofourone) {
		var s2 = new HashMap<String, Integer>(scores);
		s2.put(left_player, s2.get(left_player) + 3); //split city
		s2.put(right_player, s2.get(right_player) + 3); //split city
		calculate_recursive(opponents, s2, new_free, num_rounds, cut_size,
				    outcomes, twofourone);
	    }
	}
    }

    private void outcome(HashMap<String, Integer> outcomes, String player) {
	if(!outcomes.containsKey(player))
	    outcomes.put(player, 1);
	else
	    outcomes.put(player, outcomes.get(player)+1);
    }


    private class Standing implements Comparable<Standing>{
	String name;
	Integer score = 0;
	Double SoS = 0d;
	Double ESoS = 0d;

	public Standing(String name, int score) {
	    this.name = name;
	    this.score = score;
	}

	public String toString() {
	    return String.format("%-20s %3d %06.3f %3.3f", name, score, SoS, ESoS);
	}

	public void makeSOS(HashMap<String, Integer> scores,
			    HashMap<String, TreeSet<String>> opps,
			    int rounds) {
	    var my_opponents = opps.get(name);

	    if(got_bye(my_opponents))
		rounds--;

	    for(var opponent : my_opponents)
		SoS += scores.get(opponent);

	    SoS /= (double)(rounds * (3));
	}

	public void makeESOS(HashMap<String, TreeSet<String>> opps,
			     ArrayList<Standing> standings,
			     int rounds) {
	    if(got_bye(opps.get(name)))
		rounds--;

	    for(var stand : standings)
		if(opps.get(name).contains(stand.name)
		   && !stand.name.equals("(Bye)"))
		    ESoS += stand.SoS;

	    ESoS /= rounds;
	}

	private boolean got_bye(TreeSet<String> opponents) {
	    return opponents.contains("(Bye)");
	}

	public int compareTo(Standing s) {
	    var res = s.score.compareTo(score);
	    if(res != 0) return res;

	    res = s.SoS.compareTo(SoS);
	    if(res != 0) return res;

	    res = s.ESoS.compareTo(ESoS);
	    if(res != 0) return res;

	    res = name.compareTo(s.name);
	    return res;
	}
    }

    private void free(HashMap<String, String> free, String left, String right) {
	free.put(left, right);
    }

    private Integer parse_score(String score) {
	if(score.length() == 0 || score == null)
	    return null;

	return Integer.parseInt(score.split(" ")[0]);
    }

    public void score(HashMap<String, Integer> scores,
		      String player, int score) {
	if(scores.get(player) == null)
	    scores.put(player, score);
	else
	    scores.put(player, scores.get(player) + score);
    }

    public void entry(HashMap<String, TreeSet<String>> matchings,
		      String player, String... opponents) {

	if(matchings.get(player) == null)
		matchings.put(player, new TreeSet<String>());

	TreeSet<String> opps = matchings.get(player);

	for(int i = 0; i < opponents.length; i++)
	    opps.add(opponents[i]);
    }

    /* set commands */
    @Override public Command[] setCommands() {
	//do you want paged input to be optional? This is mainly a debugging thing,
	//or a memory management/speed thing
	_PAGE_OPTIONAL = false; //page does not show up as a user input command
	_PAGE_ENABLED = false;  //page is set to disabled by default

	return new Command[] {new FileCommand("Pairings",
					      "The list of all pairings for this tournament",
					      true,
					      "-p", "--pairings"),

			      new IntCommand(1, 10, true, 1, "-r", "--round-count")
			      .setName("Rounds")
			      .setDescription("Number of rounds for this tournament"),

			      new IntCommand(1, 10, true, 1, "-cs", "--cut-size")
			      .setName("Cut Size")
			      .setDescription("Number of players in the top cut"),

			      new StringCommand("Inspect Player",
						"Examines the exact (free) scenarios in which a given player can make it into the cut",
						null,
						false,
						"-ip", "--inspect-player", "--show-me"),

			      new IntCommand(1, 1000, false, 5, "--scenario-max", "--max")
			      .setName("Scenario Max")
			      .setDescription("Maximum number of scenarios to display for Inspect Player command"),

			      new StringCommand("Show Opponents",
						"Shows the opponents/pairings for a player",
						null,
						false,
						"-sp", "--show-opponents", "--pairings")
	};
    }

    /* act after commands processed - userCommands stores all the commands set in setCommands */
    @Override public int actOnCommands(Command[] userCommands) throws Exception {
	//do whatever you want based on the commands you have given
	//at this stage, they should all be resolved

	pairings = readFileLines(((FileCommand)userCommands[0]).getValue());
	roundCount = ((IntCommand)userCommands[1]).getValue();
	cutSize = ((IntCommand)userCommands[2]).getValue();
	inspectPlayer = ((StringCommand)userCommands[3]).getValue();
	scenarioMax = ((IntCommand)userCommands[4]).getValue();
	showOpponents = ((StringCommand)userCommands[5]).getValue();
	return 0;
    }

    /**
     * Creates and runs an instance of your class - do not modify
     */
    public static void main(String[] argv) {
        new Outcomes().run(argv);
    }
}
