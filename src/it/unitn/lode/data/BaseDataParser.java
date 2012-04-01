package it.unitn.lode.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class BaseDataParser implements DataParser {
    // names of the XML tags
    static final String SLIDE = "slide";
    static final String TIME = "tempo";
    static final String TITLE = "titolo";
    static final String IMAGE = "immagine";
    static final String TIMEDSLIDES = "TIMED_SLIDES";
    static final String TITOLOC = "TITOLOC";
    static final String YEAR = "YEAR";
    static final String DOCENTEC = "DOCENTEC";
    static final String FOLDERC = "FOLDERC";
    static final String COURSES = "COURSES";
    static final String COURSE = "COURSE";
    
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
