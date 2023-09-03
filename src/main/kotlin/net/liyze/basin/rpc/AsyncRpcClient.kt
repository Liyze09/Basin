package net.liyze.basin.rpc

import net.liyze.basin.async.Result
import net.liyze.basin.async.async
import net.liyze.basin.rpc.RpcService.FURY
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody


fun request(host: String, serviceName: String, arg: Any): Result<Any, *> {
    return async {
        val request: Request = Request.Builder()
            .url(host)
            .post(FURY.serialize(arg).toRequestBody())
            .header("Service-Name", serviceName)
            .build()
        return@async FURY.deserialize(client.newCall(request).execute().body?.bytes())
    }
}

private val client: OkHttpClient = OkHttpClient()