package com.example.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.android.pets.datapackage.PetContract.PetEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int PET_LOADER = 0;
    private AdapterClass adapterClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                intent.setData(null);
                startActivity(intent);
            }
        });

        View emptyView = findViewById(R.id.empty_view);
        ListView listView = (ListView) findViewById(R.id.pets);
        listView.setEmptyView(emptyView);

        adapterClass = new AdapterClass(this,null);
        listView.setAdapter(adapterClass);

        getSupportLoaderManager().initLoader(PET_LOADER,null,this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            public void onItemClick(AdapterView<?> parent , View view , int position , long id) {
                Intent intent = new Intent(MainActivity.this,AddActivity.class);
                Uri currentUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI,id);
                intent.setData(currentUri);
                startActivity(intent);
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            case R.id.action_delete_all_entries:
                deletePet();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deletePet() {
        int rowsDeleted = getApplicationContext().getContentResolver().delete(PetEntry.CONTENT_URI,null,null);

        Toast.makeText(this,"Rows Deleted = " + rowsDeleted,Toast.LENGTH_SHORT).show();
    }

    private void insertPet() {

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME,"Toto");
        values.put(PetEntry.COLUMN_PET_BREED,"Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT,7);

        Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI,values);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public Loader<Cursor> onCreateLoader(int id ,Bundle args) {
        String[] projection = new String[]{
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED
        };

        return new CursorLoader(this, PetEntry.CONTENT_URI, projection, null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader , Cursor data) {
        adapterClass.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        adapterClass.swapCursor(null);
    }
}