/**
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3.0 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package goldengate.common.future;

import java.util.concurrent.TimeUnit;

/**
 * Future completed
 *
 * @author Frederic Bregier
 *
 */
public abstract class GgCompletedFuture extends GgFuture {
    /**
	 */
    protected GgCompletedFuture() {
        super(false);
    }

    @Override
    public GgFuture await() throws InterruptedException {
        return this;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit)
            throws InterruptedException {
        return true;
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        return true;
    }

    @Override
    public GgFuture awaitUninterruptibly() {
        return this;
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        return true;
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        return true;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public boolean setFailure(Throwable cause) {
        return false;
    }

    @Override
    public boolean setSuccess() {
        return false;
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
