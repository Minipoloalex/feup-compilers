package pt.up.fe.comp2024.optimization.ollir;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.tree.TreeNode;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.*;

public class LivenessAnalysis {
    List<Report> reports;
    private final FunctionClassMap<TreeNode, String> handlers;
    OllirResult ollirResult;

    // using sets to prevent duplicates (e.g.: b = a + a)
    private final List<Set<String>> usedVariables = new ArrayList<>();
    private final List<Set<String>> definedVariables = new ArrayList<>();

    Method currentMethod;

    int instr_nr;

    public LivenessAnalysis(OllirResult ollirResult) {
        this.ollirResult = ollirResult;
        handlers = new FunctionClassMap<>();
        reports = new ArrayList<>();

        handlers.put(Method.class, this::handleMethod);
        handlers.put(AssignInstruction.class, this::handleAssign);
        handlers.put(SingleOpInstruction.class, this::handleSingleOp);
        handlers.put(LiteralElement.class, this::handleLiteral);
        handlers.put(ArrayOperand.class, this::handleArrayOperand);
        handlers.put(Operand.class, this::handleOperand);
        handlers.put(BinaryOpInstruction.class, this::handleBinaryOp);
        handlers.put(UnaryOpInstruction.class, this::handleUnaryOp);
        handlers.put(ReturnInstruction.class, this::handleReturn);
        handlers.put(CallInstruction.class, this::handleCall);
        handlers.put(PutFieldInstruction.class, this::handlePutField);
        handlers.put(GetFieldInstruction.class, this::handleGetField);

        // imports, fields, ... are not necessary here
    }
    private void addToUseSet(Element element) {
        if (element instanceof Operand operand) {
            usedVariables.get(instr_nr).add(operand.getName());
        }
    }
    private String handleArrayOperand(ArrayOperand arrayOperand) {
        String arrayOperandName = arrayOperand.getName();
        List<Element> indexOperands = arrayOperand.getIndexOperands();
        indexOperands.forEach(handlers::apply);
        return null;
    }
    private String handleGetField(GetFieldInstruction getFieldInstruction) {
        Element object = getFieldInstruction.getObject();
        handlers.apply(object);
        return null;
    }
    private String handleCall(CallInstruction callInstruction) {
        for (var arg : callInstruction.getArguments()) {
            handlers.apply(arg);
        }
        return null;
    }
    private String handleUnaryOp(UnaryOpInstruction unaryOpInstruction) {
        Element operand = unaryOpInstruction.getOperand();
        handlers.apply(operand);
        return null;
    }
    private String handleOperand(Operand operand) {
        return null;
    }
    private String handleSingleOp(SingleOpInstruction singleOpInstruction) {
        Element operand = singleOpInstruction.getSingleOperand();
        handlers.apply(operand);
        return null;
    }
    private String handleLiteral(LiteralElement literalElement) {
        return null;
    }
    private String handleMethod(Method method) {
        currentMethod = method;
        String methodName = method.getMethodName();
        List<Instruction> instructions = method.getInstructions();

        usedVariables.clear();
        definedVariables.clear();

        for (int i = 0; i < instructions.size(); i++) {
            usedVariables.add(new HashSet<>());
            definedVariables.add(new HashSet<>());
        }

        for (instr_nr = 0; instr_nr < instructions.size(); instr_nr++) {
            Instruction instruction = instructions.get(instr_nr);
            Set<String> used = usedVariables.get(instr_nr);
            Set<String> defined = definedVariables.get(instr_nr);
            handlers.apply(instruction);
        }

        return null;
    }
    private String handleAssign(AssignInstruction assignInstruction) {
        var currentMethodName = currentMethod.getMethodName();

        var lhs = assignInstruction.getDest();
        if (!(lhs instanceof Operand lhsOperand)) {
            throw new NotImplementedException(lhs.getClass());
        }
        // Set<String> used = usedVariables.get(instr_nr);
        Set<String> defined = definedVariables.get(instr_nr);

        defined.add(lhsOperand.getName());
        handlers.apply(assignInstruction.getRhs());

        return null;
    }
    private String handleBinaryOp(BinaryOpInstruction binaryOpInstruction) {
        Element left = binaryOpInstruction.getLeftOperand();
        Element right = binaryOpInstruction.getRightOperand();

        handlers.apply(left);   // TODO: change this?
        handlers.apply(right);
        return null;
    }
    private String handleReturn(ReturnInstruction returnInstruction) {
        Element returnOperand = returnInstruction.getOperand();
        System.out.println("Return: " + returnOperand);
        if (returnOperand != null) {
            handlers.apply(returnOperand);
        }
        return null;
    }
    private String handlePutField(PutFieldInstruction putFieldInstruction) {
        Element object = putFieldInstruction.getObject();
        Element value = putFieldInstruction.getValue();
        handlers.apply(object);
        handlers.apply(value);
        return null;
    }

    public LivenessAnalysisResult obtainResult(Method method) {
        handlers.apply(method);
        System.out.println("Method: " + method.getMethodName());
        System.out.println("Used: " + usedVariables);
        System.out.println("Defined: " + definedVariables);

        List<Set<String>> liveIn = new ArrayList<>(), liveOut = new ArrayList<>();
        int nrInstructions = currentMethod.getInstructions().size();
        for (int i = 0; i < nrInstructions; i++) {
            liveIn.add(new HashSet<>());
            liveOut.add(new HashSet<>());
        }
        List<Set<String>> prevLiveIn, prevLiveOut;
        do {
            prevLiveIn = new ArrayList<>(liveIn);
            prevLiveOut = new ArrayList<>(liveOut);
            for (int i = 0; i < nrInstructions; i++) {
                Set<String> instrLiveIn = liveIn.get(i);
                Set<String> instrLiveOut = liveOut.get(i);
                Set<String> instrUsed = usedVariables.get(i);
                Set<String> instrDefined = definedVariables.get(i);
                Set<Integer> instrSuccessors = new HashSet<>(); // TODO: actually get the successors

                // instrLiveIn = instrUsed U (instrLiveOut - instrDefined)
                instrLiveIn.clear();
                instrLiveIn.addAll(Set.copyOf(instrLiveOut));
                instrLiveIn.removeAll(instrDefined);
                instrLiveIn.addAll(instrUsed);

                // instrLiveOut = U { s in instrSuccessors } liveIn[s]
                for (Integer successor : instrSuccessors) {
                    instrLiveOut.addAll(liveIn.get(successor));
                }
            }
        } while(!liveIn.equals(prevLiveIn) || !liveOut.equals(prevLiveOut));    // repeat until liveIn and liveOut don't change

        return new LivenessAnalysisResult(liveIn, liveOut);
    }
}
