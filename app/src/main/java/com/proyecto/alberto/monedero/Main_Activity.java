package com.proyecto.alberto.monedero;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.proyecto.alberto.monedero.Gestiones.AlarmDeleteFileReceiver;
import com.proyecto.alberto.monedero.Gestiones.Alertas;
import com.proyecto.alberto.monedero.Interfaces.Login;
import com.proyecto.alberto.monedero.Interfaces.MenuPrincipal;
import com.proyecto.alberto.monedero.Tablas.Usuario;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

public class Main_Activity extends Activity {

    public static final int ENVIADO = 1001;
    private static final int ID_NOTIFICACION_CREAR = 1;
    private static PendingIntent pendingIntent;
    private Usuario usuario;
    private boolean comprado = false;
    private FrameLayout main;
    private AdView adView;
    private AdRequest adRequest;
    private NotificationManager nm;
    private boolean inicio;

    private DrawerLayout drawerLayout;
    private ListView drawer;
    private FragmentTransaction ft;

    private SharedPreferences config;


    private IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        drawer = (ListView) findViewById(R.id.drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        //Nueva lista de drawer items
        ArrayList<ItemMenuLateral> items = new ArrayList<>();
        items.add(new ItemMenuLateral("Opciones",R.drawable.ic_action_settings));
        // Relacionar el adaptador y la escucha de la lista del drawer
        drawer.setAdapter(new ListAdapterMenuLateral(this, items));

        drawer.setOnItemClickListener(new DrawerItemClickListener());

        main = (FrameLayout) findViewById(R.id.container);

        inicio = true;

        if (savedInstanceState == null) {

            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new Login(), Login.TAG_FRAGMENT)
                    .commit();

        } else {

            comprado = savedInstanceState.getBoolean("comprado");

            try {

                usuario.setNombre(savedInstanceState.getString("usuario"));
                usuario.setPassword(savedInstanceState.getString("password"));

            } catch (NullPointerException e) {

                Log.w("Usuario", "No hay usuario logeado");

            }

        }

        config = getSharedPreferences("app_taxi", MODE_PRIVATE);


      /*  Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);


        ArrayList<String> skuList = new ArrayList<>();
        skuList.add("premium");

        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("premium", skuList);

        //Esto se debe hacer en un proceso
        try {
            Bundle skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/

    }

    //ITEMS MENU LATERAL
    private void selectItem(int position) {

        AlertDialog alerta;

        switch (position) {

            case 0:

                ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.animator.slide_go_in, R.animator.slide_go_out, R.animator.slide_back_in, R.animator.slide_back_out);
                ft.replace(R.id.container, new Opciones(), Opciones.TAG_FRAGMENT);
                ft.addToBackStack(MenuPrincipal.TAG_FRAGMENT);
                ft.commit();


                break;

        }

    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    public void cargarBanner() {

        if (!comprado) {

            if (adView == null) {

                adView = (AdView) this.findViewById(R.id.adView);
                adRequest = new AdRequest.Builder().addTestDevice("15FCC00F662AD8A2F9D5F7333EA10824").build();
                adView.loadAd(adRequest);
                adView.setVisibility(View.VISIBLE);

            }

        } else {

            if (adView != null) {

                adView.setVisibility(View.GONE);
                adView = null;
                adRequest = null;

            }

        }

    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public boolean isComprado() {
        return comprado;
    }

    public void setComprado(boolean comprado) {
        this.comprado = comprado;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to

        if (requestCode == ENVIADO) {

            File sdCard = Environment.getExternalStorageDirectory();

            String nombreU = this.getUsuario().getNombre();

            File file = new File(sdCard.getAbsolutePath()
                    + "/Backup Taxi", "/backup_" + nombreU + ".sql");

            AlarmDeleteFileReceiver alarm = new AlarmDeleteFileReceiver(
                    this,
                    System.currentTimeMillis() + 1000 * 20, //20 segundos
                    file.getAbsolutePath());
        }

    }

    public void backup(String backup) {

        File sdCard, directory, file = null;

        try {
            // validamos si se encuentra montada nuestra memoria externa
            if (Environment.getExternalStorageState().equals("mounted")) {

                // Obtenemos el directorio de la memoria externa
                sdCard = Environment.getExternalStorageDirectory();

                // Clase que permite grabar texto en un archivo
                FileOutputStream fout = null;
                try {
                    // instanciamos un onjeto File para crear un nuevo

                    // creamos el archivo en el nuevo directorio creado
                    file = new File(sdCard.getAbsolutePath()
                            + "/Download", "backup_" + usuario.getNombre() + ".sql");

                    fout = new FileOutputStream(file);

                    // Convierte un stream de caracteres en un stream de
                    // bytes
                    OutputStreamWriter ows = new OutputStreamWriter(fout);
                    ows.write(backup); // Escribe en el buffer la cadena de texto
                    ows.flush(); // Volca lo que hay en el buffer al archivo
                    ows.close(); // Cierra el archivo de texto

                } catch (IOException e) {

                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }

            }

        } catch (Exception e) {
            // TODO: handle exception

        }
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        Log.d("Guardado", "guardado");

        savedInstanceState.putBoolean("comprado", comprado);

        if (usuario != null) {
            Log.d("Guardado", "guardado");
            savedInstanceState.putString("usuario", usuario.getNombre());
            savedInstanceState.putString("password", usuario.getPassword());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mService != null) {
            unbindService(mServiceConn);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        if (getFragmentManager().findFragmentById(R.id.container).getClass().getSimpleName().equals("MenuPrincipal")) {

            if (keyCode == KeyEvent.KEYCODE_BACK) {

                Alertas.salirAplicacion(this);

                return true;

            }

        }

        return super.onKeyDown(keyCode, event);
    }

    public void alertaExterna() {

        boolean servicio = config.getBoolean("servicio", false);


        if (!servicio) {

            ActivarAlarma();

            SharedPreferences.Editor editver = config.edit();
            editver.putBoolean("servicio", true);
            editver.commit();


        } else {

            DesactivarAlarma();

            SharedPreferences.Editor editver = config.edit();
            editver.putBoolean("servicio", false);
            editver.commit();

        }
    }

    public void DesactivarAlarma() {

        Intent intent = new Intent(this, alarmChecker.class);
        stopService(intent);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        pendingIntent = null;

        Toast.makeText(this, "Alarma desactivada", Toast.LENGTH_LONG).show();


    }

    public void ActivarAlarma() {

        int comprobacionIntervaloSegundos = 60;

        Intent intent = new Intent(this, alarmChecker.class);
        startService(intent);
        pendingIntent = PendingIntent.getService(this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), comprobacionIntervaloSegundos * 1000, pendingIntent);

        Toast.makeText(this, "Alarma iniciada", Toast.LENGTH_LONG).show();


    }

    public SharedPreferences getConfig() {
        return config;
    }

    public boolean isInicio() {
        return inicio;
    }

    public void setInicio(boolean inicio) {
        this.inicio = inicio;
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d("hola", position + "");
            selectItem(position);


        }
    }
}



