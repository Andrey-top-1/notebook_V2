package com.example.notebook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SpisokActivity extends AppCompatActivity {

    // Объявляем элементы интерфейса
    ListView userList;
    EditText searchEditText;
    // База данных для взаимодействия с данными
    SQLiteDatabase db;
    // Курсор для получения данных из базы
    Cursor userCursor;
    // Адаптер для отображения данных в ListView
    NoteAdapter noteAdapter;

    // Список заметок, которые будут отображаться
    ArrayList<Note> notes;
    ArrayList<Note> filteredNotes; // Список для фильтрованных заметок

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spisok);

        // Инициализируем элементы интерфейса
        userList = findViewById(R.id.notesListView);
        searchEditText = findViewById(R.id.searchEditText);

        // Настраиваем слушатель для клика по записям в ListView
        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Создаем Intent для запуска CreateNoteActivity
                Intent intent = new Intent(SpisokActivity.this, CreateNoteActivity.class);
                // Передаем id записи и действие (edit) в Intent
                intent.putExtra("id", filteredNotes.get(position).id); // Используем filteredNotes
                intent.putExtra("action", "edit");
                // Запускаем CreateNoteActivity
                startActivity(intent);
            }
        });

        // Получаем доступ к базе данных
        db = new DatabaseHelper(this).getReadableDatabase();

        // Настраиваем кнопку создания заметки
        findViewById(R.id.addNoteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Создаем Intent для запуска CreateNoteActivity
                Intent intent = new Intent(SpisokActivity.this, CreateNoteActivity.class);
                // Передаем действие (create) в Intent
                intent.putExtra("action", "create");
                // Запускаем CreateNoteActivity
                startActivity(intent);
            }
        });

        // Настраиваем слушатель для ввода текста в поле поиска
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Не используется
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Не используется
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Фильтруем список заметок
                filterNotes(s.toString());
            }
        });
    }

    @SuppressLint("Range")
    @Override
    public void onResume() {
        super.onResume();

        // Выполняем запрос для получения всех записей из таблицы Note
        userCursor = db.rawQuery("select id, title from Note", null);
        // Инициализируем списки заметок
        notes = new ArrayList<>();
        filteredNotes = new ArrayList<>();

        // Проверяем, есть ли записи в базе данных
        if (userCursor.getCount() != 0) {
            // Устанавливаем курсор на первую запись
            userCursor.moveToFirst();
            // Проходим по всем записям и добавляем их в список
            do {
                notes.add(new Note(
                        userCursor.getInt(userCursor.getColumnIndex("id")),
                        userCursor.getString(userCursor.getColumnIndex("title"))
                ));
                filteredNotes.add(new Note(
                        userCursor.getInt(userCursor.getColumnIndex("id")),
                        userCursor.getString(userCursor.getColumnIndex("title"))
                ));
            } while (userCursor.moveToNext());
        }

        // Закрываем курсор
        userCursor.close();

        // Создаем адаптер для отображения данных в ListView
        noteAdapter = new NoteAdapter(this, filteredNotes); // Используем filteredNotes
        // Устанавливаем адаптер для ListView
        userList.setAdapter(noteAdapter);
    }

    private void filterNotes(String text) {
        // Очищаем список фильтрованных заметок
        filteredNotes.clear();

        // Если поле поиска пустое, отображаем все заметки
        if (text.isEmpty()) {
            filteredNotes.addAll(notes);
        } else {
            // Фильтруем записи по названию
            for (Note note : notes) {
                if (note.title.toLowerCase().contains(text.toLowerCase())) {
                    filteredNotes.add(note);
                }
            }
        }

        // Обновляем адаптер, чтобы отобразить изменения
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Закрываем подключение к базе данных
        db.close();
    }
}