
public class Transition {

	private String condition;
	private State nextState;
	
	
	public Transition(String transitionLabel, State nextState) {
		super();
		this.condition = transitionLabel;
		this.nextState = nextState;
	}

	public State getNextState() {
		return nextState;
	}

	public void setNextState(State nextState) {
		this.nextState = nextState;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}
}
