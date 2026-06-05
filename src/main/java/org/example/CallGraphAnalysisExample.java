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
import org.opalj.log.StandardLogContext;
import org.opalj.log.OPALLogger$;
import org.opalj.log.Info$;
import org.opalj.log.ConsoleOPALLogger;
import org.opalj.log.GlobalLogContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


public class CallGraphAnalysisExample {

    public static void main(String[] args) {

                        // Forces OPAL to run synchronously, eliminating all thread race crashes
        // System.setProperty("org.opalj.threads.CPUBoundTasks", "1");
        // System.setProperty("org.opalj.threads.IOBoundTasks", "1");
        if (args.length != 4) {
            System.err.println("Usage: CallGraphAnalysisExample <app-dir> <output-file> <algorithm> <tamiflex-log>");
            System.exit(1);
        }

        boolean use_tamiflex = true;
        String tamiflexLogPath = args[3];
        if (tamiflexLogPath.equals("None")){
            use_tamiflex = false;
            System.out.println("not using tamiflex");
        }

        // String pathToJar = args[0];
        // String output = args[1];
        // String algorithm = args[2];
        // String JDK_path = "/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar";
        // // String JDK_path = "/usr/local/openjdk-8/jre/lib/rt.jar";

        // ArrayList<File> applicationJars = new ArrayList<>();
        // File projectJar = new File(pathToJar);
        // if (!projectJar.exists() || !projectJar.isFile()) {
        //     System.err.println("Error: The specified JAR file does not exist or is not a valid file.");
        //     System.exit(1);
        // }
        // applicationJars.add(projectJar);

        String pathToAppDirectory = args[0]; // Renamed for clarity
        String output = args[1];
        String algorithm = args[2];
        String JDK_path = "/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar";
        // String JDK_path = "/usr/local/openjdk-8/jre/lib/rt.jar";

        ArrayList<File> applicationJars = new ArrayList<>();
        File appDirectory = new File(pathToAppDirectory);

        // --- START: MODIFIED SECTION ---

        // 1. Validate that the path is an existing directory
        if (!appDirectory.exists() || !appDirectory.isDirectory()) {
            System.err.println("Error: The specified path does not exist or is not a valid directory.");
            System.exit(1);
        }

        // 2. List all files in the directory and filter for .jar files
        File[] filesInDir = appDirectory.listFiles();
        if (filesInDir != null) {
            for (File file : filesInDir) {
                // Add file to the list if it's a file and its name ends with .jar
                if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                    applicationJars.add(file);
                    System.out.println("Adding application JAR: " + file.getAbsolutePath());
                }
            }
        }
        
        // 3. (Optional but recommended) Check if any JARs were found
        if (applicationJars.isEmpty()) {
            System.err.println("Error: No .jar files found in the directory: " + pathToAppDirectory);
            System.exit(1);
        }

        // --- END: MODIFIED SECTION ---


        ArrayList<File> libraryJars = new ArrayList<>();
        File jdkJar = new File(JDK_path);
        if (!jdkJar.exists()) {
            System.err.println("FATAL: Cannot find Java 8 rt.jar at " + JDK_path);
            System.err.println("OPAL will crash with a NullPointerException without this file.");
            System.exit(1);
        }
        libraryJars.add(jdkJar);
        libraryJars.add(new File("/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jce.jar"));
        libraryJars.add(new File("/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jsse.jar"));
        libraryJars.add(new File("/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jfr.jar"));
        libraryJars.add(new File("/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/resources.jar"));
        libraryJars.add(new File("/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/charsets.jar"));

        File[] appFiles = applicationJars.toArray(new File[0]);
        File[] libFiles = libraryJars.toArray(new File[0]);

        System.out.println(args[0]);
        System.out.println(args[1]);
        System.out.println(args[2]);
        System.out.println("Using JDK path: " + JDK_path);



        // --- Configuration Setup ---
        // Base configuration
        Config baseConfig = ConfigFactory.load();
        // Config baseConfig = ConfigFactory.load().withValue(
        //     "org.opalj.br.reader.ClassFileReader.Invokedynamic.rewrite",
        //     ConfigValueFactory.fromAnyRef(true)
        // );

