
package cz.habarta.typescript.generator.gradle;

import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.Input;
import java.io.*;
import java.net.*;
import java.util.*;
import org.gradle.api.*;
import org.gradle.api.tasks.*;


public class GenerateTask extends DefaultTask {

    public String outputFile;
    public TypeScriptFileType outputFileType;
    public TypeScriptOutputKind outputKind;
    public String module;
    public String namespace;
    public String umdNamespace;
    public List<String> classes;
    public List<String> classPatterns;
    public String classesFromJaxrsApplication;
    public boolean classesFromAutomaticJaxrsApplication;
    public List<String> excludeClasses;
    public List<String> excludeClassPatterns;
    public List<String> includePropertyAnnotations;
    public JsonLibrary jsonLibrary;
    public boolean declarePropertiesAsOptional;
    public boolean declarePropertiesAsReadOnly;
    public String removeTypeNamePrefix;
    public String removeTypeNameSuffix;
    public String addTypeNamePrefix;
    public String addTypeNameSuffix;
    public List<String> customTypeNaming;
    public String customTypeNamingFunction;
    public List<String> referencedFiles;
    public List<String> importDeclarations;
    public List<String> customTypeMappings;
    public DateMapping mapDate;
    public EnumMapping mapEnum;
    public ClassMapping mapClasses;
    public boolean disableTaggedUnions;
    public boolean generateJaxrsApplicationInterface;
    public boolean generateJaxrsApplicationClient;
    public String restResponseType;
    public String restOptionsType;
    public String customTypeProcessor;
    public boolean sortDeclarations;
    public boolean sortTypeDeclarations;
    public boolean noFileComment;
    public List<File> javadocXmlFiles;
    public List<String> extensionClasses;
    public List<String> optionalAnnotations;
    public boolean generateNpmPackageJson;
    public String npmName;
    public String npmVersion;
    public StringQuotes stringQuotes;
    public boolean displaySerializerWarning = true;
    public boolean disableJackson2ModuleDiscovery;

    @TaskAction
    public void generate() throws Exception {
        if (outputKind == null) {
            throw new RuntimeException("Please specify 'outputKind' property.");
        }
        if (jsonLibrary == null) {
            throw new RuntimeException("Please specify 'jsonLibrary' property.");
        }

        TypeScriptGenerator.printVersion();

        // class loader
        final List<URL> urls = new ArrayList<>();
        for (Task task : getProject().getTasksByName("compileJava", false)) {
            for (File file : task.getOutputs().getFiles()) {
                urls.add(file.toURI().toURL());
            }
        }
        for (File file : getProject().getConfigurations().getAt("compile").getFiles()) {
            urls.add(file.toURI().toURL());
        }
        final URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());

        // Settings
        final Settings settings = new Settings();
        if (outputFileType != null) {
            settings.outputFileType = outputFileType;
        }
        settings.outputKind = outputKind;
        settings.module = module;
        settings.namespace = namespace;
        settings.umdNamespace = umdNamespace;
        settings.setExcludeFilter(excludeClasses, excludeClassPatterns);
        settings.jsonLibrary = jsonLibrary;
        settings.declarePropertiesAsOptional = declarePropertiesAsOptional;
        settings.declarePropertiesAsReadOnly = declarePropertiesAsReadOnly;
        settings.removeTypeNamePrefix = removeTypeNamePrefix;
        settings.removeTypeNameSuffix = removeTypeNameSuffix;
        settings.addTypeNamePrefix = addTypeNamePrefix;
        settings.addTypeNameSuffix = addTypeNameSuffix;
        settings.customTypeNaming = Settings.convertToMap(customTypeNaming);
        settings.customTypeNamingFunction = customTypeNamingFunction;
        settings.referencedFiles = referencedFiles;
        settings.importDeclarations = importDeclarations;
        settings.customTypeMappings = Settings.convertToMap(customTypeMappings);
        settings.mapDate = mapDate;
        settings.mapEnum = mapEnum;
        settings.mapClasses = mapClasses;
        settings.disableTaggedUnions = disableTaggedUnions;
        settings.generateJaxrsApplicationInterface = generateJaxrsApplicationInterface;
        settings.generateJaxrsApplicationClient = generateJaxrsApplicationClient;
        settings.restResponseType = restResponseType;
        settings.restOptionsType = restOptionsType;
        settings.loadCustomTypeProcessor(classLoader, customTypeProcessor);
        settings.sortDeclarations = sortDeclarations;
        settings.sortTypeDeclarations = sortTypeDeclarations;
        settings.noFileComment = noFileComment;
        settings.javadocXmlFiles = javadocXmlFiles;
        settings.loadExtensions(classLoader, extensionClasses);
        settings.loadIncludePropertyAnnotations(classLoader, includePropertyAnnotations);
        settings.loadOptionalAnnotations(classLoader, optionalAnnotations);
        settings.generateNpmPackageJson = generateNpmPackageJson;
        settings.npmName = npmName != null && generateNpmPackageJson ? getProject().getName() : npmName;
        settings.npmVersion = npmVersion != null && generateNpmPackageJson ? settings.getDefaultNpmVersion() : npmVersion;
        settings.setStringQuotes(stringQuotes);
        settings.displaySerializerWarning = displaySerializerWarning;
        settings.disableJackson2ModuleDiscovery = disableJackson2ModuleDiscovery;
        settings.classLoader = classLoader;
        final File output = outputFile != null
                ? getProject().file(outputFile)
                : new File(new File(getProject().getBuildDir(), "typescript-generator"), getProject().getName() + settings.getExtension());
        settings.validateFileName(output);

        // TypeScriptGenerator
        new TypeScriptGenerator(settings).generateTypeScript(
                Input.fromClassNamesAndJaxrsApplication(classes, classPatterns, classesFromJaxrsApplication, classesFromAutomaticJaxrsApplication, settings.getExcludeFilter(), classLoader),
                Output.to(output)
        );
    }

}
