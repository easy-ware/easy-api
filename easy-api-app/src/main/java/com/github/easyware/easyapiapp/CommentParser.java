package com.github.easyware.easyapiapp;

import com.github.easyware.easyapiapp.object.MethodComment;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.printer.PrettyPrintVisitor;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.javaparser.utils.PositionUtils.sortByBeginPosition;

public class CommentParser {
    private static Logger logger= LoggerFactory.getLogger(CommentParser.class);
    CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
    JavaParserFacade javaParserFacade=null;
    private Map<String,MethodComment> methodComments=new ConcurrentHashMap<>();
    private Map<String,Map<String,String>> fieldComments=new ConcurrentHashMap<>();//<className, <prop,comment>>
    JavaParser javaParser=new JavaParser();

    private long count;

    public Map<String, MethodComment> getMethodComments() {
        return methodComments;
    }

    public Map<String, Map<String, String>> getFieldComments() {
        return fieldComments;
    }

    public void searchSourceDir(File dir){
        combinedTypeSolver.add(new ReflectionTypeSolver());
        //combinedTypeSolver.add(new JavaParserTypeSolver(new File("/Users/apple/github/easy-api/easy-api-app/src/test/java")));

        new DirSearch(dir){

            @Override
            protected void handle(int level, File file) {
                // /src/main/java
                if(file.isDirectory() && file.getAbsolutePath().endsWith("/src/main/java") ){
                    combinedTypeSolver.add(new JavaParserTypeSolver(file));
                }

            }
        };

        javaParserFacade=JavaParserFacade.get(combinedTypeSolver);

    }
    public void parse(File dir){
        count=1;
        new DirSearch(dir){

            @Override
            protected void handle(int level, File file) {
                if(file.isFile() && file.getName().endsWith(".java")) {
                    try {

                        CompilationUnit c = javaParser.parse(file).getResult().get();
                        List<ClassOrInterfaceDeclaration> classNodes = c.findAll(ClassOrInterfaceDeclaration.class);
                        for(ClassOrInterfaceDeclaration classNode:classNodes){
                            if(!classNode.isPublic()) continue;
                            if(classNode.isInterface())continue;
                            System.out.println(file.getAbsolutePath()+","+count++);

                            //controlller
                            if(classNode.isAnnotationPresent(RestController.class)|| classNode.isAnnotationPresent(Controller.class)){
                                parseMethod(classNode);

                            }else{

                                parseField(classNode);

                            }
                        }

                    }catch (Exception e){
                        logger.error("",e);
                    }
                }

            }
        };
    }

    private void parseMethod(ClassOrInterfaceDeclaration classNode){
        List<MethodDeclaration> methodNodes = classNode.findAll(MethodDeclaration.class)  ;
        for(MethodDeclaration methodNode:methodNodes){
            MethodComment  methodComment=getMethodComment( methodNode);
            if(methodComment!=null){
                String key=classNode.getFullyQualifiedName().get()+"#"+methodNode.getName();
                methodComment.setFullName(key);
                methodComments.put(key,methodComment);
            }
        }
    }

    private MethodComment getMethodComment(MethodDeclaration methodNode){
        if(!methodNode.isPublic()) return null;
        if(!methodNode.getJavadoc().isPresent())return null;
        Javadoc javadoc=methodNode.getJavadoc().get();
        MethodComment methodComment=new MethodComment();

        String desc=javadoc.getDescription().toText();
        String[] desc2=parseDesc(desc);
        if(desc2!=null){
            methodComment.setSummary(desc2[0]);
            methodComment.setDesc(desc2[1]);
        }

         if(javadoc.getBlockTags()!=null) {
             for (JavadocBlockTag tag : javadoc.getBlockTags()) {
                 String name = null;
                 if (tag.getName().isPresent()) name = tag.getName().get();
                 if("param".equals(tag.getTagName())){
                    if(name!=null) methodComment.getParams().put(name,tag.getContent().toText());
                 }else if("return".equals(tag.getTagName())){
                     methodComment.setReturnComment(tag.getContent().toText());
                 }

             }
         }

         return methodComment;

    }
    private void parseField(ClassOrInterfaceDeclaration classNode){
        List<FieldDeclaration> fieldNodes = classNode.findAll(FieldDeclaration.class)  ;
        Map<String,String> map=new HashMap<>();
        for (FieldDeclaration node : fieldNodes) {
            VariableDeclarator var=node.getVariable(0);
            String name=var.getName().getId();
            String comment=parseFieldComment(node);
            if(!comment.isEmpty()){
                map.put(name,comment);
            }

        }
        if(map.size()>0){

            fieldComments.put(classNode.getFullyQualifiedName().get(),map);
        }
    }

    /**
     * 参考 PrettyPrintVisitor#printOrphanCommentsBeforeThisChildNode(com.github.javaparser.ast.Node)
     * @param node
     * @return
     */
    private String parseFieldComment(FieldDeclaration node ){
    //if(node.getVariable(0).getName().getId().equals("sex"))
      //  System.out.println(node.toString());
       Node p =node.getParentNode().get();
        List<Node> childs = new ArrayList<>(p.getChildNodes());
        sortByBeginPosition(childs);
       int n1=-1;
       int n2=-1;
        for(int i=0;i<childs.size();i++){
            if(childs.get(i)==node){
                n2=i-1;
                break;
            }
        }
        for(int i=n2;i>=0;i--){
            if(!(childs.get(i) instanceof Comment)){
                n1=i+1;
                break;
            }
        }
        StringBuilder sb=new StringBuilder();
        if(node.getComment().isPresent()) {
            sb.append(node.getComment().get().getContent());// LineComment
        }
        for(int i=n1;i<=n2;i++){
            sb.append(((Comment)childs.get(i)).getContent());
        }
        return sb.toString();

    }

    private String[] parseDesc(String desc){
        if(!StringUtils.hasLength(desc) )return null;
        int n=-1;
        for(int i=0;i<desc.length();i++){
            char c=desc.charAt(i);
            if(c=='.' || c=='。'|| c=='\r' || c=='\n' ){
                return new String[]{desc.substring(0,i),desc.substring(i+1)};

            }else if(i==80){
                return new String[] {desc.substring(0,80),desc};
            }
        }
        return new String[] {desc,desc};

    }
}
