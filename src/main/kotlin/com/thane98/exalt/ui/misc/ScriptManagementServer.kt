package com.thane98.exalt.ui.misc

import com.thane98.exalt.model.Game
import com.thane98.exalt.ui.model.ScriptOpenRequest
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import java.io.File
import java.nio.file.Files

class ScriptManagementServer(
    private val scriptOpenRequestProcessor: ScriptOpenRequestProcessor,
    port: Int
) {
    private val app = { request: Request -> processRequest(request) }
    private val server = app.asServer(SunHttp(port)).start()

    fun terminate() {
        server.stop()
    }

    private fun processRequest(request: Request): Response {
        return try {
            validateRequest(request)

            val scriptOpenPath = request.query("scriptOpenPath")
            val scriptSavePath = request.query("scriptSavePath")
            val game = request.query("game")
            val scriptOpenRequest = ScriptOpenRequest(scriptOpenPath!!, scriptSavePath!!, Game.valueOf(game!!))
            scriptOpenRequestProcessor.processRequest(scriptOpenRequest)
            Response(OK)
        } catch (ex: IllegalArgumentException) {
            Response(BAD_REQUEST).body(ex.message.orEmpty())
        } catch (ex: Exception) {
            Response(INTERNAL_SERVER_ERROR).body(ex.message.orEmpty())
        }
    }

    private fun validateRequest(request: Request) {
        val scriptOpenPath = request.query("scriptOpenPath")
        val scriptSavePath = request.query("scriptSavePath")
        val game = request.query("game")
        if (scriptOpenPath == null || scriptSavePath == null || game == null) {
            throw IllegalArgumentException("scriptOpenPath, scriptSavePath, and game are required fields")
        }
        if (!File(scriptOpenPath).exists()) {
            throw IllegalArgumentException("scriptOpenPath does not point to a valid file")
        }

        try {
            Game.valueOf(game)
        } catch (_: Exception) {
            throw IllegalArgumentException("$game is not a recognized game")
        }
    }
}