package com.github.easyware.easyapiapp;

import com.alibaba.fastjson.JSON;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.common.base.Strings;
import org.junit.Test;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

import java.util.List;
import java.util.stream.Collectors;

public class ParserTest {

    /**
     *  遍历所有源文件。
     *  待遇@Controller 或 @RestController的需要处理方法；其它的处理属性。
     *  方法：方法概要、描述。 每个参数说明。 返回说明
     *  其它类：类说明（不用）、属性说明
     */
    @Test
    public  void test1(){

        File projectDir = new File("/Users/apple/github/easy-api/easy-api-app/");
        JavaParser javaParser=new JavaParser();
        new DirExplorer((level, path, file) -> path.endsWith("Controller.java"), (level, path, file) -> {
            System.out.println(file.getAbsolutePath());
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
                        super.visit(n, arg);

                        if (n.getComment().isPresent() && n.getComment().get() instanceof JavadocComment) {
                            String title = String.format("%s (%s)", n.getName(), path);
                            System.out.println("path="+title);
                            System.out.println(Strings.repeat("=", title.length()));
                            System.out.println(n.getComment().get());
                        }
                    }
                }.visit(javaParser.parse(file).getResult().get(), null);
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }).explore(projectDir);
    }

    @Test
    public  void test2(){

        File projectDir = new File("/Users/apple/github/easy-api/easy-api-app/");
        JavaParser javaParser=new JavaParser();
        new DirExplorer((level, path, file) -> path.endsWith("Controller.java"), (level, path, file) -> {
            System.out.println(file.getAbsolutePath());
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(JavadocComment comment, Object arg) {
                        super.visit(comment, arg);
                        String title = null;
                        if (comment.getCommentedNode().isPresent()) {

                            title = String.format("%s (%s)", describe(comment.getCommentedNode().get()), path);
                        } else {

                            title = String.format("No element associated (%s)", path);
                        }
                        System.out.println(title);
                        System.out.println(Strings.repeat("=", title.length()));
                        System.out.println(comment);
                    }
                }.visit(javaParser.parse(file).getResult().get(), null);
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }).explore(projectDir);

    }

    @Test
    public  void testMethod() throws FileNotFoundException {
        CompilationUnit c = new JavaParser().parse(new File("/Users/apple/github/easy-api/easy-api-app/src/test/java/com/github/easyware/easyapiapp/TestController.java"))
                .getResult().get();
        List<ClassOrInterfaceDeclaration> classNodes = c.findAll(ClassOrInterfaceDeclaration.class);

        ClassOrInterfaceDeclaration classNode=classNodes.get(0);
        System.out.println(  classNode.getAnnotation(0));
        System.out.println(classNode.isAnnotationPresent(RestController.class));
        List<MethodDeclaration> methodNodes = classNode.findAll(MethodDeclaration.class)  ;
        for(MethodDeclaration methodNode:methodNodes){
            if(methodNode.isPublic()){

             Javadoc javadoc= methodNode.getJavadoc().get();
                //System.out.println(javadoc);
                System.out.println(javadoc.getDescription().toText());
                for(JavadocBlockTag tag:javadoc.getBlockTags()) {
                    String name=null;
                    if(tag.getName().isPresent()) name=tag.getName().get();
                    System.out.println("tag="+tag.getTagName() + "\r\nname=" +name+ "\r\ntext="
                            + tag.getContent().toText());
                }


            }
        }
    }

    @Test
    public  void testField() throws FileNotFoundException {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(new File("/Users/apple/github/easy-api/easy-api-app/src/test/java")));
        //combinedTypeSolver.add(new JavaParserTypeSolver(new File("src/test/resources/javaparser_src/generated")));

        CompilationUnit c = new JavaParser().parse(new File("/Users/apple/github/easy-api/easy-api-app/src/test/java/com/github/easyware/easyapiapp/TestController.java"))
                .getResult().get();
        List<ClassOrInterfaceDeclaration> classNodes = c.findAll(ClassOrInterfaceDeclaration.class);
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration=classNodes.get(0);
        System.out.println("classname="+classOrInterfaceDeclaration.getName());
        List<FieldDeclaration> fieldNodes = classOrInterfaceDeclaration.findAll(FieldDeclaration.class)  ;

        for (FieldDeclaration node : fieldNodes) {
            node.toString();
            //List<String> varNames = node.getVariables().stream().map(v -> v.getName().getId()).collect(Collectors.toList());
            VariableDeclarator var=node.getVariable(0);
            ResolvedType resolvedType = JavaParserFacade.get(combinedTypeSolver).getType(node);
            System.out.println(resolvedType.describe());

            //System.out.println(typeOfTheNode.asReferenceType().getAllAncestors().get(0).describe());
            System.out.println( "name=" +var.getName().getId()+",type="+var.getType());
            //Node cn=  node.getComment().get().getCommentedNode().get();
            //System.out.println(cn.getClass()+","+cn);
            //for(Node n:node.getChildNodes()){


            //node.getComment()
          // System.out.println("------------\r\n" + node.getClass() + "," + node.toString());


        }
    }
    @Test
    public  void test3() throws FileNotFoundException {
        CompilationUnit c= new JavaParser().parse(new File("/Users/apple/github/easy-api/easy-api-app/src/test/java/com/github/easyware/easyapiapp/TestController.java"))
                .getResult().get() ;
        List<ClassOrInterfaceDeclaration> classNodes=  c.findAll(ClassOrInterfaceDeclaration.class);
        List<Node> nodes=classNodes.get(0).getChildNodes();
        for(Node node:nodes){
            System.out.println("------------\r\n"+node.getClass()+","+node.toString());

        }
        if(true)return;

        //System.out.println(  c.getComment().get().getClass());
        for(Comment comment:c.getAllComments()){
            System.out.println("--------------------------------------------");
            if(comment.getCommentedNode().isPresent())
            System.out.println(comment.getCommentedNode().get().getClass());
            System.out.println(comment.getClass()+","+ comment.toString());
        }
    }


    private static String describe(Node node) {
        if (node instanceof MethodDeclaration) {
            MethodDeclaration methodDeclaration = (MethodDeclaration) node;
            return "Method " + methodDeclaration.getDeclarationAsString();
        }
        if (node instanceof ConstructorDeclaration) {
            ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) node;
            return "Constructor " + constructorDeclaration.getDeclarationAsString();
        }
        if (node instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) node;
            if (classOrInterfaceDeclaration.isInterface()) {
                return "Interface " + classOrInterfaceDeclaration.getName();
            } else {
                return "Class " + classOrInterfaceDeclaration.getName();
            }
        }
        if (node instanceof EnumDeclaration) {
            EnumDeclaration enumDeclaration = (EnumDeclaration) node;
            return "Enum " + enumDeclaration.getName();
        }
        if (node instanceof FieldDeclaration) {
            FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
            List<String> varNames = fieldDeclaration.getVariables().stream().map(v -> v.getName().getId()).collect(Collectors.toList());
            return "Field " + String.join(", ", varNames);
        }
        return node.toString();
    }

    @Test
    public void commentParserTest() {
        CommentParser commentParser = new CommentParser();
        File file=new File("/Users/apple/workspace/");
        //file=new File("/Users/apple/github/easy-api/easy-api-app/src/test/java/com/github/easyware/easyapiapp")
        commentParser.parseDir(file);

        System.out.println(JSON.toJSONString(commentParser.getMethodComments(), true));
        System.out.println("------------------------------");
        System.out.println(JSON.toJSONString(commentParser.getFieldComments(), true));
    }
}
