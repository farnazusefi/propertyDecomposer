import java.util.Hashtable;

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
		// TODO Auto-generated method stub
		return true;
	}
}
