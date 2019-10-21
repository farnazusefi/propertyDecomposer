import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.io.parsers.ParserException;
import org.logicng.io.parsers.PropositionalParser;

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
		State trapState = new State("qtrap");

		Hashtable<String, State> states = outBuchiAutomata.getStates();
		Set<Entry<String, State>> entrySet = states.entrySet();

		if (BuchiAutomata.isDeterministic(outBuchiAutomata)) {
			outBuchiAutomata.addState(trapState);
			for (Entry<String, State> tableElement : entrySet) {
				State state = tableElement.getValue();
				Set<Transition> transitions = state.getTransitions();
				FormulaFactory trapFormulaFactory = new FormulaFactory();
				final PropositionalParser p = new PropositionalParser(trapFormulaFactory);
				Formula trapFormula = p.parse("true");
				if (transitions != null) {
					HashSet<Formula> conditionFormulaSet = new HashSet<Formula>();
					for (Transition transition : transitions) {
						conditionFormulaSet.add(transition.getCondition());
					}
					trapFormula = trapFormulaFactory.not(trapFormulaFactory.and(conditionFormulaSet));
				}
				state.addTransition(new Transition(trapFormula, trapState));
			}
		} else {
			BuchiAutomata envBuchiAutomata = createEnvBuchiAutomata(inputBuchiAutomata);

			// TODO calculating m X env
		}
		return outBuchiAutomata;
	}

	private BuchiAutomata createEnvBuchiAutomata(BuchiAutomata inputBuchiAutomata) {
		Cloner cloner = new Cloner();
		BuchiAutomata envBuchiAutomata = cloner.deepClone(inputBuchiAutomata);
		Hashtable<String, State> states = envBuchiAutomata.getStates();
		State trapState = new State("qtrap");

		Set<Entry<String, State>> entrySet = states.entrySet();

		for (Entry<String, State> tableElement : entrySet) {
			State state = tableElement.getValue();
			if (state.getType().equals(StateType.FINAL))
				state.setType(StateType.MIDDLE);
		}
		trapState.setType(StateType.FINAL);
		envBuchiAutomata.addState(trapState);
		// TODO transition of env
		return null;
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
