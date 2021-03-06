package com.fox.imok.domain.notificaciones;

import android.content.SharedPreferences;
import android.util.Log;

import com.activeandroid.query.Update;
import com.fox.imok.domain.bd.TBContactos;
import com.fox.imok.domain.io.ConstantsUrls;
import com.fox.imok.domain.io.JsonKeys;
import com.fox.imok.domain.models.ContactosOperaciones;
import com.fox.imok.domain.models.ContactosOperacionesContrato;
import com.fox.imok.splash.SplashActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String KEY_PUSH = "PUSH_FOX_OK";
    private SharedPreferences preferences;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            String data = remoteMessage.getData().toString();
            data = data.replaceAll("\\\\\\\\", "");
            JSONObject notificacion = new JSONObject(data);
            JSONObject jsonDefault = notificacion.optJSONObject(JsonKeys.DEFAULT);
            JSONObject jsonGCM = jsonDefault.optJSONObject(JsonKeys.GCM);
            final JSONObject jsonData = jsonGCM.optJSONObject(JsonKeys.DATA);
            preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
            String eventOld = preferences.getString(JsonKeys.EVENT, "");
            String eventNew = jsonData.optString(JsonKeys.EVENT);
            SharedPreferences.Editor editor = preferences.edit();
            if (!eventOld.equalsIgnoreCase(eventNew))
                editor.putBoolean(ContactosOperaciones.KEY_INIT, false);
            editor.putString(ContactosOperaciones.KEY_MESSAGE, jsonData.optString("description"));
            editor.putString(ConstantsUrls.Params.EVENT, jsonData.optString(JsonKeys.EVENT));
            editor.apply();
            editor.commit();
            new Update(TBContactos.class).set("ok=?, fechahorarespuesta = ?", false, 0).execute();
            final ContactosOperacionesContrato contrato = new ContactosOperaciones();
            //if (jsonData.optString("title").toLowerCase().equalsIgnoreCase("alert"))
            //  contrato.enviarSMS(jsonData.optString("description"));

            contrato.initContactos(new ContactosOperacionesContrato.AgendaCallback() {
                @Override
                public void onResult(boolean result, String message) {

                    if (jsonData.optString("title").toLowerCase().equalsIgnoreCase("alert")) {
                        contrato.enviarSMS(jsonData.optString("description"));
                    }

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
