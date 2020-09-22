package core.types

class ApplicationException(cause: Throwable, val id: String) : RuntimeException(cause)
