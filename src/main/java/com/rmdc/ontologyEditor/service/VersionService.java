package com.rmdc.ontologyEditor.service;

import com.rmdc.ontologyEditor.UtilMethods;
import com.rmdc.ontologyEditor.model.*;
import org.mindswap.pellet.utils.iterator.IteratorUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VersionService {

    private final DBService dbService;
    private VersionTreeNode versionTree;

    private List<OntoVersion> versions;
    private List<OntoChange> changes;
    public VersionService(DBService dbService) {
        this.dbService = dbService;
    }

    public VersionTreeNode printVersionTree(){
        if(versions==null){
            versions = IteratorUtils.toList(dbService.getVersions().iterator());
        }
        changes = IteratorUtils.toList(dbService.getChanges().iterator());

        OntoVersion root  =versions.stream().filter(v->v.getId()==1).findFirst().get();

        versionTree = new VersionTreeNode(root.getMainVersion()+"."+root.getSubVersion()+"."+root.getChangeVersion(),"1", getChangesForVersion(1));
        versionTree.setId(root.getId().toString());
        printVersionTree(root, 0);
        return versionTree;
    }

    private void printVersionTree(OntoVersion parent, int level){
        List<OntoVersion> vs = versions.stream().filter(v->v.getPrior()==parent.getId()).collect(Collectors.toList());
        if(!vs.isEmpty()){
            VersionTreeNode pN = UtilMethods.searchTreeEx(parent.getMainVersion()+"."+parent.getSubVersion()+"."+parent.getChangeVersion(),versionTree);
            for(OntoVersion o: vs){
                pN.addChild(o.getMainVersion()+"."+o.getSubVersion()+"."+o.getChangeVersion(),o.getId().toString(),getChangesForVersion(o.getId()));
                printVersionTree(o, level+1);
            }
        }
    }


    public List<VersionTable> getAllVersions(){
        if(versions==null){
            versions = IteratorUtils.toList(dbService.getVersions().iterator());
        }
        List<VersionTable> table = new ArrayList<>();

        versions.stream().filter(vf->vf.getPrior()!=0).forEach(v->{
            VersionTable row = new VersionTable();
            row.setId(v.getId());
            row.setNumber(v.getMainVersion()+"."+v.getSubVersion()+"."+v.getChangeVersion());
            row.setDescription(v.getName());
            OntoVersion prior = versions.stream().filter(pv-> pv.getId().equals(v.getId())).findFirst().get();
            row.setPrior(String.valueOf(prior.getMainVersion()+"."+prior.getSubVersion()+"."+prior.getChangeVersion()));
            row.setTime(v.getTimestamp().toString());
            table.add(row);

        });
        return table;
    }

    private List<String> getChangesForVersion(int id){
        List<String> changeList=new ArrayList<>();
        changes.stream().filter(c->c.getOntoVersion().getId()==id).collect(Collectors.toList()).forEach(
                fc->changeList.add(
                        fc.getConcept() + " changed by :" +
                                fc.getUser().getName()+ " :"+
                                fc.getDescription() + " on " +
                                fc.getTimestamp().toString()));
        return changeList;
    }
}
