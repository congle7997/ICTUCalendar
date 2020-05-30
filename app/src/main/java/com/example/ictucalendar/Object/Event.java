package com.example.ictucalendar.Object;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "tb_event")
public class Event extends Model {
    @Column(name = "time")
    private String time;
    @Column(name = "date")
    private String date;
    @Column(name = "subject_name")
    private String subjectName;
    @Column(name = "place")
    private String place;
    @Column(name = "lecturer")
    private String lecturer;
    @Column(name = "class_id")
    private String classID;

    @Column(name = "type")
    private String type;
    @Column(name = "content_note")
    private String contentNote;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getLecturer() {
        return lecturer;
    }

    public void setLecturer(String lecturer) {
        this.lecturer = lecturer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContentNote() {
        return contentNote;
    }

    public void setContentNote(String contentNote) {
        this.contentNote = contentNote;
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    @Override
    public String toString() {
        return "Type: " + type + " - " + "Time: " + time;
    }
}
