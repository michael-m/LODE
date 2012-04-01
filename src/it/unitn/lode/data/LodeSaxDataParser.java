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
        Element course = root.getChild(COURSE);
        course.setEndElementListener(new EndElementListener(){
            public void end() {
                courses.add(currentCourse.copy());
            }
        });
        course.getChild(TITOLOC).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentCourse.setTitoloc(body);
            }
        });
        course.getChild(YEAR).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentCourse.setYear(body);
            }
        });
        course.getChild(DOCENTEC).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentCourse.setDocentec(body);
            }
        });
        course.getChild(FOLDERC).setEndTextElementListener(new EndTextElementListener(){
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
	@Override
	public List<Lectures> parseLectures() {
        final Lectures currentLecture = new Lectures();
        RootElement root = new RootElement(LECTURES);
        final List<Lectures> lectures = new ArrayList<Lectures>();
        Element lecture = root.getChild(LECTURE);
        lecture.setEndElementListener(new EndElementListener(){
            public void end() {
                lectures.add(currentLecture.copy());
            }
        });
        lecture.getChild(IDL).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentLecture.setIdl(body);
            }
        });
        lecture.getChild(URLLEZ).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentLecture.setUrllez(body);
            }
        });
        lecture.getChild(FOLDERL).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentLecture.setFolderl(body);
            }
        });
        lecture.getChild(DATEL).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentLecture.setDatel(body);
            }
        });
        lecture.getChild(DOCENTEL).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentLecture.setDocentel(body);
            }
        });
        lecture.getChild(TITOLOL).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentLecture.setTitolol(body);
            }
        });
        try {
            Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return lectures;
	}
}
