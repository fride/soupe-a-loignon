package core.types

import arrow.fx.IO

typealias CrudOperation<A> = (A) -> IO<Unit>
