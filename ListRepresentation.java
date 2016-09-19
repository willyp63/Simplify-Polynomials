import java.util.ArrayList;
import java.util.List;

public class ListRepresentation {
	private ExpressionAtom nodeVal = null;
	public List<ListRepresentation> operands = new ArrayList<ListRepresentation>();
	private boolean isNegative = false;
	
	public void setNodeVal(ExpressionAtom inputNodeVal) {
		this.nodeVal = inputNodeVal;
	}
	
	public void setNegative(boolean inputIsNegative) {
		this.isNegative = inputIsNegative;
	}
		
	public ExpressionAtom getNodeVal() {
		return this.nodeVal;
	}
	
	public boolean isNegative() {
		return this.isNegative;
	}
	
	public ListRepresentation copy(){
		ListRepresentation lr = new ListRepresentation();
		lr.setNegative(isNegative);
		lr.setNodeVal(new ExpressionAtom(nodeVal.getVariablesOrOperator(), nodeVal.getAtomType(), nodeVal.getCoefficient()));
		
		for(int i = 0; i < operands.size(); i++){
			ListRepresentation operand = operands.get(i).copy();
			lr.operands.add(operand);
		}
		
		return lr;
	}
}