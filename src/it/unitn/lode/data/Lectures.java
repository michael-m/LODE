package it.unitn.lode.data;

public class Lectures{
	private String idl;
    private String urllez;
    private String folderl;
    private String datel;
    private String docentel;
    private String titolol;

    public String getIdl() {
		return idl;
	}
	public void setIdl(String idl) {
		this.idl = idl;
	}
	public String getUrllez() {
		return urllez;
	}
	public void setUrllez(String urllez) {
		this.urllez = urllez;
	}
	public String getFolderl() {
		return folderl;
	}
	public void setFolderl(String folderl) {
		this.folderl = folderl;
	}
	public String getDatel() {
		return datel;
	}
	public void setDatel(String datel) {
		this.datel = datel;
	}
	public String getDocentel() {
		return docentel;
	}
	public void setDocentel(String docentel) {
		this.docentel = docentel;
	}
	public String getTitolol() {
		return titolol;
	}
	public void setTitolol(String titolol) {
		this.titolol = titolol;
	}
	public Lectures copy() {
		Lectures lectures = new Lectures();
		lectures.idl = getIdl();
		lectures.urllez = getUrllez();
		lectures.folderl = getFolderl();
		lectures.datel = getDatel();
		lectures.docentel = getDocentel();
		lectures.titolol = getTitolol();
		return lectures;
	}
}