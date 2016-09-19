import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Polynomial {
	List<ExpressionAtom> infixExpression = new ArrayList<ExpressionAtom>();
	
	List<ExpressionAtom> finalExpression;

	ListRepresentation listRepresentation;

	private String identifyUnaryMinuses(String expression) {
		if(expression.startsWith("-")) {
			expression = "%" + expression.substring(1); 
		}
		
		char[] expressionAtoms = expression.toCharArray();
		String returnExpression = String.valueOf(expressionAtoms[0]);
		for(int i = 1; i < expressionAtoms.length; ++i) {
			if(expressionAtoms[i] == '-' && expressionAtoms[i-1] == '(')
				returnExpression += "%";
			else
				returnExpression += String.valueOf(expressionAtoms[i]);
		}
		
		return returnExpression;
	}
	
	private String insertMultiplicationSigns(String expression) {
		char[] expressionAtoms = expression.toCharArray();
		String returnExpression = String.valueOf(expressionAtoms[0]);
		
		for(int i = 1; i < expressionAtoms.length; ++i) {
			if(!isOperator(expressionAtoms[i]) && !Character.isDigit(expressionAtoms[i]) 
					&& Character.isDigit(expressionAtoms[i-1]))
				returnExpression += "*";
			returnExpression +=  String.valueOf(expressionAtoms[i]);
		}
		
		return returnExpression;
	}
	
	private boolean isOperator(char token) {
		return token == '+' || token == '-' || token == '*' || token == '^'
				|| token == '(' || token == ')';
	}
	
	private List<ExpressionAtom> parseInputPolynomial(String inputExpression) {
		inputExpression = identifyUnaryMinuses(inputExpression);
		inputExpression = insertMultiplicationSigns(inputExpression);
		
		List<ExpressionAtom> inputExpressionTokens = new ArrayList<ExpressionAtom>();
		
		char[] inputChars = inputExpression.toCharArray();
		for(int i = 0; i < inputChars.length; ++i) {
			if(isOperator(inputChars[i]) || inputChars[i] == '%') {
				inputExpressionTokens.add(new ExpressionAtom(String.valueOf(inputChars[i]), 
						AtomType.OPERATOR, 1));
			} else {
				int lastIndex = inputExpressionTokens.size() - 1;
				if(lastIndex >= 0 && inputExpressionTokens.get(lastIndex).getAtomType() 
						== AtomType.OPERAND) {
					ExpressionAtom lastElement = inputExpressionTokens.remove(lastIndex);
					if(Character.isDigit(inputChars[i])) {
						lastElement.setCoefficient(lastElement.getCoefficient() * 10 + 
								Character.getNumericValue(inputChars[i]));
					} else {
						lastElement.setVariablesOrOperator(lastElement.getVariablesOrOperator() + 
								String.valueOf(inputChars[i]));
					}
					inputExpressionTokens.add(lastElement);
				} else if(Character.isDigit(inputChars[i])) {
					inputExpressionTokens.add(new ExpressionAtom("", AtomType.OPERAND, 
							Character.getNumericValue(inputChars[i])));
				} else {
					inputExpressionTokens.add(new ExpressionAtom(String.valueOf(
							inputChars[i]), AtomType.OPERAND, 1));
				}
			}
		}
		
		return inputExpressionTokens;
	}
	
	private ListRepresentation convertToListRepresentation() {
		//create new list of ListRepresentations
		List<ListRepresentation> list = new ArrayList<ListRepresentation>();
		
		//add expressions wrapped in a new ListRepresentaion
		for(ExpressionAtom ea : this.infixExpression){
			ListRepresentation lr = new ListRepresentation();
			lr.setNodeVal(ea);
			list.add(lr);
		}
		
		//return simplified list
		return simplify(list);
	}
	
	private ListRepresentation simplify(List<ListRepresentation> list){
		//check for negative sign
		simplifyNegativeSign(list);
		
		//simplify parenthesis first using recursion
		//loop through all expressions
		for(int i = 0; i < list.size(); i++){
			ExpressionAtom ea = list.get(i).getNodeVal();

			//check for opening parenthesis
			if(isOperator(ea, "(")){
				//track parenthesis encountered
				int count = 0;
				
				//look for closing parenthesis throughout the rest of the list
				for(int j = i + 1; j < list.size(); j++){
					ea = list.get(j).getNodeVal();
					
					//increment count if another opening parenthesis is encountered
					if(isOperator(ea, "(")){
						count++;
					}else if(isOperator(ea, ")")){
						//check if this is the matching parenthesis
						if(count == 0){
							//remove parenthesis
							list.remove(j);
							list.remove(i);
							
							//simplify what is inside the parenthesis
							List<ListRepresentation> subList = list.subList(i, j - 1);
							simplify(subList);
							
							break;
						}
						//decrement count
						else{
							count--;
						}
					}
				}	
			}
		}
		
		//apply operators in order of operations
		simplifyOperator(list, "^");
		simplifyOperator(list, "*");
		simplifyOperator(list, "+");
		simplifyOperator(list, "-");
		
		//return simplified ListRepresentation containing all expressions
		return list.get(0);
	}
	
	private void simplifyNegativeSign(List<ListRepresentation> list){
		//get first expression
		ListRepresentation lr = list.get(0);
		ExpressionAtom ea = lr.getNodeVal();
		
		//check for negative sign
		if(ea.getVariablesOrOperator().equals("%")){
			//get operand
			ListRepresentation operand = list.get(1);
			
			//negate operand
			operand.getNodeVal().setCoefficient(operand.getNodeVal().getCoefficient() * -1);
			
			//remove operator from list
			list.remove(0);
		}
	}
	
	private void simplifyOperator(List<ListRepresentation> list, String operator){
		//search for operator in list
		for(int i = 0; i < list.size(); i++){
			ListRepresentation lr = list.get(i);
			ExpressionAtom ea = lr.getNodeVal();
			
			if(isOperator(ea, operator)){
				//get operands
				ListRepresentation operand1 = list.get(i - 1);
				ListRepresentation operand2 = list.get(i + 1);
				
				//check for subtraction
				if(operator.equals("-")){
					//change to plus
					ea.setVariablesOrOperator("+");
					
					//check atomType of operand2
					if(operand2.getNodeVal().getAtomType() == AtomType.OPERAND){
						//negate second operand's nodeVal
						operand2.getNodeVal().setCoefficient(operand2.getNodeVal().getCoefficient() * -1);
					}else{
						//negate second operand
						operand2.setNegative(true);
					}
				}
				
				//add operands to the operator's operands list
				lr.operands.add(operand1);
				lr.operands.add(operand2);
				
				//remove operands from list
				list.remove(operand1);
				list.remove(operand2);
				
				//adjust i for deletion
				i--;
			}
		}
	}
			
	private List<ExpressionAtom> evaluateExpression() {
		//create new list
		List<ExpressionAtom> returnList = new ArrayList<ExpressionAtom>();
		
		//expand exponents to a product of expressions
		expandExponents(listRepresentation);
		//unify additions within additions
		unifyAdditions(listRepresentation);
		
		//factor multiplications to a sum of expressions
		factorMultiplication(listRepresentation);
		//unify additions within additions
		unifyAdditions(listRepresentation);
		
		//listRepresentation is now a single sum of expressions
		//add expressions to returnList
		for(int i = 0; i < listRepresentation.operands.size(); i++){
			ListRepresentation listRep = listRepresentation.operands.get(i);
			
			//transfer isNegative to coefficient of nodeVal
			if(listRep.isNegative())
				listRep.getNodeVal().setCoefficient(listRep.getNodeVal().getCoefficient() * -1);
			
			returnList.add(listRep.getNodeVal());
		}
		
		return returnList;
	}
	
	private void expandExponents(ListRepresentation listRep){
		ExpressionAtom expresAtom = listRep.getNodeVal();
		
		//check for exponent
		if(isOperator(expresAtom, "^")){
			//get exponent value
			int exponent = listRep.operands.get(1).getNodeVal().getCoefficient();
			
			//check for 0 exponent
			if(exponent == 0){
				//change listRep to 1
				listRep.operands.clear();
				expresAtom.setVariablesOrOperator("");
				expresAtom.setCoefficient(1);
				expresAtom.setAtomType(AtomType.OPERAND);
			}else{
				//change operator to "*"
				expresAtom.setVariablesOrOperator("*");
				
				//remove exponent
				listRep.operands.remove(1);
				
				//get operand
				ListRepresentation operand = listRep.operands.get(0);
				
				//copy and add operand exponent number of times
				for(int i = 1; i < exponent; i++){
					listRep.operands.add(operand.copy());
				}
			}
		}
		
		//continue search recursively
		for(int i = 0; i < listRep.operands.size(); i++)
			expandExponents(listRep.operands.get(i));
	}
	
	private void unifyAdditions(ListRepresentation listRep){
		ExpressionAtom expresAtom = listRep.getNodeVal();
		
		//check for addition
		if(isOperator(expresAtom, "+")){
			
			//loop through all operands
			for(int i = 0; i < listRep.operands.size(); i++){
				ListRepresentation operandListRep = listRep.operands.get(i);
				ExpressionAtom operandExpresAtom = operandListRep.getNodeVal();
				
				//check for addition
				if(isOperator(operandExpresAtom, "+")){
					for(int j = 0; j < operandListRep.operands.size(); j++){
						//transfer isNegative to nodeVal's coefficient
						if(operandListRep.isNegative()){
							int coefficient = operandListRep.operands.get(j).getNodeVal().getCoefficient();
							operandListRep.operands.get(j).getNodeVal().setCoefficient(coefficient * -1);
						}
						
						//add to above addition's operands list
						listRep.operands.add(operandListRep.operands.get(j));
					}
					
					//remove sub addition
					listRep.operands.remove(i);
					
					//adjust i for deletion
					i--;
				}
				//continue search recursively
				else{
					unifyAdditions(listRep.operands.get(i));
				}
			}
		}
		//continue search recursively
		else{
			for(int i = 0; i < listRep.operands.size(); i++)
				unifyAdditions(listRep.operands.get(i));
		}
	}
	
	private void factorMultiplication(ListRepresentation listRep){
		//check for multiplication
		if(isOperator(listRep.getNodeVal(), "*")){
			factor(listRep);
		}
		//continue search recursively
		else{
			for(int i = 0; i < listRep.operands.size(); i++)
				factorMultiplication(listRep.operands.get(i));
		}
	}
	
	private void factor(ListRepresentation listRep){
		int numOperands = listRep.operands.size();
		
		//loop through all operands starting at index 1
		for(int i = 1; i < numOperands; i ++){
			//get operands
			ListRepresentation operand1ListRep = listRep.operands.get(i - 1);
			ListRepresentation operand2ListRep = listRep.operands.get(i);
			ExpressionAtom operand1ExpresAtom = operand1ListRep.getNodeVal();
			ExpressionAtom operand2ExpresAtom = operand2ListRep.getNodeVal();
			
			//make recursive call if its another multiplication
			if(isOperator(operand1ExpresAtom, "*"))
				factor(operand1ListRep);
			if(isOperator(operand2ExpresAtom, "*"))
				factor(operand2ListRep);
			
			//check if both are operands
			if(operand1ExpresAtom.getAtomType() == AtomType.OPERAND && operand2ExpresAtom.getAtomType() == AtomType.OPERAND){
				//calculate product for simple terms
				int productCoefficient = operand1ExpresAtom.getCoefficient() * operand2ExpresAtom.getCoefficient();
				String productVariablesOrOperator = operand1ExpresAtom.getVariablesOrOperator() + operand2ExpresAtom.getVariablesOrOperator();
				
				//make operand2 the product
				operand2ExpresAtom.setCoefficient(productCoefficient);
				operand2ExpresAtom.setVariablesOrOperator(productVariablesOrOperator);
			}
			//check if operand1 is operand and operand2 is "+" operator
			else if(operand1ExpresAtom.getAtomType() == AtomType.OPERAND && isOperator(operand2ExpresAtom, "+")){
				//create new list to store generated factored terms
				List<ListRepresentation> factoredTerms = new ArrayList<ListRepresentation>();
				
				//loop through all operators in operator2
				for(int j = 0; j < operand2ListRep.operands.size(); j++){
					ListRepresentation factorTermListRep = operand2ListRep.operands.get(j);
					ExpressionAtom factorTermExpresAtom = factorTermListRep.getNodeVal();
					
					//make recursive call if its another multiplication
					if(isOperator(factorTermExpresAtom, "*"))
						factor(factorTermListRep);
					
					//calculate product for simple terms
					int productCoefficient = operand1ExpresAtom.getCoefficient() * factorTermExpresAtom.getCoefficient();
					String productVariablesOrOperator = operand1ExpresAtom.getVariablesOrOperator() + factorTermExpresAtom.getVariablesOrOperator();
					
					//add product to factoredTerms
					ListRepresentation factoredTerm = new ListRepresentation();
					factoredTerm.setNodeVal(new ExpressionAtom(productVariablesOrOperator, AtomType.OPERAND, productCoefficient));
					factoredTerms.add(factoredTerm);
				}
				
				//make operand2 the sum of factored terms
				operand2ListRep.operands.clear();
				operand2ListRep.operands.addAll(factoredTerms);
			}
			//check if operand2 is operand and operand1 is "+" operator
			else if(operand2ExpresAtom.getAtomType() == AtomType.OPERAND && isOperator(operand1ExpresAtom, "+")){
				//create new list to store generated factored terms
				List<ListRepresentation> factoredTerms = new ArrayList<ListRepresentation>();
				
				//loop through all operators in operator1
				for(int j = 0; j < operand1ListRep.operands.size(); j++){
					ListRepresentation factorTermListRep = operand1ListRep.operands.get(j);
					ExpressionAtom factorTermExpresAtom = factorTermListRep.getNodeVal();
					
					//make recursive call if its another multiplication
					if(isOperator(factorTermExpresAtom, "*"))
						factor(factorTermListRep);
					
					//calculate product for simple terms
					int productCoefficient = operand2ExpresAtom.getCoefficient() * factorTermExpresAtom.getCoefficient();
					String productVariablesOrOperator = operand2ExpresAtom.getVariablesOrOperator() + factorTermExpresAtom.getVariablesOrOperator();
					
					//add product to factoredTerms
					ListRepresentation factoredTerm = new ListRepresentation();
					factoredTerm.setNodeVal(new ExpressionAtom(productVariablesOrOperator, AtomType.OPERAND, productCoefficient));
					factoredTerms.add(factoredTerm);
				}
				
				//make operand2 the sum of factored terms
				operand2ExpresAtom.setCoefficient(1);
				operand2ExpresAtom.setAtomType(AtomType.OPERATOR);
				operand2ExpresAtom.setVariablesOrOperator("+");
				operand2ListRep.operands.addAll(factoredTerms);
			}
			//operand2 is operand and operand1 is "+" operator
			else if(isOperator(operand1ExpresAtom, "+") && isOperator(operand1ExpresAtom, "+")){
				//create new list to store generated factored terms
				List<ListRepresentation> factoredTerms = new ArrayList<ListRepresentation>();
				
				//loop through all operators in operator1
				for(int j = 0; j < operand1ListRep.operands.size(); j++){
					for(int k = 0; k < operand2ListRep.operands.size(); k++){
						ListRepresentation factor1TermListRep = operand1ListRep.operands.get(j);
						ListRepresentation factor2TermListRep = operand2ListRep.operands.get(k);
						ExpressionAtom factor1TermExpresAtom = factor1TermListRep.getNodeVal();
						ExpressionAtom factor2TermExpresAtom = factor2TermListRep.getNodeVal();
						
						//make recursive call if its another multiplication
						if(isOperator(factor1TermExpresAtom, "*"))
							factor(factor1TermListRep);
						if(isOperator(factor2TermExpresAtom, "*"))
							factor(factor2TermListRep);
						
						//calculate product for simple terms
						int productCoefficient = factor1TermExpresAtom.getCoefficient() * factor2TermExpresAtom.getCoefficient();
						String productVariablesOrOperator = factor1TermExpresAtom.getVariablesOrOperator() + factor2TermExpresAtom.getVariablesOrOperator();
						
						//add product to factoredTerms
						ListRepresentation factoredTerm = new ListRepresentation();
						factoredTerm.setNodeVal(new ExpressionAtom(productVariablesOrOperator, AtomType.OPERAND, productCoefficient));
						factoredTerms.add(factoredTerm);
					}
				}
				
				//make operand2 the sum of factored terms
				operand2ListRep.operands.clear();
				operand2ListRep.operands.addAll(factoredTerms);
			}
		}
		
		//get the last operand which now holds all factored terms
		ListRepresentation factoredTerms = listRep.operands.get(numOperands - 1);
		
		//check for single expression
		if(factoredTerms.getNodeVal().getAtomType() == AtomType.OPERAND){
			//make listRep the expression
			listRep.operands.clear();
			listRep.getNodeVal().setVariablesOrOperator(factoredTerms.getNodeVal().getVariablesOrOperator());
			listRep.getNodeVal().setCoefficient(factoredTerms.getNodeVal().getCoefficient());
			listRep.getNodeVal().setAtomType(AtomType.OPERAND);
		}else{
			//make listRep the sum of factored terms
			listRep.operands.clear();
			listRep.getNodeVal().setVariablesOrOperator("+");
			listRep.operands.addAll(factoredTerms.operands);
		}
	}
	
	private boolean isOperator(ExpressionAtom expresAtom, String operator){
		//return true if expresAtom is the operator represented by the String
		return (expresAtom.getAtomType() == AtomType.OPERATOR && expresAtom.getVariablesOrOperator().equals(operator));
	}

	private List<ExpressionAtom> simplifyAndNormalize(List<ExpressionAtom> evaluatedExpression) {
		//loop through all expressions
		for(int i = 0; i < evaluatedExpression.size(); i++){
			ExpressionAtom expresAtom1 = evaluatedExpression.get(i);
			
			//loop through all remaining expressions
			for(int j = i + 1; j < evaluatedExpression.size(); j++){
				ExpressionAtom expresAtom2 = evaluatedExpression.get(j);
				
				//check for same variables
				if(expresAtom1.getVariablesOrOperator().equals(expresAtom2.getVariablesOrOperator())){
					//add the two coefficients and set expresAtom1's coefficient to the sum
					int sumCoefficient = expresAtom1.getCoefficient() + expresAtom2.getCoefficient();
					expresAtom1.setCoefficient(sumCoefficient);
					
					//remove second expression
					evaluatedExpression.remove(j);
					
					//adjust j for deletion
					j--;
				}
			}
		}
		
		return evaluatedExpression;
	}
	
	public Polynomial(String inputPolynomial) {
		this.infixExpression = parseInputPolynomial(inputPolynomial);
		
		this.listRepresentation = convertToListRepresentation();
	}
	
	private String sortString(String termVars) {
		char[] ar = termVars.toCharArray();
		Arrays.sort(ar);
		return String.valueOf(ar);
	}

	public void evaluate() {
		List<ExpressionAtom> evaluatedExpression = evaluateExpression();
		
		for(int i = 0; i < evaluatedExpression.size(); ++i) {
			evaluatedExpression.get(i).setVariablesOrOperator((sortString(
					evaluatedExpression.get(i).getVariablesOrOperator())));
		}
		
		this.finalExpression = simplifyAndNormalize(evaluatedExpression);
	}
}