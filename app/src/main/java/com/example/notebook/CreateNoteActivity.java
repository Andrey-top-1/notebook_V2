package com.example.notebook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreateNoteActivity extends AppCompatActivity {

    // Элементы интерфейса
    ImageView imageView;
    EditText titleEditText;
    EditText contentEditText;
    ImageView saveButton;
    ImageView addImageBtn;
    ImageView saveAsPDFBtn;
    ImageView voiceBtn;
    ImageView deleteNoteBtn;

    // База данных для взаимодействия с данными
    SQLiteDatabase db;

    // ID заметки (для редактирования)
    int id = -1;
    // Действие (создание или редактирование заметки)
    String action;

    // Код запроса для выбора изображения
    private static final int IMAGE_PICK_CODE = 1000;

    // Uri для выбранного изображения
    private Uri imageUri = null;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        // Инициализируем элементы интерфейса
        imageView = findViewById(R.id.imageView);
        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        saveButton = findViewById(R.id.saveButton);
        addImageBtn = findViewById(R.id.addImageButton);
        saveAsPDFBtn = findViewById(R.id.saveAsPDFBtn);
        //voiceBtn = findViewById(R.id.voiceBtn);
        deleteNoteBtn = findViewById(R.id.deleteButton);

        // Получаем доступ к базе данных
        db = new DatabaseHelper(this).getWritableDatabase();

        // Получаем ID и действие из Intent
        id = getIntent().getIntExtra("id", -1);
        action = getIntent().getStringExtra("action");

        // Если действие - редактирование, загружаем данные из базы
        if (action.equals("edit")) {
            loadNote();
        }

        // Настраиваем кнопку сохранения заметки
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });

        // Настраиваем кнопку добавления изображения
        addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Вызываем Intent для выбора изображения из галереи или камеры
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_PICK_CODE);
            }
        });

        // Настраиваем кнопку сохранения в PDF
        saveAsPDFBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNoteAsPDF();
            }

            // Метод для сохранения заметки в PDF
            private void saveNoteAsPDF() {
                String title = titleEditText.getText().toString();
                String note_text = contentEditText.getText().toString();
                ImageView imageView = findViewById(R.id.imageView);
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                try {
                    // Получаем директорию "Documents" на внешнем хранилище
                    File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

                    // Создаем имя файла
                    String filename = title + ".pdf";
                    File pdfFile = new File(documentsDir, filename);

                    // Создаем новый PDF документ
                    Document document = new Document();

                    // Создаем PDF writer
                    PdfWriter.getInstance(document, new FileOutputStream(pdfFile));

                    // Открываем документ
                    document.open();

                    // Добавляем текст
                    document.add(new Paragraph(note_text));

                    // Добавляем изображение
                    if (bitmap != null) {
                        // Создаем поток байтов для сохранения изображения в формате PNG
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        // Сжимаем изображение в формат PNG с максимальным качеством (100)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        // Получаем массив байтов, представляющий изображение
                        byte[] imageBytes = stream.toByteArray();
                        // Создаем объект Image из массива байтов
                        Image image = Image.getInstance(imageBytes);
                        // Масштабируем изображение, чтобы оно соответствовало размеру 250x250 пикселей
                        image.scaleToFit(250, 250);
                        // Выравниваем изображение по правому краю
                        image.setAlignment(Image.ALIGN_RIGHT);
                        // Устанавливаем отступ справа в 20 пунктов
                        image.setIndentationRight(20);
                        // Добавляем изображение в документ
                        document.add(image);
                    }

                    // Закрываем документ
                    document.close();

                    Toast.makeText(CreateNoteActivity.this, "PDF созданно", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(CreateNoteActivity.this, "PDF не созданно", Toast.LENGTH_SHORT).show();
                } catch (DocumentException e) {
                    e.printStackTrace();
                    Toast.makeText(CreateNoteActivity.this, "PDF не созданно", Toast.LENGTH_SHORT).show();
                }
            }
        });


/*/ Инициализируем кнопку "Voice"
        voiceBtn = findViewById(R.id.voiceBtn); //  Инициализируем кнопку

        // Запрашиваем разрешение на использование микрофона
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        // Инициализируем SpeechRecognizer и Intent
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // Удаляем "void"
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        // Настраиваем обработчик клика для кнопки "Voice"
        voiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Запускаем прослушивание речи
                speechRecognizer.startListening(speechRecognizerIntent);
                Toast.makeText(CreateNoteActivity.this, "Говорите...", Toast.LENGTH_SHORT).show();
            }
        });

        // Устанавливаем обработчик событий для SpeechRecognizer
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            // ... (остальной код RecognitionListener)
        });
    }*/


        // Настраиваем кнопку удаления заметки
        deleteNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Удаляете заметку из базы данных
                db.execSQL("delete from Note where id = " + id);
                // Возвращаетесь на экран списка заметок
                Intent intent = new Intent(CreateNoteActivity.this, SpisokActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Метод для загрузки данных заметки из базы
    @SuppressLint("Range")
    private void loadNote() {
        Cursor cursor = db.rawQuery("select title, note_text, image from Note where id = " + id, null);
        if (cursor.moveToFirst()) {
            titleEditText.setText(cursor.getString(cursor.getColumnIndex("title")));
            contentEditText.setText(cursor.getString(cursor.getColumnIndex("note_text")));
            byte[] photo = cursor.getBlob(cursor.getColumnIndex("image"));
            if (photo != null)
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0, photo.length));
        }
    }

    // Метод для сохранения заметки
    private void saveNote() {
        String title = titleEditText.getText().toString();
        String note_text = contentEditText.getText().toString();

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageInByte = baos.toByteArray();

        // Если действие - создание новой заметки
        if (action.equals("create")) {
            // Вставляем новую заметку в базу данных
            //db.execSQL("insert into Note (title, note_text, image) values (" + title + "," + note_text + ", " + imageInByte + ")");

            db.execSQL("insert into Note (title, note_text, image) VALUES (?, ?, ?)", new Object[] {title, note_text, imageInByte});

            Toast.makeText(this, "Заметка созданна", Toast.LENGTH_SHORT).show();
        }
        // Если действие - редактирование существующей заметки
        else if (action.equals("edit")) {
            // Обновляем данные заметки в базе данных
            //db.execSQL("update Note set title = '" + title + "', note_text = '" + note_text + "', image = '" + imageInByte + "' where id = " + id);
            db.execSQL("UPDATE Note SET title = ?, note_text = ?, image = ? WHERE id = ?", new Object[] {title, note_text, imageInByte, id});
            Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
        }

        // Возвращаемся на экран списка заметок
        Intent intent = new Intent(this, SpisokActivity.class);
        startActivity(intent);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUri);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Закрываем подключение к базе данных
        db.close();
    }

}