package  com.devpush.coreNetwork.apiService

import com.devpush.coreNetwork.model.game.GameResponse
import com.devpush.coreNetwork.model.gameDetails.GameDetailsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ApiService(
    val httpClient: HttpClient
) {
    // a9e3650357a146bdbc8c7ea6c4b33232

    suspend fun getGames(): Result<GameResponse> {
        return try {
            val response = httpClient.get("api/games") {
                url {
                    parameter("key", "a9e3650357a146bdbc8c7ea6c4b33232")
                }
            }.body<GameResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun search(q: String): Result<GameResponse> {
        return try {
            val response = httpClient.get("api/games") {
                url {
                    parameter("key", "1abb1867f52548a4aa9f54dd4946af2f")
                    parameter("search", q)
                }
            }.body<GameResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getDetails(id: Int): Result<GameDetailsResponse> {
        return try {
            val response = httpClient.get("api/games/${id}") {
                url {
                    parameter("key", "1abb1867f52548a4aa9f54dd4946af2f")
                }
            }.body<GameDetailsResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}