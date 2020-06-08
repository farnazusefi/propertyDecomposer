package main.java;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

public class BuchiAutomata {

	private Hashtable<String, State> states;

	public BuchiAutomata() {

		states = new Hashtable<String, State>();
	}

	public Hashtable<String, State> getStates() {
		return states;
	}

	public void setStates(Hashtable<String, State> states) {
		this.states = states;
	}

	public boolean hasState(String fromStateLabel) {

		return states.containsKey(fromStateLabel);
	}

	public State getState(String fromStateLabel) {
		if (hasState(fromStateLabel))
			return (states.get(fromStateLabel));
		return null;
	}

	public void addState(State fromState) {

		states.put(fromState.getLabel(), fromState);

	}

	public static boolean isDeterministic(BuchiAutomata outBuchiAutomata) {
		Hashtable<String, State> states = outBuchiAutomata.getStates();
		Set<Entry<String, State>> entrySet = states.entrySet();
		int numOfInitialStates = 0;
		for (Entry<String, State> tableElement : entrySet) {
			State state = tableElement.getValue();
			if (state.getType().equals(StateType.INIT)) {
				numOfInitialStates++;
				if (numOfInitialStates > 1) {
					return false;
				}
			}

		}
		for (Entry<String, State> tableElement : entrySet) {
			ArrayList<Expression<String>> transitionStatements = new ArrayList<Expression<String>>();
			State state = tableElement.getValue();
			Set<Transition> transitions = state.getTransitions();
			for (Transition trans : transitions) {
				Expression<String> condition = trans.getCondition();
				transitionStatements.add(condition);
			}
			for (int i = 0; i < transitionStatements.size(); i++) {
				Expression<String> firstStatement = transitionStatements.get(i);
				for (int j = i + 1; j < transitionStatements.size(); j++) {
					Expression<String> secondStatement = transitionStatements.get(j);
					Expression<String> simplified = RuleSet.simplify(And.of(firstStatement, secondStatement));
					Expression<String> fExp = ExprParser.parse("false");
					if (!simplified.equals(fExp)) {
						return false;
					}
				}
			}

		}
		return true;
	}
}
