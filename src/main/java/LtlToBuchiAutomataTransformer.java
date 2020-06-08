package main.java;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import gov.nasa.ltl.graph.Degeneralize;
import gov.nasa.ltl.graph.Edge;
import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.SCCReduction;
import gov.nasa.ltl.graph.SFSReduction;
import gov.nasa.ltl.graph.Simplify;
import gov.nasa.ltl.graph.SuperSetReduction;
import gov.nasa.ltl.trans.Formula;
import gov.nasa.ltl.trans.Node;
import gov.nasa.ltl.trans.ParseErrorException;
import gov.nasa.ltl.trans.Pool;
import gov.nasa.ltl.trans.Rewriter;
import gov.nasa.ltl.trans.Translator;

public class LtlToBuchiAutomataTransformer {

	
	
	public static Graph ltl2BA(Formula ltlFormula) {
		try {
			ltlFormula = Rewriter.rewrite(ltlFormula);
		} catch (ParseErrorException e) {
			e.printStackTrace();
		}
		Translator.set_algorithm(Translator.LTL2BUCHI);
		Graph gba = Translator.translate(ltlFormula);
		gba = SuperSetReduction.reduce(gba);
		Graph ba = Degeneralize.degeneralize(gba);
		ba = SCCReduction.reduce(ba);
		ba = Simplify.simplify(ba);
		ba = SFSReduction.reduce(ba);

		Node.reset_static();
		Formula.reset_static();
		Pool.reset_static();
		return ba;
	}

	public static String exportGraph(Graph graph) {
		StringBuffer result = new StringBuffer("\n");
		LinkedList<gov.nasa.ltl.graph.Node> openBorder = new LinkedList<gov.nasa.ltl.graph.Node>();
		Set<gov.nasa.ltl.graph.Node> visited = new HashSet<gov.nasa.ltl.graph.Node>();
		openBorder.add(graph.getInit());
		visited.add(graph.getInit());
		while (!openBorder.isEmpty()) {
			gov.nasa.ltl.graph.Node node = openBorder.removeFirst();
			if (node.getBooleanAttribute("accepting"))
				result.insert(0, node.getId() + ", ");
			for (Edge next : node.getOutgoingEdges()) {
				result.append(node.getId() + "->" + next.getNext().getId() + "[" + next.getAction() + ", "
						+ next.getGuard() + "]\n");
				if (!visited.contains(next.getNext())) {
					visited.add(next.getNext());
					openBorder.addLast(next.getNext());
				}
			}
		}

		return result.toString();
	}

	public static void main(String[] args) {
		//[](a && <>b \/ Ox)
		//To become familiar with creating different instances of Formula, check lines 476-580 of Formula class.
		Formula a = new Formula('p', true, null, null, "a");
		Formula b = new Formula('p', true, null, null, "b");
		Formula TRUE = new Formula('t', true, null, null, null);
		Formula Fb = new Formula('U', false, TRUE, b, null); // <>b = true U b
		Formula a_and_Fb = new Formula('A', false, a, Fb, null);
		Formula x = new Formula('p', true, null, null, "x");
		Formula Ox = new Formula('X', false, x, null, null);
		Formula a_and_Fb_or_nextX = new Formula('O', false, a_and_Fb, Ox, null);
		
		Formula FALSE = new Formula('f', true, null, null, null);
		Formula root = new Formula('V', false, FALSE, a_and_Fb_or_nextX, null);
		
		Graph ltl2ba = ltl2BA(root);
		System.out.println(exportGraph(ltl2ba));
		
	}
}
