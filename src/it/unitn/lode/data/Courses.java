package it.unitn.lode.data;

public class Courses{
	private String titoloc;
    private String year;
    private String docentec;
    private String folderc;

    public String getTitoloc() {
		return titoloc;
	}
	public void setTitoloc(String titoloc) {
		this.titoloc = titoloc;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getDocentec() {
		return docentec;
	}
	public void setDocentec(String docentec) {
		this.docentec = docentec;
	}
	public String getFolderc() {
		return folderc;
	}
	public void setFolderc(String folderc) {
		this.folderc = folderc;
	}
	public Courses copy() {
		Courses courses = new Courses();
		courses.titoloc = getTitoloc();
		courses.year = getYear();
		courses.docentec = getDocentec();
		courses.folderc = getFolderc();
		return courses;
	}

}