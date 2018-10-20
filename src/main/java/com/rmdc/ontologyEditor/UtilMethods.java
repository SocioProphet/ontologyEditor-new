package com.rmdc.ontologyEditor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmdc.ontologyEditor.model.TreeNode;
import com.rmdc.ontologyEditor.model.VersionTreeNode;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Lotus on 8/20/2017.
 */
public class UtilMethods {

    public static TreeNode searchTree(String className, TreeNode node) {
        if (node.getText().equals(className)) {
            return node;
        }
        List<?> children = node.getChildren();
        TreeNode res = null;
        for (int i = 0; res == null && i < children.size(); i++) {
            res = searchTree(className, (TreeNode) children.get(i));
        }
        return res;
    }

    public static VersionTreeNode searchTreeEx(String className, VersionTreeNode node) {
        if (node.getText().equals(className)) {
            return node;
        }
        List<?> children = node.getChildren();
        VersionTreeNode res = null;
        for (int i = 0; res == null && i < children.size(); i++) {
            res = searchTreeEx(className, (VersionTreeNode) children.get(i));
        }
        return res;
    }

    public static String convertAxiom(OWLAxiom axiomToExplain) {

        OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();
        return  DlToEnglish(renderer.render(axiomToExplain));
    }


    private static String DlToEnglish(String exp){
        String eng = exp.replace("≡","is equivalent to")
                .replace("⊓","and")
                .replace("⊔","or")
                .replace("¬","not ")
                .replace("⊑","is a sub class of ")
                .replace("≥","at least")
                .replace("≤","at most")
                .replace("=","exact");


        int lastIndex = 0;
        while(lastIndex != -1){
            if(eng.contains("∃ ")){
                eng = eng.replaceFirst("∃ ","")
                        .replaceFirst("\\."," has some ");
            }
            if(eng.contains("∀ ")){
                eng = eng.replaceFirst("∀ ","")
                        .replaceFirst("\\."," has only ");
            }

            lastIndex = eng.indexOf("∃ ",lastIndex);

            if(lastIndex != -1){
                lastIndex += "∃ ".length();
            }
        }
        eng = eng.replace("has some {","has value {").replace("⊤","").replaceAll("\\.","");
        return eng;
    }

    public static byte[] toBits(Object o) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] bytes = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            bytes = bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
        return bytes;
    }

    public static Object toObject(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        Object o = null;
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            o = in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
        return o;
    }

    public static String toManchesterFormat(OWLAxiom axiomToExplain) {
        OWLObjectRenderer renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        return renderer.render(axiomToExplain);
    }

    public static String buildTree(boolean condition, TreeNode node){
        if(condition){
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(node);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}
