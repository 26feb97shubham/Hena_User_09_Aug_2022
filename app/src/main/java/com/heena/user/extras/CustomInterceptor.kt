package com.heena.user.extras

import android.content.Context
import android.util.Log
import com.heena.user.utils.Utility
import okhttp3.*
import okio.Buffer
import okio.BufferedSource
import java.io.IOException

import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.concurrent.TimeUnit


class CustomInterceptor(private val logger: Logger) : Interceptor {
    var headers: Headers? = null

    @Volatile
    private var level = Level.NONE
    var version: String? = null
    var lang: String? = null
    private var context: Context? = null

    constructor(context: Context?, lang: String?, version: String?) : this(Logger.DEFAULT) {
        setLevel(Level.BODY)
        this.context = context
        this.version = version
        this.lang = lang
    }

    /**
     * Change the level at which this interceptor logs.
     */
    fun setLevel(level: Level) {
        this.level = level
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val level = level
        val original: Request = chain.request()
        val requestBuilder: Request.Builder =
            original.newBuilder().addHeader("os", "android").addHeader("version", version)
                .addHeader("language", lang)
        val request: Request = requestBuilder.build()
        val response: Response = chain.proceed(request)
        val logBody = level == Level.BODY
        val logHeaders = logBody || level == Level.HEADERS
        val requestBody: RequestBody? = request.body()
        val hasRequestBody = requestBody != null
        val connection: Connection? = chain.connection()
        val protocol: Protocol =
            if (connection != null) connection.protocol() else Protocol.HTTP_1_1
        var requestStartMessage: String =
            "--> " + request.method() + ' ' + request.url() + ' ' + protocol
        if (requestBody != null) {
            if (!logHeaders && hasRequestBody) {
                requestStartMessage += " (" + requestBody.contentLength() + "-byte body)"
            }
        }
        logger.log(requestStartMessage)
        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody != null) {
                    if (requestBody.contentType() != null) {
                        logger.log("Content-Type: " + requestBody.contentType())
                    }
                }
                if (requestBody != null) {
                    if (requestBody.contentLength() != -1L) {
                        logger.log("Content-Length: " + requestBody.contentLength())
                    }
                }
            }
            val headers: Headers = request.headers()
            var i = 0
            val count: Int = headers.size()
            while (i < count) {
                val name: String = headers.name(i)
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equals(
                        name,
                        ignoreCase = true
                    ) && !"Content-Length".equals(name, ignoreCase = true)
                ) {
                    logger.log(name + ": " + headers.value(i))
                }
                i++
            }
            if (!logBody || !hasRequestBody) {
                logger.log("--> END " + request.method())
            } else if (bodyEncoded(request.headers())) {
                logger.log("--> END " + request.method().toString() + " (encoded body omitted)")
            } else {
                val buffer = Buffer()
                if (requestBody != null) {
                    requestBody.writeTo(buffer)
                }
                var charset: Charset = UTF8
                val contentType: MediaType? = requestBody?.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF8)!!
                }
                logger.log("")
                assert(charset != null)
                logger.log(buffer.readString(charset))
                if (requestBody != null) {
                    logger.log(
                        "--> END " + request.method()
                            .toString() + " (" + requestBody.contentLength().toString() + "-byte body)"
                    )
                }
            }
        }
        val startNs = System.nanoTime()
        val tookMs: Long = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        val responseBody: ResponseBody? = response.body()
        assert(responseBody != null)
        val contentLength = responseBody?.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        logger.log(
            "<-- " + response.code() + ' ' + response.message() + ' '
                    + response.request().url()
                .toString() + " (" + tookMs.toString() + "ms" + (if (!logHeaders) ", "
                    + bodySize + " body" else "") + ')'
        )
        if (logHeaders) {
            val headers: Headers = response.headers()
            var i = 0
            val count: Int = headers.size()
            while (i < count) {
                logger.log(headers.name(i).toString() + ": " + headers.value(i))
                i++
            }
            if (!logBody /*|| !HttpEngine.hasBody(response)*/) {
                logger.log("<-- END HTTP")
            } else if (bodyEncoded(response.headers())) {
                logger.log("<-- END HTTP (encoded body omitted)")
            } else {
                val source = responseBody?.source()
                if (source != null) {
                    source.request(Long.MAX_VALUE)
                } // Buffer the entire body.
                val buffer: Buffer = source!!.buffer()
                var charset: Charset = UTF8
                val contentType: MediaType? = responseBody?.contentType()
                if (contentType != null) {
                    try {
                        charset = contentType.charset(UTF8)!!
                    } catch (e: UnsupportedCharsetException) {
                        logger.log("")
                        logger.log("Couldn't decode the response body; charset is likely malformed.")
                        logger.log("<-- END HTTP")
                        return response
                    }
                }
                if (contentLength != 0L) {
                    logger.log("")
                    logger.log(buffer.clone().readString(charset))
                }
                logger.log("<-- END HTTP (" + buffer.size().toString() + "-byte body)")
            }
        }
        if (context?.let { Utility.hasConnection(it) } == true) {
            val maxAge = 60 // read from cache for 1 minute
            return response.newBuilder()
                .header("Cache-Control", "public, max-age=$maxAge")
                .build()
        } else {
            val maxStale = 60 * 60 * 24 * 28 // tolerate 4-weeks stale
            return response.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                .build()
        }

        // return response;
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding: String? = headers.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    enum class Level {
        NONE, BASIC, HEADERS, BODY
    }

    interface Logger {
        fun log(message: String?)

        companion object {
            val DEFAULT: Logger = object : Logger {
                override fun log(message: String?) {
                    //Platform.get().logW(message);
                    message?.let { Log.e("Response", it) }
                    //AppUtils.showRoughLog("Response",message);
                }
            }
        }
    }

    companion object {
        private val UTF8: Charset = Charset.forName("UTF-8")
        private fun protocol(protocol: Protocol): String {
            return if (protocol === Protocol.HTTP_1_0) "HTTP/1.0" else "HTTP/1.1"
        }

        private fun requestPath(url: HttpUrl): String {
            val path = url.encodedPath()
            val query = url.encodedQuery()
            return if (query != null) ("$path?$query") else path
        }
    }
}
