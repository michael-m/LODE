package it.unitn.lode.data;

import java.util.List;

public interface DataParser {
	List<TimedSlides> parseSlides();
	List<Courses> parseCourses();
	List<Lectures> parseLectures();
}
