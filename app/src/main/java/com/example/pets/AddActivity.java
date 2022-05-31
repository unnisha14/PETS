package com.example.pets;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.android.pets.datapackage.PetContract.PetEntry;

import datapackage.PetDatabase;
import datapackage.PetProvider;

public class AddActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private EditText nameText;
    private EditText breedText;
    private EditText weightText;
    private Spinner genderSpinner;
    private int genderInt = 0;
    private Uri PetUri;
    private int id;

    private boolean PetChange = false;
    View.OnTouchListener onTouchListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View v , MotionEvent event) {
            PetChange = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);

        nameText = (EditText) findViewById(R.id.name);
        breedText = (EditText) findViewById(R.id.breed);
        weightText = (EditText) findViewById(R.id.weight);
        genderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        Intent intent = getIntent();
        PetUri = intent.getData();

        if (PetUri == null){
            setTitle("Add a pet");
            invalidateOptionsMenu();
        }
        else{
            setTitle("Edit pet");
            id = Integer.parseInt(String.valueOf(ContentUris.parseId(PetUri)));
            getSupportLoaderManager().initLoader(0,null,this);
        }

        setSpinner();

        nameText.setOnTouchListener(onTouchListener);
        breedText.setOnTouchListener(onTouchListener);
        weightText.setOnTouchListener(onTouchListener);
        genderSpinner.setOnTouchListener(onTouchListener);
    }

    private void setSpinner() {
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_gender, android.R.layout.simple_spinner_item);
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        genderSpinner.setAdapter(genderSpinnerAdapter);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent , View view , int position , long id) {
                String selection = (String) parent.getItemAtPosition(position);

                switch (selection){
                    case "Male":
                        genderInt = PetEntry.GENDER_MALE;
                        break;
                    case "Female":
                        genderInt = PetEntry.GENDER_FEMALE;
                        break;
                    default:
                        genderInt = PetEntry.GENDER_UNKNOWN;
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                genderInt = 0;
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (PetUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                savePet();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmMessage();
                return true;
            case android.R.id.home:
                if (!PetChange){
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                else{
                    DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            NavUtils.navigateUpFromSameTask(AddActivity.this);
                        }
                    };
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePet() {
        String name = nameText.getText().toString().trim();
        String breed = breedText.getText().toString().trim();
        String weightString = weightText.getText().toString().trim();
        int weight = 0;

        if (PetUri == null && TextUtils.isEmpty(name) && TextUtils.isEmpty(breed) && TextUtils.isEmpty(weightString) && genderInt == PetEntry.GENDER_UNKNOWN){
            return;
        }

        if (!TextUtils.isEmpty(weightText.getText().toString().trim())){
            weight = Integer.parseInt(weightString);
        }

        ContentValues values = new ContentValues();

        values.put(PetEntry.COLUMN_PET_NAME,name);
        values.put(PetEntry.COLUMN_PET_BREED,breed);
        values.put(PetEntry.COLUMN_PET_GENDER,genderInt);
        values.put(PetEntry.COLUMN_PET_WEIGHT,weight);

        //Toast.makeText(this , "Gender = " + genderInt , Toast.LENGTH_SHORT).show();

        if (PetUri != null){
            int rowsAffected = getContentResolver().update(PetUri,values,null,null);
            if (rowsAffected == 0) {
                Toast.makeText(this,"Update unsuccessful",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,"Update successful", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI,values);
            if (uri == null) {
                Toast.makeText(this, "Insert failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Insert successful", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Loader<Cursor> onCreateLoader(int id ,Bundle args) {
        String[] projection = new String[]{
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        return new CursorLoader(getApplicationContext(),PetUri,projection, null,null,null);
    }

    public void onLoadFinished(Loader<Cursor> loader , Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1){
            return;
        }

        if (cursor.moveToFirst()){

            String name = cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME));
            String breed = cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED));
            int gender = cursor.getInt(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_GENDER));
            int weight = cursor.getInt(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_WEIGHT));

            nameText.setText(name);
            breedText.setText(breed);
            weightText.setText(Integer.toString(weight));

            switch (gender){
                case PetEntry.GENDER_FEMALE:
                    genderSpinner.setSelection(2);
                    break;
                case PetEntry.GENDER_MALE:
                    genderSpinner.setSelection(1);
                    break;
                default:
                    genderSpinner.setSelection(0);
                    break;
            }
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        nameText.setText("");
        breedText.setText("");
        genderSpinner.setSelection(0);
        weightText.setText("");
    }

    @Override
    public void onBackPressed() {
        if (!PetChange){
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog , int which) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard changes and quit editing");
        builder.setPositiveButton("Discard",discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog , int which) {
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this pet");

        builder.setPositiveButton("Delete",new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog , int which) {
                deletePet();
            }
        });

        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog , int which) {
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet() {
        if (PetUri != null){
            int rowsAffected = getContentResolver().delete(PetUri,null,null);

            if (rowsAffected == 0){
                Toast.makeText(this,"Delete unsuccessful",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this,"Delete successful",Toast.LENGTH_SHORT).show();}
        }

        finish();
    }
}
