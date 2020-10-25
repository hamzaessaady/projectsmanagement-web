package isi.essaady.dashboard;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import isi.essaady.ejbs.CollaboratorBean;
import isi.essaady.ejbs.CompetenceBean;
import isi.essaady.ejbs.ProjectBean;
import isi.essaady.entities.Collaborator;
import isi.essaady.entities.Competence;
import isi.essaady.entities.Project;

@Named
@RequestScoped
public class DashboardBacking implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<Project> projects;
	private List<Collaborator> collabs;
	private List<Competence> comps;
     
    @EJB
    private ProjectBean projectBean;
    @EJB
    private CollaboratorBean collabBean;
    @EJB
    private CompetenceBean compBean;
     
     
    @PostConstruct
    public void init() {
        projects = projectBean.getAllProjects();
        collabs = collabBean.getAllCollabs();
        comps = compBean.getAllComps();
    }
    
    
    /*
     * GETTERS - SETTERS
     * */
    public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

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
	
	
	
}
