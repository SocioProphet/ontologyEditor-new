package com.rmdc.ontologyEditor.service;

import com.rmdc.ontologyEditor.model.*;
import org.mindswap.pellet.utils.iterator.IteratorUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ChangeService {

    private final DBService dbService;
    List<OntoChange> ontoChanges;

    public ChangeService(DBService dbService) {
        this.dbService = dbService;
    }

    public List<MainChangeCol> getAllChanges(){
        List<MainChangeCol> mainChangeCols = new ArrayList<>();

        ontoChanges = IteratorUtils.toList(dbService.getChanges().iterator());
        for(OntoChange ontoChange:ontoChanges){
            MainChangeCol cols = new MainChangeCol();
            cols.setId(ontoChange.getId());
            cols.setChangeType(ontoChange.getChangeType().getDescription());
            cols.setDescription(ontoChange.getDescription());
            cols.setAuthor(ontoChange.getUser().getName());
            cols.setVersion(ontoChange.getOntoVersion().getMainVersion()+". "+ontoChange.getOntoVersion().getSubVersion()+". "+ontoChange.getOntoVersion().getChangeVersion());
            cols.setTime(ontoChange.getTimestamp().toString());
            cols.setConcept(ontoChange.getConcept());
            cols.setChangeOn(ontoChange.getChangeAxiom());
            mainChangeCols.add(cols);
        }
        return mainChangeCols;
    }

    public List<DetailChange> getDetailChanges(int ontoChangeId){
        Iterable<ChangeDes> changeDes = dbService.getDesForChange(
                ontoChanges.stream().filter(c->c.getId()==ontoChangeId).findFirst().get());

        List<DetailChange> changes = new ArrayList<>();
        for(ChangeDes cd:changeDes){
            DetailChange dc = new DetailChange();
            dc.setId(cd.getId());
            dc.setChangeId(cd.getOntoChange().getId());
            dc.setDescription(cd.getDescription());
            changes.add(dc);
        }
        return changes;
    }

    public List<DetailChange> getInstanceChanges(int id) {
        Iterable<ChangeInstances> changeInstances = dbService.getInstancesForChange(
                ontoChanges.stream().filter(c->c.getId()==id).findFirst().get());

        List<DetailChange> changes = new ArrayList<>();
        for(ChangeInstances cd:changeInstances){
            DetailChange dc = new DetailChange();
            dc.setId(cd.getId());
            dc.setChangeId(cd.getOntoChange().getId());
            dc.setDescription(cd.getDescription());
            changes.add(dc);
        }
        return changes;
    }

    public List<DetailChange> getAnnotationChanges(int id) {
        Iterable<ChangeAnnotation> changeDes = dbService.changeAnnotationRepository(
                ontoChanges.stream().filter(c->c.getId()==id).findFirst().get());

        List<DetailChange> changes = new ArrayList<>();
        for(ChangeAnnotation cd:changeDes){
            DetailChange dc = new DetailChange();
            dc.setId(cd.getId());
            dc.setChangeId(cd.getOntoChange().getId());
            dc.setKey(cd.getAnnKey());
            dc.setDescription(cd.getAnnValue());
            changes.add(dc);
        }
        return changes;
    }
}
