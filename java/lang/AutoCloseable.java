/*
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang;

/**
 * An object that may hold resources (such as file or socket handles)
 * until it is closed. The {@link #close()} method of an {@code AutoCloseable}
 * object is called automatically when exiting a {@code
 * try}-with-resources block for which the object has been declared in
 * the resource specification header. This construction ensures prompt
 * release, avoiding resource exhaustion exceptions and errors that
 * may otherwise occur.
 *
 * @apiNote
 * <p>It is possible, and in fact common, for a base class to
 * implement AutoCloseable even though not all of its subclasses or
 * instances will hold releasable resources.  For code that must operate
 * in complete generality, or when it is known that the {@code AutoCloseable}
 * instance requires resource release, it is recommended to use {@code
 * try}-with-resources constructions. However, when using facilities such as
 * {@link java.util.stream.Stream} that support both I/O-based and
 * non-I/O-based forms, {@code try}-with-resources blocks are in
 * general unnecessary when using non-I/O-based forms.
 *
 * @author Josh Bloch
 * @since 1.7
 */
/**
 * 一个可能持有资源（如文件或套接字句柄）的对象，直到它被关闭。当退出声明了该对象的try-with-resources块时，
 * AutoCloseable对象的close()方法会自动调用。这种构造确保了资源的及时释放，避免了资源耗尽异常和可能发生的其他错误。
 */
public interface AutoCloseable {
    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     *
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     *
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     *
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     *
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     *
     * <p>Note that unlike the {@link java.io.Closeable#close close}
     * method of {@link java.io.Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     *
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    /**
     * 关闭此资源，释放任何底层资源。此方法在由try-with-resources语句管理的对象上自动调用。
     * 虽然此接口方法声明抛出Exception，但强烈建议实现者声明close方法的具体实现以抛出更具体的异常，或者如果关闭操作不会失败则不抛出任何异常。
     * 关闭操作可能失败的情况需要实现者的仔细关注。强烈建议在抛出异常之前释放底层资源并在内部标记资源为已关闭。close方法不太可能被调用多次，因此这确保了资源及时释放。此外，它减少了当资源包装或被包装时可能出现的问题。
     * 此接口的实现者也强烈建议不要让close方法抛出InterruptedException。此异常与线程的中断状态相互作用，如果InterruptedException被抑制，则可能发生运行时不当行为。
     * 更一般地说，如果异常被抑制会导致问题，则AutoCloseable.close方法不应该抛出它。
     * 注意，与Closeable的close方法不同，此close方法不需要是幂等的。换句话说，多次调用此close方法可能会产生一些可见的副作用，而Closeable.close被要求多次调用时没有任何影响。
     * 但是，此接口的实现者强烈建议使其close方法幂等。
     *
     * 代码示例：
     * // 1. 实现AutoCloseable接口的资源类
     * class FileResource implements AutoCloseable {
     *     private boolean closed = false;
     *
     *     @Override
     *     public void close() throws IOException {
     *         if (closed) {
     *             return;  // 幂等性：多次调用不会产生副作用
     *         }
     *         // 释放底层资源
     *         System.out.println("释放文件资源");
     *         closed = true;
     *     }
     * }
     *
     * // 2. 使用try-with-resources自动管理资源
     * try (FileResource resource = new FileResource()) {
     *     System.out.println("使用资源");
     * } // 自动调用close()方法，无需显式调用
     *
     * // 3. 不推荐的做法：手动管理资源
     * FileResource resource = new FileResource();
     * try {
     *     System.out.println("使用资源");
     * } finally {
     *     resource.close();  // 需要手动调用
     * }
     */
    void close() throws Exception;
}
