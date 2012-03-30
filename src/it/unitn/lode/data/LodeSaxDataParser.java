package it.unitn.lode.data;

import java.util.ArrayList;
import java.util.List;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

public class LodeSaxDataParser extends BaseTimedSlidesParser {

	public LodeSaxDataParser(String lectureUrl) {
		super(lectureUrl);
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

}
