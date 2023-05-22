package com.example.yourney;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class DialogoCerrarSesion extends DialogFragment {

    private Context context;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        this.context = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.msg_cerrar_sesion);
        builder.setNegativeButton("No", null); // La accion de cerrar el dialogo se realiza por defecto
        builder.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Elimino el username logueado
                Sesion sesion = new Sesion(getActivity());
                String user = sesion.getUsername();
                sesion.deleteUsername();

                // Obtengo el token del usuario logueado
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                     @Override
                     public void onComplete(@NonNull Task<String> task) {
                         if (!task.isSuccessful()) {
                             String token = "";
                             return;
                         }
                         String token = task.getResult();

                         // Registro el token del usuario en la bd
                         Data datos = new Data.Builder()
                                 .putString("accion", "delete")
                                 .putString("consulta", "Tokens")
                                 .putString("username", user)
                                 .putString("token", token)
                                 .build();

                         OneTimeWorkRequest delete = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                 .setInputData(datos)
                                 .build();

                         WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(delete.getId()).observe(getActivity(), new Observer<WorkInfo>() {
                             @Override
                             public void onChanged(WorkInfo workInfo) {
                                 if (workInfo != null && workInfo.getState().isFinished()) {
                                     System.out.println("Sesion cerrada del todo");
                                 }
                             }
                         });
                         WorkManager.getInstance(getActivity()).enqueue(delete);
                     }
                 });
                // Vuelvo a la actividad de login y registro
                Intent intent = new Intent(getActivity(), LoginRegisterActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        // Devuelvo el dialogo creado por el builder
        return builder.create();
    }

}
