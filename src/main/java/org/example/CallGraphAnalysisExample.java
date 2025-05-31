package org.example;

import org.opalj.br.analyses.Project;
import org.opalj.br.analyses.DeclaredMethodsKey$;
import org.opalj.tac.cg.CallGraph;
import org.opalj.tac.cg.CallGraphSerializer;
import org.opalj.tac.cg.RTACallGraphKey$;
import org.opalj.tac.cg.CHACallGraphKey$;
import org.opalj.tac.cg.AllocationSiteBasedPointsToCallGraphKey$;
import org.opalj.tac.cg.XTACallGraphKey$;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.opalj.tac.cg.CFA_1_0_CallGraphKey$;
import org.opalj.tac.cg.CFA_1_1_CallGraphKey$;
import org.opalj.tac.cg.CTACallGraphKey$;
import org.opalj.tac.cg.FTACallGraphKey$;
import org.opalj.tac.cg.MTACallGraphKey$;
import org.opalj.tac.cg.TypeBasedPointsToCallGraphKey$;

import org.opalj.log.LogContext;
import org.opalj.log.GlobalLogContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginContext;



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
        String JDK_path = "/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar";

        ArrayList<File> applicationJars = new ArrayList<>();
        File projectJar = new File(pathToJar);
        applicationJars.add(projectJar);

        ArrayList<File> libraryJars = new ArrayList<>();
        File jdkJar = new File(JDK_path);
        libraryJars.add(jdkJar);

        File[] appFiles = applicationJars.toArray(new File[0]);
        File[] libFiles = libraryJars.toArray(new File[0]);

        System.out.println(args[0]);
        System.out.println(args[1]);
        System.out.println(args[2]);
        System.out.println("Using JDK path: " + JDK_path);

        // --- Configuration Setup ---
        // Base configuration
        Config baseConfig = ConfigFactory.load().withValue(
            "org.opalj.br.reader.ClassFileReader.Invokedynamic.rewrite",
            ConfigValueFactory.fromAnyRef(true)
        );


        List<? extends ConfigObject> existingEntryPoints =
            baseConfig.getObjectList("org.opalj.br.analyses.cg.InitialEntryPointsKey.entryPoints");


        // Define the new entry point
        Map<String, String> mainEntryPoint = new HashMap<>();
        mainEntryPoint.put("declaringClass", "Entrypoint");
        mainEntryPoint.put("name", "main");

        List<ConfigValue> updatedEntryPoints = new ArrayList<>(existingEntryPoints);
        updatedEntryPoints.add(ConfigValueFactory.fromMap(mainEntryPoint));


        // Configure the initial entry points
        Config config;
        config = baseConfig
            .withValue(
                    "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
                    ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.ConfigurationEntryPointsFinder")
            )
            .withValue(
                    "org.opalj.br.analyses.cg.InitialEntryPointsKey.entryPoints",
                    ConfigValueFactory.fromIterable(updatedEntryPoints)
            )
            .withValue(
                    "org.opalj.br.analyses.cg.InitialInstantiatedTypesKey.analysis",
                    ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.ApplicationInstantiatedTypesFinder")
            );
        

        // Apply additional configurations from the Scala script
        config = config
                .withValue("org.opalj.fpcf.analyses.AllocationSiteBasedPointsToAnalysis.mergeStringConstants", ConfigValueFactory.fromAnyRef(false))
                .withValue("org.opalj.fpcf.analyses.AllocationSiteBasedPointsToAnalysis.mergeClassConstants", ConfigValueFactory.fromAnyRef(false));

        config = config
                .withValue("org.opalj.fpcf.analyses.cg.reflection.ReflectionRelatedCallsAnalysis.highSoundness", ConfigValueFactory.fromAnyRef(false));
        
        LogContext projectLogContext = GlobalLogContext.successor();

        // Project<?> project = Project.apply(projectJar, projectLogContext, config);
        Project<?> project = Project.apply(appFiles, libFiles, projectLogContext, config);

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
                case "MTA":
                    // Perform and write XTA call graph
                    System.out.println("Generating MTA Call Graph...");
                    callGraph = project.get(MTACallGraphKey$.MODULE$);
                    CallGraphSerializer.writeCG(callGraph, outputFile, project.get(DeclaredMethodsKey$.MODULE$));
                    break;
                case "CTA":
                    // Perform and write XTA call graph
                    System.out.println("Generating CTA Call Graph...");
                    callGraph = project.get(CTACallGraphKey$.MODULE$);
                    CallGraphSerializer.writeCG(callGraph, outputFile, project.get(DeclaredMethodsKey$.MODULE$));
                    break;
                case "FTA":
                    // Perform and write XTA call graph
                    System.out.println("Generating FTA Call Graph...");
                    callGraph = project.get(FTACallGraphKey$.MODULE$);
                    CallGraphSerializer.writeCG(callGraph, outputFile, project.get(DeclaredMethodsKey$.MODULE$));
                    break;
                case "01cfa":
                    // Perform and write PointsTo call graph
                    System.out.println("Generating 0-1cfa Call Graph...");
                    callGraph = project.get(AllocationSiteBasedPointsToCallGraphKey$.MODULE$);
                    CallGraphSerializer.writeCG(callGraph, outputFile, project.get(DeclaredMethodsKey$.MODULE$));
                    break;
                case "0cfa":
                    // Perform and write PointsTo call graph
                    System.out.println("Generating 0cfa Call Graph...");
                    callGraph = project.get(TypeBasedPointsToCallGraphKey$.MODULE$);
                    CallGraphSerializer.writeCG(callGraph, outputFile, project.get(DeclaredMethodsKey$.MODULE$));
                    break;
                case "10cfa":
                    // Perform and write PointsTo call graph
                    System.out.println("Generating 1-0cfa Call Graph...");
                    callGraph = project.get(CFA_1_0_CallGraphKey$.MODULE$);
                    CallGraphSerializer.writeCG(callGraph, outputFile, project.get(DeclaredMethodsKey$.MODULE$));
                    break;
                case "11cfa":
                    // Perform and write PointsTo call graph
                    System.out.println("Generating 1-1cfa Call Graph...");
                    callGraph = project.get(CFA_1_1_CallGraphKey$.MODULE$);
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
