import org.logicng.formulas.Formula;

public class Transition {

	private Formula condition;
	private State nextState;
	
	
	public Transition(Formula transitionLabel, State nextState) {
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

	public Formula getCondition() {
		return condition;
	}

	public void setCondition(Formula condition) {
		this.condition = condition;
	}
}
