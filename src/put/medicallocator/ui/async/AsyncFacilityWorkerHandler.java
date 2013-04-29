package put.medicallocator.ui.async;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import put.medicallocator.io.model.Facility;
import put.medicallocator.utils.MyLog;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * {@link Handler} supporting {@link FacilityQueryExecutor} executions in an asynchronous way.
 * Callbacks are dispatched to the thread associated with this Handler.
 */
public class AsyncFacilityWorkerHandler extends Handler {

    private static final String ASYNC_THREAD_NAME = "AsyncWorker";

    /* Message identifiers */
    private static final int QUERY_STARTED = 0;
    private static final int QUERY_COMPLETED = 1;

    private final HandlerThread asyncWorkerThread;
    private final Looper asyncLooper;
    private final Handler asyncHandler;

    private WeakReference<FacilityQueryListener> listener;

    /**
     * Simple executor interface providing single method returning {@link List} of {@link Facility}.
     */
    public interface FacilityQueryExecutor {
        List<Facility> execute() throws Exception;
    }

    /**
     * Defines the callbacks for {@link Facility} queries executions.
     */
    public interface FacilityQueryListener {
        /**
         * This callback is invoked as soon as query is started.
         */
        void onAsyncFacilityQueryStarted();

        /**
         * This callback is invoked as soon as query is finished.
         */
        void onAsyncFacilityQueryCompleted(List<Facility> result);
    }

    /**
     * Default constructor for {@link AsyncFacilityWorkerHandler}.
     * It creates internally the {@link HandlerThread} and executes processing there.
     */
    public AsyncFacilityWorkerHandler(FacilityQueryListener listener) {
        super();
        this.asyncWorkerThread = new HandlerThread(ASYNC_THREAD_NAME);
        this.asyncWorkerThread.start();
        this.asyncLooper = asyncWorkerThread.getLooper();
        this.asyncHandler = new WorkerHandler(asyncLooper, this);
        this.listener = new WeakReference<FacilityQueryListener>(listener);
    }

    /**
     * Invoke this when {@link Context} associated with this worker is being destroyed.
     * (f.e. {@link Activity#onDestroy()}).
     */
    public void onDestroy() {
        asyncLooper.quit();
    }

    /**
     * Invokes the provided {@link FacilityQueryExecutor} asynchronously.
     */
    public void invokeAsyncQuery(FacilityQueryExecutor executor) {
        dispatchMessage(asyncHandler, WorkerHandler.START_QUERY, executor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(Message msg) {
        final FacilityQueryListener targetListener;
        synchronized (this) {
            targetListener = listener.get();
        }

        switch (msg.what) {
            case QUERY_STARTED:
                if (targetListener != null) {
                    targetListener.onAsyncFacilityQueryStarted();
                }
                return;
            case QUERY_COMPLETED:
                if (targetListener != null) {
                    final List<Facility> result = (List<Facility>) msg.obj;
                    targetListener.onAsyncFacilityQueryCompleted(result);
                }
                return;
        }
        super.handleMessage(msg);
    }

    /**
     * {@link Handler} associated with the {@link HandlerThread}, performing asynchronous execution.
     * Interacts with the other {@link Handler} to make callbacks (in this case, it should be
     * {@link AsyncFacilityWorkerHandler}).
     */
    private static class WorkerHandler extends Handler {

        private static final int START_QUERY = 0;

        private final Handler targetHandler;

        public WorkerHandler(Looper looper, Handler targetHandler) {
            super(looper);
            this.targetHandler = targetHandler;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_QUERY:
                    AsyncFacilityWorkerHandler.dispatchMessage(targetHandler, QUERY_STARTED, null);
                    FacilityQueryExecutor executor = (FacilityQueryExecutor) msg.obj;
                    try {
                        MyLog.d("TEST", "Executing query in " + Thread.currentThread());
                        AsyncFacilityWorkerHandler.dispatchMessage(targetHandler, QUERY_COMPLETED, executor.execute());
                    } catch (Exception e) {
                        // Query didn't finish properly! There is no need to invoke the callback.
                    }
                    return;
            }
            super.handleMessage(msg);
        }

    }

    private static void dispatchMessage(Handler handler, int what, Object result) {
        final Message message = handler.obtainMessage(what);
        message.obj = result;
        message.sendToTarget();
    }

}
