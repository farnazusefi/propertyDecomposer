import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import com.rits.cloning.Cloner;

public class PropertyDecomposer {

	private BuchiAutomata safetyBuchiAutomata;
	private BuchiAutomata livenessBuchiAutomata;

	public PropertyDecomposer() {

		safetyBuchiAutomata = new BuchiAutomata();
		livenessBuchiAutomata = new BuchiAutomata();
	}

	public void decomposeBuchiAutomata(BuchiAutomata inputBuchiAutomata) {

		safetyBuchiAutomata = createSafetyBuchiAutomataOfProperty(inputBuchiAutomata);
		livenessBuchiAutomata = createLivenessBuchiAutomata(inputBuchiAutomata);
	}

	private BuchiAutomata createLivenessBuchiAutomata(BuchiAutomata inputBuchiAutomata) {

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
				String trapLabel = "true";
				if (transitions != null) {
					trapLabel = "~ (";
					for (Transition transition : transitions) {
						String condition = transition.getCondition();
						trapLabel += "(" + condition + ")" + "V";
					}
					String substring = trapLabel.substring(trapLabel.length() - 1, trapLabel.length());
					if (substring.equals("V"))
						trapLabel = trapLabel.substring(0, trapLabel.length() - 2);
					trapLabel += ")";
				}
				state.addTransition(new Transition(trapLabel, trapState));
			}
		} else {
			BuchiAutomata envBuchiAutomata = createEnvBuchiAutomata(inputBuchiAutomata);

			//TODO calculating m X env
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
