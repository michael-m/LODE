package it.unitn.lode.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class BaseDataParser implements DataParser {
    // names of the XML tags
    protected static final String SLIDE = "slide";
    protected static final String TIME = "tempo";
    protected static final String TITLE = "titolo";
    protected static final String IMAGE = "immagine";
    protected static final String TIMEDSLIDES = "TIMED_SLIDES";
    protected static final String TITOLOC = "TITOLOC";
    protected static final String YEAR = "YEAR";
    protected static final String DOCENTEC = "DOCENTEC";
    protected static final String FOLDERC = "FOLDERC";
    protected static final String COURSES = "COURSES";
    protected static final String COURSE = "COURSE";
    protected static final String LECTURES = "LECTURES";
    protected static final String LECTURE = "LECTURE";
    protected static final String IDL = "IDL";
    protected static final String URLLEZ = "URLLEZ";
    protected static final String FOLDERL = "FOLDERL";
    protected static final String DATEL = "DATEL";
    protected static final String DOCENTEL = "DOCENTEL";
    protected static final String TITOLOL = "TITOLOL";
    
    final URL url;

    protected BaseDataParser(String url){
        try {
        	this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    protected InputStream getInputStream() {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
