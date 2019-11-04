import org.logicng.formulas.Formula;

import com.bpodgursky.jbool_expressions.Expression;

public class Transition {

	private Expression<String> condition;
	private State nextState;
	
	
	public Transition(Expression<String> transitionLabel, State nextState) {
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

	public Expression<String> getCondition() {
		return condition;
	}

	public void setCondition(Expression<String> condition) {
		this.condition = condition;
	}
}
