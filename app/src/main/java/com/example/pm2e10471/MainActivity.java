package com.example.pm2e10471;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pm2e10471.Configuracion.SQLiteConnection;
import com.example.pm2e10471.Configuracion.Trans;
import com.example.pm2e10471.Models.Paises;
import com.example.pm2e10471.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SQLiteConnection conexion;
    static final int peticion_acceso_camera = 101;
    static final int peticion_toma_fotografia = 102;
    String currentPhotoPath;
    EditText nombre, telefono, nota;
    Spinner paises;
    ImageButton imagenPerfil, btn_addPais;
    Button guardar, contactos;
    String paisSeleccionado, codigoSeleccionado;
    Bitmap imageBitmap = null;
    byte[] imagenPerfilByteArray;
    ArrayList<Paises> listPais;
    ArrayList<String> arregloPaises;
    ArrayAdapter<CharSequence> adp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        conexion = new SQLiteConnection(this, Trans.namedb, null, 1);
        nombre = (EditText) findViewById(R.id.txt_nombre);
        telefono = (EditText) findViewById(R.id.txt_telefono);
        nota = (EditText) findViewById(R.id.txt_nota);
        paises = (Spinner) findViewById(R.id.cmb_paises);
        imagenPerfil = (ImageButton) findViewById(R.id.perfil_imagen);
        btn_addPais = (ImageButton) findViewById(R.id.btn_addPais);
        guardar = (Button) findViewById(R.id.btn_Guardar);
        contactos = (Button) findViewById(R.id.btn_Contactos);

        getPaises();

        adp = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arregloPaises);
        paises.setAdapter(adp);

        paises.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                paisSeleccionado = listPais.get(i).getPais();
                codigoSeleccionado = listPais.get(i).getCodigo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        imagenPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permisos();
            }
        });

        View.OnClickListener buttonClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Class<?> actividad = null;
                if (view.getId()==R.id.btn_Contactos) {
                    actividad = ActivityContactos.class;
                }
                if (actividad != null) {
                    moveActivity(actividad);
                }
            }
        };

        contactos.setOnClickListener(buttonClick);

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nombre.getText().toString().trim().isEmpty() || telefono.getText().toString().trim().isEmpty() || nota.getText().toString().trim().isEmpty()){
                    nombre.setError("Porfavor ingrese un nombre para el contacto, no se permiten campos vacios!!!");
                    telefono.setError("Porfavor ingrese un numero telefonico, no se permiten campos vacios!!!");
                    nota.setError("Porfavor ingrese una nota para el contacto, no se permiten campos vacios!!!");
                } else if (imageBitmap == null) {
                    Toast.makeText(getApplicationContext(), "Porfavor tome una foto al contacto!", Toast.LENGTH_LONG).show();
                }else if (paises.getSelectedItemPosition() == 0){
                    Toast.makeText(getApplicationContext(), "Porfavor seleccione un pais de la lista!", Toast.LENGTH_LONG).show();
                }else{
                    addContact();
                }
            }
        });
    }

    private void addContact() {
        try {

            SQLiteDatabase db = conexion.getWritableDatabase();

            ContentValues valores = new ContentValues();
            valores.put(Trans.nombres, nombre.getText().toString());
            valores.put(Trans.pais, paisSeleccionado);
            valores.put(Trans.codigo, codigoSeleccionado);
            valores.put(Trans.telefono, telefono.getText().toString());
            valores.put(Trans.nota, nota.getText().toString());
            valores.put(Trans.imagen, imagenPerfilByteArray);

            Long result = db.insert(Trans.tablaContactos, Trans.id, valores);

            Toast.makeText(this, getString(R.string.respuesta), Toast.LENGTH_SHORT).show();
            db.close();
            recreate();
            nombre.setText("");
            telefono.setText("");
            nota.setText("");
            paises.setSelection(0);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.errorIngreso), Toast.LENGTH_SHORT).show();
        }
    }

    private void getPaises() {
        try {
            SQLiteDatabase db = conexion.getReadableDatabase();
            Paises pais = null;
            listPais = new ArrayList<Paises>();
            db.rawQuery(Trans.SelectTablePais, null);

            Cursor cursor = db.rawQuery(Trans.SelectTablePais, null);
            while (cursor.moveToNext()) {
                pais = new Paises();
                pais.setId(cursor.getInt(0));
                pais.setPais(cursor.getString(1));
                pais.setCodigo(cursor.getString(2));

                listPais.add(pais);
            }
            cursor.close();
            fillCombo();
        } catch (Exception ex) {
            ex.toString();
        }
    }

    private void fillCombo() {
        arregloPaises = new ArrayList<String>();
        for (int i = 0; i < listPais.size(); i++) {
            arregloPaises.add(listPais.get(i).getId() + " - " +
                    listPais.get(i).getPais() + " - " +
                    listPais.get(i).getCodigo());
        }
    }

    private void moveActivity(Class<?> actividad) {
        Intent intent = new Intent(getApplicationContext(), actividad);
        startActivity(intent);
    }

    private void permisos() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, peticion_acceso_camera);
        } else {
            tomarFoto();
        }
    }

    private void tomarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, peticion_toma_fotografia);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == peticion_acceso_camera) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarFoto();
            } else {
                Toast.makeText(getApplicationContext(), "Permiso Denegado!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == peticion_toma_fotografia && resultCode == RESULT_OK) {
            try {
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");
                imagenPerfil.setImageBitmap(imageBitmap);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                imagenPerfilByteArray = stream.toByteArray();

            }catch (Exception ex){
                ex.toString();
            }
        }
    }

    public void showAddPaisDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Pais");

        View dialogView = getLayoutInflater().inflate(R.layout.add_pais, null);
        builder.setView(dialogView);

        final EditText paisEditText = dialogView.findViewById(R.id.editPais);
        final EditText areaCodeEditText = dialogView.findViewById(R.id.editAreaCode);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pais = paisEditText.getText().toString();
                String areaCode = "("+areaCodeEditText.getText().toString()+")";

                addPais(pais, areaCode);
                updateSpinner();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    private void addPais(String pais, String codigo) {
        try {
            SQLiteDatabase db = conexion.getWritableDatabase();

            ContentValues valores = new ContentValues();
            valores.put(Trans.pais, pais);
            valores.put(Trans.codigoArea, codigo);

            long result = db.insert(Trans.tablaPaises, null, valores);

            if (result != -1) {
                Log.d("DatabaseSuccess", "Inserted data with row ID: " + result);
                Toast.makeText(this, getString(R.string.respuesta), Toast.LENGTH_SHORT).show();
            } else {
                Log.e("DatabaseError", "Error inserting data");
                Toast.makeText(this, getString(R.string.errorIngreso), Toast.LENGTH_SHORT).show();
            }

            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DatabaseError", "Error inserting data: " + e.getMessage());
            Toast.makeText(this, getString(R.string.errorIngreso), Toast.LENGTH_SHORT).show();
        }
    }


    private void updateSpinner() {
        // Step 1: Retrieve the updated data from the database
        paises.setAdapter(null);
        getPaises();
        adp = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arregloPaises);
        paises.setAdapter(adp);
    }
}