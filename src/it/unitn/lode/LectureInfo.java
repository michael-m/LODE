package it.unitn.lode;

import android.widget.ImageButton;
import android.widget.TextView;

public class LectureInfo {
	private TextView tvLectureInfo;
	private ImageButton btnWatch;
	private ImageButton btnDownload;
	private String videoUrl;
	private String lectureDataUrl;

	public LectureInfo(TextView tvLectureInfo, ImageButton btnWatch, ImageButton btnDownload, String videoUrl, 
			String lectureDataUrl){
		this.tvLectureInfo = tvLectureInfo;
		this.btnWatch = btnWatch;
		this.btnDownload = btnDownload;
		this.videoUrl = videoUrl;
		this.lectureDataUrl = lectureDataUrl;
	}

	public TextView getTvLectureInfo() {
		return tvLectureInfo;
	}

	public void setTvLectureInfo(TextView tvLectureInfo) {
		this.tvLectureInfo = tvLectureInfo;
	}

	public ImageButton getBtnWatch() {
		return btnWatch;
	}

	public void setBtnWatch(ImageButton btnWatch) {
		this.btnWatch = btnWatch;
	}

	public ImageButton getBtnDownload() {
		return btnDownload;
	}

	public void setBtnDownload(ImageButton btnDownload) {
		this.btnDownload = btnDownload;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getLectureDataUrl() {
		return lectureDataUrl;
	}

	public void setLectureDataUrl(String lectureDataUrl) {
		this.lectureDataUrl = lectureDataUrl;
	}
}
