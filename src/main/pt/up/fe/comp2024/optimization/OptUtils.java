package pt.up.fe.comp2024.optimization;

import org.specs.comp.ollir.Instruction;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2024.ast.NodeUtils;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.List;
import java.util.Optional;

import static pt.up.fe.comp2024.ast.Kind.TYPE;

public class OptUtils {
    private static int tempNumber = -1;

    public static String getTemp() {

        return getTemp("tmp");
    }

    public static String getTemp(String prefix) {

        return prefix + getNextTempNum();
    }

    public static int getNextTempNum() {

        tempNumber += 1;
        return tempNumber;
    }

    public static String toOllirType(JmmNode typeNode) {

        TYPE.checkOrThrow(typeNode);

        String typeName = typeNode.getKind();

        return toOllirType(typeName);
    }

    public static String toOllirType(Type type) {
        return toOllirType(type.getName());
    }

    private static String toOllirType(String typeName) {

        String type = "." + switch (typeName) {
            case "int", "Int" -> "i32";
            case "boolean", "Boolean" -> "bool";
            case "void", "Void" -> "V";
            default -> throw new NotImplementedException(typeName);
        };

        return type;
    }


}
