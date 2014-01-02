package org.isochrone.util

import scala.collection.mutable.ListBuffer

trait CollectionSplit {
    implicit class SplitOps[T](col: Iterable[T]) {
        def split(f: (T, T) => Boolean) = {
            val buf = new ListBuffer[List[T]]
            val cur = new ListBuffer[T]
            cur += col.head
            for (Seq(a, b) <- col.sliding(2)) {
                if (f(a, b)) {
                    buf += cur.toList
                    cur.clear()
                }
                cur += b
            }
            buf += cur.toList
            buf.toList
        }
    }
}