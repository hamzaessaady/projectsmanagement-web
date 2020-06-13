package isi.essaady.dashboard.projs;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.model.timeline.TimelineEvent;
import org.primefaces.model.timeline.TimelineModel;

@Named("customTimelineView")
@ViewScoped
public class CustomTimelineView implements Serializable {  
   
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TimelineModel<String, ?> model;
    private LocalDateTime start;
    private LocalDateTime end;
   
    @PostConstruct 
    public void init() {  
        // set initial start / end dates for the axis of the timeline  
        start = LocalDateTime.now().minusHours(4);
        end = LocalDateTime.now().plusHours(8);
 
        // groups  
        String[] NAMES = new String[] {"User 1", "User 2", "User 3", "User 4", "User 5", "User 6"};  
   
        // create timeline model  
        model = new TimelineModel<>();
   
        for (String name : NAMES) {
            LocalDateTime end = LocalDateTime.now().minusHours(12).withMinute(0).withSecond(0).withNano(0);
 
            for (int i = 0; i < 5; i++) {
                LocalDateTime start = end.plusHours(Math.round(Math.random() *5));
                end = start.plusHours(4 + Math.round(Math.random() *5));
 
                long r = Math.round(Math.random() * 2);  
                String availability = (r == 0 ? "Unavailable" : (r == 1 ? "Available" : "Maybe"));  
   
                // create an event with content, start / end dates, editable flag, group name and custom style class
                TimelineEvent event = TimelineEvent.builder()
                        .data(availability)
                        .startDate(start)
                        .endDate(end)
                        .editable(true)
                        .group(name)
                        .styleClass(availability.toLowerCase())
                        .build();
 
                model.add(event);
            }  
        }  
    }  
   
    public TimelineModel<String, ?> getModel() {
        return model;  
    }  
   
    public LocalDateTime getStart() {
        return start;  
    }  
   
    public LocalDateTime getEnd() {
        return end;  
    }  
}