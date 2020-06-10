package isi.essaady.dashboard.collabs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.NotNull;

import org.primefaces.PrimeFaces;
import org.primefaces.event.RowEditEvent;

import isi.essaady.ejbs.CollaboratorBean;
import isi.essaady.ejbs.CompetenceBean;
import isi.essaady.entities.Collaborator;
import isi.essaady.entities.Competence;
import isi.essaady.helpers.Helpers;

@Named
@ViewScoped
public class CollabsBacking implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private static final java.util.regex.Pattern EMAIL_PATTERN =
			java.util.regex.Pattern.compile("[\\w\\.-]*[a-zA-Z0-9_]@[\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]");
	
	private List<Collaborator> collabs;
	private List<Competence> comps;
	private List<Collaborator> filteredCollabs;
	private List<String> compsSelect;
	private Map<Integer, Collaborator>  oldValuesColumns;
	private Collaborator selectedCollab;
	
	@NotNull
	private String fNameInput;
	@NotNull
	private String lNameInput;
	@NotNull
	@Pattern (regexp = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
	private String emailInput;
	@NotNull
	private String genderRadio;
	private Set<Competence> newCompsSelect;

	@EJB
    private CollaboratorBean collabBean;
	@EJB
    private CompetenceBean compBean;
     
    @PostConstruct
    public void init() {
        collabs = collabBean.getAllCollabs();
        comps = compBean.getAllComps();
        compsSelect = new ArrayList<String>();
        oldValuesColumns = new HashMap<Integer, Collaborator>();
    }

	
    /**
     * Updates collaborators info. It's called when a row is edited.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowEdit(RowEditEvent<Collaborator> event) {
    	int collabId = event.getObject().getIdCollab();
    	Collaborator newCollabValues = event.getObject();
    	Collaborator oldCollabValues = oldValuesColumns.get(collabId);
    	
    	if(newCollabValues.getEmail()==null
    			|| newCollabValues.getFirstName()==null
    			|| newCollabValues.getLastName()==null
    			|| newCollabValues.getGender()==null) {
    		event.getObject().setEmail(oldCollabValues.getEmail());
    		event.getObject().setFirstName(oldCollabValues.getFirstName());
    		event.getObject().setLastName(oldCollabValues.getLastName());
    		event.getObject().setGender(oldCollabValues.getGender());
    		Helpers.addMessage(FacesMessage.SEVERITY_WARN,"Warning","Some fields are empty. Values must be given.");
			return;
    	}
    	else {
    		if(!oldCollabValues.getFirstName().equals(newCollabValues.getFirstName().trim())
    				|| !oldCollabValues.getLastName().equals(newCollabValues.getLastName().trim())
    				|| !oldCollabValues.getGender().equals(newCollabValues.getGender())
    				|| !oldCollabValues.getEmail().equals(newCollabValues.getEmail().trim())
    				|| isCompsChanged(event.getObject().getCompetences()))
    		{
    			/* Test Email pattern match*/
    			boolean isMatching = EMAIL_PATTERN.matcher(newCollabValues.getEmail().trim())
                        					      .matches();
    			if(!isMatching) {
    				event.getObject().setEmail(oldCollabValues.getEmail());
    	    		event.getObject().setFirstName(oldCollabValues.getFirstName());
    	    		event.getObject().setLastName(oldCollabValues.getLastName());
    	    		event.getObject().setGender(oldCollabValues.getGender());
					Helpers.addMessage(FacesMessage.SEVERITY_ERROR,"Error",
							"Not a valid email.");
					return;
    			}
    			
    			/* Tests passed : Save changes to DB*/
    			// Include the choosed competences
    			includeSelectedComps(event.getObject());
    			// Save to DB
    			collabBean.updateCollab(event.getObject());
    	    	Helpers.addMessage(FacesMessage.SEVERITY_INFO,"Collaborator Edited",
    	    			"The new collaborator's information are successfully saved.");
    		}	
    	}
    }
    
    
    /**
     * Compares the new selected competences with the previous ones.
     * Used by onRowEdit() and includeSelectedComps() methods.
     * 
     * @param  oldComps  The old selected competences
     * @return  boolean  Test result. True if the competences are changed. 
     */
    public boolean isCompsChanged(Set<Competence> oldComps) {
    	List<String> oldCompsAsStrs = new ArrayList<String>();
    	oldComps.forEach(c -> oldCompsAsStrs.add(c.getName()));
    	
    	return !(this.compsSelect.size()==oldCompsAsStrs.size()
    			&& oldCompsAsStrs.containsAll(compsSelect));
    }
    
    
    /**
     * Inserts the new selected competences in the edited collaborator row as
     * Competence objects.
     * 
     * @param  eventCollab  The collaborator row
     * @return  Set<Competence>  The selected competences.
     */
    public Set<Competence> includeSelectedComps(Collaborator eventCollab) {
    	Set<Competence> eventComps = eventCollab.getCompetences();
    	if(isCompsChanged(eventComps)){
    		Set<Competence> selectedComps = new HashSet<Competence>();
    		this.compsSelect.forEach(cStr -> {
    			Optional<Competence> comp =
    					this.comps.stream().filter(c -> c.getName()==cStr).findFirst();
    			selectedComps.add(comp.get());
    		});
    		
    		eventCollab.setCompetences(selectedComps);
    		
    		return selectedComps;
    	}
    	
    	return null;
    }
    
    /**
     * Pushes the old columns values to the oldValuesColumns map and saves the collab's
     * competences in the compsSelect list as strings.
     * It's called when a competence row switches to edit mode.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowEditInit(RowEditEvent<Collaborator> event) {
    	// Load old values
    	Collaborator oldCollabObject = new Collaborator(event.getObject());
    	this.oldValuesColumns
    		.put(event.getObject().getIdCollab(), oldCollabObject);
    	// Init competences selections
    	compsSelect.clear();
    	event.getObject().getCompetences().forEach(c -> compsSelect.add(c.getName()));
    	// Refresh competences selections
    	PrimeFaces.current().ajax().update("sec-form:tbl1:"+event.getComponent().getAttributes().get("index")+":menuSelectComps");
    }
    
    
    /**
     * Pops the old column values from the oldValuesColumns map.
     * It's called when a competence row edit is cancelled.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowCancel(RowEditEvent<Collaborator> event) {
    	this.oldValuesColumns
    		.remove(event.getObject().getIdCollab());
    	Helpers.addMessage(FacesMessage.SEVERITY_INFO,"Edit Cancelled", "No changes tracked");
    }
    
    
    /**
     * Calculates the total assigned hours of a given collaborator.
     * 
     * @param  collabId  The collaborator's ID.
     * @return int  The total assigned hours.
     */
    public int totalAssignedHours(int collabId) {
    	
    	Optional<Collaborator> collab = 
    			this.collabs.stream().filter(c -> c.getIdCollab()==collabId).findFirst();
    	
    	AtomicInteger sum = new AtomicInteger(0);
    	collab.get().getCollabTaskPlans().forEach(p -> sum.addAndGet(p.getAssignedHours()));
    	
    	return sum.get();
    }
    
    
    /**
     * Creates a new collaborator and saves it to DB.
     * 
     * @return  String  Main view URI. 
     */
    public String addNewCollab() {
    	//Create new Collab object
    	Collaborator newCollab = new Collaborator();
    	newCollab.setFirstName(fNameInput.trim());
    	newCollab.setLastName(lNameInput.trim());
    	newCollab.setEmail(emailInput.trim().toLowerCase());
    	newCollab.setGender(genderRadio.trim());
    	newCollab.setCompetences(newCompsSelect);
    	// Save to DB
    	collabBean.createCollab(newCollab);
    	this.collabs = collabBean.getAllCollabs();
    	Helpers.addMessage(FacesMessage.SEVERITY_INFO, "New collaborator added",
        		"The new collaborator '"+ fNameInput + " " + lNameInput + "' is added successfully.");

    	return FacesContext.getCurrentInstance().getViewRoot().getViewId()
    			+  "?faces-redirect=true";	
    }
    
    
    /**
     * Removes a collaborator.
     * 
     * @param  collab  The selected collaborator to remove.
     */
    public void removeCollab(Collaborator collab) {
    	collabBean.deleteCollab(collab);
    	collabs.remove(collab);
    	Helpers.addMessage(FacesMessage.SEVERITY_INFO, "Collaborator Deleted",
    			"Collaborator'"+collab.getLastName()+" "+collab.getFirstName()+"' is successfully deleted.");
    }
    
    
    /*
     * GETTERS - SETTERS
     * */
    public List<Collaborator> getCollabs() {
		return collabs;
	}

	public void setCollabs(List<Collaborator> collabs) {
		this.collabs = collabs;
	}
	
	public List<Competence> getComps() {
		return comps;
	}

	public void setComps(List<Competence> comps) {
		this.comps = comps;
	}

	public List<Collaborator> getFilteredCollabs() {
		return filteredCollabs;
	}

	public void setFilteredCollabs(List<Collaborator> filteredCollabs) {
		this.filteredCollabs = filteredCollabs;
	}

	public String getGenderRadio() {
		return genderRadio;
	}

	public void setGenderRadio(String genderSelect) {
		this.genderRadio = genderSelect;
	}

	public List<String> getCompsSelect() {
		return compsSelect;
	}

	public void setCompsSelect(List<String> compsSelect) {
		this.compsSelect = compsSelect;
	}

	public Map<Integer, Collaborator> getOldValuesColumns() {
		return oldValuesColumns;
	}

	public void setOldValuesColumns(Map<Integer, Collaborator> oldValuesColumns) {
		this.oldValuesColumns = oldValuesColumns;
	}

	public String getfNameInput() {
		return fNameInput;
	}

	public String getlNameInput() {
		return lNameInput;
	}

	public String getEmailInput() {
		return emailInput;
	}

	public void setfNameInput(String fNameInput) {
		this.fNameInput = fNameInput;
	}

	public void setlNameInput(String lNameInput) {
		this.lNameInput = lNameInput;
	}

	public void setEmailInput(String emailInput) {
		this.emailInput = emailInput;
	}

	public Set<Competence> getNewCompsSelect() {
		return newCompsSelect;
	}

	public void setNewCompsSelect(Set<Competence> newCompsSelect) {
		this.newCompsSelect = newCompsSelect;
	}

	public Collaborator getSelectedCollab() {
		return selectedCollab;
	}

	public void setSelectedCollab(Collaborator selectedCollab) {
		this.selectedCollab = selectedCollab;
	}
	
}
