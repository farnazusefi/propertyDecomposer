package main.java;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Set;

import org.logicng.formulas.FormulaFactory;
import org.logicng.io.parsers.ParserException;
import org.logicng.io.parsers.PropositionalParser;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Not;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

public class Main {

	public static void main(String[] args) throws IOException, ParserException {

		String fileName = "ltl2ba";
//		BuchiAutomata inputBuchiAutomata = constructBuchiAutomataFromFile(fileName);
		BuchiAutomata inputBuchiAutomata = constructBuchiAutomataFromLtl2ba(fileName);
		PropertyDecomposer decomposer = new PropertyDecomposer();
		decomposer.decomposeBuchiAutomata(inputBuchiAutomata);
		BuchiAutomata livenessBuchiAutomata = decomposer.getLivenessBuchiAutomata();
		BuchiAutomata safetyBuchiAutomata = decomposer.getSafetyBuchiAutomata();
	}

	private static BuchiAutomata constructBuchiAutomataFromLtl2ba(String fileName) throws IOException {

		BuchiAutomata buchiAutomata = new BuchiAutomata();
		RandomAccessFile file = new RandomAccessFile(fileName, "r");
		String line;
		file.readLine();
		while (!(line = file.readLine()).contains("}")) {
			System.out.println(line);
			String fromStateLabel = line.split(":")[0].trim();
			State fromState;
			Set<Expression<String>> transitionsFormulaSet = new HashSet<Expression<String>>();
			if (!buchiAutomata.hasState(fromStateLabel)) {
				fromState = new State(fromStateLabel);
				setStateType(buchiAutomata, fromStateLabel, fromState);
			} else {
				fromState = buchiAutomata.getState(fromStateLabel);
			}
			line = file.readLine();
			if (line.contains("skip"))
				continue;
			String remainingTransitionTo = null;
			while (!(line = file.readLine()).contains("fi;")) {
				String[] lineArr = (line.split("::")[1]).split("->");
				String transitionLabel = lineArr[0].trim();
				Expression<String> transitionFormula = RuleSet.simplify(ExprParser.parse((transitionLabel.replaceAll("&&", "&").replace("||","|"))));
				String toStateLabel = lineArr[1].substring(5).trim();
				State toState;
				if (transitionLabel.contains("(1)"))
					remainingTransitionTo = lineArr[1].substring(5).trim();
				else {
					if (!buchiAutomata.hasState(toStateLabel)) {
						toState = new State(toStateLabel);
						setStateType(buchiAutomata, toStateLabel, toState);
					} else {
						toState = buchiAutomata.getState(toStateLabel);
					}
					Transition transition = new Transition(transitionFormula, toState);
					fromState.addTransition(transition);
					transitionsFormulaSet.add(transitionFormula);
				}

			}
			if (remainingTransitionTo != null) {
				Expression<String> elseFormula = ExprParser.parse("true");
				if (transitionsFormulaSet != null) {
					elseFormula = ExprParser.parse("false");
					for (Expression<String> trFormula : transitionsFormulaSet) {
						elseFormula = Or.of(elseFormula, trFormula);
					}
					elseFormula = RuleSet.simplify(Not.of(elseFormula));

				}
				State elseState;
				if (buchiAutomata.hasState(remainingTransitionTo))
					elseState = buchiAutomata.getState(remainingTransitionTo);
				else {
					elseState = new State(remainingTransitionTo);
					setStateType(buchiAutomata, remainingTransitionTo, elseState);
				}
				Transition transition = new Transition(elseFormula, elseState);
				fromState.addTransition(transition);

			}
		}
		file.close();
		return buchiAutomata;
	}

	protected static void setStateType(BuchiAutomata buchiAutomata, String toStateLabel, State toState) {
		if (toStateLabel.contains("init"))
			toState.setType(StateType.INIT);
		if (toStateLabel.contains("accept"))
			toState.setType(StateType.FINAL);
		buchiAutomata.addState(toState);
	}

	private static BuchiAutomata constructBuchiAutomataFromFile(String fileName) throws IOException, ParserException {

		BuchiAutomata buchiAutomata = new BuchiAutomata();

		RandomAccessFile file = new RandomAccessFile(fileName, "r");
		String line;
		while ((line = file.readLine()) != null) {

			String[] arrOfStr = line.split("--");
			String fromStateLabel = arrOfStr[0];
			String toStateLabel = arrOfStr[2];
			// FormulaFactory f = new FormulaFactory();
			// PropositionalParser p = new PropositionalParser(f);
			Expression<String> transitionFormula = RuleSet.simplify(ExprParser.parse(arrOfStr[1]));
			State fromState;
			State toState;
			if (fromStateLabel.contains("[init]")) {
				fromStateLabel = fromStateLabel.replace("[init]", "");
				if (buchiAutomata.hasState(fromStateLabel))
					fromState = buchiAutomata.getState(fromStateLabel);
				else {
					fromState = new State(fromStateLabel);
					fromState.setType(StateType.INIT);
					buchiAutomata.addState(fromState);
				}
			} else if (fromStateLabel.contains("[final]")) {
				fromStateLabel = fromStateLabel.replace("[final]", "");
				if (buchiAutomata.hasState(fromStateLabel))
					fromState = buchiAutomata.getState(fromStateLabel);
				else {
					fromState = new State(fromStateLabel);
					fromState.setType(StateType.FINAL);
					buchiAutomata.addState(fromState);
				}
			} else {
				if (buchiAutomata.hasState(fromStateLabel))
					fromState = buchiAutomata.getState(fromStateLabel);
				else {
					fromState = new State(fromStateLabel);
					buchiAutomata.addState(fromState);
				}
			}
			if (toStateLabel.contains("[init]")) {
				toStateLabel = toStateLabel.replace("[init]", "");
				if (buchiAutomata.hasState(toStateLabel))
					toState = buchiAutomata.getState(toStateLabel);
				else {
					toState = new State(toStateLabel);
					toState.setType(StateType.INIT);
					buchiAutomata.addState(toState);
				}
			} else if (toStateLabel.contains("[final]")) {
				toStateLabel = toStateLabel.replace("[final]", "");
				if (buchiAutomata.hasState(toStateLabel))
					toState = buchiAutomata.getState(toStateLabel);
				else {
					toState = new State(toStateLabel);
					toState.setType(StateType.FINAL);
					buchiAutomata.addState(toState);
				}
			} else {
				if (buchiAutomata.hasState(toStateLabel))
					toState = buchiAutomata.getState(toStateLabel);
				else {
					toState = new State(toStateLabel);
					buchiAutomata.addState(toState);
				}
			}
			Transition transition = new Transition(transitionFormula, toState);
			fromState.addTransition(transition);
		}
		file.close();

		return buchiAutomata;
	}

}
