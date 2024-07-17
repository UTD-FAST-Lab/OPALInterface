package org.example;

import org.opalj.br.analyses.Project;
import org.opalj.br.analyses.DeclaredMethodsKey$;
import org.opalj.tac.cg.CallGraph;
import org.opalj.tac.cg.CallGraphSerializer;
import org.opalj.tac.cg.RTACallGraphKey$;
import org.opalj.tac.cg.CHACallGraphKey$;
import org.opalj.tac.cg.AllocationSiteBasedPointsToCallGraphKey$;
import org.opalj.tac.cg.XTACallGraphKey$;


import java.io.File;


public class CallGraphAnalysisExample {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: CallGraphAnalysisExample <path-to-jar> <output-file> <algorithm>");
            System.err.println("Supported algorithms: CHA, RTA, XTA, PointsTo");
            System.exit(1);
        }

        String pathToJar = args[0];
        String output = args[1];
        String algorithm = args[2];

        File projectJar = new File(pathToJar);

        System.out.println(args[0]);
        System.out.println(args[1]);
        System.out.println(args[2]);

        Project<?> project = Project.apply(projectJar);

        try {
            writeCallGraph(project, algorithm, new File(output));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeCallGraph(Project<?> project, String algorithm, File outputFile) {
        try {
            long startTime = System.currentTimeMillis();
            CallGraph callGraph;
            switch (algorithm) {
                case "CHA":
                    // Perform and write CHA call graph
                    System.out.println("Generating CHA Call Graph...");
                    callGraph = project.get(CHACallGraphKey$.MODULE$);
                    System.out.print(callGraph.toString());
                    CallGraphSerializer.writeCG(callGraph, outputFile, project.get(DeclaredMethodsKey$.MODULE$));
                    break;
                case "RTA":
                    // Perform and write RTA call graph
                    System.out.println("Generating RTA Call Graph...");
                    callGraph = project.get(RTACallGraphKey$.MODULE$);
                    CallGraphSerializer.writeCG(callGraph, outputFile, project.get(DeclaredMethodsKey$.MODULE$));
                    break;
                case "XTA":
                    // Perform and write XTA call graph
                    System.out.println("Generating XTA Call Graph...");
                    callGraph = project.get(XTACallGraphKey$.MODULE$);
                    CallGraphSerializer.writeCG(callGraph, outputFile, project.get(DeclaredMethodsKey$.MODULE$));
                    break;
                case "PointsTo":
                    // Perform and write PointsTo call graph
                    System.out.println("Generating PointsTo Call Graph...");
                    callGraph = project.get(AllocationSiteBasedPointsToCallGraphKey$.MODULE$);
                    CallGraphSerializer.writeCG(callGraph, outputFile, project.get(DeclaredMethodsKey$.MODULE$));
                    break;
                default:
                    System.out.println("Algorithm not supported");
                    break;
            }
            System.out.println("Call graph written to: " + outputFile.getAbsolutePath());
            long endTime = System.currentTimeMillis();
            System.out.println("Call graph generation and writing took " + (endTime - startTime) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
