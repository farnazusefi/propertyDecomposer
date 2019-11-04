import java.util.HashSet;
import java.util.Set;

enum StateType {
	INIT, MIDDLE, FINAL;
}

public class State {
	static int unique_id;
	private int id;
	private Set<Transition> transitions;
	private StateType type;
	private String label;

	public State(String label) {
		this.setLabel(label);
		resetTransitions();
		type = StateType.MIDDLE;
	}

	// public State() {
	// setId(unique_id++);
	// resetTransitions();
	// type = StateType.MIDDLE;
	// }

	final void resetTransitions() {
		transitions = new HashSet<Transition>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Set<Transition> getTransitions() {
		return transitions;
	}

	public void addTransition(Transition t) {
		transitions.add(t);
	}

	public void removeTransition(Transition t) {
		transitions.remove(t);
	}

	public StateType getType() {
		return type;
	}

	public void setType(StateType type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	private void setLabel(String label) {
		this.label = label;
	}

}
