package eventos.com.br.eventos.config;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import eventos.com.br.eventos.dao.DataBaseHelper;
import eventos.com.br.eventos.model.Usuario;
import eventos.com.br.eventos.util.Prefs;

/**
 * Created by Antonio Cesar on 18/07/2016.
 */
public class EventosApplication extends Application {

    //private static String URL = "http://10.100.3.49:8080/eventos/rest/";
    //private static String URL = "http://192.168.43.100:8080/eventos/rest/";
    private static String URL = "https://eventostest.herokuapp.com/rest/";
    private static EventosApplication instance = null;
    private Usuario usuario = null;
    private DataBaseHelper dataBaseHelper = null;

    @Override
    public void onCreate() {
        super.onCreate();
        // Salva a instância para termos acesso como Singleton
        instance = this;
        // register to be informed of activities starting up
        registerActivityLifecycleCallbacks(getCallback());
    }

    @Override
    public void onTerminate() {
        if (dataBaseHelper != null) {
            OpenHelperManager.releaseHelper();
            dataBaseHelper = null;
        }
        super.onTerminate();
    }

    public static EventosApplication getInstance() {
        return instance;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public DataBaseHelper getDataBaseHelper() {
        if (dataBaseHelper == null) {
            dataBaseHelper = OpenHelperManager.getHelper(this, DataBaseHelper.class);
        }
        return dataBaseHelper;
    }

    public static void setURL(String endereco, Context context) {
        URL = "http://" + endereco + ":8080/eventos/rest/";
        Prefs.setString(context, "url", URL);
    }

    public static String getURL(Context context) {
        String url = Prefs.getString(context, "url");
        if (url != null && !"".equals(url)) {
            return url;
        }

        return URL;
    }

    @NonNull
    private ActivityLifecycleCallbacks getCallback() {
        return new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                // new activity created; force its orientation to portrait
                activity.setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        };
    }
}
