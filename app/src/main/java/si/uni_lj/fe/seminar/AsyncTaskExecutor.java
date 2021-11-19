package si.uni_lj.fe.seminar;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncTaskExecutor {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    /* Vmesnik, ki ga mora implementirati razred, ki želi biti prejemnik
     rezultata izvajanja niti (Callable) */
    public interface Callback<R> {
        void onComplete(R rezultat);
    }

    /* Izvrši asinhrono opravilo, ki implementira vmesnik Callable.
    Ko se to izvrši, pokliče objekt, ki implementira vmesnik Callback*/
    public <R> void execute(Callable<R> callable, Callback<R> callback) {
        executor.execute(() -> {
            final R rezultat;
            try {
                rezultat = callable.call();
                handler.post(() -> {
                    callback.onComplete(rezultat);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}