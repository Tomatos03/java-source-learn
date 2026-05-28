/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.util.stream;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

/**
 * A sequence of elements supporting sequential and parallel aggregate
 * operations.  The following example illustrates an aggregate operation using
 * {@link Stream} and {@link IntStream}:
 *
 * <pre>{@code
 *     int sum = widgets.stream()
 *                      .filter(w -> w.getColor() == RED)
 *                      .mapToInt(w -> w.getWeight())
 *                      .sum();
 * }</pre>
 *
 * In this example, {@code widgets} is a {@code Collection<Widget>}.  We create
 * a stream of {@code Widget} objects via {@link Collection#stream Collection.stream()},
 * filter it to produce a stream containing only the red widgets, and then
 * transform it into a stream of {@code int} values representing the weight of
 * each red widget. Then this stream is summed to produce a total weight.
 *
 * <p>In addition to {@code Stream}, which is a stream of object references,
 * there are primitive specializations for {@link IntStream}, {@link LongStream},
 * and {@link DoubleStream}, all of which are referred to as "streams" and
 * conform to the characteristics and restrictions described here.
 *
 * <p>To perform a computation, stream
 * <a href="package-summary.html#StreamOps">operations</a> are composed into a
 * <em>stream pipeline</em>.  A stream pipeline consists of a source (which
 * might be an array, a collection, a generator function, an I/O channel,
 * etc), zero or more <em>intermediate operations</em> (which transform a
 * stream into another stream, such as {@link Stream#filter(Predicate)}), and a
 * <em>terminal operation</em> (which produces a result or side-effect, such
 * as {@link Stream#count()} or {@link Stream#forEach(Consumer)}).
 * Streams are lazy; computation on the source data is only performed when the
 * terminal operation is initiated, and source elements are consumed only
 * as needed.
 *
 * <p>Collections and streams, while bearing some superficial similarities,
 * have different goals.  Collections are primarily concerned with the efficient
 * management of, and access to, their elements.  By contrast, streams do not
 * provide a means to directly access or manipulate their elements, and are
 * instead concerned with declaratively describing their source and the
 * computational operations which will be performed in aggregate on that source.
 * However, if the provided stream operations do not offer the desired
 * functionality, the {@link #iterator()} and {@link #spliterator()} operations
 * can be used to perform a controlled traversal.
 *
 * <p>A stream pipeline, like the "widgets" example above, can be viewed as
 * a <em>query</em> on the stream source.  Unless the source was explicitly
 * designed for concurrent modification (such as a {@link ConcurrentHashMap}),
 * unpredictable or erroneous behavior may result from modifying the stream
 * source while it is being queried.
 *
 * <p>Most stream operations accept parameters that describe user-specified
 * behavior, such as the lambda expression {@code w -> w.getWeight()} passed to
 * {@code mapToInt} in the example above.  To preserve correct behavior,
 * these <em>behavioral parameters</em>:
 * <ul>
 * <li>must be <a href="package-summary.html#NonInterference">non-interfering</a>
 * (they do not modify the stream source); and</li>
 * <li>in most cases must be <a href="package-summary.html#Statelessness">stateless</a>
 * (their result should not depend on any state that might change during execution
 * of the stream pipeline).</li>
 * </ul>
 *
 * <p>Such parameters are always instances of a
 * <a href="../function/package-summary.html">functional interface</a> such
 * as {@link java.util.function.Function}, and are often lambda expressions or
 * method references.  Unless otherwise specified these parameters must be
 * <em>non-null</em>.
 *
 * <p>A stream should be operated on (invoking an intermediate or terminal stream
 * operation) only once.  This rules out, for example, "forked" streams, where
 * the same source feeds two or more pipelines, or multiple traversals of the
 * same stream.  A stream implementation may throw {@link IllegalStateException}
 * if it detects that the stream is being reused. However, since some stream
 * operations may return their receiver rather than a new stream object, it may
 * not be possible to detect reuse in all cases.
 *
 * <p>Streams have a {@link #close()} method and implement {@link AutoCloseable},
 * but nearly all stream instances do not actually need to be closed after use.
 * Generally, only streams whose source is an IO channel (such as those returned
 * by {@link Files#lines(Path, Charset)}) will require closing.  Most streams
 * are backed by collections, arrays, or generating functions, which require no
 * special resource management.  (If a stream does require closing, it can be
 * declared as a resource in a {@code try}-with-resources statement.)
 *
 * <p>Stream pipelines may execute either sequentially or in
 * <a href="package-summary.html#Parallelism">parallel</a>.  This
 * execution mode is a property of the stream.  Streams are created
 * with an initial choice of sequential or parallel execution.  (For example,
 * {@link Collection#stream() Collection.stream()} creates a sequential stream,
 * and {@link Collection#parallelStream() Collection.parallelStream()} creates
 * a parallel one.)  This choice of execution mode may be modified by the
 * {@link #sequential()} or {@link #parallel()} methods, and may be queried with
 * the {@link #isParallel()} method.
 *
 * @param <T> the type of the stream elements
 * @since 1.8
 * @see IntStream
 * @see LongStream
 * @see DoubleStream
 * @see <a href="package-summary.html">java.util.stream</a>
 */
public interface Stream<T> extends BaseStream<T, Stream<T>> {

    /**
     * Returns a stream consisting of the elements of this stream that match
     * the given predicate.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to each element to determine if it
     *                  should be included
     * @return the new stream
     */
    // 返回包含与给定谓词匹配的此流元素的流（中间操作）
    Stream<T> filter(Predicate<? super T> predicate);

    /**
     * Returns a stream consisting of the results of applying the given
     * function to the elements of this stream.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param <R> The element type of the new stream
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element
     * @return the new stream
     */
    // 返回包含将给定函数应用于此流元素的结果的流（中间操作）
    <R> Stream<R> map(Function<? super T, ? extends R> mapper);

    /**
     * Returns an {@code IntStream} consisting of the results of applying the
     * given function to the elements of this stream.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">
     *     intermediate operation</a>.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element
     * @return the new stream
     */
    // 返回包含将给定函数应用于此流元素的结果的整数流（中间操作）
    IntStream mapToInt(ToIntFunction<? super T> mapper);

    /**
     * Returns a {@code LongStream} consisting of the results of applying the
     * given function to the elements of this stream.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element
     * @return the new stream
     */
    // 返回包含将给定函数应用于此流元素的结果的长整数流（中间操作）
    LongStream mapToLong(ToLongFunction<? super T> mapper);

