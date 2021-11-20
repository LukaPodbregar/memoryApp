package si.uni_lj.fe.seminar;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncTaskExecutor {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback<R> {
        void onComplete(R rezultat);
    }

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