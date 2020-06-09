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
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

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
	private static final Pattern EMAIL_PATTERN =
			   Pattern.compile("[\\w\\.-]*[a-zA-Z0-9_]@[\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]");
	
	private List<Collaborator> collabs;
	private List<Competence> comps;
	private List<Collaborator> filteredCollabs;
	private String genderRadio;
	private List<String> compsSelect;
	private Map<Integer, Collaborator>  oldValuesColumns;

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
     * Updates competences. Tests the new value before merging it.
     * It's called when a row is edited.
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
     * isCompsChanged
     * TODO JAVA DOC
     */
    public boolean isCompsChanged(Set<Competence> oldComps) {
    	List<String> oldCompsAsStrs = new ArrayList<String>();
    	oldComps.forEach(c -> oldCompsAsStrs.add(c.getName()));
    	
    	return !(this.compsSelect.size()==oldCompsAsStrs.size()
    			&& oldCompsAsStrs.containsAll(compsSelect));
    }
    
    /**
     * includeSelectedComps
     * TODO JAVA DOC
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
     * Pushes the old column value to the oldNameColumns map.
     * It's called when a competence row switches to edit mode.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowEditInit(RowEditEvent<Collaborator> event) {
    	// Load old values
    	Collaborator oldCollabObject = new Collaborator(event.getObject());
    	System.out.println("OLD COMPS INIT: " +event.getObject().getCompetences().size());
    	this.oldValuesColumns
    		.put(event.getObject().getIdCollab(), oldCollabObject);
    	System.out.println("OLD INIT : "+oldCollabObject.getGender());
    	// Init competences selections
    	compsSelect.clear();
    	event.getObject().getCompetences().forEach(c -> compsSelect.add(c.getName()));
    	// Refresh competences selections
    	PrimeFaces.current().ajax().update("sec-form:tbl1:"+event.getComponent().getAttributes().get("index")+":menuSelectComps");
    }
    
    
    /**
     * Pops the old column value from the oldNameColumns map.
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
     * totalAssignedHours
     * TODO JAVA DOC
     */
    public int totalAssignedHours(int collabId) {
    	
    	Optional<Collaborator> collab = 
    			this.collabs.stream().filter(c -> c.getIdCollab()==collabId).findFirst();
    	
    	AtomicInteger sum = new AtomicInteger(0);
    	collab.get().getCollabTaskPlans().forEach(p -> sum.addAndGet(p.getAssignedHours()));
    	
    	return sum.get();
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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((collabs == null) ? 0 : collabs.hashCode());
		result = prime * result + ((comps == null) ? 0 : comps.hashCode());
		result = prime * result + ((compsSelect == null) ? 0 : compsSelect.hashCode());
		result = prime * result + ((filteredCollabs == null) ? 0 : filteredCollabs.hashCode());
		result = prime * result + ((genderRadio == null) ? 0 : genderRadio.hashCode());
		result = prime * result + ((oldValuesColumns == null) ? 0 : oldValuesColumns.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CollabsBacking other = (CollabsBacking) obj;
		if (collabs == null) {
			if (other.collabs != null)
				return false;
		} else if (!collabs.equals(other.collabs))
			return false;
		if (comps == null) {
			if (other.comps != null)
				return false;
		} else if (!comps.equals(other.comps))
			return false;
		if (compsSelect == null) {
			if (other.compsSelect != null)
				return false;
		} else if (!compsSelect.equals(other.compsSelect))
			return false;
		if (filteredCollabs == null) {
			if (other.filteredCollabs != null)
				return false;
		} else if (!filteredCollabs.equals(other.filteredCollabs))
			return false;
		if (genderRadio == null) {
			if (other.genderRadio != null)
				return false;
		} else if (!genderRadio.equals(other.genderRadio))
			return false;
		if (oldValuesColumns == null) {
			if (other.oldValuesColumns != null)
				return false;
		} else if (!oldValuesColumns.equals(other.oldValuesColumns))
			return false;
		return true;
	}
	
	
	
	
	
   
}
