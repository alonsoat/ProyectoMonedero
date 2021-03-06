package com.proyecto.alberto.monedero.Interfaces.Movimientos;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.proyecto.alberto.monedero.Gestiones.Alertas;
import com.proyecto.alberto.monedero.Gestiones.SubProcesosGestion;
import com.proyecto.alberto.monedero.R;
import com.proyecto.alberto.monedero.Tablas.Concepto;
import com.proyecto.alberto.monedero.Tablas.Movimiento;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Fragment donde se muestra en detalle un movimiento
 */
public class Movimientos_Detalle extends Fragment implements View.OnClickListener {

    public static String TAG_FRAGMENT = "MOVIMIENTOS_DETALLE";

    private View view;
    private FragmentActivity context;

    private int posicion = 0;

    //UI
    private DatePickerDialog datePickerdialog;
    private TimePickerDialog timePickerDialog;
    private DateFormat dateFormatter;
    private Spinner conceptos;

    private Button guardar;

    private TextView fecha;
    private TextView hora;
    private RadioButton radioEntrada;
    private RadioButton radioSalida;

    private EditText km;
    private EditText importe;

    private TableLayout movdetalle;

    private Concepto concepto;
    private ArrayList<Concepto> conceptos_list;
    private ArrayList<String> nombresconcepto_list;

    public Movimientos_Detalle() {

    }

    public static Movimientos_Detalle newInstance(Bundle arguments) {

        Movimientos_Detalle md = new Movimientos_Detalle();

        if (arguments != null) {

            md.setArguments(arguments);

        }

        return md;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        this.view = inflater.inflate(R.layout.movimiento_detalle_fragment, container, false);
        this.context = getActivity();

        view.findViewById(R.id.save).setOnClickListener(this);
        view.findViewById(R.id.radioEntrada).setOnClickListener(this);
        view.findViewById(R.id.radioSalida).setOnClickListener(this);

        //Iniciar variables;
        fecha = (TextView) view.findViewById(R.id.dias_edittext);
        hora = (TextView) view.findViewById(R.id.horaedit);
        km = (EditText) view.findViewById(R.id.kmedit);
        importe = (EditText) view.findViewById(R.id.importe_edittext);
        guardar = (Button) view.findViewById(R.id.save);
        radioEntrada = (RadioButton) view.findViewById(R.id.radioEntrada);
        radioSalida = (RadioButton) view.findViewById(R.id.radioSalida);
        movdetalle = (TableLayout) view.findViewById(R.id.movdetalle);
        conceptos = (Spinner) view.findViewById(R.id.concepto_edittext);

        conceptos_list = new ArrayList();
        nombresconcepto_list = new ArrayList<>();

        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Configurar variables si se pasan datos
        if (getArguments() != null) {

            guardar.setText(getString(R.string.actualizar));

            if (this.getArguments().getDouble("movimiento_mov") >= 0) {

                movdetalle.setBackground(getResources().getDrawable(R.color.DarkSeaGreen));
                radioEntrada.setChecked(true);
                radioSalida.setChecked(false);

            } else {

                movdetalle.setBackground(getResources().getDrawable(R.color.IndianRed));
                radioEntrada.setChecked(false);
                radioSalida.setChecked(true);

            }

            importe.setText(String.valueOf(this.getArguments().getDouble("movimiento_mov")));
            fecha.setText(this.getArguments().getString("fecha_mov"));
            hora.setText(this.getArguments().getString("hora_mov"));
            km.setText(String.valueOf(this.getArguments().getDouble("km_mov")));

        } else {

            guardar.setText(getString(R.string.añadir));

            movdetalle.setBackground(getResources().getDrawable(R.color.DarkSeaGreen));
            fechaPorDefecto();
            horaPorDefecto();

        }

        setDateTimeField();

        new SubProcesosGestion(this, context, SubProcesosGestion.CONCEPTOS_BAJAR).execute();

    }

    public void configurar_adapter() {

        if (conceptos_list.size() == 0) {

            Alertas.ningunConcepto(context, this);

            //Si se le pasa argumentos busca el de los argumentos, sino rellena y coge la primera posición
        } else if (getArguments() != null) {

            for (int i = 0; i < conceptos_list.size(); i++) {

                nombresconcepto_list.add(conceptos_list.get(i).getNombre());

                if (conceptos_list.get(i).getId() == this.getArguments().getInt("id_concepto")) {

                    posicion = i;

                }
            }

        } else {

            for (int i = 0; i < conceptos_list.size(); i++) {

                nombresconcepto_list.add(conceptos_list.get(i).getNombre());

            }

            posicion = 0;

        }

        ArrayAdapter spin_adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, nombresconcepto_list);
        spin_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        conceptos.setAdapter(spin_adapter);

        conceptos.setSelection(posicion);

