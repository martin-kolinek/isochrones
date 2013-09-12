package org.isochrone

trait ActionComponent {
    trait Executor {
        def execute(): Unit
    }
}

trait SomeActionComponent {
    self: ActionComponent =>
    val executor: Executor
}