        // List<? extends ConfigObject> existingEntryPoints =
        //     baseConfig.getObjectList("org.opalj.br.analyses.cg.InitialEntryPointsKey.entryPoints");


        // Define the new entry point
        // Map<String, String> mainEntryPoint = new HashMap<>();
        // mainEntryPoint.put("declaringClass", "Harness");
        // mainEntryPoint.put("name", "main");

        // // List<ConfigValue> updatedEntryPoints = new ArrayList<>(existingEntryPoints);
        // List<ConfigValue> updatedEntryPoints = new ArrayList<>();
        // updatedEntryPoints.add(ConfigValueFactory.fromMap(mainEntryPoint));
        



        // Configure the initial entry points
        // Config config;
        // config = baseConfig
        //     .withValue(
        //             "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
        //             ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.ConfigurationEntryPointsFinder")
        //             // ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.ApplicationWithoutJREEntryPointsFinder")
        //     )
        //     .withValue(
        //             "org.opalj.br.analyses.cg.InitialEntryPointsKey.entryPoints",
        //             ConfigValueFactory.fromIterable(updatedEntryPoints)
        //     )
        //     .withValue(
        //             "org.opalj.br.analyses.cg.InitialInstantiatedTypesKey.analysis",
        //             ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.ApplicationInstantiatedTypesFinder")
        //     );
        

        // Apply additional configurations from the Scala script
        // config = config
        //         .withValue("org.opalj.fpcf.analyses.AllocationSiteBasedPointsToAnalysis.mergeStringConstants", ConfigValueFactory.fromAnyRef(false))
        //         .withValue("org.opalj.fpcf.analyses.AllocationSiteBasedPointsToAnalysis.mergeClassConstants", ConfigValueFactory.fromAnyRef(false));

        // config = config
        //         .withValue("org.opalj.fpcf.analyses.cg.reflection.ReflectionRelatedCallsAnalysis.highSoundness", ConfigValueFactory.fromAnyRef(false));
        
                
                
                
                // tamiflex configs
        if (use_tamiflex) {

            baseConfig = baseConfig.withValue(
                "org.opalj.tac.fpcf.analyses.pointsto.TamiFlex.logFile",
                ConfigValueFactory.fromAnyRef(tamiflexLogPath)
            );
                
            // config = config.withValue(
            //     "org.opalj.tac.fpcf.analyses.pointsto.TamiFlex.logFile",
            //     ConfigValueFactory.fromAnyRef(tamiflexLogPath)
            // );
            // List<String> tamiflexSchedulers = Arrays.asList(
            //     "org.opalj.tac.fpcf.analyses.cg.reflection.TamiFlexCallGraphAnalysisScheduler"
            // );

            // 2. Fetch the existing active modules using the CORRECT config key
            // List<String> existingModules = config.getStringList("org.opalj.tac.cg.CallGraphKey.modules");

            // 3. Combine them
            // List<String> updatedModules = new ArrayList<>(existingModules);
            // List<String> updatedModules = new ArrayList<>();
            // updatedModules.addAll(tamiflexSchedulers);

            // 4. Overwrite the module list in your config
            // config = config.withValue(
            //     "org.opalj.tac.cg.CallGraphKey.modules",
            //     ConfigValueFactory.fromIterable(updatedModules)
            // );

        }   
        
        // LogContext projectLogContext = GlobalLogContext.successor();

        // // Project<?> project = Project.apply(projectJar, projectLogContext, config);
        // Project<?> project = Project.apply(appFiles, libFiles, projectLogContext, config);
        LogContext projectLogContext = new StandardLogContext();
        OPALLogger$.MODULE$.register(
            projectLogContext, 
            new ConsoleOPALLogger(false, Info$.MODULE$) 
        );
        // Project<?> project = Project.apply(appFiles, libFiles, projectLogContext, config);
        Project<?> project = Project.apply(appFiles, libFiles, projectLogContext, baseConfig);

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