    /**
     * Returns a {@code DoubleStream} consisting of the results of applying the
     * given function to the elements of this stream.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element
     * @return the new stream
     */
    // 返回包含将给定函数应用于此流元素的结果的浮点数流（中间操作）
    DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper);

    /**
     * Returns a stream consisting of the results of replacing each element of
     * this stream with the contents of a mapped stream produced by applying
     * the provided mapping function to each element.  Each mapped stream is
     * {@link java.util.stream.BaseStream#close() closed} after its contents
     * have been placed into this stream.  (If a mapped stream is {@code null}
     * an empty stream is used, instead.)
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @apiNote
     * The {@code flatMap()} operation has the effect of applying a one-to-many
     * transformation to the elements of the stream, and then flattening the
     * resulting elements into a new stream.
     *
     * <p><b>Examples.</b>
     *
     * <p>If {@code orders} is a stream of purchase orders, and each purchase
     * order contains a collection of line items, then the following produces a
     * stream containing all the line items in all the orders:
     * <pre>{@code
     *     orders.flatMap(order -> order.getLineItems().stream())...
     * }</pre>
     *
     * <p>If {@code path} is the path to a file, then the following produces a
     * stream of the {@code words} contained in that file:
     * <pre>{@code
     *     Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8);
     *     Stream<String> words = lines.flatMap(line -> Stream.of(line.split(" +")));
     * }</pre>
     * The {@code mapper} function passed to {@code flatMap} splits a line,
     * using a simple regular expression, into an array of words, and then
     * creates a stream of words from that array.
     *
     * @param <R> The element type of the new stream
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element which produces a stream
     *               of new values
     * @return the new stream
     */
    <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);

    /**
     * Returns an {@code IntStream} consisting of the results of replacing each
     * element of this stream with the contents of a mapped stream produced by
     * applying the provided mapping function to each element.  Each mapped
     * stream is {@link java.util.stream.BaseStream#close() closed} after its
     * contents have been placed into this stream.  (If a mapped stream is
     * {@code null} an empty stream is used, instead.)
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element which produces a stream
     *               of new values
     * @return the new stream
     * @see #flatMap(Function)
     */
    IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper);

    /**
     * Returns an {@code LongStream} consisting of the results of replacing each
     * element of this stream with the contents of a mapped stream produced by
     * applying the provided mapping function to each element.  Each mapped
     * stream is {@link java.util.stream.BaseStream#close() closed} after its
     * contents have been placed into this stream.  (If a mapped stream is
     * {@code null} an empty stream is used, instead.)
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element which produces a stream
     *               of new values
     * @return the new stream
     * @see #flatMap(Function)
     */
    LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper);

    /**
     * Returns an {@code DoubleStream} consisting of the results of replacing
     * each element of this stream with the contents of a mapped stream produced
     * by applying the provided mapping function to each element.  Each mapped
     * stream is {@link java.util.stream.BaseStream#close() closed} after its
     * contents have placed been into this stream.  (If a mapped stream is
     * {@code null} an empty stream is used, instead.)
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element which produces a stream
     *               of new values
     * @return the new stream
     * @see #flatMap(Function)
     */
    DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper);

    /**
     * Returns a stream consisting of the distinct elements (according to
     * {@link Object#equals(Object)}) of this stream.
     *
     * <p>For ordered streams, the selection of distinct elements is stable
     * (for duplicated elements, the element appearing first in the encounter
     * order is preserved.)  For unordered streams, no stability guarantees
     * are made.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">stateful
     * intermediate operation</a>.
     *
     * @apiNote
     * Preserving stability for {@code distinct()} in parallel pipelines is
     * relatively expensive (requires that the operation act as a full barrier,
     * with substantial buffering overhead), and stability is often not needed.
     * Using an unordered stream source (such as {@link #generate(Supplier)})
     * or removing the ordering constraint with {@link #unordered()} may result
     * in significantly more efficient execution for {@code distinct()} in parallel
     * pipelines, if the semantics of your situation permit.  If consistency
     * with encounter order is required, and you are experiencing poor performance
     * or memory utilization with {@code distinct()} in parallel pipelines,
     * switching to sequential execution with {@link #sequential()} may improve
     * performance.
     *
     * @return the new stream
     */
    /**
     * 返回由此流的不同元素（根据 equals 方法）组成的流。
     *
     * 这是一个有状态的中间操作。
     *
     * 对于有序流： 不同元素的选择是稳定的（对于重复元素，保留顺序中首次出现的元素）。
     * 对于无序流： 不提供稳定性保证。
     *
     * 示例：有序流的稳定性
     *     // 有序流（如 List.stream()）
     *     List<String> list = Arrays.asList("apple", "banana", "apple", "cherry", "banana");
     *     List<String> result = list.stream().distinct().collect(Collectors.toList());
     *     // 结果: ["apple", "banana", "cherry"]
     *     // apple 首次出现在索引0，banana 首次出现在索引1，cherry 首次出现在索引3
     *     // 重复的元素被移除，保留首次出现的位置
     *
     * 示例：无序流的不确定性
     *     // 无序流（如 Stream.generate() 或 parallelStream().unordered()）
     *     Stream<String> unordered = Stream.of("apple", "banana", "apple", "cherry", "banana")
     *                                      .parallel()
     *                                      .unordered();
     *     List<String> result2 = unordered.distinct().collect(Collectors.toList());
     *     // 结果: ["apple", "banana", "cherry"] 但顺序不确定
     *     // 可能是 ["cherry", "apple", "banana"] 或其他顺序
     *     // 因为无序流不保证保留首次出现的顺序
     *
     *
     * 在并行管道中保持 distinct 的稳定性相对昂贵（要求该操作充当完全屏障，
     * 具有大量的缓冲开销），而且稳定性通常不是必需的。使用无序流源（例如
     * generate 方法）或通过 unordered 方法移除排序约束，
     * 如果语义允许，可能会显著提高并行管道中 distinct 的执行效率。
     * 如果需要与遭遇顺序保持一致，且在并行管道中使用 distinct 时遇到
     * 性能或内存利用率问题，切换到 sequential 顺序执行可能会改善性能。
     *
     * 示例：使用 unordered 提高性能
     *     // 并行流默认保持稳定性，性能开销大
     *     List<String> list = Arrays.asList("a", "b", "a", "c", "b");
     *     list.parallelStream().distinct().collect(Collectors.toList());
     *
     *     // 移除排序约束，提高并行性能
     *     list.parallelStream().unordered().distinct().collect(Collectors.toList());
     *
     * 示例：需要顺序一致性时改用顺序执行
     *     // 并行流性能不佳时
     *     list.parallelStream().distinct().collect(Collectors.toList());
     *
     *     // 切换到顺序执行
     *     list.stream().distinct().collect(Collectors.toList());
     *
     */
    Stream<T> distinct();

    /**
     * Returns a stream consisting of the elements of this stream, sorted
     * according to natural order.  If the elements of this stream are not
     * {@code Comparable}, a {@code java.lang.ClassCastException} may be thrown
     * when the terminal operation is executed.
     *
     * <p>For ordered streams, the sort is stable.  For unordered streams, no
     * stability guarantees are made.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">stateful
     * intermediate operation</a>.
     *
     * @return the new stream
     */
    /**
     * 返回由此流的元素组成的流，按照自然顺序排序(按照流元素重写的compareTo方法逻辑排序）。
     * 如果此流的元素不是 Comparable 的，在执行终端操作时可能会抛出 java.lang.ClassCastException。
     *
     * 对于有序流，排序是稳定的。对于无序流，不提供稳定性保证。
     *
     * 排序的稳定性：如果两个元素的值相等，排序后它们的相对顺序保持不变**
     *
     * 这是一个有状态的中间操作。
     *
     */
    Stream<T> sorted();

    /**
     * Returns a stream consisting of the elements of this stream, sorted
     * according to the provided {@code Comparator}.
     *
     * <p>For ordered streams, the sort is stable.  For unordered streams, no
     * stability guarantees are made.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">stateful
     * intermediate operation</a>.
     *
     * @param comparator a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                   <a href="package-summary.html#Statelessness">stateless</a>
     *                   {@code Comparator} to be used to compare stream elements
     * @return the new stream
     */
    // 按照提供的比较器对流元素进行排序
    Stream<T> sorted(Comparator<? super T> comparator);

    /**
     * Returns a stream consisting of the elements of this stream, additionally
     * performing the provided action on each element as elements are consumed
     * from the resulting stream.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * <p>For parallel stream pipelines, the action may be called at
     * whatever time and in whatever thread the element is made available by the
     * upstream operation.  If the action modifies shared state,
     * it is responsible for providing the required synchronization.
     *
     * @apiNote This method exists mainly to support debugging, where you want
     * to see the elements as they flow past a certain point in a pipeline:
     * <pre>{@code
     *     Stream.of("one", "two", "three", "four")
     *         .filter(e -> e.length() > 3)
     *         .peek(e -> System.out.println("Filtered value: " + e))
     *         .map(String::toUpperCase)
     *         .peek(e -> System.out.println("Mapped value: " + e))
     *         .collect(Collectors.toList());
     * }</pre>
     *
     * @param action a <a href="package-summary.html#NonInterference">
     *                 non-interfering</a> action to perform on the elements as
     *                 they are consumed from the stream
     * @return the new stream
     */
    /**
     * 返回由此流的元素组成的流，在从结果流中消费元素时，对每个元素执行提供的操作。
     *
     * 这是一个中间操作。
     *
     * 对于并行流管道，操作可能在任何时间和任何线程中被调用，该线程由上游操作提供元素。
     * 如果操作修改共享状态，则有责任提供所需的同步。
     *
     * 示例：不安全的并行流操作
     *     List<String> sharedList = new ArrayList<>();
     *     IntStream.range(0, 1000).parallel().peek(e -> {
     *         // 多个线程同时修改共享列表，可能导致并发修改异常或数据丢失
     *         sharedList.add(String.valueOf(e));
     *     }).count();
     *
     * 示例：安全的并行流操作
     *     List<String> safeList = Collections.synchronizedList(new ArrayList<>());
     *     IntStream.range(0, 1000).parallel().peek(e -> {
     *         // 使用线程安全的集合来保护共享状态
     *         safeList.add(String.valueOf(e));
     *     }).count();
     *
     * 示例：最佳实践 - 避免在并行流中修改共享状态
     *     List<String> result = IntStream.range(0, 1000).parallel()
     *         .mapToObj(String::valueOf)
     *         .collect(Collectors.toList());  // 使用收集器，线程安全
     *
     *
     */
    Stream<T> peek(Consumer<? super T> action);

    /**
     * Returns a stream consisting of the elements of this stream, truncated
     * to be no longer than {@code maxSize} in length.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * stateful intermediate operation</a>.
     *
     * @apiNote
     * While {@code limit()} is generally a cheap operation on sequential
     * stream pipelines, it can be quite expensive on ordered parallel pipelines,
     * especially for large values of {@code maxSize}, since {@code limit(n)}
     * is constrained to return not just any <em>n</em> elements, but the
     * <em>first n</em> elements in the encounter order.  Using an unordered
     * stream source (such as {@link #generate(Supplier)}) or removing the
     * ordering constraint with {@link #unordered()} may result in significant
     * speedups of {@code limit()} in parallel pipelines, if the semantics of
     * your situation permit.  If consistency with encounter order is required,
     * and you are experiencing poor performance or memory utilization with
     * {@code limit()} in parallel pipelines, switching to sequential execution
     * with {@link #sequential()} may improve performance.
     *
     * @param maxSize the number of elements the stream should be limited to
     * @return the new stream
     * @throws IllegalArgumentException if {@code maxSize} is negative
     */
    /**
     * 返回由此流的元素组成的流，截断长度不超过 maxSize。
     *
     * 这是一个短路有状态中间操作。
     *
     * @apiNote
     * 虽然 limit 在顺序流管道上通常是低成本操作，但在有序并行管道上可能相当昂贵，
     * 尤其是对于较大的 maxSize 值，因为 limit(n) 被限制为返回的不仅仅是任意 n 个元素，
     * 而是遭遇顺序中的前 n 个元素。使用无序流源（例如 generate 方法）或通过
     * unordered 方法移除排序约束，如果语义允许，可能会显著提高 limit 在并行管道中的执行速度。
     * 如果需要与遭遇顺序保持一致，且在并行管道中使用 limit 时遇到性能或内存利用率问题，
     * 切换到顺序执行可能会改善性能。
     *
     * 示例：有序并行流的 limit 操作
     *     // 有序并行流，limit 需要保持遭遇顺序
     *     List<Integer> result1 = IntStream.range(0, 1000000).parallel()
     *         .limit(10)
     *         .boxed()
     *         .collect(Collectors.toList());
     *     // 结果: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9] - 保持顺序，但性能开销大
     *
     * 示例：使用 unordered 提高性能
     *     // 移除排序约束，允许返回任意 10 个元素
     *     List<Integer> result2 = IntStream.range(0, 1000000).parallel()
     *         .unordered()
     *         .limit(10)
     *         .boxed()
     *         .collect(Collectors.toList());
     *     // 结果: 任意 10 个元素，顺序不确定，但性能更好
     *
     * 示例：需要顺序一致性时改用顺序执行
     *     // 并行流性能不佳时，切换到顺序执行
     *     List<Integer> result3 = IntStream.range(0, 1000000)
     *         .limit(10)  // 顺序流，性能稳定
     *         .boxed()
     *         .collect(Collectors.toList());
     *     // 结果: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9] - 保持顺序，性能可预测
     *
     * @param maxSize 流应该限制的元素数量
     * @return 新的流
     * @throws IllegalArgumentException 如果 maxSize 为负数
     */
    Stream<T> limit(long maxSize);

    /**
     * Returns a stream consisting of the remaining elements of this stream
     * after discarding the first {@code n} elements of the stream.
     * If this stream contains fewer than {@code n} elements then an
     * empty stream will be returned.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">stateful
     * intermediate operation</a>.
     *
     * @apiNote
     * While {@code skip()} is generally a cheap operation on sequential
     * stream pipelines, it can be quite expensive on ordered parallel pipelines,
     * especially for large values of {@code n}, since {@code skip(n)}
     * is constrained to skip not just any <em>n</em> elements, but the
     * <em>first n</em> elements in the encounter order.  Using an unordered
     * stream source (such as {@link #generate(Supplier)}) or removing the
     * ordering constraint with {@link #unordered()} may result in significant
     * speedups of {@code skip()} in parallel pipelines, if the semantics of
     * your situation permit.  If consistency with encounter order is required,
     * and you are experiencing poor performance or memory utilization with
     * {@code skip()} in parallel pipelines, switching to sequential execution
     * with {@link #sequential()} may improve performance.
     *
     * @param n the number of leading elements to skip
     * @return the new stream
     * @throws IllegalArgumentException if {@code n} is negative
     */
    /**
     * 返回由此流的元素组成的流，在丢弃流的前 n 个元素后，保留剩余的元素。
     * 如果此流包含的元素少于 n 个，则返回空流。
     *
     * <p>这是一个有状态的中间操作。
     *
     * @apiNote
     * 虽然 skip 在顺序流管道上通常是低成本操作，但在有序并行管道上可能相当昂贵，
     * 尤其是对于较大的 n 值，因为 skip(n) 被限制为跳过的不仅仅是任意 n 个元素，
     * 而是遭遇顺序中的前 n 个元素。使用无序流源（例如 generate 方法）或通过
     * unordered 方法移除排序约束，如果语义允许，可能会显著提高 skip 在并行管道中的执行速度。
     * 如果需要与遭遇顺序保持一致，且在并行管道中使用 skip 时遇到性能或内存利用率问题，
     * 切换到顺序执行可能会改善性能。
     *
     * 示例：有序并行流的 skip 操作
     *     List<Integer> result2 = IntStream.range(0, 1000000).parallel().skip(999990).boxed().collect(Collectors.toList());
     *     // 结果: [999990, 999991, ..., 999999] - 需要跳过前 999990 个元素，性能开销大
     *
     * 示例：使用 unordered 提高性能
     *     List<Integer> result3 = IntStream.range(0, 1000000).parallel().unordered().skip(999990).boxed().collect(Collectors.toList());
     *     // 结果: 任意 10 个元素，顺序不确定，但性能更好
     *
     * 示例：需要顺序一致性时改用顺序执行
     *     List<Integer> result4 = IntStream.range(0, 1000000).skip(999990).boxed().collect(Collectors.toList());
     *     // 结果: [999990, 999991, ..., 999999] - 顺序流，性能稳定
     *
     *
     * @param n 要跳过的前导元素数量
     * @return 新的流
     * @throws IllegalArgumentException 如果 n 为负数
     */
    Stream<T> skip(long n);

    /**
     * Performs an action for each element of this stream.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * <p>The behavior of this operation is explicitly nondeterministic.
     * For parallel stream pipelines, this operation does <em>not</em>
     * guarantee to respect the encounter order of the stream, as doing so
     * would sacrifice the benefit of parallelism.  For any given element, the
     * action may be performed at whatever time and in whatever thread the
     * library chooses.  If the action accesses shared state, it is
     * responsible for providing the required synchronization.
     *
     * @param action a <a href="package-summary.html#NonInterference">
     *               non-interfering</a> action to perform on the elements
     */
    // 为流中的每一个元素执行action
    void forEach(Consumer<? super T> action);

    /**
     * Performs an action for each element of this stream, in the encounter
     * order of the stream if the stream has a defined encounter order.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * <p>This operation processes the elements one at a time, in encounter
     * order if one exists.  Performing the action for one element
     * <a href="../concurrent/package-summary.html#MemoryVisibility"><i>happens-before</i></a>
     * performing the action for subsequent elements, but for any given element,
     * the action may be performed in whatever thread the library chooses.
     *
     * @param action a <a href="package-summary.html#NonInterference">
     *               non-interfering</a> action to perform on the elements
     * @see #forEach(Consumer)
     */
    /**
     * 对流中的每个元素执行操作，如果流有定义的遭遇顺序，则按照遭遇顺序执行。
     *
     * <p>这是一个终端操作。
     *
     * <p>此操作一次处理一个元素，如果存在遭遇顺序则按顺序处理。
     * 对一个元素执行操作发生在对后续元素执行操作之前，
     * 但对于任何给定的元素，操作可能在库选择的任何线程中执行。
     *
     * 示例：顺序执行操作
     *     List<String> list = Arrays.asList("a", "b", "c", "d");
     *     list.stream().forEachOrdered(System.out::println);
     *     // 输出: a, b, c, d（按顺序输出）
     *
     * 示例：并行流保持顺序
     *     List<Integer> list2 = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
     *     list2.parallelStream().forEachOrdered(e -> System.out.print(e + " "));
     *     // 输出: 1 2 3 4 5 6 7 8（即使并行执行，也保持遭遇顺序）
     *
     * 示例：与 forEach 对比
     *     list2.parallelStream().forEach(e -> System.out.print(e + " "));
     *     // 输出: 顺序不确定（可能不是 1 2 3 4 5 6 7 8）
     *
     * @param action 在流元素上执行的操作
     * @see #forEach(Consumer)
     */
    void forEachOrdered(Consumer<? super T> action);

    /**
     * Returns an array containing the elements of this stream.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @return an array containing the elements of this stream
     */
    // 返回包含流中所有元素的对象数组
    Object[] toArray();

    /**
     * Returns an array containing the elements of this stream, using the
     * provided {@code generator} function to allocate the returned array, as
     * well as any additional arrays that might be required for a partitioned
     * execution or for resizing.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @apiNote
     * The generator function takes an integer, which is the size of the
     * desired array, and produces an array of the desired size.  This can be
     * concisely expressed with an array constructor reference:
     * <pre>{@code
     *     Person[] men = people.stream()
     *                          .filter(p -> p.getGender() == MALE)
     *                          .toArray(Person[]::new);
     * }</pre>
     *
     * @param <A> the element type of the resulting array
     * @param generator a function which produces a new array of the desired
     *                  type and the provided length
     * @return an array containing the elements in this stream
     * @throws ArrayStoreException if the runtime type of the array returned
     * from the array generator is not a supertype of the runtime type of every
     * element in this stream
     */
    <A> A[] toArray(IntFunction<A[]> generator);

    /**
     * Performs a <a href="package-summary.html#Reduction">reduction</a> on the
     * elements of this stream, using the provided identity value and an
     * <a href="package-summary.html#Associativity">associative</a>
     * accumulation function, and returns the reduced value.  This is equivalent
     * to:
     * <pre>{@code
     *     T result = identity;
     *     for (T element : this stream)
     *         result = accumulator.apply(result, element)
     *     return result;
     * }</pre>
     *
     * but is not constrained to execute sequentially.
     *
     * <p>The {@code identity} value must be an identity for the accumulator
     * function. This means that for all {@code t},
     * {@code accumulator.apply(identity, t)} is equal to {@code t}.
     * The {@code accumulator} function must be an
     * <a href="package-summary.html#Associativity">associative</a> function.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @apiNote Sum, min, max, average, and string concatenation are all special
     * cases of reduction. Summing a stream of numbers can be expressed as:
     *
     * <pre>{@code
     *     Integer sum = integers.reduce(0, (a, b) -> a+b);
     * }</pre>
     *
     * or:
     *
     * <pre>{@code
     *     Integer sum = integers.reduce(0, Integer::sum);
     * }</pre>
     *
     * <p>While this may seem a more roundabout way to perform an aggregation
     * compared to simply mutating a running total in a loop, reduction
     * operations parallelize more gracefully, without needing additional
     * synchronization and with greatly reduced risk of data races.
     *
     * @param identity the identity value for the accumulating function
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function for combining two values
     * @return the result of the reduction
     */
    T reduce(T identity, BinaryOperator<T> accumulator);

    /**
     * Performs a <a href="package-summary.html#Reduction">reduction</a> on the
     * elements of this stream, using an
     * <a href="package-summary.html#Associativity">associative</a> accumulation
     * function, and returns an {@code Optional} describing the reduced value,
     * if any. This is equivalent to:
     * <pre>{@code
     *     boolean foundAny = false;
     *     T result = null;
     *     for (T element : this stream) {
     *         if (!foundAny) {
     *             foundAny = true;
     *             result = element;
     *         }
     *         else
     *             result = accumulator.apply(result, element);
     *     }
     *     return foundAny ? Optional.of(result) : Optional.empty();
     * }</pre>
     *
     * but is not constrained to execute sequentially.
     *
     * <p>The {@code accumulator} function must be an
     * <a href="package-summary.html#Associativity">associative</a> function.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function for combining two values
     * @return an {@link Optional} describing the result of the reduction
     * @throws NullPointerException if the result of the reduction is null
     * @see #reduce(Object, BinaryOperator)
     * @see #min(Comparator)
     * @see #max(Comparator)
     */
    /**
     * 归约操作（Reduction）：就是将多个元素"折叠"或"聚合"成一个单一的值
     * 对流中的元素执行归约操作，使用关联的累加函数，返回描述归约值的 Optional（如果有的话）。
     * 等效于以下伪代码：
     *     boolean foundAny = false;
     *     T result = null;
     *     for (T element : this stream) {
     *         if (!foundAny) {
     *             foundAny = true;
     *             result = element;
     *         }
     *         else
     *             result = accumulator.apply(result, element);
     *     }
     *     return foundAny ? Optional.of(result) : Optional.empty();
     *
     * 但不局限于顺序执行。
     *
     * 累加器函数必须是关联函数(满足结合律的函数, 例如： f(f(a, b), c) = f(a, f(b, c)))
     *
     * 这是一个终端操作。
     *
     * 示例：计算流中元素的和
     *     List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
     *     Optional<Integer> sum = numbers.stream().reduce((a, b) -> a + b);
     *     // sum = Optional[15]
     *
     * 示例：计算最大值
     *     Optional<Integer> max = numbers.stream().reduce(Integer::max);
     *     // max = Optional[5]
     *
     * 示例：空流的情况
     *     List<Integer> empty = Arrays.asList();
     *     Optional<Integer> result = empty.stream().reduce((a, b) -> a + b);
     *     // result = Optional.empty()
     *
     * @param accumulator 用于组合两个值的关联、非干扰、无状态函数
     * @return 描述归约结果的 Optional
     * @throws NullPointerException 如果归约结果为 null
     * @see #reduce(Object, BinaryOperator)
     */
    Optional<T> reduce(BinaryOperator<T> accumulator);

    /**
     * Performs a <a href="package-summary.html#Reduction">reduction</a> on the
     * elements of this stream, using the provided identity, accumulation and
     * combining functions.  This is equivalent to:
     * <pre>{@code
     *     U result = identity;
     *     for (T element : this stream)
     *         result = accumulator.apply(result, element)
     *     return result;
     * }</pre>
     *
     * but is not constrained to execute sequentially.
     *
     * <p>The {@code identity} value must be an identity for the combiner
     * function.  This means that for all {@code u}, {@code combiner(identity, u)}
     * is equal to {@code u}.  Additionally, the {@code combiner} function
     * must be compatible with the {@code accumulator} function; for all
     * {@code u} and {@code t}, the following must hold:
     * <pre>{@code
     *     combiner.apply(u, accumulator.apply(identity, t)) == accumulator.apply(u, t)
     * }</pre>
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @apiNote Many reductions using this form can be represented more simply
     * by an explicit combination of {@code map} and {@code reduce} operations.
     * The {@code accumulator} function acts as a fused mapper and accumulator,
     * which can sometimes be more efficient than separate mapping and reduction,
     * such as when knowing the previously reduced value allows you to avoid
     * some computation.
     *
     * @param <U> The type of the result
     * @param identity the identity value for the combiner function
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function for incorporating an additional element into a result
     * @param combiner an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function for combining two values, which must be
     *                    compatible with the accumulator function
     * @return the result of the reduction
     * @see #reduce(BinaryOperator)
     * @see #reduce(Object, BinaryOperator)
     */
    /**
     * 对流中的元素执行归约操作，使用提供的 identity、accumulator 和 combiner 函数。
     * 对于顺序流，等效于以下伪代码：
     *     U result = identity;
     *     for (T element : this stream)
     *         result = accumulator.apply(result, element)
     *     return result;
     *
     * 对于并行流，combiner 函数用于合并不同线程的部分结果：
     *     U result1 = identity;
     *     U result2 = identity;
     *     // 线程1处理前半部分
     *     for (T element : partition1)
     *         result1 = accumulator.apply(result1, element)
     *     // 线程2处理后半部分
     *     for (T element : partition2)
     *         result2 = accumulator.apply(result2, element)
     *     // 合并两个结果
     *     return combiner.apply(result1, result2);
     *
     * 但不局限于顺序执行。
     *
     * identity 值必须是 combiner 函数的单位元。这意味着对于所有 u，
     * combiner(identity, u) 等于 u。此外，combiner 函数必须与 accumulator 函数兼容；
     * 对于所有 u 和 t，以下必须成立：
     *     combiner.apply(u, accumulator.apply(identity, t)) == accumulator.apply(u, t)
     *
     * 这是一个终端操作。
     *
     * @apiNote 许多使用此形式的归约操作可以通过 map 和 reduce 操作的显式组合更简单地表示。
     * accumulator 函数充当融合的映射器和累加器，有时比单独的映射和归约更高效，
     * 例如当知道先前的归约值可以避免某些计算时。
     *
     * 示例：将字符串列表转换为单个字符串
     *     List<String> words = Arrays.asList("Hello", " ", "World");
     *     String result = words.stream().reduce(
     *         "",
     *         (acc, word) -> acc + word,
     *         (acc1, acc2) -> acc1 + acc2
     *     );
     *     // result = "Hello World"
     *
     * 示例：计算列表中所有数字的和（类型转换）
     *     List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
     *     Integer sum = numbers.stream().reduce(
     *         0,
     *         (acc, num) -> acc + num,
     *         (acc1, acc2) -> acc1 + acc2
     *     );
     *     // sum = 15
     *
     * 示例：并行流中的使用
     *     List<String> list = Arrays.asList("a", "b", "c", "d");
     *     String result2 = list.parallelStream().reduce(
     *         "",
     *         (acc, str) -> acc + str,
     *         (acc1, acc2) -> acc1 + acc2  // 用于合并并行结果
     *     );
     *     // result2 = "abcd"
     *
     * @param <U> 结果的类型
     * @param identity combiner 函数的单位元值
     * @param accumulator 用于将额外元素合并到结果中的关联、非干扰、无状态函数
     * @param combiner 用于合并两个值的关联、非干扰、无状态函数，必须与 accumulator 函数兼容
     * @return 归约的结果
     */
    <U> U reduce(U identity,
                 BiFunction<U, ? super T, U> accumulator,
                 BinaryOperator<U> combiner);

    /**
     * Performs a <a href="package-summary.html#MutableReduction">mutable
     * reduction</a> operation on the elements of this stream.  A mutable
     * reduction is one in which the reduced value is a mutable result container,
     * such as an {@code ArrayList}, and elements are incorporated by updating
     * the state of the result rather than by replacing the result.  This
     * produces a result equivalent to:
     * <pre>{@code
     *     R result = supplier.get();
     *     for (T element : this stream)
     *         accumulator.accept(result, element);
     *     return result;
     * }</pre>
     *
     * <p>Like {@link #reduce(Object, BinaryOperator)}, {@code collect} operations
     * can be parallelized without requiring additional synchronization.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @apiNote There are many existing classes in the JDK whose signatures are
     * well-suited for use with method references as arguments to {@code collect()}.
     * For example, the following will accumulate strings into an {@code ArrayList}:
     * <pre>{@code
     *     List<String> asList = stringStream.collect(ArrayList::new, ArrayList::add,
     *                                                ArrayList::addAll);
     * }</pre>
     *
     * <p>The following will take a stream of strings and concatenates them into a
     * single string:
     * <pre>{@code
     *     String concat = stringStream.collect(StringBuilder::new, StringBuilder::append,
     *                                          StringBuilder::append)
     *                                 .toString();
     * }</pre>
     *
     * @param <R> type of the result
     * @param supplier a function that creates a new result container. For a
     *                 parallel execution, this function may be called
     *                 multiple times and must return a fresh value each time.
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function for incorporating an additional element into a result
     * @param combiner an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function for combining two values, which must be
     *                    compatible with the accumulator function
     * @return the result of the reduction
     */
    /**
     * 对流中的元素执行可变归约操作。可变归约是指归约值是可变结果容器（如 ArrayList），
     * 元素通过更新结果的状态而不是替换结果来合并。这产生的结果等效于：
     *     R result = supplier.get();
     *     for (T element : this stream)
     *         accumulator.accept(result, element);
     *     return result;
     *
     * 与 reduce(Object, BinaryOperator) 类似，collect 操作可以并行化，无需额外的同步。
     *
     * 这是一个终端操作。
     *
     * 示例：将字符串流收集到 ArrayList
     *     List<String> result = stringStream.collect(
     *         ArrayList::new,      // supplier - 创建新的 ArrayList
     *         ArrayList::add,      // accumulator - 将元素添加到 ArrayList
     *         ArrayList::addAll    // combiner - 合并两个 ArrayList
     *     );
     *
     * 示例：将字符串流连接成单个字符串
     *     String concat = stringStream.collect(
     *         StringBuilder::new,       // supplier - 创建新的 StringBuilder
     *         StringBuilder::append,    // accumulator - 追加字符串
     *         StringBuilder::append     // combiner - 合并两个 StringBuilder
     *     ).toString();
     *
     * 示例：计算流中元素的和
     *     Integer sum = numbers.stream().collect(
     *         () -> new int[1],                    // supplier - 创建长度为 1 的数组
     *         (acc, num) -> acc[0] += num,         // accumulator - 累加
     *         (acc1, acc2) -> acc1[0] += acc2[0]   // combiner - 合并结果
     *     )[0];
     *
     * 示例：并行流中的使用
     *     List<String> result = stringStream.parallelStream().collect(
     *         ArrayList::new,
     *         ArrayList::add,
     *         ArrayList::addAll  // 并行执行时用于合并各线程的结果
     *     );
     *
     * 并行执行伪代码（与顺序执行的区别）：
     * 顺序执行：
     *     R result = supplier.get();
     *     for (T element : stream)
     *         accumulator.accept(result, element);
     *     return result;
     *
     * 并行执行：
     *     // 将流分成多个分区
     *     Partition partition1 = stream.partition1;  // 分区1
     *     Partition partition2 = stream.partition2;  // 分区2
     *     Partition partition3 = stream.partition3;  // 分区3
     *
     *     // 每个线程独立处理其分区
     *     Thread1: R result1 = supplier.get();       // 调用 supplier 创建结果容器
     *             for (T element : partition1)
     *                 accumulator.accept(result1, element);
     *
     *     Thread2: R result2 = supplier.get();       // 调用 supplier 创建结果容器
     *             for (T element : partition2)
     *                 accumulator.accept(result2, element);
     *
     *     Thread3: R result3 = supplier.get();       // 调用 supplier 创建结果容器
     *             for (T element : partition3)
     *                 accumulator.accept(result3, element);
     *
     *     // 使用 combiner 合并各线程的结果
     *     R combined = result1;
     *     combiner.accept(combined, result2);  // 合并 result2 到 combined
     *     combiner.accept(combined, result3);  // 合并 result3 到 combined
     *     return combined;
     *
     * 关键点：
     * - supplier 在并行执行时会被调用多次（每个线程一次），创建独立的结果容器
     * - 每个线程使用 accumulator 处理其分区的元素，互不干扰
     * - combiner 函数负责将各线程的结果合并为最终结果
     * - 由于每个线程有独立的结果容器，无需额外的同步机制
     *
     * @param <R> 结果的类型
     * @param supplier 创建新结果容器的函数。对于并行执行，此函数可能被调用多次，
     *                 每次都必须返回一个新鲜值
     * @param accumulator 用于将额外元素合并到结果中的关联、非干扰、无状态函数
     * @param combiner 用于合并两个值的关联、非干扰、无状态函数，必须与 accumulator 函数兼容
     * @return 归约的结果
     */
    <R> R collect(Supplier<R> supplier,
                  BiConsumer<R, ? super T> accumulator,
                  BiConsumer<R, R> combiner);

    /**
     * Performs a <a href="package-summary.html#MutableReduction">mutable
     * reduction</a> operation on the elements of this stream using a
     * {@code Collector}.  A {@code Collector}
     * encapsulates the functions used as arguments to
     * {@link #collect(Supplier, BiConsumer, BiConsumer)}, allowing for reuse of
     * collection strategies and composition of collect operations such as
     * multiple-level grouping or partitioning.
     *
     * <p>If the stream is parallel, and the {@code Collector}
     * is {@link Collector.Characteristics#CONCURRENT concurrent}, and
     * either the stream is unordered or the collector is
     * {@link Collector.Characteristics#UNORDERED unordered},
     * then a concurrent reduction will be performed (see {@link Collector} for
     * details on concurrent reduction.)
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * <p>When executed in parallel, multiple intermediate results may be
     * instantiated, populated, and merged so as to maintain isolation of
     * mutable data structures.  Therefore, even when executed in parallel
     * with non-thread-safe data structures (such as {@code ArrayList}), no
     * additional synchronization is needed for a parallel reduction.
     *
     * @apiNote
     * The following will accumulate strings into an ArrayList:
     * <pre>{@code
     *     List<String> asList = stringStream.collect(Collectors.toList());
     * }</pre>
     *
     * <p>The following will classify {@code Person} objects by city:
     * <pre>{@code
     *     Map<String, List<Person>> peopleByCity
     *         = personStream.collect(Collectors.groupingBy(Person::getCity));
     * }</pre>
     *
     * <p>The following will classify {@code Person} objects by state and city,
     * cascading two {@code Collector}s together:
     * <pre>{@code
     *     Map<String, Map<String, List<Person>>> peopleByStateAndCity
     *         = personStream.collect(Collectors.groupingBy(Person::getState,
     *                                                      Collectors.groupingBy(Person::getCity)));
     * }</pre>
     *
     * @param <R> the type of the result
     * @param <A> the intermediate accumulation type of the {@code Collector}
     * @param collector the {@code Collector} describing the reduction
     * @return the result of the reduction
     * @see #collect(Supplier, BiConsumer, BiConsumer)
     * @see Collectors
     */
    /**
     * 使用 Collector 对流中的元素执行可变归约操作。Collector 封装了用作
     * collect(Supplier, BiConsumer, BiConsumer) 参数的函数，允许重用收集策略
     * 和组合收集操作，如多级分组或分区。
     *
     * 如果流是并行的，且 Collector 是并发的，并且流是无序的或收集器是无序的，
     * 则将执行并发归约。
     *
     * 这是一个终端操作。
     *
     * 在并行执行时，可能会实例化、填充和合并多个中间结果，以保持可变数据结构的隔离。
     * 因此，即使与非线程安全的数据结构（如 ArrayList）并行执行，并行归约也不需要额外的同步。
     *
     * 示例：并行流的并发归约（无需额外同步）
     *     List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
     *     // 并行流自动处理线程安全，无需手动同步
     *     List<Integer> result = numbers.parallelStream()
     *         .collect(Collectors.toList());
     *     // result = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
     *     // 多个线程各自创建独立的 ArrayList，最后合并，无需同步
     *
     * 示例：并行流的分组操作
     *     List<String> words = Arrays.asList("apple", "apricot", "banana", "blueberry", "cherry");
     *     Map<Character, List<String>> groupedByFirstLetter = words.parallelStream()
     *         .collect(Collectors.groupingBy(word -> word.charAt(0)));
     *     // 并行执行分组，Collector 自动处理并发
     *     // groupedByFirstLetter = {
     *     //   'a': [apple, apricot],
     *     //   'b': [banana, blueberry],
     *     //   'c': [cherry]
     *     // }
     *
     * 示例：顺序流 vs 并行流
     *     List<Integer> largeList = IntStream.range(0, 1000000).boxed().collect(Collectors.toList());
     *     // 顺序流 - 单线程处理
     *     List<Integer> seq = largeList.stream().collect(Collectors.toList());
     *     // 并行流 - 多线程处理，自动分割和合并
     *     List<Integer> par = largeList.parallelStream().collect(Collectors.toList());
     *
     * 示例：将字符串流收集到列表
     *     List<String> list = Arrays.asList("apple", "banana", "cherry");
     *     List<String> result = list.stream().collect(Collectors.toList());
     *     // result = [apple, banana, cherry]
     *
     * 示例：按城市分组 Person 对象
     *     List<Person> people = Arrays.asList(
     *         new Person("Alice", "New York"),
     *         new Person("Bob", "Los Angeles"),
     *         new Person("Charlie", "New York")
     *     );
     *     Map<String, List<Person>> peopleByCity = people.stream()
     *         .collect(Collectors.groupingBy(Person::getCity));
     *     // peopleByCity = {
     *     //   "New York": [Alice, Charlie],
     *     //   "Los Angeles": [Bob]
     *     // }
     *
     * @param <R> 结果的类型
     * @param <A> Collector 的中间累加类型
     * @param collector 描述归约的 Collector
     * @return 归约的结果
     */
    <R, A> R collect(Collector<? super T, A, R> collector);

    /**
     * Returns the minimum element of this stream according to the provided
     * {@code Comparator}.  This is a special case of a
     * <a href="package-summary.html#Reduction">reduction</a>.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @param comparator a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                   <a href="package-summary.html#Statelessness">stateless</a>
     *                   {@code Comparator} to compare elements of this stream
     * @return an {@code Optional} describing the minimum element of this stream,
     * or an empty {@code Optional} if the stream is empty
     * @throws NullPointerException if the minimum element is null
     */
    /**
     * 根据提供的比较器返回此流的最小元素。这是规约操作的特殊情况。
     *
     * <p>这是一个终端操作。
     *
     * 示例：找到列表中最小的数字
     *     List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9, 3);
     *     Optional<Integer> min = numbers.stream().min(Integer::compareTo);
     *     // min = Optional[1]
     *
     * 示例：找到最短的字符串
     *     List<String> words = Arrays.asList("apple", "pie", "banana");
     *     Optional<String> shortest = words.stream().min(Comparator.comparingInt(String::length));
     *     // shortest = Optional[pie]
     *
     * 示例：找到自定义对象的最小值
     *     List<Person> people = Arrays.asList(
     *         new Person("Alice", 25),
     *         new Person("Bob", 20),
     *         new Person("Charlie", 30)
     *     );
     *     Optional<Person> youngest = people.stream().min(Comparator.comparingInt(Person::getAge));
     *     // youngest = Optional[Bob (age 20)]
     *
     * 示例：空流的情况
     *     List<Integer> empty = Arrays.asList();
     *     Optional<Integer> result = empty.stream().min(Integer::compareTo);
     *     // result = Optional.empty()
     *
     * @param comparator 用于比较流中元素的非干扰、无状态比较器
     * @return 描述此流的最小元素的 Optional，如果流为空则返回空 Optional
     * @throws NullPointerException 如果最小元素为 null
     */
    Optional<T> min(Comparator<? super T> comparator);

    /**
     * @throws NullPointerException if the maximum element is null
     */
    /**
     * 根据提供的比较器返回此流的最大元素。这是规约操作的特殊情况。
     *
     * <p>这是一个终端操作。
     *
     * 示例：找到列表中最大的数字
     *     List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9, 3);
     *     Optional<Integer> max = numbers.stream().max(Integer::compareTo);
     *     // max = Optional[9]
     *
     * 示例：找到最长的字符串
     *     List<String> words = Arrays.asList("apple", "pie", "banana");
     *     Optional<String> longest = words.stream().max(Comparator.comparingInt(String::length));
     *     // longest = Optional[banana]
     *
     * 示例：找到自定义对象的最大值
     *     List<Person> people = Arrays.asList(
     *         new Person("Alice", 25),
     *         new Person("Bob", 20),
     *         new Person("Charlie", 30)
     *     );
     *     Optional<Person> oldest = people.stream().max(Comparator.comparingInt(Person::getAge));
     *     // oldest = Optional[Charlie (age 30)]
     *
     * 示例：空流的情况
     *     List<Integer> empty = Arrays.asList();
     *     Optional<Integer> result = empty.stream().max(Integer::compareTo);
     *     // result = Optional.empty()
     *
     * @param comparator 用于比较流中元素的非干扰、无状态比较器
     * @return 描述此流的最大元素的 Optional，如果流为空则返回空 Optional
     * @throws NullPointerException 如果最大元素为 null
     */
    Optional<T> max(Comparator<? super T> comparator);

    /**
     * Returns the count of elements in this stream.  This is a special case of
     * a <a href="package-summary.html#Reduction">reduction</a> and is
     * equivalent to:
     * <pre>{@code
     *     return mapToLong(e -> 1L).sum();
     * }</pre>
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @return the count of elements in this stream
     */
    // 返回流中元素数量，这是一个特殊的规约函数，等价于
    // return mapToLong(e -> 1L).sum();
    long count();

    /**
     * Returns whether any elements of this stream match the provided
     * predicate.  May not evaluate the predicate on all elements if not
     * necessary for determining the result.  If the stream is empty then
     * {@code false} is returned and the predicate is not evaluated.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * @apiNote
     * This method evaluates the <em>existential quantification</em> of the
     * predicate over the elements of the stream (for some x P(x)).
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to elements of this stream
     * @return {@code true} if any elements of the stream match the provided
     * predicate, otherwise {@code false}
     */
    /**
     * 返回此流中是否有任何元素与提供的谓词匹配。如果不需要评估所有元素来确定结果，
     * 则可能不会评估所有元素上的谓词。如果流为空，则返回 false，且不评估谓词。
     *
     * <p>这是一个短路终端操作。
     *
     * 示例：检查是否有偶数
     *     List<Integer> numbers = Arrays.asList(1, 3, 5, 7, 8, 9);
     *     boolean hasEven = numbers.stream().anyMatch(n -> n % 2 == 0);
     *     // hasEven = true（找到 8 后立即返回，不继续检查）
     *
     * 示例：检查是否有空字符串
     *     List<String> words = Arrays.asList("apple", "banana", "", "cherry");
     *     boolean hasEmpty = words.stream().anyMatch(String::isEmpty);
     *     // hasEmpty = true
     *
     * 示例：空流的情况
     *     List<Integer> empty = Arrays.asList();
     *     boolean result = empty.stream().anyMatch(n -> n > 0);
     *     // result = false
     *
     * @param predicate 应用于流元素的非干扰、无状态谓词
     * @return 如果流中有任何元素与提供的谓词匹配，则返回 true，否则返回 false
     */
    boolean anyMatch(Predicate<? super T> predicate);

    /**
     * Returns whether all elements of this stream match the provided predicate.
     * May not evaluate the predicate on all elements if not necessary for
     * determining the result.  If the stream is empty then {@code true} is
     * returned and the predicate is not evaluated.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * @apiNote
     * This method evaluates the <em>universal quantification</em> of the
     * predicate over the elements of the stream (for all x P(x)).  If the
     * stream is empty, the quantification is said to be <em>vacuously
     * satisfied</em> and is always {@code true} (regardless of P(x)).
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to elements of this stream
     * @return {@code true} if either all elements of the stream match the
     * provided predicate or the stream is empty, otherwise {@code false}
     */
    /**
     * 返回此流中是否所有元素都与提供的谓词匹配。如果不需要评估所有元素来确定结果，
     * 则可能不会评估所有元素上的谓词。如果流为空，则返回 true，且不评估谓词。
     *
     * <p>这是一个短路终端操作。
     *
     * 示例：检查是否所有数字都是正数
     *     List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
     *     boolean allPositive = numbers.stream().allMatch(n -> n > 0);
     *     // allPositive = true
     *
     * 示例：检查是否所有字符串都非空
     *     List<String> words = Arrays.asList("apple", "banana", "", "cherry");
     *     boolean allNonEmpty = words.stream().allMatch(s -> !s.isEmpty());
     *     // allNonEmpty = false（找到空字符串后立即返回）
     *
     * 示例：空流的情况
     *     List<Integer> empty = Arrays.asList();
     *     boolean result = empty.stream().allMatch(n -> n > 0);
     *     // result = true（空流时总是返回 true）
     *
     * @param predicate 应用于流元素的非干扰、无状态谓词
     * @return 如果流中所有元素都与提供的谓词匹配或流为空，则返回 true，否则返回 false
     */
    boolean allMatch(Predicate<? super T> predicate);

    /**
     * Returns whether no elements of this stream match the provided predicate.
     * May not evaluate the predicate on all elements if not necessary for
     * determining the result.  If the stream is empty then {@code true} is
     * returned and the predicate is not evaluated.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * @apiNote
     * This method evaluates the <em>universal quantification</em> of the
     * negated predicate over the elements of the stream (for all x ~P(x)).  If
     * the stream is empty, the quantification is said to be vacuously satisfied
     * and is always {@code true}, regardless of P(x).
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to elements of this stream
     * @return {@code true} if either no elements of the stream match the
     * provided predicate or the stream is empty, otherwise {@code false}
     */
    /**
     * 返回此流中是否没有任何元素与提供的谓词匹配。如果不需要评估所有元素来确定结果，
     * 则可能不会评估所有元素上的谓词。如果流为空，则返回 true，且不评估谓词。
     *
     * <p>这是一个短路终端操作。
     *
     * 示例：检查是否没有负数
     *     List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
     *     boolean noNegative = numbers.stream().noneMatch(n -> n < 0);
     *     // noNegative = true
     *
     * 示例：检查是否没有空字符串
     *     List<String> words = Arrays.asList("apple", "banana", "", "cherry");
     *     boolean noEmpty = words.stream().noneMatch(String::isEmpty);
     *     // noEmpty = false（找到空字符串后立即返回）
     *
     * 示例：空流的情况
     *     List<Integer> empty = Arrays.asList();
     *     boolean result = empty.stream().noneMatch(n -> n < 0);
     *     // result = true（空流时总是返回 true）
     *
     * @param predicate 应用于流元素的非干扰、无状态谓词
     * @return 如果流中没有任何元素与提供的谓词匹配或流为空，则返回 true，否则返回 false
     */
    boolean noneMatch(Predicate<? super T> predicate);

    /**
     * Returns an {@link Optional} describing the first element of this stream,
     * or an empty {@code Optional} if the stream is empty.  If the stream has
     * no encounter order, then any element may be returned.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * @return an {@code Optional} describing the first element of this stream,
     * or an empty {@code Optional} if the stream is empty
     * @throws NullPointerException if the element selected is null
     */
    /**
     * 返回描述此流的第一个元素的 Optional，如果流为空则返回空 Optional。
     * 如果流没有定义遭遇顺序，则可能返回任何元素。
     *
     * <p>这是一个短路终端操作。
     *
     * 示例：找到列表中的第一个元素
     *     List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
     *     Optional<Integer> first = numbers.stream().findFirst();
     *     // first = Optional[1]
     *
     * 示例：找到满足条件的第一个元素
     *     List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
     *     Optional<Integer> firstEven = numbers.stream()
     *         .filter(n -> n % 2 == 0)
     *         .findFirst();
     *     // firstEven = Optional[2]
     *
     * 示例：空流的情况
     *     List<Integer> empty = Arrays.asList();
     *     Optional<Integer> result = empty.stream().findFirst();
     *     // result = Optional.empty()
     *
     * 示例：并行流中的 findFirst
     *     List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
     *     Optional<Integer> first = numbers.parallelStream().findFirst();
     *     // first = Optional[1]（总是返回第一个元素，即使是并行流）
     *
     * @return 描述此流的第一个元素的 Optional，如果流为空则返回空 Optional
     * @throws NullPointerException 如果选中的元素为 null
     */
    Optional<T> findFirst();

    /**
     * Returns an {@link Optional} describing some element of the stream, or an
     * empty {@code Optional} if the stream is empty.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * <p>The behavior of this operation is explicitly nondeterministic; it is
     * free to select any element in the stream.  This is to allow for maximal
     * performance in parallel operations; the cost is that multiple invocations
     * on the same source may not return the same result.  (If a stable result
     * is desired, use {@link #findFirst()} instead.)
     *
     * @return an {@code Optional} describing some element of this stream, or an
     * empty {@code Optional} if the stream is empty
     * @throws NullPointerException if the element selected is null
     * @see #findFirst()
     */
    /**
     * 返回描述流中某个元素的 Optional，如果流为空则返回空 Optional。
     *
     * <p>这是一个短路终端操作。
     *
     * <p>此操作的行为是显式非确定性的；它可以自由选择流中的任何元素。
     * 这是为了在并行操作中获得最大性能；代价是对同一源的多次调用可能不会返回相同的结果。
     * （如果需要稳定的结果，请改用 findFirst()。）
     *
     * 示例：找到列表中的任意元素
     *     List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
     *     Optional<Integer> any = numbers.stream().findAny();
     *     // any = Optional[1]（可能是任何元素）
     *
     * 示例：找到满足条件的任意元素
     *     List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
     *     Optional<Integer> anyEven = numbers.stream()
     *         .filter(n -> n % 2 == 0)
     *         .findAny();
     *     // anyEven = Optional[2] 或 Optional[4]（任意偶数）
     *
     * 示例：空流的情况
     *     List<Integer> empty = Arrays.asList();
     *     Optional<Integer> result = empty.stream().findAny();
     *     // result = Optional.empty()
     *
     * 示例：并行流中的 findAny（性能更好）
     *     List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
     *     Optional<Integer> any = numbers.parallelStream().findAny();
     *     // any = Optional[任意元素]（可能是 1、2、3、4 或 5）
     *     // 并行流中，findAny 比 findFirst 性能更好，因为不需要保证顺序
     *
     * @return 描述流中某个元素的 Optional，如果流为空则返回空 Optional
     * @throws NullPointerException 如果选中的元素为 null
     * @see #findFirst()
     */
    Optional<T> findAny();

    // Static factories

    /**
     * Returns a builder for a {@code Stream}.
     *
     * @param <T> type of elements
     * @return a stream builder
     */
    public static<T> Builder<T> builder() {
        return new Streams.StreamBuilderImpl<>();
    }

    /**
     * Returns an empty sequential {@code Stream}.
     *
     * @param <T> the type of stream elements
     * @return an empty sequential stream
     */
    public static<T> Stream<T> empty() {
        return StreamSupport.stream(Spliterators.<T>emptySpliterator(), false);
    }

    /**
     * Returns a sequential {@code Stream} containing a single element.
     *
     * @param t the single element
     * @param <T> the type of stream elements
     * @return a singleton sequential stream
     */
    public static<T> Stream<T> of(T t) {
        return StreamSupport.stream(new Streams.StreamBuilderImpl<>(t), false);
    }

    /**
     * Returns a sequential ordered stream whose elements are the specified values.
     *
     * @param <T> the type of stream elements
     * @param values the elements of the new stream
     * @return the new stream
     */
    @SafeVarargs
    @SuppressWarnings("varargs") // Creating a stream from an array is safe
    public static<T> Stream<T> of(T... values) {
        return Arrays.stream(values);
    }

    /**
     * Returns an infinite sequential ordered {@code Stream} produced by iterative
     * application of a function {@code f} to an initial element {@code seed},
     * producing a {@code Stream} consisting of {@code seed}, {@code f(seed)},
     * {@code f(f(seed))}, etc.
     *
     * <p>The first element (position {@code 0}) in the {@code Stream} will be
     * the provided {@code seed}.  For {@code n > 0}, the element at position
     * {@code n}, will be the result of applying the function {@code f} to the
     * element at position {@code n - 1}.
     *
     * @param <T> the type of stream elements
     * @param seed the initial element
     * @param f a function to be applied to to the previous element to produce
     *          a new element
     * @return a new sequential {@code Stream}
     */
    public static<T> Stream<T> iterate(final T seed, final UnaryOperator<T> f) {
        Objects.requireNonNull(f);
        final Iterator<T> iterator = new Iterator<T>() {
            @SuppressWarnings("unchecked")
            T t = (T) Streams.NONE;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                return t = (t == Streams.NONE) ? seed : f.apply(t);
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
    }

    /**
     * Returns an infinite sequential unordered stream where each element is
     * generated by the provided {@code Supplier}.  This is suitable for
     * generating constant streams, streams of random elements, etc.
     *
     * @param <T> the type of stream elements
     * @param s the {@code Supplier} of generated elements
     * @return a new infinite sequential unordered {@code Stream}
     */
    public static<T> Stream<T> generate(Supplier<T> s) {
        Objects.requireNonNull(s);
        return StreamSupport.stream(
                new StreamSpliterators.InfiniteSupplyingSpliterator.OfRef<>(Long.MAX_VALUE, s), false);
    }

    /**
     * Creates a lazily concatenated stream whose elements are all the
     * elements of the first stream followed by all the elements of the
     * second stream.  The resulting stream is ordered if both
     * of the input streams are ordered, and parallel if either of the input
     * streams is parallel.  When the resulting stream is closed, the close
     * handlers for both input streams are invoked.
     *
     * @implNote
     * Use caution when constructing streams from repeated concatenation.
     * Accessing an element of a deeply concatenated stream can result in deep
     * call chains, or even {@code StackOverflowException}.
     *
     * @param <T> The type of stream elements
     * @param a the first stream
     * @param b the second stream
     * @return the concatenation of the two input streams
     */
    public static <T> Stream<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        @SuppressWarnings("unchecked")
        Spliterator<T> split = new Streams.ConcatSpliterator.OfRef<>(
                (Spliterator<T>) a.spliterator(), (Spliterator<T>) b.spliterator());
        Stream<T> stream = StreamSupport.stream(split, a.isParallel() || b.isParallel());
        return stream.onClose(Streams.composedClose(a, b));
    }

    /**
     * A mutable builder for a {@code Stream}.  This allows the creation of a
     * {@code Stream} by generating elements individually and adding them to the
     * {@code Builder} (without the copying overhead that comes from using
     * an {@code ArrayList} as a temporary buffer.)
     *
     * <p>A stream builder has a lifecycle, which starts in a building
     * phase, during which elements can be added, and then transitions to a built
     * phase, after which elements may not be added.  The built phase begins
     * when the {@link #build()} method is called, which creates an ordered
     * {@code Stream} whose elements are the elements that were added to the stream
     * builder, in the order they were added.
     *
     * @param <T> the type of stream elements
     * @see Stream#builder()
     * @since 1.8
     */
    public interface Builder<T> extends Consumer<T> {

        /**
         * Adds an element to the stream being built.
         *
         * @throws IllegalStateException if the builder has already transitioned to
         * the built state
         */
        @Override
        void accept(T t);

        /**
         * Adds an element to the stream being built.
         *
         * @implSpec
         * The default implementation behaves as if:
         * <pre>{@code
         *     accept(t)
         *     return this;
         * }</pre>
         *
         * @param t the element to add
         * @return {@code this} builder
         * @throws IllegalStateException if the builder has already transitioned to
         * the built state
         */
        default Builder<T> add(T t) {
            accept(t);
            return this;
        }

        /**
         * Builds the stream, transitioning this builder to the built state.
         * An {@code IllegalStateException} is thrown if there are further attempts
         * to operate on the builder after it has entered the built state.
         *
         * @return the built stream
         * @throws IllegalStateException if the builder has already transitioned to
         * the built state
         */
        Stream<T> build();

    }
}
