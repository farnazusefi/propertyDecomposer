package main.java;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import org.logicng.io.parsers.ParserException;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Not;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.rits.cloning.Cloner;

public class PropertyDecomposer {

	private BuchiAutomata safetyBuchiAutomata;
	private BuchiAutomata livenessBuchiAutomata;

	public PropertyDecomposer() {

		safetyBuchiAutomata = new BuchiAutomata();
		livenessBuchiAutomata = new BuchiAutomata();
	}

	public void decomposeBuchiAutomata(BuchiAutomata inputBuchiAutomata) throws ParserException {

		safetyBuchiAutomata = createSafetyBuchiAutomataOfProperty(inputBuchiAutomata);
		livenessBuchiAutomata = createLivenessBuchiAutomata(inputBuchiAutomata);
	}

	private BuchiAutomata createLivenessBuchiAutomata(BuchiAutomata inputBuchiAutomata) throws ParserException {

		Cloner cloner = new Cloner();
		BuchiAutomata outBuchiAutomata = cloner.deepClone(inputBuchiAutomata);

		if (BuchiAutomata.isDeterministic(outBuchiAutomata)) {
			outBuchiAutomata = enhanceBuchiAutomataWithTrapState(outBuchiAutomata);
		} else {
			BuchiAutomata envBuchiAutomata = createEnvBuchiAutomata(inputBuchiAutomata);
			outBuchiAutomata = createProductAutomata(inputBuchiAutomata, envBuchiAutomata);
		}
		return outBuchiAutomata;
	}

	protected BuchiAutomata enhanceBuchiAutomataWithTrapState(BuchiAutomata outBuchiAutomata) {
		State trapState = new State("qtrap");
		Hashtable<String, State> states = outBuchiAutomata.getStates();
		Set<Entry<String, State>> entrySet = states.entrySet();
		outBuchiAutomata.addState(trapState);
		for (Entry<String, State> tableElement : entrySet) {
			State state = tableElement.getValue();
			Set<Transition> transitions = state.getTransitions();
			Expression<String> trapFormula = ExprParser.parse("true");
			if (transitions != null) {
				trapFormula = ExprParser.parse("false");
				for (Transition transition : transitions) {
					trapFormula = Or.of(trapFormula, transition.getCondition());
				}
				trapFormula = RuleSet.simplify(Not.of(trapFormula));

			}

			state.addTransition(new Transition(trapFormula, trapState));
		}
		return outBuchiAutomata;
	}

	private BuchiAutomata createEnvBuchiAutomata(BuchiAutomata inputBuchiAutomata) {
		Cloner cloner = new Cloner();
		BuchiAutomata envBuchiAutomata = cloner.deepClone(inputBuchiAutomata);
		Hashtable<String, State> states = envBuchiAutomata.getStates();
		// State trapState = new State("qtrap");
		envBuchiAutomata = enhanceBuchiAutomataWithTrapState(envBuchiAutomata);

		Set<Entry<String, State>> entrySet = states.entrySet();

		for (Entry<String, State> tableElement : entrySet) {
			State state = tableElement.getValue();
			if (state.getType().equals(StateType.FINAL))
				state.setType(StateType.MIDDLE);
		}
		envBuchiAutomata.getState("qtrap").setType(StateType.FINAL);
		return envBuchiAutomata;
	}

	public BuchiAutomata createProductAutomata(BuchiAutomata b1, BuchiAutomata b2) {
		b1 = decomposeTransitionsOfBuchiAutomata(b1);
		b2 = decomposeTransitionsOfBuchiAutomata(b2);
		BuchiAutomata productAutomata = new BuchiAutomata();

		Hashtable<String, State> b1States = b1.getStates();
		Set<Entry<String, State>> entrySet1 = b1States.entrySet();

		Hashtable<String, State> b2States = b2.getStates();
		Set<Entry<String, State>> entrySet2 = b2States.entrySet();

		for (Entry<String, State> tableElement1 : entrySet1) {
			State state1 = tableElement1.getValue();
			Set<Transition> transitionsOf1 = state1.getTransitions();
			for (Entry<String, State> tableElement2 : entrySet2) {
				State state2 = tableElement2.getValue();
				Set<Transition> transitionsOf2 = state2.getTransitions();

				for (Transition t1 : transitionsOf1) {
					for (Transition t2 : transitionsOf2) {
						if (t1.getCondition().equals(t2.getCondition())) {
							String sourceLabel = state1.getLabel() + "." + state2.getLabel();
							if (!productAutomata.hasState(sourceLabel)) {
								State newState = new State(sourceLabel);
								newState.setType(typeOfProductAutomataState(state1, state2));
								productAutomata.addState(newState);
							}
							String destLabel = t1.getNextState().getLabel() + "." + t2.getNextState().getLabel();
							if (!productAutomata.hasState(destLabel)) {
								State newState = new State(destLabel);
								newState.setType(typeOfProductAutomataState(state1, state2));
								productAutomata.addState(newState);

							}
							productAutomata.getState(sourceLabel).addTransition(
									new Transition(t1.getCondition(), productAutomata.getState(destLabel)));
						}
					}
				}
			}

		}

		return composeTransitionsOfBuchiAutomata(productAutomata);
	}

