package com.example.user.bookapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText titleInput, publisherInput,ratingInput, titleInputFind, publisherInputFind,titleInputUpdate, publisherInputUpdate, ratingInputUpdate,titleInputDelete, publisherInputDelete;
    TextView worstBook1Title, worstBook1Publisher, worstBook1Rating, worstBook2Title, worstBook2Publisher, worstBook2Rating, worstBook3Title, worstBook3Publisher, worstBook3Rating;
    TextView bestBook1Title, bestBook1Publisher, bestBook1Rating, bestBook2Title, bestBook2Publisher, bestBook2Rating, bestBook3Title, bestBook3Publisher, bestBook3Rating;
    TextView[] worstBook1Array, worstBook2Array, worstBook3Array;
    TextView[] bestBook1Array, bestBook2Array, bestBook3Array;
    List<TextView[]> worstBookTable = new ArrayList<>();
    List<TextView[]> bestBookTable = new ArrayList<>();;
    Button entryButton, findButton, updateButton;

    String titleGivenStr, publisherGivenStr,ratingGivenStr;
    boolean IsTitleGiven, isPublisherGiven,IsRatingGiven = true;

    boolean[] IsEverythingEditted;
    EditText[] textInputs;

    DatabaseHelper dbHelper;
    String targetingTitle="", targetingPublisher="";
    double ratingGivenParsed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialization();
    }

    //Core function of adding a new book ( database entry ).
    public void OnNewEntry(View view){
        //First we are checking if our fields are populated ( not empty ).
        if(isEmpty(titleInput)){
            Toast.makeText(this, R.string.enter_name, Toast.LENGTH_LONG).show();
            return;
        }
        titleGivenStr = titleInput.getText().toString();

        publisherGivenStr = publisherInput.getText().toString();

        ratingGivenStr = ratingInput.getText().toString();

        try {
            ratingGivenParsed = Double.parseDouble(ratingGivenStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(this,R.string.fill_blanks, Toast.LENGTH_LONG).show();
            return;
        }

        for(int i=0; i < textInputs.length;i++){
            if(!isEmpty(textInputs[i])){
                IsEverythingEditted[i] = true;
            }
            else
            {
                IsEverythingEditted[i] = false;
            }
        }

        if(IsEverythingEditted[0] && IsEverythingEditted[1] && IsEverythingEditted[2]) {
            if(ratingGivenParsed > 5.0 || ratingGivenParsed < 1.0){
                Toast.makeText(this,R.string.valid_rating,Toast.LENGTH_LONG).show();
                return;
            }
        }
        else {
            Toast.makeText(this,R.string.edit_fields,Toast.LENGTH_LONG).show();
            return;
        }

        //At this point, every field that the user edited is correct so we proceed with the database insertion.

        int rows = dbHelper.getRows();
        dbHelper.InsertEntryToDb(titleGivenStr , publisherGivenStr , ratingGivenParsed);
        if(dbHelper.getRows() == rows) {
            Toast.makeText(this, R.string.already_entered, Toast.LENGTH_LONG).show();
        }
        PopulateTables();

        ClearEditText(titleInput);
        ClearEditText(publisherInput);
        ClearEditText(ratingInput);
    }

    //Helper fuction to help the core method UpdateBook.
    //This method finds the database entry that the user wants to update by checking
    //the primary key ( combination of title and publisher ).
    //If there is not such a book, the method doesn't allow you to update a book
    //that doesn't exist.
    public void FindBook(View view) {
        if(isEmpty(titleInputFind) || isEmpty(publisherInputFind)){
            Toast.makeText(this,R.string.enter_info, Toast.LENGTH_LONG).show();
            return;
        }

        targetingTitle = titleInputFind.getText().toString();
        targetingPublisher = publisherInputFind.getText().toString();

        if(dbHelper.findBook(targetingTitle, targetingPublisher)) {

            titleInputFind.setEnabled(false);
            publisherInputFind.setEnabled(false);
            findButton.setEnabled(false);

            titleInputUpdate.setEnabled(true);
            publisherInputUpdate.setEnabled(true);
            ratingInputUpdate.setEnabled(true);
            updateButton.setEnabled(true);
        }
        else {
            Toast.makeText(this, R.string.nothing_deleted, Toast.LENGTH_SHORT).show();
        }

    }

    //Core fuction of updating a book ( database entry ).
    //First we need to make sure that the book the user entered is indeed inside our database.
    // ( that's why the fields here are grey. We accomplish that by first checking with the FindBook method).
    public void UpdateBook(View view) {
        if(isEmpty(titleInputUpdate)|| isEmpty(publisherInputUpdate) || isEmpty(ratingInputUpdate)){
            Toast.makeText(this, R.string.fill_blanks, Toast.LENGTH_LONG).show();
            return;
        }

        String titleGivenUpdate,publisherGivenUpdate;
        double ratingGivenParsed;

        titleGivenUpdate = titleInputUpdate.getText().toString();

        publisherGivenUpdate = publisherInputUpdate.getText().toString();

        ratingGivenParsed = Double.valueOf(ratingInputUpdate.getText().toString());
        if(ratingGivenParsed < 1.0 || ratingGivenParsed > 5.0) {
            Toast.makeText(this, R.string.valid_rating, Toast.LENGTH_LONG).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.FeedEntry.COLUMN_NAME_TITLE, titleGivenUpdate);
        values.put(DatabaseHelper.FeedEntry.COLUMN_NAME_PUBLISHER, publisherGivenUpdate);
        values.put(DatabaseHelper.FeedEntry.COLUMN_NAME_RATING, ratingGivenParsed);

        // Which row to update, based on the title
        String selection = DatabaseHelper.FeedEntry.COLUMN_NAME_TITLE + " LIKE ? AND " + DatabaseHelper.FeedEntry.COLUMN_NAME_PUBLISHER + " LIKE ?";
        String[] selectionArgs = { targetingTitle, targetingPublisher };

        int count = db.update(
                DatabaseHelper.FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        PopulateTables();

        titleInputFind.setEnabled(true);
        publisherInputFind.setEnabled(true);
        findButton.setEnabled(true);
        titleInputUpdate.setEnabled(false);
        publisherInputUpdate.setEnabled(false);
        ratingInputUpdate.setEnabled(false);
        updateButton.setEnabled(false);

        ClearEditText(titleInputUpdate);
        ClearEditText(publisherInputUpdate);
        ClearEditText(titleInputFind);
        ClearEditText(ratingInputUpdate);
        ClearEditText(publisherInputFind);

    }


    //Core function of deleting a book ( database entry ).
    public void DeleteBook(View view) {
        if(isEmpty(titleInputDelete) || isEmpty(publisherInputDelete)){
            Toast.makeText(this,R.string.enter_info, Toast.LENGTH_LONG).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String titleGivenDelete,publisherGivenDelete;

        titleGivenDelete = titleInputDelete.getText().toString();

        publisherGivenDelete = publisherInputDelete.getText().toString();

        // Define 'where' part of query.
        String publisherSelection = DatabaseHelper.FeedEntry.COLUMN_NAME_PUBLISHER + " LIKE ?";
        String titleSelection = DatabaseHelper.FeedEntry.COLUMN_NAME_TITLE + " LIKE ? AND " + publisherSelection;


        // Specify arguments in placeholder order.
        String[] selectionArgs = { titleGivenDelete , publisherGivenDelete
        };
        // Issue SQL statement.
        int deletedRows = db.delete(DatabaseHelper.FeedEntry.TABLE_NAME, titleSelection, selectionArgs);

        PopulateTables();

        ClearEditText(titleInputDelete);
        ClearEditText(publisherInputDelete);
    }


    //This method focus on the initialization of the fields.
    //We assign our DatabaseHelper object here so we can test if it
    //is the first time that our database is instantiated.
    void initialization(){
        titleInput = (EditText) findViewById(R.id.titleInput);
        publisherInput = (EditText) findViewById(R.id.publisherInput);
        ratingInput = (EditText) findViewById(R.id.ratingInput);

        entryButton = (Button) findViewById(R.id.newEntryButton);
        findButton = (Button) findViewById(R.id.findButton);
        updateButton = (Button) findViewById(R.id.updateButton);
        updateButton.setEnabled(false);

        worstBook1Title = (TextView) findViewById(R.id.worstBook1Title);
        worstBook1Publisher = (TextView) findViewById(R.id.worstBook1Publisher);
        worstBook1Rating = (TextView) findViewById(R.id.worstBook1Rating);
        worstBook2Title = (TextView) findViewById(R.id.worstBook2Title);
        worstBook2Publisher = (TextView) findViewById(R.id.worstBook2Publisher);
        worstBook2Rating = (TextView) findViewById(R.id.worstBook2Rating);
        worstBook3Title = (TextView) findViewById(R.id.worstBook3Title);
        worstBook3Publisher = (TextView) findViewById(R.id.worstBook3Publisher);
        worstBook3Rating = (TextView) findViewById(R.id.worstBook3Rating);
        bestBook1Title = (TextView) findViewById(R.id.bestBook1Title);
        bestBook1Publisher = (TextView) findViewById(R.id.bestBook1Publisher);
        bestBook1Rating = (TextView) findViewById(R.id.bestBook1Rating);
        bestBook2Title = (TextView) findViewById(R.id.bestBook2Title);
        bestBook2Publisher = (TextView) findViewById(R.id.bestBook2Publisher);
        bestBook2Rating = (TextView) findViewById(R.id.bestBook2Rating);
        bestBook3Title = (TextView) findViewById(R.id.bestBook3Title);
        bestBook3Publisher = (TextView) findViewById(R.id.bestBook3Publisher);
        bestBook3Rating = (TextView) findViewById(R.id.bestBook3Rating);

        titleInputFind = (EditText) findViewById(R.id.titleInputFind);
        publisherInputFind = (EditText) findViewById(R.id.publisherInputFind);
        titleInputUpdate = (EditText) findViewById(R.id.titleInputUpdate);
        titleInputUpdate.setEnabled(false);
        publisherInputUpdate = (EditText) findViewById(R.id.publisherInputUpdate);
        publisherInputUpdate.setEnabled(false);
        ratingInputUpdate = (EditText) findViewById(R.id.ratingInputUpdate);
        ratingInputUpdate.setEnabled(false);
        titleInputDelete = (EditText) findViewById(R.id.titleInputDelete);
        publisherInputDelete = (EditText) findViewById(R.id.publisherInputDelete);

        worstBook1Array = BookTableTv(worstBook1Title, worstBook1Publisher, worstBook1Rating);
        worstBookTable.add(worstBook1Array);
        worstBook2Array = BookTableTv(worstBook2Title, worstBook2Publisher, worstBook2Rating);
        worstBookTable.add(worstBook2Array);
        worstBook3Array = BookTableTv(worstBook3Title, worstBook3Publisher, worstBook3Rating);
        worstBookTable.add(worstBook3Array);

        bestBook1Array = BookTableTv(bestBook1Title, bestBook1Publisher, bestBook1Rating);
        bestBookTable.add(bestBook1Array);
        bestBook2Array = BookTableTv(bestBook2Title, bestBook2Publisher, bestBook2Rating);
        bestBookTable.add(bestBook2Array);
        bestBook3Array = BookTableTv(bestBook3Title, bestBook3Publisher, bestBook3Rating);
        bestBookTable.add(bestBook3Array);

        textInputs = new EditText[3];
        textInputs[0] = titleInput;
        textInputs[1] = publisherInput;
        textInputs[2] = ratingInput;

        IsEverythingEditted = new boolean[3];
        IsEverythingEditted[0] = IsTitleGiven;
        IsEverythingEditted[1] = isPublisherGiven;
        IsEverythingEditted[2] = IsRatingGiven;

        //Test whether we have a database or not and if there isn't we create a new one.
        dbHelper = new DatabaseHelper(getApplicationContext());
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            dbHelper.onCreate(db);
        }
        catch (Exception e) {

        }
        PopulateTables();
    }


    //Helper fuction so we can call together the two methods that update our textViews.
    private void PopulateTables() {
        PopulateWorstTable();
        PopulateBestTable();
    }

    //Helper fuction so we can call check if the EditText is empty.
    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        }

        return true;
    }

    //Helper fuction for the initialization.
    private TextView[] BookTableTv(TextView title, TextView publisher, TextView rating){
        TextView array[] = new TextView[3];
        array[0] = title;
        array[1] = publisher;
        array[2] = rating;
        return array;
    }

    //Helper fuction to clear the editText.
    private void ClearEditText(EditText et) {
        et.setText("");
    }

    //Core fuction for updating the table with the best rating.
    private void PopulateBestTable() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                DatabaseHelper.FeedEntry.COLUMN_NAME_TITLE,
                DatabaseHelper.FeedEntry.COLUMN_NAME_PUBLISHER,
                DatabaseHelper.FeedEntry.COLUMN_NAME_RATING
        };

        String sortOrder =
                DatabaseHelper.FeedEntry.COLUMN_NAME_RATING + " DESC";

        String title="" , publisher="";
        double score=0;

        //We are using this Cursor object to iterate later through our database based on our own
        //criterias, in which in this case we want them to be sorted as DESC.
        Cursor cursor = db.query(
                DatabaseHelper.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,            // The columns for the WHERE clause
                null,            // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        //Database iteration and data save in a local temp arrayList so we can access them and populate the textViews.
        List<String> temp = new ArrayList<>();
        int counter=0;
        while(cursor.moveToNext()) {
            if(counter == 3) {
                break;
            }
            title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_TITLE));
            publisher = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_PUBLISHER));
            score = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_RATING));

            temp.add(title);
            temp.add(publisher);
            temp.add(String.valueOf(score));
            counter++;
        }
        cursor.close();


        //Populate the textViews.
        int tempListCounter =0;
        for(int i=0; i < 3; i++) {
            for(int j=0; j < 3; j++) {
                try {
                    bestBookTable.get(i)[j].setText(temp.get(tempListCounter + j));
                }
                catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            tempListCounter +=3;
        }
    }



    //Same as above method PopulateBestTable(), with the exception that this table is populated
    // after the first 3 movies, due to the fact that the first three will be shown in BestRating table.
    private void PopulateWorstTable() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int rows = dbHelper.getRows();

        String[] projection = {
                BaseColumns._ID,
                DatabaseHelper.FeedEntry.COLUMN_NAME_TITLE,
                DatabaseHelper.FeedEntry.COLUMN_NAME_PUBLISHER,
                DatabaseHelper.FeedEntry.COLUMN_NAME_RATING
        };
        //String selection = FeedEntry.COLUMN_NAME_TITLE + " = ?";
        //String[] selectionArgs = { "My Title" };
        String sortOrder =
                DatabaseHelper.FeedEntry.COLUMN_NAME_RATING + " ASC";

        String title = "", publisher = "";
        double score = 0;

        Cursor cursor = db.query(
                DatabaseHelper.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,            // The columns for the WHERE clause
                null,            // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        List<String> temp = new ArrayList<>();
        int counter = 0;

        if (rows >= 4) {
            switch (rows) {
                case 4:

                    int tempCounter=0;
                    while (cursor.moveToNext()) {
                        if (tempCounter == 3) {
                            break;
                        }

                        title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_TITLE));
                        publisher = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_PUBLISHER));
                        score = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_RATING));

                        temp.add(title);
                        temp.add(publisher);
                        temp.add(String.valueOf(score));
                        tempCounter++;
                    }
                    cursor.close();

                    for(int i=0; i < 1; i++) {
                        for(int j=0; j < 3; j++) {
                            worstBookTable.get(i)[j].setText(temp.get(j));
                        }
                    }

                    int tempListCounter=0;
                    for(int i=1; i < 3; i++) {
                        for(int j=0; j < 3; j++) {
                            worstBookTable.get(i)[j].setText("");
                        }
                    }
                    break;

                case 5:

                    tempCounter=0;
                    while (cursor.moveToNext()) {
                        if (tempCounter == 3) {
                            break;
                        }

                        title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_TITLE));
                        publisher = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_PUBLISHER));
                        score = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_RATING));

                        temp.add(title);
                        temp.add(publisher);
                        temp.add(String.valueOf(score));
                        tempCounter++;
                    }
                    cursor.close();

                    tempListCounter = 0;
                    for(int i=0; i < 2; i++) {
                        for(int j=0; j < 3; j++) {
                            worstBookTable.get(i)[j].setText(temp.get(tempListCounter + j));
                        }
                        tempListCounter += 3;
                    }

                    for(int i=2; i < 3; i++) {
                        for(int j=0; j < 3; j++) {
                            worstBookTable.get(i)[j].setText("");
                        }
                    }
                    break;

                default:
                    tempCounter=0;
                    while (cursor.moveToNext()) {
                        if (tempCounter == 3) {
                            break;
                        }

                        title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_TITLE));
                        publisher = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_PUBLISHER));
                        score = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_RATING));

                        temp.add(title);
                        temp.add(publisher);
                        temp.add(String.valueOf(score));
                        tempCounter++;
                    }
                    cursor.close();

                    tempListCounter = 0;
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            try {
                                worstBookTable.get(i)[j].setText(temp.get(tempListCounter + j));
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }
                        tempListCounter += 3;
                    }
                    break;
            }
        }
    }
}
