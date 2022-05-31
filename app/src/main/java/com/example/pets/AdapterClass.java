package com.example.pets;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.datapackage.PetContract.PetEntry;

public class AdapterClass extends CursorAdapter {
    public AdapterClass(Context context , Cursor c) {
        super(context , c , 0);
    }

    @Override
    public View newView(Context context , Cursor cursor , ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view , Context context , Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.text1);
        TextView breed = (TextView) view.findViewById(R.id.text2);

        name.setText(cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME)));

        String breedText = cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED));
        if (!TextUtils.isEmpty(breedText))
            breed.setText(breedText);
        else
            breed.setText("Unknown");
    }
}
