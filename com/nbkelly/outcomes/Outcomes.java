package com.nbkelly.outcomes;

/* imports */
import com.nbkelly.aux.Drafter;
import com.nbkelly.aux.Command;
import com.nbkelly.aux.FileCommand;
import com.nbkelly.aux.IntCommand;
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
	    println("PLAYERS UP FOR CONTENTION");
	    sweepSplitFold_display(ssf_simple);
	}

	
	
	//println(ssf_simple);
	return DEBUG(1, t.split("Finished Processing"));
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

	println("                         SWEEP   SPLIT     FOLD");
	for(var line : lines)
	    println(line);
    }

    private ArrayList<Double> sweepSplitFold(String player,
					      HashMap<String, TreeSet<String>> opponents,
					      HashMap<String, Integer> scores, //clone
					      HashMap<String, String> free, //clone
					      int num_rounds, int cut_size) {
	var new_free = (HashMap<String, String>)(free.clone());

	var s1 = (HashMap<String, Integer>)(scores.clone());
	var s2 = (HashMap<String, Integer>)(scores.clone());
	var s3 = (HashMap<String, Integer>)(scores.clone());

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
	
	var new_free = (HashMap<String, String>)(free.clone());

	var scenario = (HashMap<String, Integer>)(scores.clone());		
	

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
		res.append(String.format("ODDS FOR TOP %d CUT (all ourcomes):%n", cut_size));
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
	    var new_free = (HashMap<String, String>)(free.clone());

	    //pick the first game free
	    var first_entry = new_free.entrySet().iterator().next();
	    new_free.remove(first_entry.getKey());

	    //run three scenarios: split, sweep (left), sweep (right)

	    var s1 = (HashMap<String, Integer>)(scores.clone());	    
	    var s3 = (HashMap<String, Integer>)(scores.clone());

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
		var s2 = (HashMap<String, Integer>)(scores.clone());		
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
			      new IntCommand(1, 10, true, 1, "-r", "--round-count"),
			      new IntCommand(1, 10, true, 1, "-cs", "--cut-size")
	};
    }
    
    /* act after commands processed - userCommands stores all the commands set in setCommands */
    @Override public int actOnCommands(Command[] userCommands) throws Exception {
	//do whatever you want based on the commands you have given
	//at this stage, they should all be resolved

	pairings = readFileLines(((FileCommand)userCommands[0]).getValue());
	roundCount = ((IntCommand)userCommands[1]).getValue();
	cutSize = ((IntCommand)userCommands[2]).getValue();
	return 0;
    }

    /**
     * Creates and runs an instance of your class - do not modify
     */
    public static void main(String[] argv) {
        new Outcomes().run(argv);
    }
}
