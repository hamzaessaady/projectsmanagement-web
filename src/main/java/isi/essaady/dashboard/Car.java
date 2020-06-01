package isi.essaady.dashboard;
public class Car{
    	private String name;
    	private String last;
    	private int tasks;
    	private int skills;
		public String getName() {
			return name;
		}
		public String getLast() {
			return last;
		}
		public int getTasks() {
			return tasks;
		}
		public int getSkills() {
			return skills;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setLast(String last) {
			this.last = last;
		}
		public void setTasks(int tasks) {
			this.tasks = tasks;
		}
		public void setSkills(int skills) {
			this.skills = skills;
		}
		public Car(String name, String last, int tasks, int skills) {
			super();
			this.name = name;
			this.last = last;
			this.tasks = tasks;
			this.skills = skills;
		}
    	
    	
    }