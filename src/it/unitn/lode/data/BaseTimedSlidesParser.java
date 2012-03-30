package it.unitn.lode.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class BaseTimedSlidesParser implements TimedSlidesParser {
    // names of the XML tags
    static final String SLIDE = "slide";
    static final String TIME = "tempo";
    static final String TITLE = "titolo";
    static final String IMAGE = "immagine";
    static final String TIMEDSLIDES = "TIMED_SLIDES";
    
    final URL lectureUrl;

    protected BaseTimedSlidesParser(String lectureUrl){
        try {
            //this.courseUrl = new URL(courseUrl + "/TIMED_SLIDES.XML");
        	this.lectureUrl = new URL("http://latemar.science.unitn.it/itunes/feeds/ScienzeMMFFNN/web_architectures/lecture2/TIMED_SLIDES.XML");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    protected InputStream getInputStream() {
        try {
            return lectureUrl.openConnection().getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
