/**
 * Frederic Bregier LGPL 25 févr. 09 FtpFuture.java goldengate.ftp.core.utils
 * GoldenGateFtp frederic
 */
package goldengate.ftp.core.utils;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.TimeUnit;

/**
 * Ftp Future operation<br>
 * Completely inspired from the excellent ChannelFuture of Netty, but without
 * any channel inside.
 * 
 * @author frederic goldengate.ftp.core.utils FtpFuture
 * 
 */
public class FtpFuture {
    private static final Throwable CANCELLED = new Throwable();

    private final boolean cancellable;

    private boolean done;

    private Throwable cause;

    private int waiters;

    /**
     * Creates a new instance.
     * 
     */
    public FtpFuture() {
        this.cancellable = false;
    }

    /**
     * Creates a new instance.
     * 
     * @param cancellable
     *            {@code true} if and only if this future can be canceled
     */
    public FtpFuture(boolean cancellable) {
        this.cancellable = cancellable;
    }

    /**
     * Returns {@code true} if and only if this future is complete, regardless
     * of whether the operation was successful, failed, or canceled.
     * 
     * @return True if the future is complete
     */
    public synchronized boolean isDone() {
        return this.done;
    }

    /**
     * Returns {@code true} if and only if the operation was completed
     * successfully.
     * 
     * @return True if the future is successful
     */
    public synchronized boolean isSuccess() {
        return this.cause == null;
    }

    /**
     * Returns the cause of the failed operation if the operation has failed.
     * 
     * @return the cause of the failure. {@code null} if succeeded or this
     *         future is not completed yet.
     */
    public synchronized Throwable getCause() {
        if (this.cause != CANCELLED) {
            return this.cause;
        }
        return null;
    }

    /**
     * Returns {@code true} if and only if this future was canceled by a
     * {@link #cancel()} method.
     * 
     * @return True if the future was canceled
     */
    public synchronized boolean isCancelled() {
        return this.cause == CANCELLED;
    }

    /**
     * Waits for this future to be completed.
     * 
     * @return The FtpFuture
     * 
     * @throws InterruptedException
     *             if the current thread was interrupted
     */
    public FtpFuture await() throws InterruptedException {
        synchronized (this) {
            while (!this.done) {
                this.waiters ++;
                try {
                    this.wait();
                } finally {
                    this.waiters --;
                }
            }
        }
        return this;
    }

    /**
     * Waits for this future to be completed within the specified time limit.
     * 
     * @param timeout
     * @param unit
     * 
     * @return {@code true} if and only if the future was completed within the
     *         specified time limit
     * 
     * @throws InterruptedException
     *             if the current thread was interrupted
     */
    public boolean await(long timeout, TimeUnit unit)
            throws InterruptedException {
        return await0(unit.toNanos(timeout), true);
    }

    /**
     * Waits for this future to be completed within the specified time limit.
     * 
     * @param timeoutMillis
     * 
     * @return {@code true} if and only if the future was completed within the
     *         specified time limit
     * 
     * @throws InterruptedException
     *             if the current thread was interrupted
     */
    public boolean await(long timeoutMillis) throws InterruptedException {
        return await0(MILLISECONDS.toNanos(timeoutMillis), true);
    }

    /**
     * Waits for this future to be completed without interruption. This method
     * catches an {@link InterruptedException} and discards it silently.
     * 
     * @return The FtpFuture
     */
    public FtpFuture awaitUninterruptibly() {
        synchronized (this) {
            while (!this.done) {
                this.waiters ++;
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    // Ignore.
                } finally {
                    this.waiters --;
                }
            }
        }

        return this;
    }

    /**
     * Waits for this future to be completed within the specified time limit
     * without interruption. This method catches an {@link InterruptedException}
     * and discards it silently.
     * 
     * @param timeout
     * @param unit
     * 
     * @return {@code true} if and only if the future was completed within the
     *         specified time limit
     */
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        try {
            return await0(unit.toNanos(timeout), false);
        } catch (InterruptedException e) {
            throw new InternalError();
        }
    }

    /**
     * Waits for this future to be completed within the specified time limit
     * without interruption. This method catches an {@link InterruptedException}
     * and discards it silently.
     * 
     * @param timeoutMillis
     * 
     * @return {@code true} if and only if the future was completed within the
     *         specified time limit
     */
    public boolean awaitUninterruptibly(long timeoutMillis) {
        try {
            return await0(MILLISECONDS.toNanos(timeoutMillis), false);
        } catch (InterruptedException e) {
            throw new InternalError();
        }
    }

    private boolean await0(long timeoutNanos, boolean interruptable)
            throws InterruptedException {
        long startTime = timeoutNanos <= 0? 0 : System.nanoTime();
        long waitTime = timeoutNanos;

        synchronized (this) {
            if (this.done) {
                return this.done;
            } else if (waitTime <= 0) {
                return this.done;
            }

            this.waiters ++;
            try {
                for (;;) {
                    try {
                        this.wait(waitTime / 1000000,
                                (int) (waitTime % 1000000));
                    } catch (InterruptedException e) {
                        if (interruptable) {
                            throw e;
                        }
                    }

                    if (this.done) {
                        return true;
                    }
                    waitTime = timeoutNanos - (System.nanoTime() - startTime);
                    if (waitTime <= 0) {
                        return this.done;
                    }
                }
            } finally {
                this.waiters --;
            }
        }
    }

    /**
     * Marks this future as a success and notifies all listeners.
     * 
     * @return {@code true} if and only if successfully marked this future as a
     *         success. Otherwise {@code false} because this future is already
     *         marked as either a success or a failure.
     */
    public boolean setSuccess() {
        synchronized (this) {
            // Allow only once.
            if (this.done) {
                return false;
            }

            this.done = true;
            if (this.waiters > 0) {
                notifyAll();
            }
        }
        return true;
    }

    /**
     * Marks this future as a failure and notifies all listeners.
     * 
     * @param cause
     * @return {@code true} if and only if successfully marked this future as a
     *         failure. Otherwise {@code false} because this future is already
     *         marked as either a success or a failure.
     */
    public boolean setFailure(Throwable cause) {
        synchronized (this) {
            // Allow only once.
            if (this.done) {
                return false;
            }

            this.cause = cause;
            this.done = true;
            if (this.waiters > 0) {
                notifyAll();
            }
        }
        return true;
    }

    /**
     * Cancels the operation associated with this future and notifies all
     * listeners if canceled successfully.
     * 
     * @return {@code true} if and only if the operation has been canceled.
     *         {@code false} if the operation can't be canceled or is already
     *         completed.
     */
    public boolean cancel() {
        if (!this.cancellable) {
            return false;
        }
        synchronized (this) {
            // Allow only once.
            if (this.done) {
                return false;
            }

            this.cause = CANCELLED;
            this.done = true;
            if (this.waiters > 0) {
                notifyAll();
            }
        }
        return true;
    }
}
