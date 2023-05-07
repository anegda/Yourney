package com.example.yourney;

import android.content.Context;
import android.content.SharedPreferences;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Locale;


public class FragmentPreferencias extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        // Layout con la pantalla de preferencias
        addPreferencesFromResource(R.xml.preferencias);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Listener para cuando cambia algo de las preferencias

        // Filtro que preferencia ha cambiado
        switch(s) {
            case "idiomaPref":
                System.out.println("############## IDIOMA ##############");
                // Cambio el idioma de la app
                Locale nuevaloc = new Locale(sharedPreferences.getString(s, null));
                Locale.setDefault(nuevaloc);
                Configuration configuration = getResources().getConfiguration();
                configuration.setLocale(nuevaloc);
                configuration.setLayoutDirection(nuevaloc);

                Context context = getActivity().createConfigurationContext(configuration);
                getActivity().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());

                // Vuelvo a cargar la actividad para aplicar el idioma
                getActivity().finish();
                startActivity(getActivity().getIntent());
                break;

            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}