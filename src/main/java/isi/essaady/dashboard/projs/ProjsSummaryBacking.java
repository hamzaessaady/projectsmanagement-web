package isi.essaady.dashboard.projs;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.event.RowEditEvent;

import isi.essaady.ejbs.ProjectBean;
import isi.essaady.entities.Project;
import isi.essaady.helpers.Constants;
import isi.essaady.helpers.Helpers;

@Named
@ViewScoped
public class ProjsSummaryBacking implements Serializable{

	private static final long serialVersionUID = 1L;

	private List<Project> projs;
	private List<Project> filteredProjs;
	private Map<Integer, String>  oldTitleColumns;
	
	@EJB
	private ProjectBean projBean;
	
	@PostConstruct
    public void init() {
		projs = projBean.getAllProjects();
		oldTitleColumns = new HashMap<Integer, String>();
	}
	
	
	/**
     * Updates projects info. It's called when a row is edited.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowEdit(RowEditEvent<Project> event) {
    	int projId = event.getObject().getIdProject();
    	String oldValue = oldTitleColumns.get(projId);
    	
    	if(event.getObject().getTitle()==null) {
    		event.getObject().setTitle(oldValue.trim());
    		Helpers.addMessage(FacesMessage.SEVERITY_WARN,"Warning","A value must be given.");
			return;
    	}
    	else {
        	String newValue = event.getObject().getTitle().trim();
        	if(!newValue.equals(oldValue)) {
        		/* Testing the new edited value*/
    	    	for (Project proj : projs) {
    				if(proj.getTitle().equalsIgnoreCase(newValue)
    						&& proj.getIdProject() != projId) {
    					event.getObject().setTitle(oldValue);
    					Helpers.addMessage(FacesMessage.SEVERITY_ERROR,"Error",
    							"Project already exists");
    					return;
    				}
    			}
    	    	
    	    	/* Tests passed : Save changes to DB*/
    	    	event.getObject().setTitle(newValue);
    	    	projBean.updateProj(event.getObject());
    	    	Helpers.addMessage(FacesMessage.SEVERITY_INFO,"Project Edited",
    	    			"The new value '"+ newValue + "' is successfully saved.");	
        	}   		
    	}
    }
    
    
    /**
     * Pushes the old columns values to the oldValuesColumns map and saves the collab's
     * competences in the compsSelect list as strings.
     * It's called when a competence row switches to edit mode.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowEditInit(RowEditEvent<Project> event) {
    	this.oldTitleColumns
			.put(event.getObject().getIdProject(), event.getObject().getTitle().trim());
    }
    
    
    /**
     * Pops the old column values from the oldValuesColumns map.
     * It's called when a competence row edit is cancelled.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowCancel(RowEditEvent<Project> event) {
    	this.oldTitleColumns
			.remove(event.getObject().getIdProject());
	
    	Helpers.addMessage(FacesMessage.SEVERITY_INFO,"Edit Cancelled", "No changes tracked");
    }
    
    
    /**
     * CountCollabs()
     */
    public int countCollabs(Project proj) {
    	Set<Integer> collabsIds = new HashSet<Integer>();
    	proj.getTasks().forEach(t -> 
    			t.getCollabTaskPlans().forEach(tp ->
    				collabsIds.add(tp.getCollaborator().getIdCollab())
    	));
    	
    	return collabsIds.size();
    }
    
    
    /**
     * isFinished()
     */
    public boolean isFinished(Project proj) {
    	Date now = new Date();
    	return now.after(proj.getEndDate());
    }
    
    
    /**
     * isInProgress()
     */
    public boolean isInProgress(Project proj) {
    	Date now = new Date();
    	return now.before(proj.getEndDate()) && now.after(proj.getStartDate());
    }
    
    
    /**
     * isUnstarted()
     */
    public boolean isUnstarted(Project proj) {
    	Date now = new Date();
    	return now.before(proj.getStartDate());
    }
	
    
    /**
     * isUnstarted()
     */
    public Map<String, Integer> countProjsByStatus(){
    	Map<String, Integer> projsStatus = new HashMap<String, Integer>();
    	AtomicInteger finished = new AtomicInteger(0);
    	AtomicInteger inProgress = new AtomicInteger(0);
    	AtomicInteger unstarted = new AtomicInteger(0);
    	
    	this.projs.forEach(p -> {
    		if(isFinished(p)) {
    			finished.incrementAndGet();
    		} else if(isInProgress(p)){
    			inProgress.incrementAndGet();
    		} else {
    			unstarted.incrementAndGet();
    		}
    	});
    	
    	projsStatus.put(Constants.FINISHED, finished.get());
    	projsStatus.put(Constants.IN_PROGRESS, inProgress.get());
    	projsStatus.put(Constants.UNSTARTED, unstarted.get());
    	
    	return projsStatus;
    }
    
	/* GETTERS AND SETTERS*/
	public List<Project> getProjs() {
		return projs;
	}

	public void setProjs(List<Project> projs) {
		this.projs = projs;
	}

	public List<Project> getFilteredProjs() {
		return filteredProjs;
	}

	public void setFilteredProjs(List<Project> filteredProjs) {
		this.filteredProjs = filteredProjs;
	}
	

}
