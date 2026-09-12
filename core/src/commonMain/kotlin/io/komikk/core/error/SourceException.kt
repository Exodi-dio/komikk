package io.komikk.core.error

/**
 * Sealed error model for source calls.
 *
 * Sources must throw exactly one of these subtypes for their failure mode and
 * must never wrap `kotlinx.coroutines.CancellationException`.
 */
sealed class SourceException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    /** Transport-level failure: DNS, connect, timeout, TLS. */
    class Network(
        message: String = "Network error",
        cause: Throwable? = null,
    ) : SourceException(message, cause)

    /** The source requires authentication the reader does not have. */
    class Unauthorized(
        message: String = "Source requires authentication",
    ) : SourceException(message)

    /** The requested resource does not exist (or was permanently removed). */
    class NotFound(
        message: String = "Resource not found",
    ) : SourceException(message)

    /** The source is rate limiting. Callers should back off, not retry hot. */
    class RateLimited(
        message: String = "Source is rate limiting; retry later",
    ) : SourceException(message)

    /** The body could not be interpreted as the expected structure. */
    class Parse(
        message: String = "Could not parse source response",
        cause: Throwable? = null,
    ) : SourceException(message, cause)

    /** The source does not implement a capability the caller asked for. */
    class FeatureNotSupported(
        message: String,
    ) : SourceException(message)
}