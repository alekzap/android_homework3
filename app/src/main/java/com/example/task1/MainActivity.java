package com.example.task1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private final String FILE_NAME = "employees.dat";

    private ListView employeeListView;
    private ArrayList<Human> humans = new ArrayList<>();
    private int normalColor;
    private int selectColor;
    private int currentItem = -1;
    private View currentView = null;
    private Bitmap femaleImage;
    private Bitmap maleImage;
    private Human humanToEdit;
    private AlertDialog dialog;
    private boolean isDialogShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        normalColor = getResources().getColor(R.color.white);
        selectColor = getResources().getColor(R.color.select);

        employeeListView = (ListView) findViewById(R.id.employee_list_view);

        loadImages();
        readData();
        setUpListView();
        createDialog();

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            humanToEdit = null;
            dialog.setTitle(getString(R.string.create_employee));
            isDialogShown = true;
            dialog.show();
            ((EditText) dialog.findViewById(R.id.first_name_edit)).setText("");
            ((EditText) dialog.findViewById(R.id.last_name_edit)).setText("");
            ((RadioButton) dialog.findViewById(R.id.male_radio)).setChecked(true);
            ((DatePicker) dialog.findViewById(R.id.date_picker))
                    .updateDate(2000, 0, 1);
            return true;
        }
        if (id == R.id.action_delete) {
            if (currentItem == -1) {
                Toast.makeText(
                        this, R.string.select_element, Toast.LENGTH_SHORT).show();
                return true;
            }

            ArrayAdapter<Human> adapter
                    = (ArrayAdapter<Human>) employeeListView.getAdapter();
            adapter.remove(humans.get(currentItem));
            adapter.notifyDataSetChanged();

            currentItem = -1;
            currentView = null;
            saveData();

            return true;
        }
        if (id == R.id.action_edit) {
            if (currentItem == -1) {
                Toast.makeText(
                        this, R.string.select_element, Toast.LENGTH_SHORT).show();
                return true;
            }

            humanToEdit = humans.get(currentItem);
            dialog.setTitle(getString(R.string.edit_employee));
            isDialogShown = true;
            dialog.show();
            ((EditText) dialog.findViewById(R.id.first_name_edit))
                   .setText(humanToEdit.firstName);
            ((EditText) dialog.findViewById(R.id.last_name_edit))
                   .setText(humanToEdit.lastName);

            if (humanToEdit.gender) {
                ((RadioButton) dialog.findViewById(R.id.male_radio)).setChecked(true);
            } else {
                ((RadioButton) dialog.findViewById(R.id.female_radio)).setChecked(true);
            }

            ((DatePicker) dialog.findViewById(R.id.date_picker))
                    .updateDate(
                            humanToEdit.birthDay.get(Calendar.YEAR),
                            humanToEdit.birthDay.get(Calendar.MONTH),
                            humanToEdit.birthDay.get(Calendar.DAY_OF_MONTH));

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void readData() {
        try (FileInputStream input = openFileInput(FILE_NAME);
             ObjectInputStream objectInput = new ObjectInputStream(input)) {
            humans = (ArrayList<Human>) objectInput.readObject();
        } catch (FileNotFoundException ex) {
            Log.e("read data", ex.toString());
        } catch (IOException ex) {
            Log.e("read data", ex.toString());
        } catch (ClassNotFoundException ex) {
            Log.e("read data", ex.toString());
        }
    }

    private void saveData() {
        try (FileOutputStream fileStream
                     = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
             ObjectOutputStream outputStream = new ObjectOutputStream(fileStream)
        ) {
            outputStream.writeObject(humans);
        }
        catch (FileNotFoundException ex) {
            Log.e("save data", ex.toString());
            Toast.makeText(
                    this, R.string.error_saving, Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            Log.e("save data", ex.toString());
            Toast.makeText(
                    this, R.string.error_saving, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putBoolean("isDialogShown", isDialogShown);
        bundle.putInt("currentItem", currentItem);
        if (humanToEdit != null) {
            bundle.putInt("humanToEdit", humans.indexOf(humanToEdit));
        }

        if (isDialogShown) {
            bundle.putString("firstName",
                    ((EditText) dialog.findViewById(R.id.first_name_edit))
                            .getText().toString());

            bundle.putString("lastName",
                    ((EditText) dialog.findViewById(R.id.last_name_edit))
                            .getText().toString());

            bundle.putBoolean("gender",
                    ((RadioButton) dialog.findViewById(R.id.male_radio)).isChecked());

            bundle.putInt("year",
                    ((DatePicker) dialog.findViewById(R.id.date_picker)).getYear());

            bundle.putInt("month",
                    ((DatePicker) dialog.findViewById(R.id.date_picker)).getMonth());

            bundle.putInt("day",
                    ((DatePicker) dialog.findViewById(R.id.date_picker))
                            .getDayOfMonth());
        }
    }

    private void restoreState(Bundle bundle) {
        isDialogShown = bundle.getBoolean("isDialogShown");
        currentItem = bundle.getInt("currentItem");

        if (currentItem == -1)
            currentView = null;
        else
            employeeListView.smoothScrollToPosition(currentItem);

        int index = bundle.getInt("humanToEdit", -1);
        if (index != -1) {
            humanToEdit = humans.get(index);
        } else {
            humanToEdit = null;
        }

        if (isDialogShown) {
            dialog.setTitle(humanToEdit != null ? getString(R.string.edit_employee)
                    : getString(R.string.create_employee));

            dialog.show();
            ((EditText) dialog.findViewById(R.id.first_name_edit))
                            .setText(bundle.getString("firstName"));

            ((EditText) dialog.findViewById(R.id.last_name_edit))
                    .setText(bundle.getString("lastName"));

            if (bundle.getBoolean("gender")) {
                ((RadioButton) dialog.findViewById(R.id.male_radio)).setChecked(true);
            }
            else {
                ((RadioButton) dialog.findViewById(R.id.female_radio)).setChecked(true);
            }

            ((DatePicker) dialog.findViewById(R.id.date_picker)).updateDate(
                    bundle.getInt("year"),
                    bundle.getInt("month"),
                    bundle.getInt("day")
            );
        }
    }

    private void loadImages() {
        AssetManager assetManager = getAssets();
        try {
            InputStream stream = assetManager.open("male.jpg");
            maleImage = BitmapFactory.decodeStream(stream);
            stream.close();

            stream = assetManager.open("female.jpg");
            femaleImage = BitmapFactory.decodeStream(stream);
            stream.close();
        }
        catch (IOException ex) {
            Log.e("read images", ex.toString());
        }
    }

    private void setUpListView() {
        ArrayAdapter<Human> adapter =
                new ArrayAdapter<Human>(
                        this, R.layout.human_item, R.id.first_name_view, humans)
                {
                    @Override
                    public View getView(
                            int position, View convertView, ViewGroup parent)
                    {
                        View view = super.getView(position, convertView, parent);
                        Human human = this.getItem(position);
                        ImageView imageView
                                = (ImageView) view.findViewById(R.id.image_view);
                        imageView.setImageBitmap(human.gender ? maleImage : femaleImage);

                        TextView firstNameView
                                = (TextView) view.findViewById(R.id.first_name_view);
                        firstNameView.setText(human.firstName);

                        TextView lastNameView
                                = (TextView) view.findViewById(R.id.last_name_view);
                        lastNameView.setText(human.lastName);

                        TextView dateView
                                = (TextView) view.findViewById(R.id.date_view);
                        dateView.setText(human.getBirthDayString());

                        if (position == MainActivity.this.currentItem)
                        {
                            view.setBackgroundColor(
                                    MainActivity.this.selectColor);
                            MainActivity.this.currentView = view;
                        }
                        else
                        {
                            view.setBackgroundColor(
                                    MainActivity.this.normalColor);
                        }

                        return view;
                    }
                };

        employeeListView.setAdapter(adapter);

        employeeListView.setOnItemClickListener((parent, view, position, id) -> {
            if (MainActivity.this.currentItem != -1)
            {
                MainActivity.this.currentView.setBackgroundColor(
                        MainActivity.this.normalColor);
            }

            MainActivity.this.currentItem = position;
            MainActivity.this.currentView = view;
            MainActivity.this.currentView.setBackgroundColor(
                    MainActivity.this.selectColor);
        });
    }

    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog,null, false);
        builder.setView(view);

        builder.setPositiveButton(R.string.apply,
                (dialog, id) -> {

                    EditText firstnameEdit =
                            (EditText) view.findViewById(R.id.first_name_edit);
                    EditText lastnameEdit =
                            (EditText) view.findViewById(R.id.last_name_edit);
                    RadioButton maleRadio =
                            (RadioButton) view.findViewById(R.id.male_radio);
                    DatePicker datePicker =
                            (DatePicker) view.findViewById(R.id.date_picker);
                    Calendar date = new GregorianCalendar(
                            datePicker.getYear(),
                            datePicker.getMonth(),
                            datePicker.getDayOfMonth()
                    );

                    if (humanToEdit != null) {
                        humanToEdit.firstName = firstnameEdit.getText().toString();
                        humanToEdit.lastName = lastnameEdit.getText().toString();
                        humanToEdit.gender = maleRadio.isChecked();
                        humanToEdit.birthDay = date;

                        ((ArrayAdapter<Human>)(employeeListView.getAdapter()))
                                .notifyDataSetChanged();
                        humanToEdit = null;
                    }
                    else {
                        Human newHuman = new Human(
                                firstnameEdit.getText().toString(),
                                lastnameEdit.getText().toString(),
                                maleRadio.isChecked(),
                                date);

                        humans.add(newHuman);
                        ((ArrayAdapter<Human>)(employeeListView.getAdapter()))
                                .notifyDataSetChanged();

                        currentItem = humans.size() - 1;
                        currentView = employeeListView.getChildAt(currentItem);
                        employeeListView.smoothScrollToPosition(currentItem);
                    }

                    saveData();
                    isDialogShown = false;
                });

        builder.setNegativeButton(
                R.string.cancel, (dialog, id) -> {
                    humanToEdit = null;
                    isDialogShown = false;
                });

        dialog = builder.create();
    }
}