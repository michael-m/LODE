package it.unitn.lode.data;

import java.util.ArrayList;
import java.util.List;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

public class LodeSaxDataParser extends BaseDataParser {

	public LodeSaxDataParser(String url) {
		super(url);
	}
	@Override
	public List<TimedSlides> parseSlides() {
        final TimedSlides currentSlide = new TimedSlides();
        RootElement root = new RootElement(TIMEDSLIDES);
        final List<TimedSlides> timedSlides = new ArrayList<TimedSlides>();
        Element slide = root.getChild(SLIDE);
        slide.setEndElementListener(new EndElementListener(){
            public void end() {
                timedSlides.add(currentSlide.copy());
            }
        });
        slide.getChild(TIME).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentSlide.setTempo(Integer.valueOf(body));
            }
        });
        slide.getChild(TITLE).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentSlide.setTitolo(body);
            }
        });
        slide.getChild(IMAGE).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentSlide.setImmagine(body);
            }
        });
        try {
            Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return timedSlides;
	}
	@Override
	public List<Courses> parseCourses() {
        final Courses currentCourse = new Courses();
        RootElement root = new RootElement(COURSES);
        final List<Courses> courses = new ArrayList<Courses>();
        Element slide = root.getChild(COURSE);
        slide.setEndElementListener(new EndElementListener(){
            public void end() {
                courses.add(currentCourse.copy());
            }
        });
        slide.getChild(TITOLOC).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentCourse.setTitoloc(body);
            }
        });
        slide.getChild(YEAR).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentCourse.setYear(body);
            }
        });
        slide.getChild(DOCENTEC).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentCourse.setDocentec(body);
            }
        });
        slide.getChild(FOLDERC).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentCourse.setFolderc(body);
            }
        });
        try {
            Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return courses;
	}
}
