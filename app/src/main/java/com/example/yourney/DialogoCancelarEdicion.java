package com.example.yourney;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class DialogoCancelarEdicion extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        Bundle args = getArguments();
        int idRuta = args.getInt("idRuta");

        System.out.println("+++++++++++++++++ " + idRuta);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.cancelar_edicion);

        builder.setNegativeButton("No", null);
        builder.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Elimino la info guardada de la ruta hasta el momento
                Data datos = new Data.Builder()
                        .putString("accion", "delete")
                        .putString("consulta", "Rutas")
                        .putInt("idRuta", idRuta)
                        .build();

                OneTimeWorkRequest delete = new OneTimeWorkRequest.Builder(ConexionBD.class)
                        .setInputData(datos)
                        .build();

                WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(delete.getId()).observe(getActivity(), new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            System.out.println("Ruta eliminada");
                        }
                    }
                });
                WorkManager.getInstance(getActivity()).enqueue(delete);

                // Vuelvo a main
                Intent intent = new Intent(getActivity(), LoginRegisterActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        // Devuelvo el dialogo creado por el builder
        return builder.create();

    }
}

