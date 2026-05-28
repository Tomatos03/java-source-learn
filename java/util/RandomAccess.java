/*
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

/**
 * Marker interface used by <tt>List</tt> implementations to indicate that
 * they support fast (generally constant time) random access.  The primary
 * purpose of this interface is to allow generic algorithms to alter their
 * behavior to provide good performance when applied to either random or
 * sequential access lists.
 *
 * <p>The best algorithms for manipulating random access lists (such as
 * <tt>ArrayList</tt>) can produce quadratic behavior when applied to
 * sequential access lists (such as <tt>LinkedList</tt>).  Generic list
 * algorithms are encouraged to check whether the given list is an
 * <tt>instanceof</tt> this interface before applying an algorithm that would
 * provide poor performance if it were applied to a sequential access list,
 * and to alter their behavior if necessary to guarantee acceptable
 * performance.
 *
 * <p>It is recognized that the distinction between random and sequential
 * access is often fuzzy.  For example, some <tt>List</tt> implementations
 * provide asymptotically linear access times if they get huge, but constant
 * access times in practice.  Such a <tt>List</tt> implementation
 * should generally implement this interface.  As a rule of thumb, a
 * <tt>List</tt> implementation should implement this interface if,
 * for typical instances of the class, this loop:
 * <pre>
 *     for (int i=0, n=list.size(); i &lt; n; i++)
 *         list.get(i);
 * </pre>
 * runs faster than this loop:
 * <pre>
 *     for (Iterator i=list.iterator(); i.hasNext(); )
 *         i.next();
 * </pre>
 *
 * <p>This interface is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.4
 */
/**
 * List 实现使用的标记接口，用于表明它们支持快速（通常是常数时间）随机访问（通过数组下标访问)。
 * 此接口的主要目的是允许泛型算法在应用于随机访问或顺序访问列表时改变其行为以提供良好的性能。
 *
 * 操作随机访问列表（如 ArrayList）的最佳算法在应用于顺序访问列表（如 LinkedList）时
 * 可能产生平方级的性能开销。建议泛型列表算法在应用可能对顺序访问列表产生不良性能的算法之前，
 * 检查给定列表是否是此接口的实例，并在必要时改变其行为以保证可接受的性能。
 *
 * 认识到随机访问和顺序访问之间的区别通常是模糊的。例如，一些 List 实现在数据量很大时
 * 提供渐近线性的访问时间，但在实践中提供常数访问时间。这样的 List 实现通常应该实现此接口。
 * 作为经验法则，如果对于类的典型实例，这个循环：
 *     for (int i=0, n=list.size(); i < n; i++)
 *         list.get(i);
 * 比这个循环运行得更快：
 *     for (Iterator i=list.iterator(); i.hasNext(); )
 *         i.next();
 * 则 List 实现应该实现此接口。
 *
 * 此接口是 Java 集合框架的成员。
 *
 * @since 1.4
 */
public interface RandomAccess {
}
