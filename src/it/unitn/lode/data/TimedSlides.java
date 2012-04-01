package it.unitn.lode.data;

public class TimedSlides{
	private int tempo;
    private String titolo;
    private String immagine;

    public int getTempo() {
		return tempo;
	}
	public void setTempo(int tempo) {
		this.tempo = tempo;
	}
	public String getTitolo() {
		return titolo;
	}
	public void setTitolo(String titolo) {
		this.titolo = titolo;
	}
	public String getImmagine() {
		return immagine;
	}
	public void setImmagine(String immagine) {
		this.immagine = immagine;
	}
	public TimedSlides copy() {
		TimedSlides ts = new TimedSlides();
		ts.immagine = getImmagine();
		ts.tempo = getTempo();
		ts.titolo = getTitolo();
		return ts;
	}
}