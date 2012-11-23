package put.medicallocator.ui.async;

import java.lang.ref.WeakReference;
import java.util.List;

import put.medicallocator.io.model.Facility;
import put.medicallocator.utils.MyLog;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class AsyncFacilityWorkerHandler extends Handler {

	private static final String ASYNC_THREAD_NAME = "AsyncWorker";
    private static final int QUERY_STARTED = 0;
	private static final int QUERY_COMPLETED = 1;

	private final HandlerThread asyncWorkerThread;
	private final Looper asyncLooper;
	private final Handler asyncHandler;

	private WeakReference<FacilityQueryListener> listener;

	public interface FacilityQueryExecutor {
		List<Facility> execute() throws Exception;
	}

	public interface FacilityQueryListener {
	    void onAsyncFacilityQueryStarted();
		void onAsyncFacilityQueryCompleted(List<Facility> result);
	}

	public AsyncFacilityWorkerHandler(FacilityQueryListener listener) {
		super();
		this.asyncWorkerThread = new HandlerThread(ASYNC_THREAD_NAME);
		this.asyncWorkerThread.start();
		this.asyncLooper = asyncWorkerThread.getLooper();
		this.asyncHandler = new WorkerHandler(asyncLooper, this);
		this.listener = new WeakReference<FacilityQueryListener>(listener);
	}

	public AsyncFacilityWorkerHandler(Looper looper, FacilityQueryListener listener) {
		super(looper);
		this.asyncWorkerThread = new HandlerThread(ASYNC_THREAD_NAME);
		this.asyncWorkerThread.start();
		this.asyncLooper = asyncWorkerThread.getLooper();
		this.asyncHandler = new WorkerHandler(asyncLooper, this);
		this.listener = new WeakReference<FacilityQueryListener>(listener);
	}

	// TODO: Alternatively, we may store the handlerThread as the static field (yikes..)
	// and just start it and leave until the application is killed.
	public void onDestroy() {
		asyncLooper.quit();
	}

	public void scheduleQuery(FacilityQueryExecutor executor) {
		final Message message = asyncHandler.obtainMessage(WorkerHandler.START_QUERY);
		message.obj = executor;
		message.sendToTarget();
	}

	public synchronized void setListener(FacilityQueryListener listener) {
		this.listener = new WeakReference<FacilityQueryListener>(listener);
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
				    dispatchMessage(QUERY_STARTED, null);
					FacilityQueryExecutor executor = (FacilityQueryExecutor) msg.obj;
					try {
						MyLog.d("TEST", "Executing query in " + Thread.currentThread());
						dispatchMessage(QUERY_COMPLETED, executor.execute());
					} catch (Exception e) {
						// Query didn't finish properly! There is no need to invoke the callback.
					}
					return;
			}
			super.handleMessage(msg);
		}

		private void dispatchMessage(int what, Object result) {
			final Message message = targetHandler.obtainMessage(what);
			message.obj = result;
			message.sendToTarget();
		}

	}

}
