package com.example.notebook;

import android.graphics.Bitmap;

public class Note {

    public int id;
    public String title;
    public String note_text;
    public Bitmap image;

    public Note(int id, String title) {
        this.id = id;
        this.title = title;
    }
    public Note(int id, String title, String note_text, Bitmap image) {
        this.id = id;
        this.title = title;
        this.note_text = note_text;
        this.image = image;
    }
}