        conceptos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                seleccionarConcepto(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                seleccionarConcepto(posicion);

            }

        });

        importe.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {

                try {

                    if (!hasFocus) {

                        double mov = Double.parseDouble(importe.getText().toString());

                        if (mov < 0) {

                            radioEntrada.setChecked(false);
                            radioSalida.setChecked(true);
                            movdetalle.setBackground(getResources().getDrawable(R.color.IndianRed));

                        } else {

                            radioEntrada.setChecked(true);
                            radioSalida.setChecked(false);
                            movdetalle.setBackground(getResources().getDrawable(R.color.DarkSeaGreen));
                        }

                    }

                } catch (NumberFormatException e) {

                    Log.e("Number", e.getMessage());
                }
            }
        });
    }

    public ArrayList<Concepto> getConceptos_list() {
        return conceptos_list;
    }

    private void setDateTimeField() {

        view.findViewById(R.id.dias_edittext).setOnClickListener(this);

        Calendar newCalendar = Calendar.getInstance();

        datePickerdialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                fecha.setText(dateFormatter.format(newDate.getTime()));

            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        view.findViewById(R.id.horaedit).setOnClickListener(this);

        timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                String h;
                String m;

                //Solución para arreglar el tema de 0
                if (selectedHour < 10) {

                    h = "0" + String.valueOf(selectedHour);

                } else {

                    h = String.valueOf(selectedHour);

                }


                if (selectedMinute < 10) {

                    m = "0" + String.valueOf(selectedMinute);

                } else {

                    m = String.valueOf(selectedMinute);

                }

                hora.setText(h + ":" + m);

            }

        }, newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);


    }

    public void seleccionarConcepto(int posicion) {

        this.concepto = conceptos_list.get(posicion);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.dias_edittext:
                datePickerdialog.show();

                break;

            case R.id.horaedit:
                timePickerDialog.show();

                break;

            case R.id.save:

                if (importe.getText().toString().equals("") || km.getText().toString().equals("")) {

                    Alertas.faltaRellenar(context);

                } else if (getArguments() != null) {

                    int id = getArguments().getInt("id_mov");

                    double mov = Double.parseDouble(importe.getText().toString());

                    if (radioSalida.isChecked() && mov > 0) {

                        mov = mov * (-1);

                    } else if (radioEntrada.isChecked() && mov < 0) {

                        mov = mov * (-1);
                    }

                    if (mov < 0) {

                        radioEntrada.setChecked(false);
                        radioSalida.setChecked(true);
                        movdetalle.setBackground(getResources().getDrawable(R.color.IndianRed));

                    } else {

                        radioEntrada.setChecked(true);
                        radioSalida.setChecked(false);
                        movdetalle.setBackground(getResources().getDrawable(R.color.DarkSeaGreen));

                    }

                    String f = fecha.getText().toString();
                    String h = hora.getText().toString();

                    double kmD = Double.parseDouble(km.getText().toString());

                    Movimiento movimiento = new Movimiento(id, concepto, mov, f, h, kmD);

                    new SubProcesosGestion(context, this, movimiento, SubProcesosGestion.MOVIMIENTOS_ACTUALIZAR).execute();


                } else {

                    final double mov = Double.parseDouble(importe.getText().toString());
                    double kmD = Double.parseDouble(km.getText().toString());

                    if (mov < 0) {

                        radioEntrada.setChecked(false);
                        radioSalida.setChecked(true);
                        movdetalle.setBackground(getResources().getDrawable(R.color.IndianRed));

                    } else {

                        radioEntrada.setChecked(true);
                        radioSalida.setChecked(false);
                        movdetalle.setBackground(getResources().getDrawable(R.color.DarkSeaGreen));

                    }

                    String f = fecha.getText().toString();
                    String h = hora.getText().toString();

                    Movimiento movimiento = new Movimiento(concepto, mov, f, h, kmD);

                    new SubProcesosGestion(context, this, movimiento, SubProcesosGestion.MOVIMIENTOS_INSERTAR).execute();



                }

                break;

            case R.id.radioEntrada:

                try {

                    double movE = Double.parseDouble(importe.getText().toString());

                    if (movE < 0) {

                        importe.setText(String.valueOf(movE * (-1)));

                    }

                } catch (NumberFormatException e) {

                }

                movdetalle.setBackground(getResources().getDrawable(R.color.DarkSeaGreen));


                break;

            case R.id.radioSalida:

                try {
                    double movS = Double.parseDouble(importe.getText().toString());

                    if (movS > 0) {

                        importe.setText(String.valueOf(movS * (-1)));

                    }

                } catch (NumberFormatException e) {

                }

                movdetalle.setBackground(getResources().getDrawable(R.color.IndianRed));


                break;


        }

    }

    public void resetearValores() {

        conceptos.setSelection(0);
        importe.setText("");
        fechaPorDefecto();
        horaPorDefecto();
        km.setText("");
        radioEntrada.setChecked(true);
        movdetalle.setBackground(getResources().getDrawable(R.color.DarkSeaGreen));

    }

    public void fechaPorDefecto() {


        Calendar newDate = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        fecha.setText(dateFormatter.format(newDate.getTime()));


    }

    public void horaPorDefecto() {

        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String dateString = sdf.format(date);
        hora.setText(dateString);

    }

}