	private BuchiAutomata composeTransitionsOfBuchiAutomata(BuchiAutomata b) {
		BuchiAutomata composedTBA = new BuchiAutomata();
		Cloner cloner = new Cloner();
		composedTBA = cloner.deepClone(b);
		Hashtable<String, State> states = b.getStates();
		Set<Entry<String, State>> entrySet = states.entrySet();
		for (Entry<String, State> tableElement : entrySet) {
			State state = tableElement.getValue();
			if (state.getTransitions() != null) {
				ArrayList<Transition> transitions = new ArrayList<Transition>(state.getTransitions());
				while (true) {
					boolean isUpdated = false;
					ArrayList<Expression<String>> toComposeConditions = new ArrayList<Expression<String>>();
					ArrayList<Transition> toComposeTransitions = new ArrayList<Transition>();
					for (int i = 0; i < state.getTransitions().size(); i++) {
						toComposeConditions.add(transitions.get(i).getCondition());
						toComposeTransitions.add(transitions.get(i));
						for (int j = i; j < state.getTransitions().size(); j++) {
							if (transitions.get(i).getNextState().equals(transitions.get(j).getNextState())) {
								isUpdated = true;
								toComposeConditions.add(transitions.get(j).getCondition());
								toComposeTransitions.add(transitions.get(j));
							}
						}
						if (isUpdated) {
							Transition newTransition = new Transition(Or.of(toComposeConditions),
									transitions.get(i).getNextState());
							state.addTransition(newTransition);
							for (Transition t : toComposeTransitions) {
								state.removeTransition(t);
							}
							break;
						}
					}
					break;
				}
			}

		}
		return composedTBA;
	}

	protected StateType typeOfProductAutomataState(State state1, State state2) {
		if (state1.getType().equals(StateType.INIT) && state2.getType().equals(StateType.INIT))
			return StateType.INIT;
		else if (state1.getType().equals(StateType.FINAL) || state2.getType().equals(StateType.FINAL))
			return StateType.FINAL;
		return StateType.MIDDLE;
	}

	private BuchiAutomata decomposeTransitionsOfBuchiAutomata(BuchiAutomata b1) {
		BuchiAutomata decomposedTBA = new BuchiAutomata();
		Cloner cloner = new Cloner();
		decomposedTBA = cloner.deepClone(b1);
		Hashtable<String, State> states = b1.getStates();
		Set<Entry<String, State>> entrySet = states.entrySet();
		for (Entry<String, State> tableElement : entrySet) {
			State state = tableElement.getValue();
			decomposedTBA.getState(state.getLabel()).resetTransitions();
			for (Transition t : state.getTransitions()) {
				Expression<String> condition = t.getCondition();
				Expression<String> sop = RuleSet.toSop(condition);
				String sopString = sop.toLexicographicString();
				if (sopString.contains("|")) {
					String[] productStatements = sopString.split("\\|");
					for (String statement : productStatements) {
						statement = statement.replaceAll("\\(|\\)", "");
						Transition newTrans = new Transition(ExprParser.parse(statement), t.getNextState());
						decomposedTBA.getState(state.getLabel()).addTransition(newTrans);
					}
				} else {
					decomposedTBA.getState(state.getLabel()).addTransition(t);
				}
				System.out.println("sop: " + sopString);
			}
		}

		return decomposedTBA;
	}

	public BuchiAutomata createSafetyBuchiAutomataOfProperty(BuchiAutomata inputBuchiAutomata) {
		Cloner cloner = new Cloner();
		BuchiAutomata outBuchiAutomata = cloner.deepClone(inputBuchiAutomata);
		Hashtable<String, State> states = outBuchiAutomata.getStates();
		Set<Entry<String, State>> entrySet = states.entrySet();
		for (Entry<String, State> tableElement : entrySet) {
			State state = tableElement.getValue();
			state.setType(StateType.FINAL);
		}
		return outBuchiAutomata;
	}

	public BuchiAutomata getSafetyBuchiAutomata() {
		return safetyBuchiAutomata;
	}

	public void setSafetyBuchiAutomata(BuchiAutomata safetyBuchiAutomata) {
		this.safetyBuchiAutomata = safetyBuchiAutomata;
	}

	public BuchiAutomata getLivenessBuchiAutomata() {
		return livenessBuchiAutomata;
	}

	public void setLivenessBuchiAutomata(BuchiAutomata livenessBuchiAutomata) {
		this.livenessBuchiAutomata = livenessBuchiAutomata;
	}
}
