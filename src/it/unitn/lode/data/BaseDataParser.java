package it.unitn.lode.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import org.xml.sax.InputSource;
import android.util.Xml;

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
    
    URL url = null;
    String sUrl = null;
    protected BaseDataParser(URL url){
       	this.url = url;
    }
    protected BaseDataParser(String sUrl){
    	this.sUrl = sUrl;
    }
    protected InputStream getInputStream() {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    protected InputSource getLocalStream() {
    	try {
			InputSource localSource = new InputSource(new InputStreamReader(new FileInputStream(sUrl)));
			localSource.setEncoding(Xml.Encoding.UTF_8.toString());
			return localSource;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
            throw new RuntimeException(e);
		}
    }
}
