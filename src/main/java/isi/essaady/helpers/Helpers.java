package isi.essaady.helpers;

import java.util.Calendar;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class Helpers {
	
	public Helpers() {}
	
	/**
     * Displays the Faces message. This is just a helper method
     * 
     * @param severity	The FacesMessage sevirity constant.
     * @param summary	Message summary.
     * @param detail	Message detail.
     */
    public static void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesMessage message = new FacesMessage(severity, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    
    /**
     * Gets the default starting date.
     * Used to initialize a starting date.
     * 
     * @param date	The starting date
     * @return Date  The default starting date
     */
    public static Date getDefaultStartDate(Date date) {
    	Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, Constants.AM_START, 0, 0);
        return calendar.getTime();
    }
    
    
    /**
     * Gets the default ending date.
     * Used to initialize an ending date.
     * 
     * @param date	The ending date
     * @return Date  The default ending date
     */
    public static Date getDefaultEndDate(Date date) {
    	Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, Constants.PM_END, 0, 0);
        return calendar.getTime();
    }
    
    
    /**
     * Determines the valide days between a range of dates.
     * Valid days are Monday through Friday.
     * 
     * @param startDate  The starting date
     * @param endDate  The ending date
     * @return Integer  The number of the valide dates of the given range 
     */
    public static int countValidDaysBetween(Date startDate, Date endDate) {
    	Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(startDate);
        cal2.setTime(endDate);

        int numberOfValidDays = 0;
        while (cal1.before(cal2)) {
            if ((Calendar.SATURDAY != cal1.get(Calendar.DAY_OF_WEEK))
               &&(Calendar.SUNDAY != cal1.get(Calendar.DAY_OF_WEEK))) {
            	numberOfValidDays++;
            }
            cal1.add(Calendar.DATE,1);
        }
        
        return numberOfValidDays;
    }
    
    
    /**
     * Calculates the number of designated hours of a starting date.
     * Default Designated hours are : 08H-12H and 14H-18H
     * For example : designated hours of 12/06/20 15h:00 is 3hrs.
     * 
     * @param date  The starting date
     * @return Integer  The designated hours of the given date 
     */
    public static int calcDesignatedHrsStart(Date date) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	
    	int designatedHrs = 0;
    	int date_hour = calendar.get(Calendar.HOUR_OF_DAY);
    	
    	
    	if(date_hour >= Constants.AM_START && date_hour <= Constants.AM_END) {
    		designatedHrs = (Constants.AM_END - date_hour)
    							+ (Constants.PM_END - Constants.PM_START);
    	}
    	else if(date_hour >= Constants.PM_START && date_hour <= Constants.PM_END) {
    		designatedHrs = (Constants.PM_END - date_hour);
    	}
    	else if (date_hour > Constants.AM_END && date_hour < Constants.PM_START) {
    		designatedHrs = Constants.PM_END - Constants.PM_START;
    	}
    	
    	return designatedHrs;
    }
    
    
    /**
     * Calculates the number of designated hours of an ending date.
     * Default Designated hours are : 08H-12H and 14H-18H
     * For example : designated hours of 12/06/20 15h:00 is 5hrs.
     * 
     * @param date  The starting date
     * @return Integer  The designated hours of the given date 
     */
    public static int calcDesignatedHrsEnd(Date date) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	
    	int designatedHrs = 0;
    	int date_hour = calendar.get(Calendar.HOUR_OF_DAY);
    	
    	if(date_hour >= Constants.AM_START && date_hour <= Constants.AM_END) {
    		designatedHrs = date_hour - Constants.AM_START;				
    	}
    	else if(date_hour >= Constants.PM_START && date_hour <= Constants.PM_END) {
    		designatedHrs = (date_hour - Constants.PM_START)
					+ (Constants.AM_END - Constants.AM_START);
    	}
    	else if (date_hour > Constants.AM_END && date_hour < Constants.PM_START) {
    		designatedHrs = Constants.AM_END - Constants.AM_START;
    	}
    	else {
    		designatedHrs = Constants.DESIGNATED_HOURS;
    	}
    	
    	return designatedHrs;
    }
    
    
    /**
     * Determines if two days correspond to a same day.
     * 
     * @param date1  The first day
     * @param date2  The second day
     * @return boolean  True if the given dates are the same day
     */
    public static boolean isSameDay(Date date1, Date date2) {
    	Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        		&& cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);      
    }
    
    
    /**
     * Calculates the maximum duration between two dates.
     * For example : The maximum duration between Friday 12/06/20 15:00 and
     * Monday 15/06/20 18:00 is 11hrs.
     * 
     * @param date1  The first day
     * @param date2  The second day
     * @return boolean  True if the given dates are the same day
     */
    public static int calcMaxAllowedDuration(Date startDate, Date endDate) {
    	if(startDate.after(endDate)) return -1;

    	if(isSameDay(startDate,endDate)) {
    		return calcDesignatedHrsStart(startDate) - calcDesignatedHrsStart(endDate);
    	}
    	
    	return calcDesignatedHrsStart(startDate) + calcDesignatedHrsEnd(endDate)
					+ (countValidDaysBetween(startDate, endDate) - 2)
						* Constants.DESIGNATED_HOURS;
    }

}
