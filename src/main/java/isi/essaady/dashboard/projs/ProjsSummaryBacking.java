package isi.essaady.dashboard.projs;

import java.io.Serializable;
import java.util.ArrayList;
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
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

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
	private List<Integer> disabledDays;
	private Project selectedProj;
	
	@NotNull
	private String titleInput;
	private String descInput;
	@NotNull
	private Date startDateInput;
	@NotNull
	private Date endDateInput;
	@NotNull
	private int durationInput;
	
	@EJB
	private ProjectBean projBean;
	
	@PostConstruct
    public void init() {
		projs = projBean.getAllProjects();
		oldTitleColumns = new HashMap<Integer, String>();
		startDateInput = Helpers.getDefaultStartDate(new Date());
		endDateInput = Helpers.getDefaultEndDate(new Date());
		durationInput = 8;
		disabledDays = new ArrayList<>();
		disabledDays.add(0);
		disabledDays.add(6);
		
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
    		event.getObject().setTitle(oldValue);
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
     * Pushes the old title values columns to the oldTitleColumns map.
     * It's called when a project row switches to edit mode.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowEditInit(RowEditEvent<Project> event) {
    	this.oldTitleColumns
			.put(event.getObject().getIdProject(), event.getObject().getTitle().trim());
    }
    
    
    /**
     * Pops the old column values from the oldTitleColumns map.
     * It's called when a project row edit is cancelled.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowCancel(RowEditEvent<Project> event) {
    	this.oldTitleColumns
			.remove(event.getObject().getIdProject());
	
    	Helpers.addMessage(FacesMessage.SEVERITY_INFO,"Edit Cancelled", "No changes tracked");
    }
    
    
    /**
     * Adds a new project after performing tests on the new data.
     * 
     * @return  String  Main view URI. 
     */
    public String addNewProj() {
    	// Test dates
    	if(startDateInput.before(new Date()) || startDateInput.after(endDateInput)) {
    		Helpers.addMessage(FacesMessage.SEVERITY_ERROR,"Error",
					"Specified dates are invalides. Start date must be a present date"
					+ " and end date must be greater than start date.");
			return null;
    	}
    	// Test Duration
    	if(durationInput <= 0) {
    		Helpers.addMessage(FacesMessage.SEVERITY_ERROR,"Error",
					"Duration must be greater than zero.");
    		return null;
    	}
 
    	int maxDuration = Helpers.calcMaxAllowedDuration(startDateInput, endDateInput);
    	if(durationInput > maxDuration) {
    		Helpers.addMessage(FacesMessage.SEVERITY_ERROR,"Error",
					"The given duration is not valid for the specified start and end dates.");
    		return null;
    	}
    	// Test Title
    	for (Project proj : projs) {
			if(proj.getTitle().equalsIgnoreCase(titleInput.trim())){
				Helpers.addMessage(FacesMessage.SEVERITY_ERROR, "Error",
						"Project title already exists.");
				return null;
			}
		}
    	// Create the new Proj object
    	Project newProj = new Project();
    	newProj.setTitle(titleInput.trim());
    	newProj.setDuration(durationInput);
    	newProj.setDescription(descInput);
    	newProj.setStartDate(startDateInput);
    	newProj.setEndDate(endDateInput);
    	// Save to DB
    	projBean.createProj(newProj);
    	this.projs = projBean.getAllProjects();
    	
    	// Keep message info displayed after redirection
    	FacesContext facesContext = FacesContext.getCurrentInstance();
    	Flash flash = facesContext.getExternalContext().getFlash();
    	flash.setKeepMessages(true);
    	Helpers.addMessage(FacesMessage.SEVERITY_INFO, "New Project added",
        		"The new project '"+ newProj.getTitle() + "' is added successfully.");
    	
    	return FacesContext.getCurrentInstance().getViewRoot().getViewId()
    			+  "?faces-redirect=true";	
    	
    }
    
    
    /**
     * Gets the number of collaborators in a project.
     * 
     * @param  proj  Project to process
     * @return  Integer  The counted collaborators
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
     * Determines if a project is in the finished state.
     * 
     * @param  proj  The project to process
     * @return  boolean  True if the project is finished
     */
    public boolean isFinished(Project proj) {
    	Date now = new Date();
    	return now.after(proj.getEndDate());
    }
    
    
    /**
     * Determines if a project is in the 'in progress' state.
     * 
     * @param  proj  The project to process
     * @return  boolean  True if the project is still in progress
     */
    public boolean isInProgress(Project proj) {
    	Date now = new Date();
    	return now.before(proj.getEndDate()) && now.after(proj.getStartDate());
    }
    
    
    /**
     * Determines if a project is in the 'unstarted' state.
     * 
     * @param  proj  The project to process
     * @return  boolean  True if the project is not started yet
     */
    public boolean isUnstarted(Project proj) {
    	Date now = new Date();
    	return now.before(proj.getStartDate());
    }
	
    
    /**
     * Determines the number of projects in the corresponding 'finished', 'in progress' and
     * 'unstarted' states.
     * 
     * @return  Map<String, Integer>  The mapped states values
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
    
    
    /**
     * Removes a project.
     * 
     * @param  proj  The selected project to remove.
     */
    public void removeProj(Project proj) {
    	projBean.deleteProj(proj);
    	projs.remove(proj);
    	Helpers.addMessage(FacesMessage.SEVERITY_INFO, "Project Deleted",
    			"Project'"+ proj.getTitle()+"' is successfully deleted.");
    }
    
    
    /**
     * Redirects to the Project tasks view with the project object passed to it.
     * 
     * @param  proj  The selected project to remove.
     */
    public String redirectProjTasks(Project proj) {
    	FacesContext.getCurrentInstance().getExternalContext().getFlash()
    				.put("proj", proj);
    	
    	return "projectsTasks";
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

	public String getTitleInput() {
		return titleInput;
	}

	public String getDescInput() {
		return descInput;
	}

	public void setTitleInput(String titleInput) {
		this.titleInput = titleInput;
	}

	public void setDescInput(String descInput) {
		this.descInput = descInput;
	}

	public Date getStartDateInput() {
		return startDateInput;
	}

	public void setStartDateInput(Date startDateInput) {
		this.startDateInput = startDateInput;
	}

	public Date getEndDateInput() {
		return endDateInput;
	}

	public int getDurationInput() {
		return durationInput;
	}

	public void setEndDateInput(Date endDateInput) {
		this.endDateInput = endDateInput;
	}

	public void setDurationInput(int durationInput) {
		this.durationInput = durationInput;
	}

	public List<Integer> getDisabledDays() {
		return disabledDays;
	}

	public void setDisabledDays(List<Integer> disabledDays) {
		this.disabledDays = disabledDays;
	}

	public Project getSelectedProj() {
		return selectedProj;
	}

	public void setSelectedProj(Project selectedProj) {
		this.selectedProj = selectedProj;
	}
	
}
